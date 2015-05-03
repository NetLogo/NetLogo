// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window


import java.awt.{ BorderLayout, Color, Dimension, Graphics, GridBagConstraints, GridBagLayout }
import java.awt.event.{ MouseAdapter, MouseEvent, MouseWheelEvent, MouseWheelListener }
import javax.swing.{ JLabel, JPanel, JSlider }
import javax.swing.event.{ ChangeEvent, ChangeListener }
import org.nlogo.api.{ I18N, Version }
import org.nlogo.awt.Fonts
import org.nlogo.log.Logger

class SpeedSliderPanel(workspace: GUIWorkspace, labelsBelow: Boolean)
    extends JPanel with ChangeListener with Events.LoadBeginEventHandler {
  implicit val i18nName = I18N.Prefix("tabs.run.speedslider")
  private val normal = new SpeedLabel(I18N.gui("normalspeed"))
  private val speedSlider = new SpeedSlider(workspace.speedSliderPosition.toInt)
  speedSlider.setFocusable(false)
  speedSlider.addChangeListener(this)
  speedSlider.addMouseListener(new MouseAdapter {
    // when we release the mouse if it's kinda close to the
    // center snappy snap.  ev 2/22/07
    override def mouseReleased(e: MouseEvent) = {
        val value = speedSlider.getValue
        if(value >= -10 && value <= 10)
        speedSlider.setValue(0)
    }
  })
  speedSlider.setOpaque(false)
  Fonts.adjustDefaultFont(normal)
  setOpaque(false)
  if(labelsBelow) {
    val gridbag = new GridBagLayout
    val c = new GridBagConstraints
    setLayout(gridbag)
    c.gridwidth = GridBagConstraints.REMAINDER
    add(speedSlider, c)
    c.gridwidth = 1
    c.anchor = GridBagConstraints.CENTER
    add(normal, c)
  } else {
    val layout = new BorderLayout
    layout.setVgap(0)
    setLayout(layout)
    add(speedSlider, BorderLayout.CENTER)
    add(normal, BorderLayout.EAST)
  }
  enableLabels(0)

  override def setEnabled(enabled: Boolean) = {
    speedSlider.setEnabled(enabled)
    // if we do setVisible() on the label, that changes the layout
    // which is a bit jarring, so do this instead - ST 9/16/08
    if(enabled) stateChanged(null) else normal.setText(" ")
  }

  def stateChanged(e: ChangeEvent) = {
    var value = speedSlider.getValue
    // adjust the speed reported to the workspace
    // so there isn't a big gap between the snap area
    // and outside the snap area. ev 2/22/07
    if(value < -10)     value += 10
    else if(value > 10) value -= 10
    else                value = 0
    workspace.speedSliderPosition(value / 2)
    if(Version.isLoggingEnabled)
      Logger.logSpeedSlider(value)
    enableLabels(value)
    workspace.updateManager.nudgeSleeper()
  }

  def enableLabels(value: Int) = normal.setText(
    if(value == 0)
      (if(labelsBelow) " "*6 else "") + I18N.gui("normalspeed")
    else if(value < 0)
      I18N.gui("slower") + (if(labelsBelow) " "*25 else "")
    else
      (if(labelsBelow) " "*25 else "") + I18N.gui("faster"))

  def handle(e: Events.LoadBeginEvent) = speedSlider.reset()

  def setValue(speed: Int) = {
    speedSlider.setValue(speed)
    enableLabels(workspace.speedSliderPosition.toInt)
  }
  def getValue = speedSlider.getValue
  def getMaximum = speedSlider.getMaximum
  override def isEnabled = speedSlider.isEnabled

  private class SpeedSlider(defaultSpeed: Int)
      extends JSlider(-110, 112, defaultSpeed) with MouseWheelListener {
    setExtent(1)
    setToolTipText("Adjust speed of model")
    addMouseWheelListener(this)

    override def getPreferredSize = new Dimension(180, super.getPreferredSize.height)
    override def getMinimumSize = new Dimension(60, super.getPreferredSize.height)
    
    def reset() = setValue(0)

    def mouseWheelMoved(e: MouseWheelEvent) = setValue(getValue - e.getWheelRotation)

    override def paint(g: Graphics) = {
      val bounds = getBounds()
      val x = bounds.x + (bounds.width / 2) - 1
      g.setColor(Color.gray)
      g.drawLine(x, bounds.y + (bounds.height * 3 / 4), x, bounds.y + bounds.height)
      super.paint(g)
    }
  }

  private class SpeedLabel(label: String) extends JLabel(label) {
    override def getPreferredSize = getMinimumSize
    override def getMinimumSize = {
      val d = super.getMinimumSize
      val fontMetrics = getFontMetrics(getFont)
      d.width = StrictMath.max(d.width,
        fontMetrics.stringWidth(if(!labelsBelow) I18N.gui("normalspeed") else " "*25 + I18N.gui("faster") + 10))
      d
    }
  }
}
