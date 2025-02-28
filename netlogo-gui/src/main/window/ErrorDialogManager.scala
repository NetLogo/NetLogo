// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component
import javax.swing.text.JTextComponent

import org.nlogo.api.{ LogoException, Version }
import org.nlogo.core.I18N
import org.nlogo.nvm.{ Context, Instruction }
import org.nlogo.swing.{ BrowserLauncher, Button, CheckBox, MessageDialog }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.util.{ SysInfo, Utils }

import scala.annotation.tailrec

case class ErrorInfo(var throwable: Throwable, var context: Option[Context] = None, var instruction: Option[Instruction] = None) {
  def ordinaryError: Boolean = throwable.isInstanceOf[LogoException]

  def hasKnownCause: Boolean = knownAncestorCause(throwable)

  def isOutOfMemory: Boolean = knownAncestorCause(throwable)

  def hasContext: Boolean = context.nonEmpty

  def errorMessage: Option[String] =
    context.flatMap(c => instruction.map(i => (c, i))).map {
      case (ctx, ins) => ctx.buildRuntimeErrorMessage(ins, throwable)
    } orElse (if (ordinaryError) Some(throwable.getMessage) else None)

  @tailrec
  private def knownAncestorCause(t: Throwable): Boolean =
    t.isInstanceOf[OutOfMemoryError] || (t.getCause != null && knownAncestorCause(t.getCause))
}

case class DebuggingInfo(var className: String, var threadName: String, var modelName: String, var eventTrace: String, var javaStackTrace: String) {
  def debugInfo =
    s"""|${Version.version}
        |main: $className
        |thread: $threadName
        |${SysInfo.getVMInfoString}
        |${SysInfo.getOSInfoString}
        |${SysInfo.getScalaVersionString}
        |${SysInfo.getJOGLInfoString}
        |${SysInfo.getGLInfoString}
        |model: $modelName""".stripMargin

  def detailedInformation: String = s"""|$javaStackTrace
                                        |$debugInfo
                                        |
                                        |$eventTrace""".stripMargin
}

// For some reason the dialogs have to be instantiated after `tabs.init`. Creating
// them before `tabs.init` somehow prevents the Code tab from receiving
// SwitchedTabsEvent and handling it. This is why `dialogs` is a lazy val and
// `additionalDialogs` is passed by name -- EL 2018-07-27

class ErrorDialogManager(owner: Component, additionalDialogs: => Map[Class[_ <: Throwable], ErrorDialog] = Map())
  extends ThemeSync {

  private val debuggingInfo = DebuggingInfo("", "", "", "", "")
  private val errorInfo = ErrorInfo(null)
  private lazy val dialogs = additionalDialogs.toSeq ++ Seq(
    classOf[LogoException]    -> new LogoExceptionDialog(owner),
    classOf[OutOfMemoryError] -> new OutOfMemoryDialog(owner),
    classOf[Throwable]        -> new UnknownErrorDialog(owner)
  )

  debuggingInfo.className = owner.getClass.getName

  def setModelName(name: String): Unit = {
    debuggingInfo.modelName = name
  }

  def alreadyVisible: Boolean = {
    dialogs.map(_._2).exists(_.isVisible)
  }

  def show(context: Context, instruction: Instruction, thread: Thread, throwable: Throwable): Unit = {
    debuggingInfo.threadName     = thread.getName
    debuggingInfo.eventTrace     = Event.recentEventTrace()
    debuggingInfo.javaStackTrace = Utils.getStackTrace(throwable)
    errorInfo.throwable   = throwable
    errorInfo.context     = Option(context)
    errorInfo.instruction = Option(instruction)
    val dialog = dialogs.collectFirst {
      case (exType, d) if exType.isInstance(throwable) => d
    }.get
    dialog.show(errorInfo, debuggingInfo)
  }

  // This was added to work around https://bugs.openjdk.java.net/browse/JDK-8198809,
  // which appears only in Java 8u162 and should be resolved in 8u172.
  // In general, this method should be used as a safety valve for non-fatal exceptions which
  // are Java's fault (this bug matches that description to a tee, but there are
  // many other bugs of this sort). - RG 3/2/18
  def safeToIgnore(t: Throwable): Boolean = {
    t match {
      case j: java.awt.IllegalComponentStateException =>
        val classAndMethodNames = Seq(
          "java.awt.Component"                                         -> "getLocationOnScreen_NoTreeLock",
          "java.awt.Component"                                         -> "getLocationOnScreen",
          "javax.swing.text.JTextComponent$InputMethodRequestsHandler" -> "getTextLocation",
          "sun.awt.im.InputMethodContext"                              -> "getTextLocation",
          "sun.awt.windows.WInputMethod$1"                             -> "run")
        val stackTraceClassAndMethodNames =
          j.getStackTrace.take(5).map(ste => ste.getClassName -> ste.getMethodName).toSeq
        classAndMethodNames == stackTraceClassAndMethodNames
      case _ => false
    }
  }

  def closeAllDialogs() {
    dialogs.foreach(_._2.setVisible(false))
  }

  override def syncTheme(): Unit = {
    dialogs.foreach(_._2.syncTheme())
  }
}

class CopyButton(textComp: JTextComponent) extends Button(I18N.gui.get("menu.edit.copy"), () => {
  textComp.select(0, textComp.getText.length)
  textComp.copy()
  textComp.setCaretPosition(0)
})

abstract class ErrorDialog(owner: Component, dialogTitle: String)
extends MessageDialog(owner, I18N.gui.get("common.buttons.dismiss")) {
  protected var message = ""
  protected var details = ""

  def show(errorInfo: ErrorInfo, debugInfo: DebuggingInfo)

  protected def doShow(showDetails: Boolean): Unit = {
    val text = if (showDetails) message + "\n\n" + details else message
    val lines = text.split('\n')
    val maxColumns = lines.maxBy(_.length).length

    val padding = 2
    val rows = lines.length.max(5).min(15)
    val columns = (maxColumns + padding).min(70)
    doShow(dialogTitle, text, rows, columns)
  }
}

class UnknownErrorDialog(owner: Component)
extends ErrorDialog(owner, I18N.gui.get("error.dialog.unknown")) {
  private var suppressed = false

  message = I18N.gui.get("error.dialog.pleaseReport")

  override def show(errorInfo: ErrorInfo, debugInfo: DebuggingInfo): Unit = if (!suppressed) {
    details = debugInfo.detailedInformation
    doShow(true)
  }

  override def makeButtons = {
    val suppressButton = new Button(I18N.gui.get("error.dialog.suppress"), () => {
      suppressed = true
      setVisible(false)
    })
    super.makeButtons ++ Seq(new CopyButton(textArea), suppressButton)
  }
}

class LogoExceptionDialog(owner: Component)
extends ErrorDialog(owner, I18N.gui.get("error.dialog.logo")) {
  private lazy val checkbox = new CheckBox(I18N.gui.get("error.dialog.showInternals"), (selected) => {
    doShow(selected)
  })

  override def show(errorInfo: ErrorInfo, debugInfo: DebuggingInfo): Unit = {
    message = errorInfo.errorMessage.getOrElse("")
    details = debugInfo.detailedInformation
    doShow(checkbox.isSelected)
  }

  override def makeButtons = super.makeButtons ++ Seq(new CopyButton(textArea), checkbox)

  override def syncTheme(): Unit = {
    super.syncTheme()

    checkbox.setForeground(InterfaceColors.dialogText)
  }
}

class OutOfMemoryDialog(owner: Component)
extends ErrorDialog(owner, I18N.gui.get("error.dialog.outOfMemory.title")) {
  message = I18N.gui.get("error.dialog.outOfMemory")

  override def show(errorInfo: ErrorInfo, debugInfo: DebuggingInfo): Unit = {
    doShow(false)
  }

  override def makeButtons = {
    super.makeButtons :+ new Button(I18N.gui.get("error.dialog.openFAQ"), () => {
      BrowserLauncher.openPath(owner, BrowserLauncher.docPath("faq.html"), "howbig")
    })
  }
}
