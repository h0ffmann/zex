package me.hoffmann.zex.configuration

import ciris.ConfigValue
import eu.timepit.refined.types.string.NonEmptyString
import ciris._
import eu.timepit.refined.types.net.UserPortNumber
import ciris.refined._
import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import ciris._
import ciris.refined._
import enumeratum.{CirisEnum, Enum, EnumEntry}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
//import eu.timepit.refined.cats._
import eu.timepit.refined.collection.MinSize
import eu.timepit.refined.string.MatchesRegex
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.W
import scala.concurrent.duration._

final case class AppConfig(server: ServerConfig)

//object AppConfig {
//  val databaseConfig: ConfigValue[AppConfig] =
//    (
//      env("BOUND_HOST").as[NonEmptyString].default("username"),
//      env("BOUND_PORT").as[UserPortNumber].secret
//      ).parMapN((x,y) => AppConfig(ServerConfig(x, y.value)))
//}
