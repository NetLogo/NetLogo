// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import java.io.{ File, FileOutputStream, IOException }
import java.net.InetAddress
import java.nio.file.{ Path, Paths }
import java.util.zip.{ ZipEntry, ZipOutputStream }

import collection.JavaConverters._
import scala.io.Codec

import org.nlogo.api.{ Equality, NetLogoAdapter }
import org.nlogo.api.Exceptions.ignoring
import org.nlogo.api.FileIO.fileToString

case class LoggerState(
  addListener:   (NetLogoAdapter) => Unit
, loggerFactory: (Path) => FileLogger
, logDirectory:  File
, events:        Set[String]
, studentName:   String
) {
  val logDirectoryPath = this.logDirectory.toPath
}

object LoggerState {
  def empty() = {
    LoggerState(
      (_) => {}
    , (_) => new NoOpLogger()
    , new File("")
    , LogEvents.defaultEvents
    , "unknown"
    )
  }
}

object LogManager {
  private var state: LoggerState               = LoggerState.empty()
  private var logger: FileLogger               = new NoOpLogger()
  private var loggingListener: LoggingListener = new LoggingListener(Set(), LogManager.logger)
  private var modelName: String                = "unset"

  def start(addListener: (NetLogoAdapter) => Unit, loggerFactory: (Path) => FileLogger, logDirectory: File, events: Set[String], studentName: String) {
    if (LogManager.isStarted) {
      throw new IllegalStateException("Logging should only be started once.")
    }

    LogManager.state = LoggerState(addListener, loggerFactory, logDirectory, events, studentName)
    LogManager.loggingListener = new LoggingListener(events, LogManager.logger)

    val restartListener = new NetLogoAdapter {
      override def modelOpened(modelName: String) {
        LogManager.modelName = modelName
        LogManager.restart()
      }
    }
    addListener(restartListener)
    addListener(LogManager.loggingListener)
  }

  private def logStart(modelName: String) {
    val loginName = System.getProperty("user.name")
    val ipAddress = LogManager.getIpAddress
    val startInfo = Map[String, Any](
      "loginName"   -> loginName
    , "studentName" -> LogManager.state.studentName
    , "ipAddress"   -> ipAddress
    , "modelName"   -> modelName
    , "events"      -> LogManager.state.events.toList.asJava
    )
    LogManager.log(LogEvents.Types.start, startInfo)
  }

  def stop() {
    LogManager.log(LogEvents.Types.stop)
    LogManager.logger.close()
    LogManager.logger = new NoOpLogger()
  }

  private def restart(thunk: () => Unit = () => {}) {
    LogManager.stop()
    thunk()
    LogManager.logger                 = LogManager.state.loggerFactory(LogManager.state.logDirectoryPath)
    LogManager.loggingListener.logger = LogManager.logger
    LogManager.logStart(modelName)
  }

  def zipLogFiles(zipFileName: String) {
    if (LogManager.isStarted) {
      val fileNameFilter = LogManager.logger.fileNameFilter
      LogManager.restart(() => {
        val zipPath = Paths.get(zipFileName)
        val zipFile = if (zipPath.isAbsolute) {
          zipPath.toFile
        } else {
          LogManager.state.logDirectoryPath.resolve(zipPath).toFile
        }

        val logFiles = LogManager.state.logDirectory.list(fileNameFilter)
        if (logFiles.nonEmpty) {
          val zipStream = new ZipOutputStream(new FileOutputStream(zipFile))
          logFiles.foreach( (logFileName) => {
            // IOException probably shouldn't ever happen but in case it does just skip the file and
            // move on. ev 3/14/07
            ignoring(classOf[IOException]) {
              zipStream.putNextEntry(new ZipEntry(logFileName))
              val logFilePath = LogManager.state.logDirectoryPath.resolve(logFileName).toAbsolutePath
              val logFileData = fileToString(logFilePath.toString)(Codec.UTF8).getBytes
              zipStream.write(logFileData, 0, logFileData.length)
              zipStream.closeEntry()
            }
          })
          zipStream.flush()
          zipStream.close()
        }
      })
    }
  }

  def deleteLogFiles() {
    if (LogManager.isStarted) {
      val fileNameFilter = LogManager.logger.fileNameFilter
      LogManager.restart( () => {
        val logFiles = LogManager.state.logDirectory.list(fileNameFilter)
        logFiles.foreach( (logFileName) => {
          val logFilePath = Paths.get(logFileName)
          val logFile     = LogManager.state.logDirectoryPath.resolve(logFilePath).toFile
          logFile.delete()
        })
      })
    }
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
    LogManager.state.events.contains(event)
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

  def userComment(comment: String) {
    if (LogManager.isLogging(LogEvents.Types.comment)) {
      val eventInfo = Map[String, Any](
        "comment" -> comment
      )
      LogManager.log(LogEvents.Types.comment, eventInfo)
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
