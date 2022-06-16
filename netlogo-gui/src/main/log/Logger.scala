// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import java.io.File
import java.net.InetAddress

import collection.JavaConverters._

import org.nlogo.api.Equality
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

object Logger {
  private var _current: FileLogger = new NoOpLogger()
  private var _events: Set[String] = Set()

  def start(listenerManager: NetLogoListenerManager, logFileDirectory: File, events: Set[String], userName: String) {
    if (Logger.isStarted) {
      throw new IllegalStateException("Logging should only be started once.")
    }
    Logger._current = new JsonFileLogger(logFileDirectory)
    Logger._events  = events

    val loginName = "unknown"
    val ipAddress = getIpAddress
    val modelName = "modelName"

    val startInfo = Map[String, Any](
      "loginName"   -> loginName
    , "netLogoName" -> userName
    , "ipAddress"   -> ipAddress
    , "modelName"   -> modelName
    , "events"      -> events.asJava
    )
    Logger.log(LogEventTypes.start, startInfo)

    val loggingListener = new LoggingListener(events, this._current)
    listenerManager.addListener(loggingListener)
  }

  def stop() {
    Logger.log(LogEventTypes.stop)
    Logger._current.close()
    Logger._current = new NoOpLogger()
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

  private def isLogging(event: String): Boolean = {
    Logger._events.contains(event)
  }

  private def log(event: String, eventInfo: Map[String, Any] = Map()) {
    Logger._current.log(event, eventInfo)
  }

  def globalChanged(globalName: String, newValue: AnyRef, oldValue: AnyRef) {
    if (Logger.isLogging(LogEventTypes.global) && !Equality.equals(newValue, oldValue)) {
      val eventInfo = Map[String, Any](
        "globalName" -> globalName
      , "newValue"   -> AnyRefFormat.forJson(newValue)
      , "oldValue"   -> AnyRefFormat.forJson(oldValue)
      )
      Logger.log(LogEventTypes.global, eventInfo)
    }
  }

  def linkCreated(id: Long, breedName: String, end1: Long, end2: Long) {
    if (Logger.isLogging(LogEventTypes.link)) {
      val eventInfo = Map[String, Any](
        "action"    -> "created"
      , "id"        -> id
      , "breedName" -> breedName
      , "end1"      -> end1
      , "end2"      -> end2
      )
      Logger.log(LogEventTypes.link, eventInfo)
    }
  }

  def linkRemoved(id: Long, breedName: String, end1: Long, end2: Long) {
    if (Logger.isLogging(LogEventTypes.link)) {
      val eventInfo = Map[String, Any](
        "action"    -> "removed"
      , "id"        -> id
      , "breedName" -> breedName
      , "end1"      -> end1
      , "end2"      -> end2
      )
      Logger.log(LogEventTypes.link, eventInfo)
    }
  }

  def speedSliderChanged(newSpeed: Double) {
    if (Logger.isLogging(LogEventTypes.speedSlider)) {
      val eventInfo = Map[String, Any](
        "newSpeed" -> newSpeed
      )
      Logger.log(LogEventTypes.speedSlider, eventInfo)
    }
  }

  def turtleCreated(who: Long, breedName: String) {
    if (Logger.isLogging(LogEventTypes.turtle)) {
      val eventInfo = Map[String, Any](
        "action"    -> "created"
      , "who"       -> who
      , "breedName" -> breedName
      )
      Logger.log(LogEventTypes.turtle, eventInfo)
    }
  }

  def turtleRemoved(who: Long, breedName: String) {
    if (Logger.isLogging(LogEventTypes.turtle)) {
      val eventInfo = Map[String, Any](
        "action"    -> "removed"
      , "who"       -> who
      , "breedName" -> breedName
      )
      Logger.log(LogEventTypes.turtle, eventInfo)
    }
  }

  def widgetAdded(widgetType: String, name: String) {
    if (Logger.isLogging(LogEventTypes.widgetEdit)) {
      val eventInfo = Map[String, Any](
        "action"     -> "added"
      , "widgetType" -> widgetType
      , "name"       -> name
      )
      Logger.log(LogEventTypes.widgetEdit, eventInfo)
    }
  }

  def widgetRemoved(widgetType: String, name: String) {
    if (Logger.isLogging(LogEventTypes.widgetEdit)) {
      val eventInfo = Map[String, Any](
        "action"     -> "removed"
      , "widgetType" -> widgetType
      , "name"       -> name
      )
      Logger.log(LogEventTypes.widgetEdit, eventInfo)
    }
  }

}
