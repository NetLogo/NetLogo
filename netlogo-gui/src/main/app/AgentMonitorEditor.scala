// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.agent.{Agent, AgentSet, Observer, Turtle, Patch, Link}
import org.nlogo.window.{EditorColorizer, Widget}
import org.nlogo.api.{ AgentVariables, Dump }
import org.nlogo.core.{ I18N, Widget => CoreWidget }
import org.nlogo.core.{ AgentKind, TokenType, Nobody }
import collection.JavaConverters._

class AgentMonitorEditor(parent: AgentMonitor) extends javax.swing.JPanel
{

  setBackground(org.nlogo.window.InterfaceColors.AGENT_EDITOR_BACKGROUND)
  setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1))

  private var editor: AgentVarEditor = null
  private val editors = new collection.mutable.ArrayBuffer[AgentVarEditor]

  reset()

  override def requestFocus() {
    editors.headOption.foreach(_.requestFocus())
  }

  def refresh() {
    editors.foreach(_.refresh(false))
  }

  def reset() {
    removeAll()
    editors.clear()
    if(vars == null || vars.isEmpty) {
      setLayout(new java.awt.FlowLayout)
      add(new javax.swing.JLabel("(no variables defined)"))
    }
    else fill()
  }

  private def fill() {
    val layout = new java.awt.GridBagLayout
    setLayout(layout)
    val labelConstraints = new java.awt.GridBagConstraints()
    labelConstraints.anchor = java.awt.GridBagConstraints.EAST
    labelConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE
    val editorConstraints = new java.awt.GridBagConstraints()
    editorConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL
    editorConstraints.weightx = 1.0
    editorConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER
    // add components
    for(variableName <- vars.asScala) {
      val label = new javax.swing.JLabel(variableName.toLowerCase)
      label.setFont(
        new java.awt.Font(org.nlogo.awt.Fonts.platformFont,
                          java.awt.Font.PLAIN, 10))
      label.setBorder(
        javax.swing.BorderFactory.createEmptyBorder(0, 1, 0, 1))
      val index =
        if(agent == null)
          workspace.world.indexOfVariable(agentKind, variableName)
        else
          workspace.world.indexOfVariable(agent, variableName)
      editor = new AgentVarEditor(this, index, variableName, label)
      editor.agentKind(agentKind)
      editors += editor
      layout.setConstraints(label, labelConstraints)
      add(label)
      layout.setConstraints(editor, editorConstraints)
      add(editor)
    }
    val fillerConstraints = new java.awt.GridBagConstraints()
    fillerConstraints.fill = java.awt.GridBagConstraints.BOTH
    fillerConstraints.weighty = 1.0
    val fillerPanel = new javax.swing.JPanel
    fillerPanel.setLayout(new java.awt.GridBagLayout)
    layout.setConstraints(fillerPanel, fillerConstraints)
    add(fillerPanel)
    revalidate()
  }

  def vars = parent.vars
  def agent = parent.agent
  def agentKind = parent.agentKind
  def setAgent(agent: Agent) { parent.setAgent(agent, 3) }
  def workspace = parent.workspace
}

// this gets complicated... if there's any way to make it less complicated I'd love to know about it
// - ST 8/17/03, 8/19/03

class AgentVarEditor(parent: AgentMonitorEditor,
                     index: Int,
                     variableName: String,
                     label: javax.swing.JLabel)
extends org.nlogo.window.JobWidget(parent.workspace.world.auxRNG)
with java.awt.event.KeyListener
with java.awt.event.FocusListener
with org.nlogo.window.Events.JobRemovedEvent.Handler
{

  type WidgetModel = org.nlogo.core.Widget

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

  setLayout(new java.awt.BorderLayout)
  setBorder(
    javax.swing.BorderFactory.createLineBorder(
      org.nlogo.window.InterfaceColors.AGENT_EDITOR_BACKGROUND, 1))

  private val editor = new org.nlogo.editor.EditorField(
    17, new java.awt.Font(org.nlogo.awt.Fonts.platformMonospacedFont,
                         java.awt.Font.PLAIN, 12),
    true, new EditorColorizer(workspace), I18N.gui.get _)
  editor.setFont(editor.getFont.deriveFont(10f))
  add(new javax.swing.JScrollPane(editor,
                                  javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                                  javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),
      java.awt.BorderLayout.CENTER)
  editor.addKeyListener(this)
  editor.addFocusListener(this)
  refresh(true)

  /// JobWidget stuff

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
  override def procedure_=(procedure: org.nlogo.nvm.Procedure) {
    super.procedure_=(procedure)
    if (procedure != null)
      new org.nlogo.window.Events.AddJobEvent(this, agents, procedure).raise(this)
  }

  // our job has finished, so we can fetch a new value for our variable
  def handle(e: org.nlogo.window.Events.JobRemovedEvent)
  {
    if(e.owner eq this)
      refresh(true)
  }

  override def handle(e: org.nlogo.window.Events.CompiledEvent) {
    super.handle(e)
    if(e.sourceOwner == this) {
      error(e.error)
      if(error != null) {
        org.nlogo.swing.OptionDialog.show(
          workspace.getFrame, I18N.gui.get("common.messages.error"), error.getMessage, Array(I18N.gui.get("common.buttons.ok")))
        setEnabled(true)
        editor.setText(get)
        lastTextBeforeUserChangedAnything = editor.getText()
        editor.selectAll()
      }
    }
  }

  // put together a full procedure definition. calling source at the end causes JobWidget and
  // CompileMoreSourceEvent stuff to call the compiler and add a job if compilation succeeds
  def wrapSource(innerSource: String) {
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
        workspace.world.observers()
      else {
        val agentset = new org.nlogo.agent.ArrayAgentSet(agent.kind, 1, false)
        agentset.add(agent)
        agentset
      }
    agents(agentset)
    source(header, innerSource, "\n" + footer)  // the \n protects against comments
  }

  ///

  def keyReleased(e: java.awt.event.KeyEvent) { }
  def keyTyped(e: java.awt.event.KeyEvent) { }
  def keyPressed(e: java.awt.event.KeyEvent) {
    e.getKeyCode match {
      case java.awt.event.KeyEvent.VK_ENTER =>
        action()
      case java.awt.event.KeyEvent.VK_ESCAPE =>
        editor.setText(get)
        editor.selectAll()
      case java.awt.event.KeyEvent.VK_TAB =>
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

  def focusGained(e: java.awt.event.FocusEvent) {
    editorFocus = true
    lastTextBeforeUserChangedAnything = editor.getText()
  }

  def focusLost(e: java.awt.event.FocusEvent) {
    editorFocus = false
    if(editor.getText() != lastTextBeforeUserChangedAnything)
      action()
  }

  override def requestFocus() {
    editor.requestFocus()
  }

  ///

  // without this, the scrollpane in AgentMonitor scrolls every time a variable changes for any
  // reason, which is very annoying - ST 8/17/03
  override def scrollRectToVisible(rect: java.awt.Rectangle) {
    if(editorFocus)
      super.scrollRectToVisible(rect)
  }

  ///

  // The "force" flag is so if a new value comes in at periodic update time, we don't interrupt the
  // user in the middle of typing something.  true means go ahead and interrupt the user if
  // necessary, false means refrain. - ST 8/17/03
  def refresh(force: Boolean) {
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

  override def setEnabled(enabled: Boolean) {
    if(enabled != isEnabled) {
      super.setEnabled(enabled)
      if((enabled || agent == null || agent.id == -1 || agent.getVariable(index) == null) &&
          editor.isEnabled != enabled)
        editor.setEnabled(enabled)
      label.setForeground(if (enabled) java.awt.Color.BLACK else java.awt.Color.DARK_GRAY)
      label.repaint()
    }
  }

  private def get =
    if(agent == null || agent.id == -1)
      ""
    else workspace.world.synchronized{
      Dump.logoObject(agent.getVariable(index), true, false)
    }

  private def action() {
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

  private def doAgentSwitch() {
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
        throw new org.nlogo.api.AgentException("expected a turtle or a who number")
    }
  }

  @throws(classOf[org.nlogo.api.AgentException])
  private def parseAgentSet(text: String): AgentSet =
    if(text.equalsIgnoreCase("LINKS"))
      workspace.world.links()
    else {
      val breed = workspace.world.getLinkBreed(text.toUpperCase)
      if(breed == null)
        throw new org.nlogo.api.AgentException("expected a link breed")
      breed
    }

  /// load and save are inapplicable

  override def load(model: CoreWidget): AnyRef =
    throw new UnsupportedOperationException

  override def model: CoreWidget =
    throw new UnsupportedOperationException
}
