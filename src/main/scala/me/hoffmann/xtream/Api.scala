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

package me.hoffmann.xtream

import akka.Done
import akka.actor.{ CoordinatedShutdown, Scheduler, ActorSystem => UntypedSystem }
import akka.actor.CoordinatedShutdown.{ PhaseServiceRequestsDone, PhaseServiceUnbind, Reason }
import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.{ ActorAttributes, Materializer, OverflowStrategy, Supervision }
import akka.stream.scaladsl.{ Sink, Source }
import akka.stream.QueueOfferResult.{ Dropped, Enqueued }
import Processor.{
  ProcessorError,
  ProcessorUnavailable,
  processorUnavailableHandler
}
import TextShuffler.{ ShuffleText, TextShuffled }

import scala.concurrent.{ Future, Promise }
import scala.concurrent.duration.FiniteDuration
import scala.util.{ Failure, Success }

object Api {

  final case class Config(hostname: String,
                          port: Int,
                          terminationDeadline: FiniteDuration,
                          processorTimeout: FiniteDuration)

  final object BindFailure extends Reason

  def apply(
      config: Config,
      textShuffler: TextShuffler.Process
  )(implicit untypedSystem: UntypedSystem, mat: Materializer, ctx: ActorContext[_]): Unit = {
    import config._
    import untypedSystem.dispatcher

    implicit val scheduler: Scheduler = untypedSystem.scheduler
    val shutdown                      = CoordinatedShutdown(untypedSystem)

    val textShufflerProcessor =
      Source
        .queue[(ShuffleText, Promise[TextShuffled])](1, OverflowStrategy.dropNew)
        .via(textShuffler)
        .to(Sink.foreach { case (textShuffled, p) => p.trySuccess(textShuffled) })
        .withAttributes(ActorAttributes.supervisionStrategy(Supervision.resumingDecider))
        .run()

    Http()
      .bindAndHandle(route(config), hostname, port)
      .onComplete {
        case Failure(cause) =>
          ctx.log.error(s"Shutting down, because cannot bind to $hostname:$port!", cause)
          shutdown.run(BindFailure)

        case Success(binding) =>
          ctx.log.info(s"Listening for HTTP connections on ${binding.localAddress}")
          shutdown.addTask(PhaseServiceUnbind, "api.unbind") { () =>
            binding.unbind()
          }
          shutdown.addTask(PhaseServiceRequestsDone, "api.terminate") { () =>
            binding.terminate(terminationDeadline).map(_ => Done)
          }
      }

    def route(config: Config)(implicit scheduler: Scheduler): Route = {
      import akka.http.scaladsl.server.Directives._
      import config._

      path("shuffle-text") {
        get {
          parameter("text") { text =>
            val promisedTextShuffled = ExpiringPromise[TextShuffled](processorTimeout)
            val shuffledText =
              textShufflerProcessor
                .offer((ShuffleText(text), promisedTextShuffled))
                .flatMap {
                  case Enqueued => promisedTextShuffled.future.map(_.text)
                  case Dropped  => Future.failed(ProcessorUnavailable("textShufflerProcessor"))
                  case other    => Future.failed(ProcessorError(other))
                }
            complete(shuffledText)
          }
        }
      }
    }
  }
}
