// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, FlowLayout, Font, GridBagConstraints, GridBagLayout, Insets, Rectangle }
import java.awt.event.{ FocusEvent, FocusListener, KeyEvent, KeyListener }
import javax.swing.{ JLabel, JPanel, ScrollPaneConstants }

import org.nlogo.agent.{ Agent, AgentSet, Turtle, Patch, Link }
import org.nlogo.api.{ AgentVariables, Dump }
import org.nlogo.awt.Fonts
import org.nlogo.core.{ AgentKind, I18N, Nobody, Widget => CoreWidget }
import org.nlogo.editor.EditorField
import org.nlogo.nvm.Procedure
import org.nlogo.swing.{ OptionPane, ScrollPane, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ Editable, EditorColorizer, Events => WindowEvents, JobWidget }

class AgentMonitorEditor(parent: AgentMonitor) extends JPanel with ThemeSync {
  private val noVarLabel = new JLabel(I18N.gui.get("tools.agentMonitor.editor.noVariables"))
  private var editors: List[AgentVarEditor] = Nil

  reset()

  override def requestFocus(): Unit = {
    editors.headOption.foreach(_.requestFocus())
  }

  def refresh(): Unit = {
    editors.foreach(_.refresh(false))
  }

  def reset(): Unit = {
    removeAll()
    editors = Nil
    if(vars == null || vars.isEmpty) {
      setLayout(new FlowLayout)
      add(noVarLabel)
    }
    else fill()
    syncTheme()
  }

  private def fill(): Unit = {
    import scala.jdk.CollectionConverters.ListHasAsScala

    setLayout(new GridBagLayout)
    val labelConstraints = new GridBagConstraints
    labelConstraints.gridx = 0
    labelConstraints.anchor = GridBagConstraints.EAST
    labelConstraints.insets = new Insets(0, 3, 0, 3)
    val editorConstraints = new GridBagConstraints
    editorConstraints.gridx = 1
    editorConstraints.fill = GridBagConstraints.HORIZONTAL
    editorConstraints.weightx = 1
    editorConstraints.insets = new Insets(3, 0, 0, 3)
    // add components
    editors = for (variableName <- vars.asScala.toList) yield {
      val label = new JLabel(variableName.toLowerCase)
      val index =
        if (agent == null)
          workspace.world.indexOfVariable(agentKind, variableName)
        else
          workspace.world.indexOfVariable(agent, variableName)
      val editor = new AgentVarEditor(this, index, variableName, label)
      editor.agentKind(agentKind)
      add(label, labelConstraints)
      add(editor, editorConstraints)
      editor
    }
    val fillerConstraints = new GridBagConstraints
    fillerConstraints.gridx = 0
    fillerConstraints.gridwidth = 2
    fillerConstraints.fill = GridBagConstraints.BOTH
    fillerConstraints.weighty = 1
    val fillerPanel = new JPanel with Transparent
    add(fillerPanel, fillerConstraints)
    revalidate()
  }

  def vars = parent.vars
  def agent = parent.agent
  def agentKind = parent.agentKind
  def setAgent(agent: Agent): Unit = { parent.setAgent(agent, 3) }
  def workspace = parent.workspace

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.dialogBackground())

    noVarLabel.setForeground(InterfaceColors.dialogText())

    editors.foreach(_.syncTheme())
  }
}

// this gets complicated... if there's any way to make it less complicated I'd love to know about it
// - ST 8/17/03, 8/19/03

class AgentVarEditor(parent: AgentMonitorEditor,
                     index: Int,
                     variableName: String,
                     label: JLabel)
extends JobWidget(parent.workspace.world.auxRNG)
with KeyListener
with FocusListener
with WindowEvents.JobRemovedEvent.Handler
with ThemeSync {
  private def specialCase = {
    parent.agentKind match {
      case AgentKind.Turtle if AgentVariables.isSpecialTurtleVariable(index) =>
        TURTLE_WHO
      case AgentKind.Patch if AgentVariables.isSpecialPatchVariable(index, workspace.world.program.dialect.is3D) =>
        PXCOR_OR_PYCOR
      case AgentKind.Link if AgentVariables.isSpecialLinkVariable(index) =>
        LINK_WHO
      case _ =>
        NORMAL
    }
  }

  // this is necessary because some strings that can be displayed
  // in the editor can't be read back in, e.g. "(agentset: 4 turtles)",
  // so we have to be careful not to try to evaluate anything that
  // the user didn't actually type themselves - ST 8/19/03
  private var lastTextBeforeUserChangedAnything = ""

  private def agent = parent.agent
  private def workspace = parent.workspace

  // some variables are special because editing them doesn't set the variable, rather, it points the
  // editor at a different agent
  private abstract sealed class SpecialCase
  private case object NORMAL extends SpecialCase
  private case object TURTLE_WHO extends SpecialCase
  private case object PXCOR_OR_PYCOR extends SpecialCase
  private case object LINK_WHO extends SpecialCase

  var editorFocus = false

  setLayout(new BorderLayout)

  private val editor = new EditorField(17, new Font(Fonts.platformMonospacedFont, Font.PLAIN, 12), true,
                                       new EditorColorizer(workspace))
  editor.setFont(editor.getFont.deriveFont(10f))
  private val scrollPane = new ScrollPane(editor, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
  add(scrollPane, BorderLayout.CENTER)
  editor.addKeyListener(this)
  editor.addFocusListener(this)
  refresh(true)

  /// JobWidget stuff

  override def getEditable: Option[Editable] = None

  // we'll make an AgentSet ourselves, don't use the standard O/T/P sets - ST 11/5/03
  override def useAgentClass = false

  displayName = {
    parent.agentKind match {
      case AgentKind.Observer => "Globals Monitor"
      case AgentKind.Turtle   => "Turtle Monitor"
      case AgentKind.Patch    => "Patch Monitor"
      case AgentKind.Link     => "Link Monitor"
    }
  }

  // gets us special treatment in errors tab, CompilerManager, etc. - ST 8/17/03
  override def isCommandCenter = true

  // this is how we're notified when we've been recompiled
  override def procedure_=(procedure: Procedure): Unit = {
    super.procedure_=(procedure)
    if (procedure != null)
      new WindowEvents.AddJobEvent(this, agents, procedure).raise(this)
  }

  // our job has finished, so we can fetch a new value for our variable
  def handle(e: WindowEvents.JobRemovedEvent): Unit =
  {
    if(e.owner eq this)
      refresh(true)
  }

  override def handle(e: WindowEvents.CompiledEvent): Unit = {
    super.handle(e)
    if(e.sourceOwner == this) {
      error(e.error)
      error().foreach { e =>
        new OptionPane(workspace.getFrame, I18N.gui.get("common.messages.error"), e.getMessage,
                       OptionPane.Options.Ok, OptionPane.Icons.Error)
        setEnabled(true)
        editor.setText(get)
        lastTextBeforeUserChangedAnything = editor.getText()
        editor.selectAll()
      }
    }
  }

  // put together a full procedure definition. calling source at the end causes JobWidget and
  // CompileMoreSourceEvent stuff to call the compiler and add a job if compilation succeeds
  def wrapSource(innerSource: String): Unit = {
    setEnabled(false)
    this.innerSource = innerSource
    var header = "to __agentvareditor [] "
    if(parent.agentKind == AgentKind.Turtle)
      header += " __turtlecode "
    else if(parent.agentKind == AgentKind.Patch)
      header += " __patchcode "
    else if(parent.agentKind == AgentKind.Link)
      header += "__linkcode "
    header += "set " + variableName + " "
    val footer = "__done end"
    val agentset =
      if(agent == null || agent.id == -1)
        // this is so you can still enter expressions into the who field of dead/empty turtle
        // monitors and the pxcor/pycor fields of empty patch monitors - ST 8/17/03
        workspace.world.observers
      else {
        AgentSet.fromAgent(agent)
      }
    agents(agentset)
    source(header, innerSource, "\n" + footer)  // the \n protects against comments
  }

  ///

  def keyReleased(e: KeyEvent): Unit = { }
  def keyTyped(e: KeyEvent): Unit = { }
  def keyPressed(e: KeyEvent): Unit = {
    e.getKeyCode match {
      case KeyEvent.VK_ENTER =>
        action()
      case KeyEvent.VK_ESCAPE =>
        editor.setText(get)
        editor.selectAll()
      case KeyEvent.VK_TAB =>
        // I really don't understand why EditorField doesn't do the right thing for us instead of
        // needing this, but oh well... - ST 8/17/03
        if(e.isShiftDown)
          editor.transferFocusBackward()
        else
          editor.transferFocus()
        e.consume()
      case _ => // do nothing
    }
  }

  def focusGained(e: FocusEvent): Unit = {
    editorFocus = true
    lastTextBeforeUserChangedAnything = editor.getText()
  }

  def focusLost(e: FocusEvent): Unit = {
    editorFocus = false
    if(editor.getText() != lastTextBeforeUserChangedAnything)
      action()
  }

  override def requestFocus(): Unit = {
    editor.requestFocus()
  }

  ///

  // without this, the scrollpane in AgentMonitor scrolls every time a variable changes for any
  // reason, which is very annoying - ST 8/17/03
  override def scrollRectToVisible(rect: Rectangle): Unit = {
    if(editorFocus)
      super.scrollRectToVisible(rect)
  }

  ///

  // The "force" flag is so if a new value comes in at periodic update time, we don't interrupt the
  // user in the middle of typing something.  true means go ahead and interrupt the user if
  // necessary, false means refrain. - ST 8/17/03
  def refresh(force: Boolean): Unit = {
    if(force || isEnabled) {
      setEnabled((agent != null && agent.id != -1 && agent.getVariable(index) != null) ||
                  specialCase == TURTLE_WHO ||
                  specialCase == PXCOR_OR_PYCOR ||
                  specialCase == LINK_WHO)
      if((force || !editorFocus) && agent != null && agent.id != -1) {
        val newString = get
        if(editor.getText() != newString) {
          editor.setText(newString)
          lastTextBeforeUserChangedAnything = newString
        }
        if(force && editorFocus)
          editor.selectAll()
      }
    }
  }

  override def setEnabled(enabled: Boolean): Unit = {
    if(enabled != isEnabled) {
      super.setEnabled(enabled)
      if((enabled || agent == null || agent.id == -1 || agent.getVariable(index) == null) &&
          editor.isEnabled != enabled)
        editor.setEnabled(enabled)
      syncTheme()
      label.repaint()
    }
  }

  private def get =
    if(agent == null || agent.id == -1  || agent.getVariable(index) == null)
      ""
    else workspace.world.synchronized{
      Dump.logoObject(agent.getVariable(index), true, false)
    }

  private def action(): Unit = {
    if(isEnabled)
      if(editor.getText().trim.isEmpty) {
        editor.setText(get)
        lastTextBeforeUserChangedAnything = editor.getText()
        editor.selectAll()
      }
      else specialCase match {
        case TURTLE_WHO | PXCOR_OR_PYCOR | LINK_WHO =>
          doAgentSwitch()
        case NORMAL =>
          wrapSource(editor.getText())
      }
  }

  private def doAgentSwitch(): Unit = {
    val newAgent = try {
      workspace.world.synchronized {
        specialCase match {
          case NORMAL => null
          case TURTLE_WHO =>
            workspace.world.getTurtle(editor.getText().toInt)
          case PXCOR_OR_PYCOR =>
            val (pxcor, pycor) =
              index match {
                case Patch.VAR_PXCOR =>
                  (editor.getText().toInt,
                   if (agent == null) 0 else agent.asInstanceOf[Patch].pycor)
                case Patch.VAR_PYCOR =>
                  (if (agent == null) 0 else agent.asInstanceOf[Patch].pxcor,
                   editor.getText().toInt)
                case _ =>
                  throw new IllegalStateException
              }
            workspace.world.getPatchAt(pxcor, pycor)
          case LINK_WHO =>
            val (end1, end2, breed) = agent match {
              case l: Link => (l.end1, l.end2, l.getBreed)
              case _ => (Nobody, Nobody, workspace.world.links)
            }
            index match {
              case Link.VAR_END1 =>
                workspace.world.getOrCreateDummyLink(
                  parseTurtleOrDouble(editor.getText()), end2, breed)
              case Link.VAR_END2 =>
                workspace.world.getOrCreateDummyLink(
                  end1, parseTurtleOrDouble(editor.getText()), breed)
              case Link.VAR_BREED =>
                val newBreed = parseAgentSet(editor.getText())
                val result = workspace.world.getOrCreateDummyLink(end1, end2, newBreed)
                if(result.isInstanceOf[Link])
                  result
                else {
                  agent.setLinkVariable(index, breed)
                  agent
                }
              case _ =>
                throw new IllegalStateException
            }
        }
      }
    }
    catch {
      case ex: NumberFormatException =>
        editor.setText(get)
        lastTextBeforeUserChangedAnything = editor.getText()
        editor.selectAll()
        return
      case e: org.nlogo.api.AgentException =>
        editor.setText(get)
        lastTextBeforeUserChangedAnything = editor.getText()
        editor.selectAll
        return
      case e: org.nlogo.core.CompilerException =>
        editor.setText(get)
        lastTextBeforeUserChangedAnything = editor.getText()
        editor.selectAll()
        return
    }
    if(newAgent != null)
      parent.setAgent(newAgent)
    editor.setText(get)
    lastTextBeforeUserChangedAnything = editor.getText()
    editor.selectAll()
  }

  @throws(classOf[org.nlogo.core.CompilerException])
  @throws(classOf[org.nlogo.api.AgentException])
  private def parseTurtleOrDouble(text: String): Turtle = {
    val obj = workspace.compiler.readFromString(
      text, workspace.world, workspace.getExtensionManager)
    obj match {
      case t: Turtle =>
        t
      case d: java.lang.Double =>
        workspace.world.getTurtle(d.longValue)
      case _ =>
        throw new org.nlogo.api.AgentException(I18N.gui.get("tools.agentMonitor.editor.expectedTurtleOrWho"))
    }
  }

  @throws(classOf[org.nlogo.api.AgentException])
  private def parseAgentSet(text: String): AgentSet =
    if(text.equalsIgnoreCase("LINKS"))
      workspace.world.links
    else {
      val breed = workspace.world.getLinkBreed(text.toUpperCase)
      if(breed == null)
        throw new org.nlogo.api.AgentException(I18N.gui.get("tools.agentMonitor.editor.edpectedLinkBreed"))
      breed
    }

  /// load and save are inapplicable

  override def load(model: CoreWidget): Unit =
    throw new UnsupportedOperationException

  override def model: CoreWidget =
    throw new UnsupportedOperationException

    // this isn't an actual widget, so make sure we don't cause any widget-related code to run. -Jeremy B November 2020
  override def raiseWidgetRemoved(): Unit = {}
  override def raiseWidgetAdded(): Unit = {}

  override def syncTheme(): Unit = {
    label.setForeground(
      if (isEnabled)
        InterfaceColors.dialogText()
      else
        InterfaceColors.menuTextDisabled())

    editor.setBackground(InterfaceColors.textAreaBackground())
    editor.setCaretColor(InterfaceColors.textAreaText())

    scrollPane.setBackground(InterfaceColors.textAreaBackground())
  }
}
