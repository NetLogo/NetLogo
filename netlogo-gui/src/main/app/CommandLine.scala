// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.window.{ CommandCenterInterface, EditorColorizer, JobWidget, Widget }
import org.nlogo.agent.{ Agent, ArrayAgentSet, OutputObject }
import org.nlogo.core.{ AgentKind, CompilerException, I18N, TokenType, Widget => CoreWidget }
import org.nlogo.editor.EditorField
import org.nlogo.nvm.Workspace
import org.nlogo.window.Events.{ AddJobEvent, CompiledEvent, OutputEvent }

import scala.collection.immutable.List

import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent

object CommandLine {
  val PROMPT = ">"
  val OBSERVER_PROMPT = I18N.gui.get("common.observer") + PROMPT
  val TURTLE_PROMPT = I18N.gui.get("common.turtles") + PROMPT
  val PATCH_PROMPT = I18N.gui.get("common.patches") + PROMPT
  val LINK_PROMPT = I18N.gui.get("common.links") + PROMPT

  val MAX_HISTORY_SIZE = 40

  case class ExecutionString(agentClass: AgentKind, string: String)
}

import CommandLine._

class CommandLine(commandCenter: CommandCenterInterface,
                     echoCommandsToOutput: Boolean,
                     fontSize: Int,
                     workspace: Workspace)
    extends JobWidget(workspace.world.mainRNG)
    with java.awt.event.ActionListener
    with java.awt.event.KeyListener
    with CompiledEvent.Handler {


  type WidgetModel = org.nlogo.core.Widget

  // this is needed for if we're embedded in an agent monitor instead
  // of the command center - ST 7/30/03
  var agent: Agent = null

  /// history handling
  private var historyPosition = -1
  private var historyBase = ""
  private var historyBaseClass: AgentKind = AgentKind.Observer
  private var history: List[ExecutionString] = List()

  val textField: EditorField =
    new org.nlogo.editor.EditorField(30,
      new java.awt.Font(org.nlogo.awt.Fonts.platformMonospacedFont,
        java.awt.Font.PLAIN, 12),
      false, new EditorColorizer(workspace),
      I18N.gui.fn)

  agentKind(AgentKind.Observer)

  textField.setFont(textField.getFont().deriveFont(fontSize.toFloat))
  textField.addKeyListener(this)

  setLayout(new java.awt.BorderLayout())
  displayName(classDisplayName)
  add(new javax.swing.JScrollPane
      (textField,
          javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
          javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),
      java.awt.BorderLayout.CENTER)


  def agent(agent: Agent): Unit = {
    this.agent = agent
  }

  ///

  ///

  // I have no idea why, but at least on Macs, without this our minimum
  // height is larger than our preferred height, which doesn't make sense
  // - ST 8/26/03
  override def getMinimumSize: Dimension =
    new Dimension(super.getMinimumSize.width, getPreferredSize.height)

  ///
  // for Errors tab
  override def classDisplayName: String = "Command Center"

  // so CompilerManager treats us specially - ST 6/9/03
  override def isCommandCenter: Boolean = true

  ///

  private def getText: String = textField.getText()

  private def setText(s: String): Unit = {
    textField.setText(s)
  }

  ///

  override def isFocusable: Boolean = false

  override def requestFocus(): Unit = {
    textField.requestFocus()
  }

  /// keyboard handling for the text field

  def keyReleased(e: KeyEvent): Unit = { }

  def keyTyped(e: KeyEvent): Unit = { }

  def keyPressed(e: KeyEvent): Unit =
    e.getKeyCode match {
      case KeyEvent.VK_ENTER =>
        executeCurrentBuffer()
        e.consume()
      case KeyEvent.VK_TAB =>
        commandCenter.cycleAgentType(!e.isShiftDown())
        e.consume()
      case KeyEvent.VK_DOWN =>
        cycleListForward()
      case KeyEvent.VK_UP =>
        cycleListBack()
      case _ =>
    }

  ///

  def actionPerformed(e: ActionEvent): Unit = {
    executeCurrentBuffer()
  }

  private def executeCurrentBuffer(): Unit = {
    var inner = getText
    if (inner.trim.equals("")) {
      setText("")
      return
    }
    if (workspace.isReporter(inner)) {
      inner = "show " + inner
      setText(inner)
    }
    var header = "to __commandline [] "
    if (kind == AgentKind.Observer) {
      header += "__observercode "
    } else if (kind == AgentKind.Turtle) {
      header += "__turtlecode "
    } else if (kind == AgentKind.Patch) {
      header += "__patchcode "
    } else if (kind == AgentKind.Link) {
      header += "__linkcode "
    }
    val footer = "\n__done end" // the \n is to protect against comments in inner
    source(header, inner, footer)
  }

  override def handle(e: CompiledEvent): Unit = {
    super.handle(e)
    if (e.sourceOwner == this) {
      error(e.error)

      error match {
        case err: CompilerException =>
          val offset = headerSource.length
          // highlight error location

          textField.select(err.start - offset, err.end - offset)
          // print error message
          new OutputEvent(false,
            new OutputObject("", "ERROR: " + err.getMessage(), true, true),
            true, true).raise(this)
        case e if e == null =>
          setText("")
          var outStr = innerSource
          if (!outStr.trim.equals("")) {
            addToHistory(outStr)
            if (echoCommandsToOutput) {
              val prefix = kind match {
                case AgentKind.Turtle => TURTLE_PROMPT
                case AgentKind.Patch  => PATCH_PROMPT
                case AgentKind.Link   => LINK_PROMPT
                case _                => OBSERVER_PROMPT
              }
              outStr = prefix + " " + outStr
              new OutputEvent(false,
                new OutputObject("", outStr, true, false),
                      false, true).raise(this)
            }
            if (agent != null) {
              val agentSet = new ArrayAgentSet(kind, 1, false)
              agentSet.add(agent)
              agents(agentSet)
            }
            new AddJobEvent(this, agents, procedure).raise(this)
          }
        case _ =>
      }
    }
  }

  private def addToHistory(str: String): Unit = {
    val executionString = new ExecutionString(kind, str)
    if (history.isEmpty || executionString != history.head) {
      history = executionString :: history
      while (history.size > MAX_HISTORY_SIZE) {
        history.dropRight(1)
      }
    }
    historyPosition = -1
  }

  protected def cycleListBack(): Unit = {
    if (!history.isEmpty) {
      if (historyPosition == -1) {
        historyBase = getText
        historyBaseClass = kind
      }
      if (historyPosition + 1 < history.size) {
        historyPosition += 1
        val es = history(historyPosition)
        setText(es.string)
        agentKind(es.agentClass)
      }
    }
    commandCenter.repaintPrompt()
  }

  protected def cycleListForward(): Unit = {
    if (historyPosition == 0) {
      setText(historyBase)
      agentKind(historyBaseClass)
      historyPosition = -1
    } else if (historyPosition > 0 && history.nonEmpty) {
      historyPosition -= 1
      val es = history(historyPosition)
      setText(es.string)
      agentKind(es.agentClass)
    }
    commandCenter.repaintPrompt()
  }

  def getExecutionList: Seq[ExecutionString] =
    history.toSeq

  private[app] def reset(): Unit = {
    clearList()
    setText("")
    agentKind(AgentKind.Observer)
  }

  private[app] def clearList(): Unit = {
    history = List[ExecutionString]()
    historyPosition = 0
  }

  private[app] def setExecutionString(es: ExecutionString): Unit = {
    setText(es.string)
    agentKind(es.agentClass)
    textField.setCaretPosition(getText.length)
    commandCenter.repaintPrompt()
  }

  override def setEnabled(enabled: Boolean): Unit = {
    super.setEnabled(enabled)
    textField.setEnabled(enabled)
  }

  override def load(model: CoreWidget): Object = {
    throw new UnsupportedOperationException()
  }

  override def model: CoreWidget = {
    throw new UnsupportedOperationException()
  }
}
