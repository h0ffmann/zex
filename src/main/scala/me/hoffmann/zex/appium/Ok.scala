package me.hoffmann.zex.appium

import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import me.hoffmann.zex.appium.appium.FoundButPredicateFailed
import org.openqa.selenium.NoSuchElementException

import scala.util.Try

object Ok {

  private def go(element: String,
                 f: String => MobileElement,
                 predicate: MobileElement => Boolean,
                 pollTimeInMillis: Long,
                 maxTimeInMillis: Long,
                 curTimeInMillis: Long = 0): MobileElement = {
    Try{
      val el = f(element)
      if(predicate(el)) {
        el
      } else {
        throw FoundButPredicateFailed(s"Predicate for element $element failed.")
      }
    }.recover{

      case NoSuchElementException | FoundButPredicateFailed(_) if curTimeInMillis < maxTimeInMillis =>
        Thread.sleep(pollTimeInMillis)
        go(element, f, predicate, pollTimeInMillis, maxTimeInMillis, curTimeInMillis+pollTimeInMillis)

      case e: Throwable =>
        throw e

    }.get
  }

  def pollElementById(id: String, maxTimeInMillis: Long,
                      predicate : MobileElement => Boolean = _ => true,
                      pollTimeInMillis: Long = 150)
                     (implicit driver: AndroidDriver[MobileElement]): MobileElement  = {
    go(id, x => driver.findElementById(x), predicate, pollTimeInMillis, maxTimeInMillis)
  }

  def pollElementByXpath(id: String, maxTimeInMillis: Long,
                      predicate : MobileElement => Boolean = _ => true,
                      pollTimeInMillis: Long = 150)
                     (implicit driver: AndroidDriver[MobileElement]): MobileElement  = {
    go(id, x => driver.findElementByXPath(x), predicate, pollTimeInMillis, maxTimeInMillis)
  }

  //...
}
