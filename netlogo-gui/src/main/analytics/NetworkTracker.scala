// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.analytics

import java.net.{ HttpURLConnection, NetworkInterface, URI }

import scala.jdk.CollectionConverters.EnumerationHasAsScala
import scala.util.Try

class NetworkTracker(domain: String) {
  private var available = false
  private var lastCheck = 0L

  private [analytics] def checkAvailable(): (Boolean, Boolean) = {
    synchronized {
      val wasAvailable = available

      if (!available && System.currentTimeMillis() - lastCheck >= 5000)
        updateAvailable()

      (wasAvailable, available)
    }
  }

  private [analytics] def checkNetwork(): Unit = {
    synchronized {
      updateAvailable()
    }
  }

  private def updateAvailable(): Unit = {
    available =
      NetworkInterface.getNetworkInterfaces().asScala.exists(_.isUp) &&
        Try {
          val url  = URI.create(s"$domain/telemetry/diagnostic").toURL
          val conn = url.openConnection().asInstanceOf[HttpURLConnection]
          conn.setConnectTimeout(5000)
          conn.   setReadTimeout(5000)
          conn.getResponseCode()
        }.toOption.contains(200)

    lastCheck = System.currentTimeMillis()
  }
}
