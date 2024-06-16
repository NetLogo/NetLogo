// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import java.awt.Component
import java.io.{ File, FileOutputStream, IOException }
import java.net.{ InetAddress, UnknownHostException }
import java.nio.file.{ Path, Paths }
import java.util.zip.{ ZipEntry, ZipOutputStream }

import collection.JavaConverters._
import scala.io.Codec

import org.nlogo.api.{ Equality, NetLogoAdapter, Version }
import org.nlogo.api.Exceptions.{ ignoring, warning }
import org.nlogo.api.FileIO.fileToString
import org.nlogo.core.I18N
import org.nlogo.swing.OptionDialog

// Welcome to Logging.

// This object and its related pieces replace the log4j-based XML logging system NetLogo
// had been using.  Facing updates to log4j 2 (needed for security reasons), I had the
// choice of learning way too much about how log4j worked, or switching to something
// simpler and more cohesive.  So I switched.  On the plus side the full logging config
// can now be managed through the preferences or via command line switches, no external
// files required.  We also have logging coming for other platforms (NetLogo Web, Turtle
// Universe), and those will log to JSON, so this can also be a step towards unifying the
// log events across systems so they can be (ideally) processed in the same ways.

// I will attempt to explain some design decisions and odd-looking things here:

// 1. The `LogManage.start()` takes in functions for adding a listener and creating the
//    logger to avoid unwanted dependencies.  For the listener, there is only the
//    `NetLogoListenerManager` which lives in `netlogo-gui` and has a bunch of GUI event
//    interfaces attached to it, so we can't move it to `netlogo-core` to share with `headless`.
//    An alternative might've been making a new trait for it, but I think we have enough
//    traits already, to be honest.  For the logger, it was because the JSON library we
//    use is not depended on by `headless`, so it stays in `netlogo` only and is
//    provided by `App`.

// 2. There is some boilerplate around `LogEvents.eventName` checks and `LogManager.log()`
//    calls that could be unified, but I couldn't think of a clean way to do it that
//    wouldn't cause more memory and processor use in the case that logging is disabled
//    (which will be most of the time).  I don't want to make eventInfo Map instances if I
//    don't have to and I also don't want to make closures that could run later.  The best
//    way might be something like a dynamic instance with its logging methods filled in at
//    runtime (as we'd do in JavaScript) but I didn't feel like hacking that in.  I
//    decided to keep it obvious, clean, and simple.

// 3. Previously logging entry points were all over the place.  There were custom events,
//    direct "static" or "global" access, the logging listener, and access via the model
//    workspace.  All of that required interface methods and made a lot of weird "hot
//    potato" with the logging messages and two logging prims.  So it's all gone and
//    hopefully simplified now.  We have access via the logging listener and direct static
//    access.  Ideally the rest of the events that depend on the static access would be
//    turned into events reported through the `NetLogoAdapter` and handled by the
//    `LoggingListener` at some point in the future.

// -Jeremy B 2022

case class LoggerState(
  addListener:   (NetLogoAdapter) => Unit
, loggerFactory: (Path) => FileLogger
, logDirectory:  File
, events:        LogEvents
, studentName:   String
) {
  val logDirectoryPath = logDirectory.toPath
}

object LoggerState {
  val emptyEvents = new LogEvents(Set())
  val noOpLogger  = new NoOpLogger()

  def empty() = {
    LoggerState(
      (_) => {}
    , (_) => LoggerState.noOpLogger
    , new File("")
    , LoggerState.emptyEvents
    , "unknown"
    )
  }
}

object LogManager {
  var isStarted: Boolean                       = false
  private var state: LoggerState               = LoggerState.empty()
  private var logger: FileLogger               = LoggerState.noOpLogger
  private var loggingListener: LoggingListener = new LoggingListener(LoggerState.emptyEvents, LogManager.logger)
  private var modelName: String                = "unset"
  private var dialogFrame: Component           = null

  def start(addListener: (NetLogoAdapter) => Unit, loggerFactory: (Path) => FileLogger, logDirectory: File,
            eventsSet: Set[String], studentName: String, dialogFrame: Component) {
    if (LogManager.isStarted) {
      throw new IllegalStateException("Logging should only be started once.")
    }

    LogManager.isStarted       = true
    val events                 = new LogEvents(eventsSet)
    LogManager.state           = LoggerState(addListener, loggerFactory, logDirectory, events, studentName)
    LogManager.loggingListener = new LoggingListener(events, LogManager.logger)
    LogManager.dialogFrame = dialogFrame

    // We don't actually start logging until a model is opened and `restart()` is called.
    val restartListener = new NetLogoAdapter {
      override def modelOpened(modelName: String) {
        LogManager.modelName = modelName
        LogManager.restart()
      }
    }
    addListener(restartListener)
    addListener(LogManager.loggingListener)
  }

  def stop() {
    LogManager.logStop()
    warning(classOf[Exception]) {
      LogManager.logger.close()
    }
    LogManager.logger = new NoOpLogger()
    LogManager.loggingListener.logger = LogManager.logger
  }

  private def restart(thunk: () => Unit = () => {}) {
    LogManager.stop()
    thunk()
    // If the logger blows up for any reason (security, disk full, etc), just ignore it and output to the error stream
    // so NetLogo can at least continune running with the NoOpLogger -Jeremy B February 2024
    try {
      LogManager.logger                 = LogManager.state.loggerFactory(LogManager.state.logDirectoryPath)
      LogManager.loggingListener.logger = LogManager.logger
    } catch {
      case _: Throwable =>
        OptionDialog.showMessage(dialogFrame, I18N.gui.get("common.messages.warning"),
                                I18N.gui.get("error.dialog.logDirectory"),
                                Array[Object](I18N.gui.get("common.buttons.ok")))
    }
    LogManager.logStart(modelName)
  }

  def zipLogFiles(zipFileName: String) {
    if (LogManager.isStarted) {
      val fileNameFilter = LogManager.logger.fileNameFilter
      LogManager.restart( () => {
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

  private def getIpAddress() = {
    try {
      InetAddress.getLocalHost.getHostAddress
    } catch {
      case _: UnknownHostException => "unknown"
    }
  }

  private def log(event: String, eventInfo: Map[String, Any] = Map()) {
    // We do not check for any IO exceptions from the logger here, we expect it to handle those internally.  That
    // prevents us from running a try/catch for events that won't be logged when it is not enabled, which is most of the
    // time.  -Jeremy B February 2024
    LogManager.logger.log(event, eventInfo)
  }

  private def logStart(modelName: String) {
    val loginName = System.getProperty("user.name")
    val ipAddress = LogManager.getIpAddress
    val startInfo = Map[String, Any](
      "loginName"   -> loginName
    , "studentName" -> LogManager.state.studentName
    , "ipAddress"   -> ipAddress
    , "modelName"   -> modelName
    , "version"     -> Version.version
    , "events"      -> LogManager.state.events.set.toList.asJava
    )
    LogManager.log(LogEvents.Types.start, startInfo)
  }

  private def logStop() {
    LogManager.log(LogEvents.Types.stop)
  }

  def globalChanged(globalName: String, newValue: AnyRef, oldValue: AnyRef) {
    if (LogManager.state.events.global && !Equality.equals(newValue, oldValue)) {
      val eventInfo = Map[String, Any](
        "globalName" -> globalName
      , "newValue"   -> newValue
      , "oldValue"   -> oldValue
      )
      LogManager.log(LogEvents.Types.global, eventInfo)
    }
  }

  def linkCreated(id: Long, breedName: String, end1: Long, end2: Long) {
    if (LogManager.state.events.link) {
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
    if (LogManager.state.events.link) {
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
    if (LogManager.state.events.speedSlider) {
      val eventInfo = Map[String, Any](
        "newSpeed" -> newSpeed
      )
      LogManager.log(LogEvents.Types.speedSlider, eventInfo)
    }
  }

  def turtleCreated(who: Long, breedName: String) {
    if (LogManager.state.events.turtle) {
      val eventInfo = Map[String, Any](
        "action"    -> "created"
      , "who"       -> who
      , "breedName" -> breedName
      )
      LogManager.log(LogEvents.Types.turtle, eventInfo)
    }
  }

  def turtleRemoved(who: Long, breedName: String) {
    if (LogManager.state.events.turtle) {
      val eventInfo = Map[String, Any](
        "action"    -> "removed"
      , "who"       -> who
      , "breedName" -> breedName
      )
      LogManager.log(LogEvents.Types.turtle, eventInfo)
    }
  }

  def userComment(comment: String) {
    if (LogManager.state.events.comment) {
      val eventInfo = Map[String, Any](
        "comment" -> comment
      )
      LogManager.log(LogEvents.Types.comment, eventInfo)
    }
  }

  def widgetAdded(isLoading: Boolean, widgetType: String, name: String) {
    if (LogManager.state.events.widgetEdit && !isLoading) {
      val eventInfo = Map[String, Any](
        "action"     -> "added"
      , "widgetType" -> widgetType
      , "name"       -> name
      )
      LogManager.log(LogEvents.Types.widgetEdit, eventInfo)
    }
  }

  def widgetRemoved(isUnloading: Boolean, widgetType: String, name: String) {
    if (LogManager.state.events.widgetEdit && !isUnloading) {
      val eventInfo = Map[String, Any](
        "action"     -> "removed"
      , "widgetType" -> widgetType
      , "name"       -> name
      )
      LogManager.log(LogEvents.Types.widgetEdit, eventInfo)
    }
  }

}
