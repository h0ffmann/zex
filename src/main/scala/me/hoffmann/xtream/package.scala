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

package me.hoffmann

import akka.actor.typed.ActorRef
import akka.stream.DelayOverflowStrategy
import akka.stream.scaladsl.FlowWithContext
import scala.concurrent.duration.FiniteDuration

package object xtream {

  type Iterable[+A]   = scala.collection.immutable.Iterable[A]
  type Seq[+A]        = scala.collection.immutable.Seq[A]
  type IndexedSeq[+A] = scala.collection.immutable.IndexedSeq[A]

  type Respondee[A] = ActorRef[Respondee.Response[A]]

  final implicit class FlowWithContextExt[In, CtxIn, Out, CtxOut](
      val flowWithContext: FlowWithContext[In, CtxIn, Out, CtxOut, Any]
  ) extends AnyVal {

    def delay(of: FiniteDuration,
              strategy: DelayOverflowStrategy): FlowWithContext[In, CtxIn, Out, CtxOut, Any] =
      FlowWithContext.fromTuples(flowWithContext.asFlow.delay(of, strategy))
  }
}
