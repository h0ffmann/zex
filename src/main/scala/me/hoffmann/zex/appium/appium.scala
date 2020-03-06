package me.hoffmann.zex.appium

object appium {

  final case class FoundButPredicateFailed(msg: String) extends Exception(msg)

}
