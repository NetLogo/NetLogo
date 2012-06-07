// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.window.GUIWorkspace
import org.nlogo.api.Perspective

class AgentMonitorViewPanel(workspace: GUIWorkspace) extends javax.swing.JPanel {

  val view = new AgentMonitorView(workspace)
  setLayout(new java.awt.BorderLayout)
  add(view, java.awt.BorderLayout.CENTER)
  view.setSize(workspace.world.worldWidth(), workspace.world.worldHeight(), 255 / workspace.world.worldWidth())
  view.applyNewFontSize(workspace.view.fontSize, 0)
  private val watchButton = new javax.swing.JButton(new WatchMeAction)
  watchButton.setFont(
    new java.awt.Font(org.nlogo.awt.Fonts.platformFont,
                      java.awt.Font.PLAIN, 10))
  watchButton.setBackground(org.nlogo.window.InterfaceColors.GRAPHICS_BACKGROUND)
  watchButton.setBorder(org.nlogo.swing.Utils.createWidgetBorder())
  watchButton.setFocusable(false)
  private val zoomer = new ZoomSlider(view)
  val controlPanel = new javax.swing.JPanel
  val gridbag = new java.awt.GridBagLayout
  val c = new java.awt.GridBagConstraints
  controlPanel.setLayout(gridbag)
  c.gridwidth = java.awt.GridBagConstraints.RELATIVE
  gridbag.setConstraints(watchButton, c)
  controlPanel.add(watchButton)
  c.gridwidth = java.awt.GridBagConstraints.REMAINDER
  gridbag.setConstraints(zoomer, c)
  controlPanel.add(zoomer)
  add(controlPanel, java.awt.BorderLayout.SOUTH)
  watchButton.setEnabled(false)
  zoomer.setEnabled(false)

  def agent(agent: org.nlogo.agent.Agent, radius: Double) {
    view.agent(agent)
    watchButton.setEnabled(true)
    zoomer.setEnabled(true)
    this.radius(radius)
  }

  def radius(radius: Double) {
    zoomer.setValue(StrictMath.round(radius * 100).toInt)
  }

  private class WatchMeAction extends javax.swing.AbstractAction("watch-me") {
    override def actionPerformed(e: java.awt.event.ActionEvent) {
      workspace.world.observer().setPerspective(Perspective.Watch, view.agent)
      workspace.view.discardOffscreenImage()
      workspace.viewManager.getPrimary.incrementalUpdateFromEventThread()
    }
  }

  private class ZoomSlider(view: AgentMonitorView)
  extends javax.swing.JSlider(0, ((workspace.world.worldWidth() - 1) / 2) * 100, (view.radius() * 100).toInt)
  with java.awt.event.MouseWheelListener
  {
    addMouseWheelListener(this)
    setInverted(true)
    override def setValue(n: Int) {
      super.setValue(n)
      view.radius(n / 100.0)
    }
    override def mouseWheelMoved(e: java.awt.event.MouseWheelEvent) {
      setValue(getValue - e.getWheelRotation)
    }
  }

}
