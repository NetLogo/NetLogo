// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

// A note on log4j: Reuven, who was one of the main clients for this logging stuff, requested log4j.
// We did not investigate log4j vs. java.util.logging but just went along with Reuven's
// suggestion. - ST 2/25/08

import org.apache.log4j.{ Appender, FileAppender, Logger => JLogger, LogManager => Log4JManager }
import org.apache.log4j.xml.DOMConfigurator
import org.nlogo.api.Version
import java.util.{ Enumeration => JEnumeration, List => JList, ArrayList }
import collection.JavaConverters.enumerationAsScalaIteratorConverter

object Logger {

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
  def logAddWidget(tyype: String, name: String) {
    widgetMsg.updateWidgetMessage(tyype.toLowerCase, name, "added")
    Widgets.info(widgetMsg)
  }
  def logWidgetRemoved(tyype: String, name: String) {
    widgetMsg.updateWidgetMessage(tyype.toLowerCase, name, "removed")
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
  def logCustomMessage(msg: String, nameValuePairs: (String, String)*) {
    customMsg.updateCustomMessage(msg, nameValuePairs)
    CustomMessages.info(customMsg)
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
  
  implicit def javaEnum2Iterator[T](e: JEnumeration[T]) = Option(e) map (_.asScala) getOrElse (Iterator())

  def configure(reader: java.io.Reader) {
    val configurator = new DOMConfigurator
    configurator.doConfigure(reader, Log4JManager.getLoggerRepository)
  }

  def changeLogDirectory(path: String) {
    import java.io.File
    val directory = new File(path)
    if (!directory.isAbsolute) {
      val newPath = System.getProperty("user.home") + File.separatorChar + "dummy.txt"
      val urlForm = new java.net.URL(
        org.nlogo.util.JUtils.toURL(new File(newPath)),
        path)
      logDirectory = new File(urlForm.getFile).getAbsolutePath
    }
    else if (directory.isDirectory) {
      logDirectory = path
    }
  }

  def addAppender(appender: Appender) {
    JLogger.getRootLogger.addAppender(appender)
  }

  var filenames: JList[String] = null // for TestLogger

  def modelOpened(name: String) {
    filenames = new java.util.ArrayList[String]
    filenames.addAll(newFiles(JLogger.getRootLogger.getAllAppenders, name))
    val loggers = JLogger.getRootLogger.getLoggerRepository.getCurrentLoggers
    loggers foreach { case l: JLogger => filenames.addAll(newFiles(l.getAllAppenders, name)) }
  }

  def newFiles(e: JEnumeration[_], name: String): JList[String] = {
    val filenames = new ArrayList[String]
    e foreach {
      case xappender: XMLAppender =>
        setupXMLAppender(name, xappender)
        xappender match {
          case appender: FileAppender =>
            val filename = logFileName(appender.getName)
            filenames.add(filename)
            appender.setFile(filename)
            appender.activateOptions()
          case wsAppender: WebStartAppender =>
            wsAppender.initialize()
          case _                      => // Otherwise, ignore
        }
    }
    filenames
  }

  private def setupXMLAppender(name: String, xappender: XMLAppender) {
    xappender.setStudentName(studentName)
    xappender.setUsername(System.getProperty("user.name"))
    xappender.setIPAddress(getIPAddress)
    xappender.setModelName(Option(name).getOrElse("new model"))
    xappender.setVersion(Version.version)
  }

  def close() {
    closeAppenders(JLogger.getRootLogger.getAllAppenders)  // Is this code redundant...?  What's going on?
    val loggers = JLogger.getRootLogger.getLoggerRepository.getCurrentLoggers
    loggers foreach { case l: JLogger => closeAppenders(l.getAllAppenders) }
  }

  private def closeAppenders(appenders: TraversableOnce[_]) {
    appenders foreach { case a: Appender => a.close() }
  }

  def getIPAddress =
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

  def requestRemoteLogDeletion() {
    JLogger.getRootLogger.getAllAppenders foreach {
      case wsa: WebStartAppender => wsa.deleteLog()
      case _                     => // Ignore appenders that aren't remote
    }
  }

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
