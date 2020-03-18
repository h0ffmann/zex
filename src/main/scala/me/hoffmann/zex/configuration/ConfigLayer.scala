package me.hoffmann.zex.configuration

import cats.effect.ContextShift
import cats.implicits._
import ciris.refined._
import ciris.{ConfigValue, env}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.cats._
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger
import zio.{Has, Layer, Task, UIO, ZLayer}
import zio.{Has, Layer, Task, ZLayer}

import scala.concurrent.ExecutionContext.Implicits.global

object ConfigLayer {

  implicit val dummy: ContextShift[cats.effect.IO] = cats.effect.IO.contextShift(global)

  private def getAppiumConfig: ConfigValue[AppiumConfig] = {
    (
      env(s"APPIUM_HOST").as[IPv4R].default(Refined.unsafeApply("0.0.0.0")),
      env(s"APPIUM_PORT").as[UserPortNumber].default(Refined.unsafeApply(4723))
      ).parMapN(AppiumConfig)
  }

  private def getZapConfig: ConfigValue[AppiumConfig] = {
    (
      env(s"ZAP_USER").as[Email],
      env(s"ZAP_PASSWORD").as[NonEmptyString].secret
      ).parMapN(ZapConfig)
  }

//  val logEnv: UIO[Logging] =
//    Slf4jLogger.make { (_, message) =>
//      " %s".format(message)
//    }

  val layer: Layer[Throwable, Configuration] = ZLayer.fromEffectMany(
    Task
      .effect{
        val appium = getAppiumConfig.load[cats.effect.IO].unsafeRunSync()
        val zap    = getZapConfig.load[cats.effect.IO].unsafeRunSync()
        (appium, zap)
      })//.map(c => Has(c._1)  Has(c._2)))
      //.map(c => (Has(c._1) ++ Has(c._2) ))
}

