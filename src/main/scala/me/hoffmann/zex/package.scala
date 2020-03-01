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

import zio.UIO
import zio.logging.{ LogAnnotation, Logging }
import zio.logging.slf4j.Slf4jLogger

package object zex {

  type Iterable[+A]   = scala.collection.immutable.Iterable[A]
  type Seq[+A]        = scala.collection.immutable.Seq[A]
  type IndexedSeq[+A] = scala.collection.immutable.IndexedSeq[A]

  val correlationId: LogAnnotation[String] = LogAnnotation[String](
    name = "correlationId",
    initialValue = "undefined-correlation-id",
    combine = (_, newValue) => newValue,
    render = identity
  )

  //val logFormat = "[correlation-id = %s] %s"
  val logFormat = " %s"

  val logEnv: UIO[Logging] =
    Slf4jLogger.make { (_, message) =>
      logFormat.format(message)
    }

  val anotherEnv: UIO[FakeEnv] = UIO.succeed(new FakeEnv {override def a: String = "potato"})

}
