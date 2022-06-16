// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import java.io.File
import java.net.InetAddress

import collection.JavaConverters._

import org.nlogo.api.{ Equality, NetLogoAdapter }
import org.nlogo.window.NetLogoListenerManager

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
    LogManager.log(LogEvents.Types.start, startInfo)
  }

  def stop() {
    LogManager.log(LogEvents.Types.stop)
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
    if (LogManager.isLogging(LogEvents.Types.global) && !Equality.equals(newValue, oldValue)) {
      val eventInfo = Map[String, Any](
        "globalName" -> globalName
      , "newValue"   -> AnyRefFormat.forJson(newValue)
      , "oldValue"   -> AnyRefFormat.forJson(oldValue)
      )
      LogManager.log(LogEvents.Types.global, eventInfo)
    }
  }

  def linkCreated(id: Long, breedName: String, end1: Long, end2: Long) {
    if (LogManager.isLogging(LogEvents.Types.link)) {
      val eventInfo = Map[String, Any](
        "action"    -> "created"
      , "id"        -> id
      , "breedName" -> breedName
      , "end1"      -> end1
      , "end2"      -> end2
      )
      LogManager.log(LogEvents.Types.link, eventInfo)
    }
  }

  def linkRemoved(id: Long, breedName: String, end1: Long, end2: Long) {
    if (LogManager.isLogging(LogEvents.Types.link)) {
      val eventInfo = Map[String, Any](
        "action"    -> "removed"
      , "id"        -> id
      , "breedName" -> breedName
      , "end1"      -> end1
      , "end2"      -> end2
      )
      LogManager.log(LogEvents.Types.link, eventInfo)
    }
  }

  def speedSliderChanged(newSpeed: Double) {
    if (LogManager.isLogging(LogEvents.Types.speedSlider)) {
      val eventInfo = Map[String, Any](
        "newSpeed" -> newSpeed
      )
      LogManager.log(LogEvents.Types.speedSlider, eventInfo)
    }
  }

  def turtleCreated(who: Long, breedName: String) {
    if (LogManager.isLogging(LogEvents.Types.turtle)) {
      val eventInfo = Map[String, Any](
        "action"    -> "created"
      , "who"       -> who
      , "breedName" -> breedName
      )
      LogManager.log(LogEvents.Types.turtle, eventInfo)
    }
  }

  def turtleRemoved(who: Long, breedName: String) {
    if (LogManager.isLogging(LogEvents.Types.turtle)) {
      val eventInfo = Map[String, Any](
        "action"    -> "removed"
      , "who"       -> who
      , "breedName" -> breedName
      )
      LogManager.log(LogEvents.Types.turtle, eventInfo)
    }
  }

  def widgetAdded(widgetType: String, name: String) {
    if (LogManager.isLogging(LogEvents.Types.widgetEdit)) {
      val eventInfo = Map[String, Any](
        "action"     -> "added"
      , "widgetType" -> widgetType
      , "name"       -> name
      )
      LogManager.log(LogEvents.Types.widgetEdit, eventInfo)
    }
  }

  def widgetRemoved(widgetType: String, name: String) {
    if (LogManager.isLogging(LogEvents.Types.widgetEdit)) {
      val eventInfo = Map[String, Any](
        "action"     -> "removed"
      , "widgetType" -> widgetType
      , "name"       -> name
      )
      LogManager.log(LogEvents.Types.widgetEdit, eventInfo)
    }
  }

}
