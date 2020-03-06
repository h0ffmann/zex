package me.hoffmann.zex.appium

import akka.actor.Scheduler
import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import me.hoffmann.zex.appium.appium.FoundButPredicateFailed
import org.openqa.selenium.NoSuchElementException
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

object Async {

  private def go(element: String,
                 f: String => MobileElement,
                 predicate: MobileElement => Boolean,
                 pollTimeInMillis: Long,
                 maxTimeInMillis: Long,
                 curTimeInMillis: Long = 0)(implicit ec: ExecutionContext, s: Scheduler): Future[MobileElement] = {
    Future{
      val el = f(element)
      if(predicate(el)) {
        el
      } else {
        throw FoundButPredicateFailed(s"Predicate for element $element failed.")
      }
    }.recoverWith{

      case NoSuchElementException | FoundButPredicateFailed(_) if curTimeInMillis < maxTimeInMillis =>
        //Thread.sleep(pollTimeInMillis)
        akka.pattern.after(pollTimeInMillis.millis, using = s)(
          go(element, f, predicate, pollTimeInMillis, maxTimeInMillis, curTimeInMillis+pollTimeInMillis)
        )

      case e: Throwable =>
        Future.failed(e)
    }
  }

  def pollElementById(id: String, maxTimeInMillis: Long,
                      predicate : MobileElement => Boolean = _ => true,
                      pollTimeInMillis: Long = 150)
                     (implicit driver: AndroidDriver[MobileElement], ec: ExecutionContext, s: Scheduler): Future[MobileElement]  = {
    go(id, x => driver.findElementById(x), predicate, pollTimeInMillis, maxTimeInMillis)
  }

  def pollElementByXpath(id: String, maxTimeInMillis: Long,
                         predicate : MobileElement => Boolean = _ => true,
                         pollTimeInMillis: Long = 150)
                        (implicit driver: AndroidDriver[MobileElement], ec: ExecutionContext, s: Scheduler): Future[MobileElement]  = {
    go(id, x => driver.findElementByXPath(x), predicate, pollTimeInMillis, maxTimeInMillis)
  }

}
