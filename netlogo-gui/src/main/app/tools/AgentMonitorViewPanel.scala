// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.BorderLayout
import java.awt.event.{ ActionEvent, MouseWheelEvent, MouseWheelListener }
import javax.swing.{ AbstractAction, JToggleButton, JPanel, JSlider }

import org.nlogo.core.I18N
import org.nlogo.agent.Agent
import org.nlogo.api.Perspective
import org.nlogo.window.GUIWorkspaceScala

class AgentMonitorViewPanel(workspace: GUIWorkspaceScala)
  extends JPanel(new BorderLayout) {

  private val view = new AgentMonitorView(workspace)
  private val watchButton = new JToggleButton(new WatchAction)
  private val zoomer = new ZoomSlider(view)

  locally {
    add(view, BorderLayout.CENTER)
    view.setSize(workspace.world.worldWidth, workspace.world.worldHeight, 255 / workspace.world.worldWidth)
    view.applyNewFontSize(workspace.view.fontSize, 0)
    watchButton.setFocusable(false)
    val controls = new JPanel
    controls.add(watchButton)
    controls.add(zoomer)
    add(controls, BorderLayout.SOUTH)
    watchButton.setEnabled(false)
    zoomer.setEnabled(false)
  }

  def agent(agent: Agent, radius: Double): Unit = {
    view.agent(agent)
    watchButton.setEnabled(true)
    zoomer.setEnabled(true)
    this.radius(radius)
  }

  def radius(radius: Double): Unit =
    zoomer.setValue(StrictMath.round(radius * 100).toInt)

  override def setEnabled(enabled: Boolean): Unit = {
    val wasEnabled = isEnabled
    super.setEnabled(enabled)
    if (! enabled && wasEnabled) {
      val grayPanel = new JPanel
      grayPanel.setPreferredSize(view.getPreferredSize)
      remove(view)
      add(grayPanel, BorderLayout.CENTER)
    }
  }

  def refresh(): Unit = {
    repaint()
    revalidate()
  }

  def close(): Unit = view.close()

  private class WatchAction extends AbstractAction(I18N.gui.get("tools.agentMonitor.view.watch")) {
    override def actionPerformed(e: ActionEvent) = {
      if (watchButton.isSelected)
        workspace.world.observer.setPerspective(Perspective.Watch(view.agent))
      else
        workspace.world.observer.resetPerspective()
      workspace.view.discardOffscreenImage()
      workspace.viewManager.getPrimary.incrementalUpdateFromEventThread()
    }
  }

  private class ZoomSlider(view: AgentMonitorView)
  extends JSlider(0, ((workspace.world.worldWidth - 1) / 2) * 100, (view.radius * 100).toInt)
  with MouseWheelListener
  {
    addMouseWheelListener(this)
    setInverted(true)
    override def setValue(n: Int) = {
      super.setValue(n)
      view.radius(n / 100.0)
    }
    override def mouseWheelMoved(e: MouseWheelEvent) =
      setValue(getValue - e.getWheelRotation)
  }

}
