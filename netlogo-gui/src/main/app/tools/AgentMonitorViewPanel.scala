// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Color => AwtColor, Font, GridBagConstraints, GridBagLayout }
import java.awt.event.{ ActionEvent, MouseWheelEvent, MouseWheelListener }
import javax.swing.{ AbstractAction, JButton, JPanel }

import org.nlogo.agent.Agent
import org.nlogo.api.Perspective
import org.nlogo.awt.Fonts
import org.nlogo.swing.{ Utils => SwingUtils }
import org.nlogo.window.{ GUIWorkspace, InterfaceColors }

class AgentMonitorViewPanel(workspace: GUIWorkspace) extends JPanel {

  val view = new AgentMonitorView(workspace)
  setLayout(new BorderLayout)
  add(view, BorderLayout.CENTER)
  view.setSize(workspace.world.worldWidth, workspace.world.worldHeight, 255 / workspace.world.worldWidth)
  view.applyNewFontSize(workspace.view.fontSize, 0)
  private val watchButton = new JButton(new WatchMeAction)
  watchButton.setFont(new Font(Fonts.platformFont, Font.PLAIN, 10))
  watchButton.setBackground(InterfaceColors.GRAPHICS_BACKGROUND)
  watchButton.setBorder(SwingUtils.createWidgetBorder())
  watchButton.setFocusable(false)
  private val zoomer = new ZoomSlider(view)
  val controlPanel = new JPanel
  val gridbag = new GridBagLayout
  val c = new GridBagConstraints
  controlPanel.setLayout(gridbag)
  c.gridwidth = GridBagConstraints.RELATIVE
  gridbag.setConstraints(watchButton, c)
  controlPanel.add(watchButton)
  c.gridwidth = GridBagConstraints.REMAINDER
  gridbag.setConstraints(zoomer, c)
  controlPanel.add(zoomer)
  add(controlPanel, BorderLayout.SOUTH)
  watchButton.setEnabled(false)
  zoomer.setEnabled(false)

  def agent(agent: Agent, radius: Double) {
    view.agent(agent)
    watchButton.setEnabled(true)
    zoomer.setEnabled(true)
    this.radius(radius)
  }

  def radius(radius: Double) {
    zoomer.setValue(StrictMath.round(radius * 100).toInt)
  }

  override def setEnabled(enablement: Boolean): Unit = {
    val wasEnabled = isEnabled
    super.setEnabled(enablement)
    if (! enablement && wasEnabled) {
      val grayPanel = new JPanel()
      grayPanel.setPreferredSize(view.getPreferredSize)
      remove(view)
      add(grayPanel, BorderLayout.CENTER)
    }
  }

  def refresh(): Unit = {
    repaint()
    revalidate()
  }

  def close(): Unit = {
    view.close()
  }

  private class WatchMeAction extends AbstractAction("watch-me") {
    override def actionPerformed(e: ActionEvent) {
      workspace.world.observer.setPerspective(Perspective.Watch(view.agent))
      workspace.view.discardOffscreenImage()
      workspace.viewManager.getPrimary.incrementalUpdateFromEventThread()
    }
  }

  private class ZoomSlider(view: AgentMonitorView)
  extends javax.swing.JSlider(0, ((workspace.world.worldWidth - 1) / 2) * 100, (view.radius() * 100).toInt)
  with MouseWheelListener
  {
    addMouseWheelListener(this)
    setInverted(true)
    override def setValue(n: Int) {
      super.setValue(n)
      view.radius(n / 100.0)
    }
    override def mouseWheelMoved(e: MouseWheelEvent) {
      setValue(getValue - e.getWheelRotation)
    }
  }

}
