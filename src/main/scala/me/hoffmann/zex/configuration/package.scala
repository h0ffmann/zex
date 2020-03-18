package me.hoffmann.zex

import ciris.Secret
import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.Contains
import eu.timepit.refined.string.IPv4
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString
import zio.{Has, ZIO}


package object configuration {

  type IPv4R = String Refined IPv4
  type Email = Refined[String, NonEmptyString And Contains['@']]

  type Configuration = Has[AppiumConfig] with Has[ZapConfig]

  final case class AppiumConfig(host: IPv4R,
                                port: UserPortNumber)

  final case class ZapConfig(user: Email,
                             password: Secret[NonEmptyString],
                             appPackage: NonEmptyString)

  val appiumConfig: ZIO[Has[AppiumConfig], Throwable, AppiumConfig] = ZIO.access(_.get)
  val zapConfig: ZIO[Has[ZapConfig], Throwable, ZapConfig] = ZIO.access(_.get)

}
