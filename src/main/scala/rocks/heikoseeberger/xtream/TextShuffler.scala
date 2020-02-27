/*
 * Copyright 2020 Matheus Hoffmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rocks.heikoseeberger.xtream

import java.util.UUID

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.ActorRef
import akka.stream.{ Attributes, DelayOverflowStrategy, Materializer, SinkRef }
import akka.stream.scaladsl.{ Flow, FlowWithContext, RestartSink, Sink, Source }
import rocks.heikoseeberger.xtream.WordShuffler.{ ShuffleWord, WordShuffled }
import scala.concurrent.{ Await, Future, Promise }
import scala.concurrent.duration.FiniteDuration

object TextShuffler {

  type Process =
    FlowWithContext[ShuffleText, Promise[TextShuffled], TextShuffled, Promise[TextShuffled], Any]

  final case class ShuffleText(text: String)
  final case class TextShuffled(text: String)

  final case class Config(delay: FiniteDuration,
                          wordShufflerProcessTimeout: FiniteDuration,
                          wordShufflerAskTimeout: FiniteDuration)

  def apply(
      config: Config,
      wordShuffler: ActorRef[WordShuffler.Command]
  )(implicit mat: Materializer, typedSystem: akka.actor.typed.ActorSystem[_]): Process = {
    import config._
    import typedSystem.executionContext

    def wordShufflerSinkRef(): Future[SinkRef[(ShuffleWord, Respondee[WordShuffled])]] =
      wordShuffler
        .ask { replyTo: ActorRef[SinkRef[(ShuffleWord, Respondee[WordShuffled])]] =>
          WordShuffler.GetSinkRef(replyTo)
        }(wordShufflerAskTimeout, typedSystem.scheduler)
        .recoverWith { case _ => wordShufflerSinkRef() }

    val wordShufflerSink =
      RestartSink.withBackoff(wordShufflerAskTimeout, wordShufflerAskTimeout, 0) { () =>
        Await.result(wordShufflerSinkRef().map(_.sink), wordShufflerAskTimeout) // Hopefully we can get rid of blocking soon: https://github.com/akka/akka/issues/25934
      }

    FlowWithContext[ShuffleText, Promise[TextShuffled]]
      .delay(delay, DelayOverflowStrategy.backpressure)
      .withAttributes(Attributes.inputBuffer(1, 1))
      .mapAsync(42) {
        case ShuffleText(text) =>
          Source
            .fromIterator(() => text.split(" ").iterator)
            .map(ShuffleWord)
            // .into(wordShufflerSink)
            .map { shuffleWord =>
              val promisedWordShuffled = Promise[WordShuffled]()
              val respondee =
                typedSystem.systemActorOf(
                  Respondee[WordShuffled](promisedWordShuffled, wordShufflerProcessTimeout),
                  s"${UUID.randomUUID().toString}"
                )
              (shuffleWord, promisedWordShuffled, respondee)
            }
            .alsoTo {
              Flow[(ShuffleWord, Promise[WordShuffled], Respondee[WordShuffled])]
                .map { case (shuffleWord, _, r) => (shuffleWord, r) }
                .to(wordShufflerSink)
            }
            .mapAsync(42) { case (_, p, _) => p.future }
            .map(_.text)
            .runWith(Sink.seq)
      }
      .map(words => TextShuffled(words.mkString(" ")))
  }
}
