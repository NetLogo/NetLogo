// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import java.io.File
import java.net.InetAddress

import collection.JavaConverters._

import org.nlogo.api.{ Equality, NetLogoAdapter }
import org.nlogo.window.NetLogoListenerManager

private[log] object LogEventTypes {
  val button        = "button"
  val chooser       = "chooser"
  val compile       = "compile"
  val commandCenter = "command-center"
  val global        = "global"
  val inputBox      = "inputBox"
  val link          = "link"
  val modelOpen     = "model-open"
  val slider        = "slider"
  val speedSlider   = "speed-slider"
  val start         = "start"
  val switch        = "switch"
  val stop          = "stop"
  val tick          = "tick"
  val turtle        = "turtle"
  val widgetEdit    = "widget-edit"
}

object LogManager {
  private var logger: FileLogger = new NoOpLogger()
  private var events: Set[String] = Set()

  def start(listenerManager: NetLogoListenerManager, logFileDirectory: File, events: Set[String], userName: String) {
    if (LogManager.isStarted) {
      throw new IllegalStateException("Logging should only be started once.")
    }
    LogManager.events = events

    val loggingListener = new LoggingListener(events, LogManager.logger)
    val restartListener = new NetLogoAdapter {
      override def modelOpened(modelName: String) {
        LogManager.stop()

        LogManager.logger      = createLogger(logFileDirectory)
        loggingListener.logger = LogManager.logger

        LogManager.logStart(userName, modelName)
      }
    }
    listenerManager.addListener(restartListener)
    listenerManager.addListener(loggingListener)
  }

  private def createLogger(logFileDirectory: File) = {
    new JsonFileLogger(logFileDirectory)
  }

  private def logStart(userName: String, modelName: String) {
    val loginName = System.getProperty("user.name")
    val ipAddress = getIpAddress
    val startInfo = Map[String, Any](
      "loginName"   -> loginName
    , "netLogoName" -> userName
    , "ipAddress"   -> ipAddress
    , "modelName"   -> modelName
    , "events"      -> LogManager.events.toList.asJava
    )
    LogManager.log(LogEventTypes.start, startInfo)
  }

  def stop() {
    LogManager.log(LogEventTypes.stop)
    LogManager.logger.close()
    LogManager.logger = new NoOpLogger()
  }

  def isStarted: Boolean = {
    !LogManager.logger.isInstanceOf[NoOpLogger]
  }

  private def getIpAddress() = {
    try {
      InetAddress.getLocalHost.getHostAddress
    } catch {
      case _: java.net.UnknownHostException => "unknown"
    }
  }

  private def isLogging(event: String): Boolean = {
    LogManager.events.contains(event)
  }

  private def log(event: String, eventInfo: Map[String, Any] = Map()) {
    LogManager.logger.log(event, eventInfo)
  }

  def globalChanged(globalName: String, newValue: AnyRef, oldValue: AnyRef) {
    if (LogManager.isLogging(LogEventTypes.global) && !Equality.equals(newValue, oldValue)) {
      val eventInfo = Map[String, Any](
        "globalName" -> globalName
      , "newValue"   -> AnyRefFormat.forJson(newValue)
      , "oldValue"   -> AnyRefFormat.forJson(oldValue)
      )
      LogManager.log(LogEventTypes.global, eventInfo)
    }
  }

  def linkCreated(id: Long, breedName: String, end1: Long, end2: Long) {
    if (LogManager.isLogging(LogEventTypes.link)) {
      val eventInfo = Map[String, Any](
        "action"    -> "created"
      , "id"        -> id
      , "breedName" -> breedName
      , "end1"      -> end1
      , "end2"      -> end2
      )
      LogManager.log(LogEventTypes.link, eventInfo)
    }
  }

  def linkRemoved(id: Long, breedName: String, end1: Long, end2: Long) {
    if (LogManager.isLogging(LogEventTypes.link)) {
      val eventInfo = Map[String, Any](
        "action"    -> "removed"
      , "id"        -> id
      , "breedName" -> breedName
      , "end1"      -> end1
      , "end2"      -> end2
      )
      LogManager.log(LogEventTypes.link, eventInfo)
    }
  }

  def speedSliderChanged(newSpeed: Double) {
    if (LogManager.isLogging(LogEventTypes.speedSlider)) {
      val eventInfo = Map[String, Any](
        "newSpeed" -> newSpeed
      )
      LogManager.log(LogEventTypes.speedSlider, eventInfo)
    }
  }

  def turtleCreated(who: Long, breedName: String) {
    if (LogManager.isLogging(LogEventTypes.turtle)) {
      val eventInfo = Map[String, Any](
        "action"    -> "created"
      , "who"       -> who
      , "breedName" -> breedName
      )
      LogManager.log(LogEventTypes.turtle, eventInfo)
    }
  }

  def turtleRemoved(who: Long, breedName: String) {
    if (LogManager.isLogging(LogEventTypes.turtle)) {
      val eventInfo = Map[String, Any](
        "action"    -> "removed"
      , "who"       -> who
      , "breedName" -> breedName
      )
      LogManager.log(LogEventTypes.turtle, eventInfo)
    }
  }

  def widgetAdded(widgetType: String, name: String) {
    if (LogManager.isLogging(LogEventTypes.widgetEdit)) {
      val eventInfo = Map[String, Any](
        "action"     -> "added"
      , "widgetType" -> widgetType
      , "name"       -> name
      )
      LogManager.log(LogEventTypes.widgetEdit, eventInfo)
    }
  }

  def widgetRemoved(widgetType: String, name: String) {
    if (LogManager.isLogging(LogEventTypes.widgetEdit)) {
      val eventInfo = Map[String, Any](
        "action"     -> "removed"
      , "widgetType" -> widgetType
      , "name"       -> name
      )
      LogManager.log(LogEventTypes.widgetEdit, eventInfo)
    }
  }

}
