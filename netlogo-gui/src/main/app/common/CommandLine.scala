// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.{ BorderLayout, Dimension, Font }
import java.awt.event.{ ActionEvent, ActionListener, KeyEvent, KeyListener }
import javax.swing.{ KeyStroke, ScrollPaneConstants }

import org.nlogo.agent.{ Agent, AgentSet, OutputObject }
import org.nlogo.core.{ AgentKind, CompilerException, I18N, Widget => CoreWidget }
import org.nlogo.editor.{ EditorArea, EditorConfiguration }
import org.nlogo.ide.{ AutoSuggestAction, CodeCompletionPopup }
import org.nlogo.swing.{ Implicits, ScrollPane, Transparent }, Implicits.thunk2documentListener
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.{ Editable, CommandCenterInterface, EditorColorizer, InterfaceMode, JobWidget,
                          Events => WindowEvents }
import org.nlogo.workspace.AbstractWorkspace

object CommandLine {
  val PROMPT = ">"
  val OBSERVER_PROMPT = I18N.gui.get("common.observer") + PROMPT
  val TURTLE_PROMPT = I18N.gui.get("common.turtles") + PROMPT
  val PATCH_PROMPT = I18N.gui.get("common.patches") + PROMPT
  val LINK_PROMPT = I18N.gui.get("common.links") + PROMPT

  val MAX_HISTORY_SIZE = 40

  case class ExecutionString(agentClass: AgentKind, string: String)
}

class CommandLine(commandCenter: CommandCenterInterface,
                     echoCommandsToOutput: Boolean,
                     fontSize: Int,
                     workspace: AbstractWorkspace)
    extends JobWidget(workspace.world.mainRNG)
    with ActionListener
    with KeyListener
    with WindowEvents.CompiledEvent.Handler {
  import CommandLine._

  // this is needed for if we're embedded in an agent monitor instead
  // of the command center - ST 7/30/03
  var agent: Agent = null

  /// history handling
  private var historyPosition = -1
  private var historyBase = ""
  private var historyBaseClass: AgentKind = AgentKind.Observer
  private var history = Seq[ExecutionString]()

  lazy val codeCompletionPopup = CodeCompletionPopup(workspace.dialect, workspace.getExtensionManager)
  lazy val actionMap = Map(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, java.awt.event.InputEvent.CTRL_DOWN_MASK)
                             -> new AutoSuggestAction("auto-suggest", codeCompletionPopup))

  lazy val textField = new EditorArea(EditorConfiguration.default(1, 30, workspace, new EditorColorizer(workspace))
                                                         .withFont(EditorConfiguration.getCodeFont)
                                                         .withFocusTraversalEnabled(true)
                                                         .withKeymap(actionMap)) {
    getDocument.addDocumentListener(() => commandCenter.fitPrompt())

    override def setText(text: String): Unit = {
      super.setText(text)

      resetUndoHistory()
    }

    override def getPreferredScrollableViewportSize: Dimension =
      new Dimension(super.getPreferredScrollableViewportSize.width, getRowHeight * (this.getText.count(_ == '\n') + 1))

    override def getPreferredSize: Dimension = {
      new Dimension(super.getPreferredSize.width, getRowHeight * (this.getText.count(_ == '\n') + 1))
    }
  }

  agentKind(AgentKind.Observer)

  textField.addKeyListener(this)

  setLayout(new BorderLayout)
  displayName(classDisplayName)

  private val scrollPane = new ScrollPane(textField, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) with Transparent

  add(scrollPane, BorderLayout.CENTER)

  def agent(agent: Agent): Unit = {
    this.agent = agent
  }

  override def getEditable: Option[Editable] = None

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

  override def setFont(font: Font): Unit = {
    textField.setFont(font.deriveFont(fontSize.toFloat))
  }

  ///

  override def isFocusable: Boolean = false

  override def requestFocus(): Unit = {
    textField.requestFocus()
  }

  /// keyboard handling for the text field

  def keyReleased(e: KeyEvent): Unit = { }

  def keyTyped(e: KeyEvent): Unit = { }

  def keyPressed(e: KeyEvent): Unit = {
    new WindowEvents.SetInterfaceModeEvent(InterfaceMode.Interact, false).raise(CommandLine.this)

    e.getKeyCode match {
      case KeyEvent.VK_ENTER if e.isShiftDown =>
        textField.replaceSelection("\n")
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

  override def handle(e: WindowEvents.CompiledEvent): Unit = {
    super.handle(e)
    if (e.sourceOwner == this) {
      error(e.error)

      error() match {
        case Some(err: CompilerException) =>
          val offset = headerSource.length
          // highlight error location

          textField.selectError(err.start - offset, err.end - offset)
          // print error message
          new WindowEvents.OutputEvent(false,
            new OutputObject("", "ERROR: " + err.getMessage(), true, true),
            true, true, System.currentTimeMillis).raise(this)
        case None =>
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
              new WindowEvents.OutputEvent(false,
                new OutputObject("", outStr, true, false),
                      false, true, System.currentTimeMillis).raise(this)
            }
            if (agent != null) {
              val agentSet = AgentSet.fromAgent(agent)
              agents(agentSet)
            }
            new WindowEvents.AddJobEvent(this, agents, procedure).raise(this)
          }
        case _ =>
      }
    }
  }

  private def addToHistory(str: String): Unit = {
    val executionString = new ExecutionString(kind, str)
    if (history.isEmpty || executionString != history.head) {
      history = executionString +: history
      while (history.size > MAX_HISTORY_SIZE) {
        history = history.dropRight(1)
      }
    }
    historyPosition = -1
    saveHistory()
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
    history

  private[app] def reset(): Unit = {
    loadHistory()
    setText("")
    agentKind(AgentKind.Observer)
    textField.resetUndoHistory()
  }

  private[app] def clearList(): Unit = {
    history = Seq()
    historyPosition = 0
    saveHistory()
  }

  private[app] def setExecutionString(es: ExecutionString): Unit = {
    setText(es.string)
    agentKind(es.agentClass)
    textField.setCaretPosition(getText.length)
    commandCenter.repaintPrompt()
  }

  private def loadHistory(): Unit = {
    history = Option(workspace.getModelPath).map(ModelConfig.getCommandHistory).getOrElse(Seq())
    historyPosition = -1
  }

  private def saveHistory(): Unit = {
    Option(workspace.getModelPath).foreach(ModelConfig.updateCommandHistory(_, history))
  }

  override def setEnabled(enabled: Boolean): Unit = {
    super.setEnabled(enabled)
    textField.setEnabled(enabled)
  }

  override def load(model: CoreWidget): Unit = {
    throw new UnsupportedOperationException()
  }

  override def model: CoreWidget = {
    throw new UnsupportedOperationException()
  }

  // this isn't an actual widget, so make sure we don't cause any widget-related code to run. -Jeremy B November 2020
  override def raiseWidgetRemoved(): Unit = {}
  override def raiseWidgetAdded(): Unit = {}

  override def syncTheme(): Unit = {
    textField.setBackground(InterfaceColors.codeBackground())
    textField.setCaretColor(InterfaceColors.displayAreaText())

    scrollPane.setBackground(InterfaceColors.codeBackground())

    codeCompletionPopup.syncTheme()
  }
}
