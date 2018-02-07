// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

// A note on log4j: Reuven, who was one of the main clients for this logging stuff, requested log4j.
// We did not investigate log4j vs. java.util.logging but just went along with Reuven's
// suggestion. - ST 2/25/08

import java.util.{ Enumeration => JEnumeration, List => JList, ArrayList }

import org.apache.log4j.{ Appender, FileAppender, Logger => JLogger, LogManager }
import org.apache.log4j.xml.DOMConfigurator

import org.nlogo.api.{ Logger => APILogger, TwoDVersion, Version }
import org.nlogo.fileformat

object Logger extends APILogger {

  private val name = classOf[Logger].getName

  val Buttons = JLogger.getLogger(name + ".BUTTONS")
  val Greens = JLogger.getLogger(name + ".GREENS")
  val Code = JLogger.getLogger(name + ".CODE")
  val Widgets = JLogger.getLogger(name + ".WIDGETS")
  val Globals = JLogger.getLogger(name + ".GLOBALS")
  val Speed = JLogger.getLogger(name + ".SPEED")
  val Turtles = JLogger.getLogger(name + ".TURTLES")
  val Links = JLogger.getLogger(name + ".LINKS")
  val CustomMessages = JLogger.getLogger(name + ".CUSTOM_MESSAGES")
  val CustomGlobals = JLogger.getLogger(name + ".CUSTOM_GLOBALS")

  val widgetMsg = LogMessage.createWidgetMessage()
  val speedMsg = LogMessage.createSpeedMessage()
  val tickMsg = LogMessage.createGlobalMessage("ticks")
  val mortalityMsg = LogMessage.createAgentMessage()
  val buttonMsg = LogMessage.createButtonMessage()
  val sliderMsg = LogMessage.createSliderMessage()
  val switchMsg = LogMessage.createGlobalMessage("switch")
  val chooserMsg = LogMessage.createGlobalMessage("chooser")
  val inputBoxMsg = LogMessage.createGlobalMessage("input box")
  val commandMsg = LogMessage.createCommandMessage()
  val codeTabMsg = LogMessage.createCodeTabMessage()
  val globalMsg = LogMessage.createGlobalMessage("globals")
  val customMsg = LogMessage.createCustomMessage()
  val customGlobals = LogMessage.createCustomGlobals()

  def logButtonStopped(name: String, onceButton: Boolean, stopping: Boolean) {
    if (Buttons.isInfoEnabled) {
      val message = (onceButton, stopping) match {
        case (true, _) => "once"
        case (_, true) => "user"
        case _ => "code"
      }
      buttonMsg.updateButtonMessage(name, "released", message)
      Buttons.info(buttonMsg)
    }
  }
  def logButtonPressed(name: String) {
    buttonMsg.updateButtonMessage(name, "pressed", "user")
    Buttons.info(buttonMsg)
  }
  def logAddWidget(tpe: String, name: String) {
    widgetMsg.updateWidgetMessage(tpe.toLowerCase, name, "added")
    Widgets.info(widgetMsg)
  }
  def logWidgetRemoved(tpe: String, name: String) {
    widgetMsg.updateWidgetMessage(tpe.toLowerCase, name, "removed")
    Widgets.info(widgetMsg)
  }
  def logSpeedSlider(value: Double) {
    speedMsg.updateSpeedMessage(value.toString)
    Speed.info(speedMsg)
  }
  def logTurtleBirth(name: String, breed: String) {
    mortalityMsg.updateAgentMessage("turtle", name, "born", breed)
    Turtles.info(mortalityMsg)
  }
  def logTurtleDeath(name: String, breed: String) {
    mortalityMsg.updateAgentMessage("turtle", name, "died", breed)
    Turtles.info(mortalityMsg)
  }
  def logGlobal(name: String, value: AnyRef, changed: Boolean) {
    globalMsg.updateGlobalMessage(name, value.toString)
    if (changed)
      Globals.info(globalMsg)
    else
      Globals.debug(globalMsg)
  }
  def logCustomMessage(msg: String): Unit = {
    customMsg.updateCustomMessage(msg)
    CustomMessages.info(customMsg)
  }
  def logCustomGlobals(nameValuePairs: (String, String)*): Unit = {
    customGlobals.updateCustomGlobals(nameValuePairs)
    CustomGlobals.info(customGlobals)
  }

  ///

  def beQuiet() {
    org.apache.log4j.helpers.LogLog.setQuietMode(true)
  }

  def configure(properties: String) {
    DOMConfigurator.configure(properties)
  }

}

class Logger(studentName: String) extends LoggingListener {

  var logDirectory = System.getProperty("java.io.tmpdir")

  var version: Version = TwoDVersion

  def configure(reader: java.io.Reader) {
    val configurator = new DOMConfigurator
    configurator.doConfigure(reader, LogManager.getLoggerRepository)
  }

  def changeLogDirectory(path: String) {
    val directory = new java.io.File(path)
    if (!directory.isAbsolute) {
      val newPath = System.getProperty("user.home") + java.io.File.separatorChar + "dummy.txt"
      val urlForm = new java.net.URL(
        org.nlogo.util.JUtils.toURL(new java.io.File(newPath)),
        path)
      logDirectory = new java.io.File(urlForm.getFile).getAbsolutePath
    }
    else if (directory.isDirectory) {
      logDirectory = path
    }
  }

  var filenames: java.util.List[String] = null // for TestLogger

  def modelOpened(name: String) {
    filenames = new java.util.ArrayList[String]
    filenames.addAll(newFiles(JLogger.getRootLogger.getAllAppenders, name))
    val loggers = JLogger.getRootLogger.getLoggerRepository.getCurrentLoggers
    fileformat.modelVersionAtPath(name).foreach { v =>
      version = v
    }
    while (loggers.hasMoreElements) {
      val l = loggers.nextElement().asInstanceOf[JLogger]
      filenames.addAll(newFiles(l.getAllAppenders, name))
    }
  }

  def newFiles(e: JEnumeration[_], name: String): JList[String] = {
    val filenames = new ArrayList[String]
    while (e.hasMoreElements)
      for(appender @ (a: FileAppender) <- Some(e.nextElement())) {
        val filename = logFileName(appender.getName)
        filenames.add(filename)
        appender.setFile(filename)
        for(xappender @ (x: XMLFileAppender) <- Some(appender))
          setupXMLFileAppender(name, xappender)
        appender.activateOptions()
      }
    filenames
  }

  private def setupXMLFileAppender(name: String, xappender: XMLFileAppender) {
    xappender.setStudentName(studentName)
    xappender.setUsername(System.getProperty("user.name"))
    xappender.setIPAddress(getIPAddress)
    xappender.setModelName(Option(name).getOrElse("new model"))
    xappender.setVersion(version.version)
  }

  def close() {
    closeFiles(JLogger.getRootLogger.getAllAppenders)
    val loggers = JLogger.getRootLogger.getLoggerRepository.getCurrentLoggers
    while (loggers.hasMoreElements) {
      val l = loggers.nextElement().asInstanceOf[JLogger]
      closeFiles(l.getAllAppenders)
    }
  }

  private def closeFiles(e: JEnumeration[_]) {
    while (e.hasMoreElements)
      e.nextElement().asInstanceOf[Appender].close()
  }

  def getIPAddress() =
    try java.net.InetAddress.getLocalHost.getHostAddress
    catch {
      case _: java.net.UnknownHostException => "unknown"
    }

  private val dateFormat =
    new java.text.SimpleDateFormat("yyyy-MM-dd.HH_mm_ss_SS", java.util.Locale.US)

  private def logFileName(appender: String) =
    logDirectory +
      System.getProperty("file.separator") + "logfile_" + appender + "_" +
      dateFormat.format(new java.util.Date) + ".xml"

  def deleteSessionFiles() {
    Files.deleteSessionFiles(logDirectory)
  }

  def zipSessionFiles(filename: String) {
    Files.zipSessionFiles(logDirectory, filename)
  }

  def getFileList: Array[String] =
    new java.io.File(logDirectory).list(
      new java.io.FilenameFilter {
        override def accept(dir: java.io.File, name: String) =
            name.startsWith("logfile_") && name.endsWith(".xml")
      })

}
