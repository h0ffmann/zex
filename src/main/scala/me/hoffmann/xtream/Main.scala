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

import akka.actor.{ ActorSystem => UntypedSystem }
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.typed.{
  Cluster,
  ClusterSingleton,
  SelfUp,
  SingletonActor,
  Subscribe,
  Unsubscribe
}
import pureconfig.generic.auto._
import akka.stream.Materializer
import pureconfig.ConfigSource

object Main {

  private final case class Config(api: Api.Config, textShuffler: TextShuffler.Config)

  def main(args: Array[String]): Unit = {

    val config = ConfigSource.default.at("xtream").loadOrThrow[Config] // Must be first to aviod creating the actor system on failure!
    val system = UntypedSystem("xtream")
    system.spawn(Main(config), "main")
  }

  def apply(config: Config): Behavior[SelfUp] =
    Behaviors.setup { implicit ctx =>
      ctx.log.info(s" started and ready to join cluster")

      val cluster = Cluster(ctx.system)
      cluster.subscriptions ! Subscribe(ctx.self, classOf[SelfUp])

      Behaviors.receive { (context, _) =>
        ctx.log.info(s"${context.system.name} joined cluster and is up")

        cluster.subscriptions ! Unsubscribe(context.self)

        implicit val untypedSystem: UntypedSystem = context.system.toClassic
        implicit val mat: Materializer            = Materializer(ctx)

        val wordShuffler =
          ClusterSingleton(context.system).init(
            SingletonActor(WordShuffler(), "word-shuffler").withStopMessage(WordShuffler.Shutdown)
          )

        Api(config.api, TextShuffler(config.textShuffler, wordShuffler)(mat, ctx.system))

        Behaviors.empty
      }
    }
}
