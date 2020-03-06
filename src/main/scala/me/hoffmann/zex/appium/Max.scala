package me.hoffmann.zex.appium

import cats.effect.{IO, Timer}
import io.appium.java_client.android.AndroidDriver
import me.hoffmann.zex.appium.appium.FoundButPredicateFailed
import retry.RetryDetails
import retry.RetryDetails.{GivingUp, WillDelayAndRetry}
import cats.effect.IO
import io.appium.java_client.MobileElement

import scala.concurrent.duration.FiniteDuration
import retry._
import retry.RetryDetails._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.FiniteDuration


object Max {
  implicit val timer: Timer[IO] = IO.timer(global)

  private def go(element: String,
                 f: String => MobileElement,
                 predicate: MobileElement => Boolean): IO[MobileElement] = {
    IO {
      val el = f(element)
      if (predicate(el)) {
        el
      } else {
        throw FoundButPredicateFailed(s"Predicate for element $element failed.")
      }
    }
  }

  private def logError(err: Throwable, details: RetryDetails): IO[Unit] = details match {

    case WillDelayAndRetry(_, retriesSoFar, acc) =>
      IO(println(s"Failed to try. So far we have retried $retriesSoFar times. Counting $acc"))

    case GivingUp(totalRetries, delay) =>
      IO(println(s"Giving up after $totalRetries within $delay retries"))
  }

  def pollElementById(id: String, predicate: MobileElement => Boolean = _ => true)(policy: RetryPolicy[IO])
                     (implicit driver: AndroidDriver[MobileElement]): IO[MobileElement] = {
    retryingOnAllErrors[MobileElement](
      policy = policy,
      onError = logError
    )(go(id, x => driver.findElementById(x), predicate))
  }

  def pollElementByXpath(id: String, predicate: MobileElement => Boolean = _ => true)(policy: RetryPolicy[IO])
                        (implicit driver: AndroidDriver[MobileElement]): IO[MobileElement] = {
    retryingOnAllErrors[MobileElement](
      policy = policy,
      onError = logError
    )(go(id, x => driver.findElementByXPath(x), predicate))
  }

  //...
}
