package me.hoffmann.zex.retry

import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver

import scala.util.Try

object Poor {

  private def go(id: String,
         pollTimeInMillis: Long,
         maxTimeInMillis: Long,
         curTimeInMillis: Long = 0)(implicit driver: AndroidDriver[MobileElement]): MobileElement = {
    Try{
      driver.findElementById(id)
    }.recover{
      case _ : NoSuchElementException if curTimeInMillis < maxTimeInMillis =>
        Thread.sleep(pollTimeInMillis)
        go(id, pollTimeInMillis, maxTimeInMillis, curTimeInMillis+pollTimeInMillis)
      case e: Throwable =>
        throw e
    }.get
  }

  def pollElementById(id: String, pollTimeInMillis: Long, maxTimeInMillis: Long)
                     (implicit driver: AndroidDriver[MobileElement]): MobileElement  = {
    go(id, pollTimeInMillis, maxTimeInMillis)
  }

}
