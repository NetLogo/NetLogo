// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Dimension, GridBagConstraints, GridBagLayout, Insets }
import java.util.{ List => JList }
import javax.swing.{ JDialog, JPanel, ScrollPaneConstants }

import org.nlogo.agent.Agent
import org.nlogo.app.common.{ CommandLine, HistoryPrompt, LinePrompt }
import org.nlogo.awt.Hierarchy
import org.nlogo.core.{ AgentKind, I18N }
import org.nlogo.swing.{ CollapsiblePane, ScrollPane, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ CommandCenterInterface, GUIWorkspace }

// implementing CommandCenterInterface lets us embed CommandLine
abstract class AgentMonitor(val workspace: GUIWorkspace, window: JDialog)
  extends JPanel(new BorderLayout) with CommandCenterInterface with ThemeSync {

  private implicit val i18nPrefix = I18N.Prefix("tools.agentMonitor")

  private var _agent: Agent = null
  def agent: Agent = _agent
  def setAgent(agent: Agent, radius: Double): Unit = {
    val oldAgent = _agent
    if (agent != oldAgent) {
      _agent = agent
      commandLine.agent(agent)
      commandLine.setEnabled(agent != null && agent.id != -1)
      historyPrompt.setEnabled(agent != null && agent.id != -1)
      agentEditor.reset()
      val window = Hierarchy.findAncestorOfClass(this, classOf[AgentMonitorWindow])
        .orNull.asInstanceOf[AgentMonitorWindow]
      if (window != null)
        window.agentChangeNotify(oldAgent)
      viewPanel.foreach(_.agent(agent, radius))
    }
  }

  def vars: JList[String] // abstract
  def agentKind: AgentKind // abstract
  private var oldVars = vars
  val commandLine = new CommandLine(this, false, 11, workspace) { // false = don't echo commands to output
    override def classDisplayName = "Agent Monitor"
    // we'll make an AgentSet ourselves, we don't want to use the standard O/T/P sets - ST 11/5/03
    override def useAgentClass = false
  }

  private val prompt = new LinePrompt(commandLine)
  private val historyPrompt = new HistoryPrompt(commandLine)
  private val agentEditor = new AgentMonitorEditor(this)

  private val scrollPane = new ScrollPane(agentEditor,
    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {

    setBorder(null)

    // never let preferred height exceed 350; after 350, just let scrolling kick in - ST 8/17/03
    override def getPreferredSize: Dimension = {
      val sup = super.getPreferredSize
      new Dimension(sup.width, StrictMath.min(sup.height, 350))
    }
  }

  private val panel = new AgentMonitorViewPanel(workspace)
  private val viewPane = new CollapsiblePane(I18N.gui("view"), panel, window)
  private val propertiesPane = new CollapsiblePane(I18N.gui("properties"), scrollPane, window)
  private val viewPanel: Option[AgentMonitorViewPanel] =
    if (agentKind == AgentKind.Observer) {
      // the observer monitor doesn't have a view or the command center. ev 6/4/08
      add(scrollPane, BorderLayout.CENTER)
      None
    } else {
      add(viewPane, BorderLayout.NORTH)
      add(propertiesPane, BorderLayout.CENTER)
      commandLine.setEnabled(agent != null && agent.id != -1)
      historyPrompt.setEnabled(agent != null && agent.id != -1)
      commandLine.agentKind(agentKind)
      prompt.setEnabled(false)
      val commandPanel = new JPanel(new GridBagLayout) with Transparent
      val c = new GridBagConstraints
      c.insets = new Insets(0, 6, 0, 0)
      commandPanel.add(prompt, c)
      c.weightx = 1
      c.fill = GridBagConstraints.BOTH
      commandPanel.add(commandLine, c)
      c.weightx = 0
      c.weighty = 1
      c.fill = GridBagConstraints.VERTICAL
      c.insets = new Insets(1, 1, 1, 1)
      commandPanel.add(historyPrompt, c)
      add(commandPanel, BorderLayout.SOUTH)
      Some(panel)
    }

  // confusing method name, should be "tabKeyPressed" or something - ST 8/16/03
  def cycleAgentType(forward: Boolean): Unit = {
    if (forward) {
      // calling commandLine.transferFocus() here didn't work for some reason - ST 8/16/03
      agentEditor.requestFocus()
    } else {
      // but this does the right thing! go figure! - ST 8/16/03
      commandLine.transferFocusBackward()
    }
  }

  def radius(radius: Double): Unit = {
    viewPanel.foreach(_.radius(radius))
  }

  override def requestFocus(): Unit = {
    if (commandLine.isEnabled) {
      commandLine.requestFocus
    } else {
      agentEditor.requestFocus
    }
  }

  def refresh(): Unit = {
    viewPanel.foreach(_.refresh())
    if (agent != null && agent.id == -1) {
      viewPanel.foreach(_.setEnabled(false))
      commandLine.setEnabled(false)
      historyPrompt.setEnabled(false)
    }
    if ((oldVars ne vars) && (vars == null || oldVars == null || !sameVars(oldVars, vars))) {
      agentEditor.reset()
      oldVars = vars
    } else {
      agentEditor.refresh()
    }
  }

  /// helpers

  private def sameVars(vars1: JList[String], vars2: JList[String]): Boolean =
    vars1.size == vars2.size &&
    (0 until vars1.size).forall(i => vars1.get(i) == vars2.get(i))

  def close(): Unit = {
    viewPanel.foreach(_.close())
  }

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.dialogBackground)

    scrollPane.setBackground(InterfaceColors.dialogBackground)

    agentEditor.syncTheme()

    viewPane.syncTheme()
    propertiesPane.syncTheme()
    commandLine.syncTheme()
  }
}
