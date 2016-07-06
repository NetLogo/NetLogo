// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component
import java.awt.event.{ ActionEvent, ActionListener, ItemEvent, ItemListener }
import java.util.{ ArrayList => JArrayList }
import java.util.{ List => JList }
import java.lang.OutOfMemoryError

import javax.swing.{ JButton, JCheckBox, JComponent }

import org.nlogo.core.I18N
import org.nlogo.api.{ LogoException, Version }
import org.nlogo.nvm.{ Context, Instruction }
import org.nlogo.workspace.AbstractWorkspace
import org.nlogo.swing.{ BrowserLauncher, MessageDialog }
import org.nlogo.util.Utils
import org.nlogo.util.SysInfo

import scala.annotation.tailrec
import scala.collection.JavaConverters._

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

object RuntimeErrorDialog {
  val PleaseReportText = I18N.gui.get("error.dialog.pleaseReport")

  private var debuggingInfo = DebuggingInfo("", "", "", "", "")
  private var errorInfo = ErrorInfo(null)
  private var unknownDialog: Option[UnknownErrorDialog]  = Option.empty[UnknownErrorDialog]
  private var logoDialog:    Option[LogoExceptionDialog] = Option.empty[LogoExceptionDialog]
  private var memoryDialog:  Option[OutOfMemoryDialog]   = Option.empty[OutOfMemoryDialog]

  def init(owner: Component): Unit = {
    debuggingInfo.className = owner.getClass.getName

    unknownDialog = Some(new UnknownErrorDialog(owner))
    logoDialog    = Some(new LogoExceptionDialog(owner))
    memoryDialog  = Some(new OutOfMemoryDialog(owner))
  }

  def setModelName(name: String): Unit = {
    debuggingInfo.modelName = name
  }

  def deactivate(): Unit = {
    unknownDialog = None
    logoDialog    = None
    memoryDialog  = None
    debuggingInfo = DebuggingInfo("", "", "", "", "")
    errorInfo = ErrorInfo(null)
  }

  def alreadyVisible: Boolean = {
    Seq(unknownDialog, logoDialog, memoryDialog).flatten.exists(_.isVisible)
  }

  def show(context: Context, instruction: Instruction, thread: Thread, throwable: Throwable): Unit = {
      debuggingInfo.threadName     = thread.getName
      debuggingInfo.eventTrace     = Event.recentEventTrace()
      debuggingInfo.javaStackTrace = Utils.getStackTrace(throwable)
      errorInfo.throwable   = throwable
      errorInfo.context     = Option(context)
      errorInfo.instruction = Option(instruction)
      throwable match {
        case l: LogoException             => logoDialog.foreach(_.doShow(errorInfo, debuggingInfo))
        case _ if errorInfo.isOutOfMemory => memoryDialog.foreach(_.doShow())
        case _                            => unknownDialog.foreach(_.doShow("Internal Error", errorInfo, debuggingInfo))
      }
  }

  def suppressJavaExceptionDialogs: Boolean = {
    unknownDialog.map(_.suppressJavaExceptionDialogs).getOrElse(false)
  }
}

import RuntimeErrorDialog._

trait CopyButton {
  def copy(): Unit

  lazy val copyButton: JButton = new JButton(I18N.gui.get("menu.edit.copy"))
  copyButton.addActionListener(new ActionListener() {
    def actionPerformed(e: ActionEvent): Unit = { copy() }
  })
}

trait RuntimeErrorDialog {
  protected var textWithDetails: String    = ""
  protected var textWithoutDetails: String = ""

  protected lazy val checkbox = {
    val b = new JCheckBox(I18N.gui.get("error.dialog.showInternals"))
    b.addItemListener(new ItemListener() {
      def itemStateChanged(e: ItemEvent): Unit = {
        showJavaDetails(b.isSelected)
      }
    })
    b
  }

  protected def showJavaDetails(showDetails: Boolean): Unit = {
    var lines = 1
    var lineBegin = 0
    var longestLine = 0
    var text =
        if (showDetails) textWithDetails else textWithoutDetails

    var i = 0
    while (i < text.length) {
      text.charAt(i) match {
        case '\n' | '\r' =>
          lines += 1
          longestLine = longestLine max (i - lineBegin)
          lineBegin = i
        case _ =>
      }
      i += 1
    }

    longestLine += 2 // pad
    lines = lines.max(5).min(15)
    longestLine = longestLine.min(70)

    showText(text, lines, longestLine)
  }

  protected def showText(text: String, rows: Int, columns: Int): Unit
}

class UnknownErrorDialog(owner: Component) extends MessageDialog(owner) with RuntimeErrorDialog with CopyButton {
  private lazy val suppressButton   = new JButton(I18N.gui.get("error.dialog.suppress"))

  private var dialogTitle: String = ""
  private var errorHeader: String = ""

  var suppressJavaExceptionDialogs: Boolean = false

  def doShow(showTitle: String, errorInfo: ErrorInfo, debuggingInfo: DebuggingInfo): Unit = {
    dialogTitle = showTitle
    // we don't need bug reports on known issues like OutOfMemoryError - ST 4/29/10
    errorHeader =
      if (! (errorInfo.ordinaryError || errorInfo.hasKnownCause)) PleaseReportText
      else ""
    // only has context if the exception occured inside running Logo code (and not, for example, in the GUI)
    suppressButton.setVisible(! (errorInfo.ordinaryError || errorInfo.hasContext))
    buildTexts(errorInfo, debuggingInfo)
    checkbox.setVisible(errorInfo.ordinaryError)
    showJavaDetails(! errorInfo.ordinaryError || checkbox.isSelected)
  }

  override def copy(): Unit = {
    val beginIndex = textArea.getText.indexOf(errorHeader) + errorHeader.length
    textArea.select(beginIndex, textArea.getText.length)
    textArea.copy()
    textArea.setCaretPosition(0)
  }

  override protected def makeButtons(): JList[JComponent] = {
    val buttons: JList[JComponent] = new JArrayList[JComponent]()
    buttons.addAll(super.makeButtons())
    buttons.add(copyButton)
    buttons.add(checkbox)
    suppressButton.addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent): Unit = {
        suppressJavaExceptionDialogs = true
        setVisible(false)
      }
    })
    buttons.add(suppressButton)
    buttons
  }

  private def buildTexts(errorInfo: ErrorInfo, debuggingInfo: DebuggingInfo): Unit = {
    val detailedInformation =
      s"$errorHeader\n${debuggingInfo.detailedInformation}"
    textWithoutDetails = errorInfo.errorMessage.getOrElse("")
    textWithDetails = errorInfo.errorMessage
      .map(_ + "\n\n" + detailedInformation)
      .getOrElse(detailedInformation)
  }

  override protected def showText(text: String, rows: Int, columns: Int): Unit =
    doShow(dialogTitle, text, rows, columns)
}

class LogoExceptionDialog(owner: Component) extends MessageDialog(owner) with RuntimeErrorDialog with CopyButton {
  private val dialogTitle: String = "Runtime Error"

  private var errorMessage: Option[String] = None

  def doShow(errorInfo: ErrorInfo, debuggingInfo: DebuggingInfo): Unit = {
    buildTexts(errorInfo, debuggingInfo)
    showJavaDetails(checkbox.isSelected)
  }

  override def copy(): Unit = {
    textArea.select(0, textArea.getText.length)
    textArea.copy()
    textArea.setCaretPosition(0)
  }

  override protected def makeButtons(): JList[JComponent] = {
    val buttons: JList[JComponent] = new JArrayList[JComponent]()
    buttons.addAll(super.makeButtons())
    buttons.add(copyButton)
    buttons.add(checkbox)
    buttons
  }

  private def buildTexts(errorInfo: ErrorInfo, debuggingInfo: DebuggingInfo): Unit = {
    textWithoutDetails = errorInfo.errorMessage.getOrElse("")
    textWithDetails    = errorInfo.errorMessage
      .map(_ + "\n\n" + debuggingInfo.detailedInformation)
      .getOrElse(debuggingInfo.detailedInformation)
  }

  override protected def showText(text: String, rows: Int, columns: Int): Unit =
    doShow(dialogTitle, text, rows, columns)
}

class OutOfMemoryDialog(owner: Component) extends MessageDialog(owner) with RuntimeErrorDialog {
  private val dialogTitle: String = "Model Too Large"
  private val ErrorText = I18N.gui.get("error.dialog.outOfMemory")

  override protected def makeButtons(): JList[JComponent] = {
    val buttons: JList[JComponent] = new JArrayList[JComponent]()
    buttons.addAll(super.makeButtons())
    val openFAQ = new JButton(I18N.gui.get("error.dialog.openFAQ"))
    val baseFaqUrl: String = {
      val docRoot = System.getProperty("netlogo.docs.dir", "docs")
      docRoot + "/faq.html"
    }
    openFAQ.addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent): Unit = {
        BrowserLauncher.openURL(owner, baseFaqUrl, "#howbig", true)
      }
    })
    buttons.add(openFAQ)
    buttons
  }

  def doShow(): Unit = {
    textWithDetails = ErrorText
    showJavaDetails(true)
  }

  override protected def showText(text: String, rows: Int, columns: Int): Unit =
    doShow(dialogTitle, text, rows, columns)
}
