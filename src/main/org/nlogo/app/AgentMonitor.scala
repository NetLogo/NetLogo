// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.agent.{Agent, Observer}

abstract class AgentMonitor(val workspace: org.nlogo.window.GUIWorkspace, window: javax.swing.JWindow)
extends javax.swing.JPanel
with org.nlogo.window.CommandCenterInterface // lets us embed CommandLine
{

  private var _agent: Agent = null
  def agent = _agent
  def setAgent(agent: org.nlogo.agent.Agent, radius: Double) {
    val oldAgent = _agent
    if(agent != oldAgent) {
      _agent = agent
      commandLine.agent(agent)
      commandLine.setEnabled(agent != null && agent.id != -1)
      historyPrompt.setEnabled(agent != null && agent.id != -1)
      agentEditor.reset()
      val window = org.nlogo.awt.Hierarchy.findAncestorOfClass(this, classOf[AgentMonitorWindow])
        .orNull.asInstanceOf[AgentMonitorWindow]
      if(window != null)
        window.agentChangeNotify(oldAgent)
      if(hasView)
        viewPanel.agent(agent, radius)
    }
  }

  def vars: java.util.List[String] // abstract
  def agentClass: Class[_ <: Agent] // abstract
  private var oldVars = vars
  val commandLine = new CommandLine(this, false, 11, workspace) { // false = don't echo commands to output
    override def classDisplayName = "Agent Monitor"
    // we'll make an AgentSet ourselves, we don't want to use the standard O/T/P sets - ST 11/5/03
    override def useAgentClass = false
  }

  private val prompt = new LinePrompt(commandLine)
  private val historyPrompt = new HistoryPrompt(commandLine)
  private val agentEditor = new AgentMonitorEditor(this)

  setLayout(new java.awt.BorderLayout)

  val scrollPane = new javax.swing.JScrollPane(agentEditor,
    javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
    javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
      // never let preferred height exceed 350; after 350, just let scrolling kick in - ST 8/17/03
      override def getPreferredSize = {
        val sup = super.getPreferredSize
        new java.awt.Dimension(sup.width, StrictMath.min(sup.height, 350))
      }
    }
  val viewPanel =
    if(agentClass eq classOf[Observer]) {
      // the observer monitor doesn't have a view or the command center. ev 6/4/08
      add(scrollPane, java.awt.BorderLayout.CENTER)
      null
    }
    else {
      val panel = new AgentMonitorViewPanel(workspace)
      add(new org.nlogo.swing.CollapsiblePane(panel, window),
          java.awt.BorderLayout.NORTH)
      add(new org.nlogo.swing.CollapsiblePane(scrollPane, window),
          java.awt.BorderLayout.CENTER)
      commandLine.setEnabled(agent != null && agent.id != -1)
      historyPrompt.setEnabled(agent != null && agent.id != -1)
      commandLine.agentClass(agentClass)
      prompt.setEnabled(false)
      val commandPanel = new javax.swing.JPanel
      val gridBag = new java.awt.GridBagLayout
      commandPanel.setLayout(gridBag)
      commandPanel.setBackground(org.nlogo.window.InterfaceColors.AGENT_COMMANDER_BACKGROUND)
      val c = new java.awt.GridBagConstraints()
      gridBag.setConstraints(prompt, c)
      commandPanel.add(prompt)
      c.weightx = 1.0
      c.fill = java.awt.GridBagConstraints.BOTH
      gridBag.setConstraints(commandLine, c)
      commandPanel.add(commandLine)
      c.weightx = 0.0
      c.fill = java.awt.GridBagConstraints.NONE
      c.insets = new java.awt.Insets(1, 1, 1, 1)
      gridBag.setConstraints(historyPrompt, c)
      commandPanel.add(historyPrompt)
      add(commandPanel, java.awt.BorderLayout.SOUTH)
      panel
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

  def radius(radius: Double) {
    if(hasView)
      viewPanel.radius(radius)
  }

  override def requestFocus() {
    if(commandLine.isEnabled)
      commandLine.requestFocus
    else
      agentEditor.requestFocus
  }

  def refresh() {
    if(agent != null && agent.id == -1) {
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

  private def sameVars(vars1: java.util.List[String], vars2: java.util.List[String]) =
    vars1.size == vars2.size &&
    (0 until vars1.size).forall(i => vars1.get(i) == vars2.get(i))

  def close() {
    if(hasView)
      workspace.viewManager.remove(viewPanel.view)
  }

  def hasView = viewPanel != null

}
