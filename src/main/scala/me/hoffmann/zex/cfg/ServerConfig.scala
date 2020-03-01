package me.hoffmann.zex.cfg

import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString

case class ServerConfig(host: NonEmptyString, port: UserPortNumber)
