// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import java.io.File
import java.net.InetAddress
import java.time.format.DateTimeFormatter

import org.nlogo.api.Equality
import org.nlogo.window.NetLogoListenerManager

private[log] object LogEventTypes {
  val global = "global"
  val slider = "slider"
}

object Logger {
  private var _current: FileLogger = new NoOpLogger()
  private var _events: Set[String] = Set()

  def start(listenerManager: NetLogoListenerManager, logFileDirectory: File, events: Set[String], userName: String) {
    if (Logger.isStarted) {
      throw new IllegalStateException("Logging should only be started once.")
    }
    Logger._current = new JsonFileLogger(logFileDirectory, userName)
    Logger._events  = events

    val loginName = "unknown"
    val ipAddress = getIpAddress
    val modelName = "modelName"

    val startInfo = Map(
      "loginName"   -> loginName
    , "netLogoName" -> userName
    , "ipAddress"   -> ipAddress
    , "modelName"   -> modelName
    )
    Logger.log("start", startInfo)

    val loggingListener = new LoggingListener(events, this._current)
    listenerManager.addListener(loggingListener)
  }

  def stop() {
    Logger.log("stop")
    Logger._current.close()
  }

  def isStarted: Boolean = {
    !Logger._current.isInstanceOf[NoOpLogger]
  }

  private def getIpAddress() = {
    try {
      InetAddress.getLocalHost.getHostAddress
    } catch {
      case _: java.net.UnknownHostException => "unknown"
    }
  }

  private[log] val fileDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
  private[log] val logDateFormat  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")

  private def isLogging(event: String): Boolean = {
    Logger._events.contains(event)
  }

  private def log(event: String, eventInfo: Map[String, String] = Map()) {
    Logger._current.log(event, eventInfo)
  }

  def globalChanged(globalName: String, newValue: AnyRef, oldValue: AnyRef) {
    if (Logger.isLogging(LogEventTypes.global) && !Equality.equals(newValue, oldValue)) {
      val eventInfo = Map(
        "globalName" -> globalName
      , "newValue"   -> newValue.toString
      , "oldValue"   -> oldValue.toString
      )
      Logger.log(LogEventTypes.global, eventInfo)
    }
  }

}
