// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Dimension, GridBagConstraints, GridBagLayout, Insets }
import java.util.{ List => JList }
import javax.swing.{ JPanel, JScrollPane, JWindow, ScrollPaneConstants }

import org.nlogo.agent.Agent
import org.nlogo.app.common.{ CommandLine, HistoryPrompt, LinePrompt }
import org.nlogo.awt.Hierarchy
import org.nlogo.core.{ AgentKind, I18N }
import org.nlogo.swing.CollapsiblePane
import org.nlogo.window.{ CommandCenterInterface, GUIWorkspaceScala, InterfaceColors, OutputArea }

abstract class AgentMonitor(val workspace: GUIWorkspaceScala, window: JWindow)
extends JPanel with CommandCenterInterface // lets us embed CommandLine
{

  private var _agent: Agent = null
  def agent = _agent
  def setAgent(agent: Agent, radius: Double) {
    val oldAgent = _agent
    if(agent != oldAgent) {
      _agent = agent
      commandLine.agent(agent)
      commandLine.setEnabled(agent != null && agent.id != -1)
      historyPrompt.setEnabled(agent != null && agent.id != -1)
      agentEditor.reset()
      val window = Hierarchy.findAncestorOfClass(this, classOf[AgentMonitorWindow])
        .orNull.asInstanceOf[AgentMonitorWindow]
      if(window != null)
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

  setLayout(new BorderLayout)

  val scrollPane = new JScrollPane(agentEditor,
    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
      // never let preferred height exceed 350; after 350, just let scrolling kick in - ST 8/17/03
      override def getPreferredSize = {
        val sup = super.getPreferredSize
        new Dimension(sup.width, StrictMath.min(sup.height, 350))
      }
    }
  val viewPanel: Option[AgentMonitorViewPanel] =
    if(agentKind == AgentKind.Observer) {
      // the observer monitor doesn't have a view or the command center. ev 6/4/08
      add(scrollPane, BorderLayout.CENTER)
      None
    }
    else {
      implicit val i18nPrefix = I18N.Prefix("tools.agentMonitor")
      val panel = new AgentMonitorViewPanel(workspace)
      add(new CollapsiblePane(I18N.gui("view"), panel, window), BorderLayout.NORTH)
      add(new CollapsiblePane(I18N.gui("properties"), scrollPane, window), BorderLayout.CENTER)
      commandLine.setEnabled(agent != null && agent.id != -1)
      historyPrompt.setEnabled(agent != null && agent.id != -1)
      commandLine.agentKind(agentKind)
      prompt.setEnabled(false)
      val commandPanel = new JPanel
      val gridBag = new GridBagLayout
      commandPanel.setLayout(gridBag)
      commandPanel.setBackground(InterfaceColors.AGENT_COMMANDER_BACKGROUND)
      val c = new GridBagConstraints
      gridBag.setConstraints(prompt, c)
      commandPanel.add(prompt)
      c.weightx = 1.0
      c.fill = GridBagConstraints.BOTH
      gridBag.setConstraints(commandLine, c)
      commandPanel.add(commandLine)
      c.weightx = 0.0
      c.fill = GridBagConstraints.NONE
      c.insets = new Insets(1, 1, 1, 1)
      gridBag.setConstraints(historyPrompt, c)
      commandPanel.add(historyPrompt)
      add(commandPanel, BorderLayout.SOUTH)
      Some(panel)
    }

  // confusing method name, should be "tabKeyPressed" or something - ST 8/16/03
  def cycleAgentType(forward: Boolean)   {
    if(forward)
      // calling commandLine.transferFocus() here didn't work for some reason - ST 8/16/03
      agentEditor.requestFocus()
    else
      // but this does the right thing! go figure! - ST 8/16/03
      commandLine.transferFocusBackward()
  }

  def radius(radius: Double): Unit = {
    viewPanel.foreach(_.radius(radius))
  }

  override def requestFocus() {
    if(commandLine.isEnabled)
      commandLine.requestFocus
    else
      agentEditor.requestFocus
  }

  def outputArea: Option[OutputArea] = None

  def refresh() {
    viewPanel.foreach(_.refresh())
    if (agent != null && agent.id == -1) {
      viewPanel.foreach(_.setEnabled(false))
      commandLine.setEnabled(false)
      historyPrompt.setEnabled(false)
    }
    if((oldVars ne vars) && (vars == null || oldVars == null || !sameVars(oldVars, vars))) {
      agentEditor.reset()
      oldVars = vars
    }
    else
      agentEditor.refresh()
  }

  /// helpers

  private def sameVars(vars1: JList[String], vars2: JList[String]) =
    vars1.size == vars2.size &&
    (0 until vars1.size).forall(i => vars1.get(i) == vars2.get(i))

  def close(): Unit = {
    viewPanel.foreach(_.close())
  }
}
