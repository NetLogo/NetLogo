// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Component, Dimension, Graphics, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ MouseEvent, MouseListener, MouseWheelEvent, MouseWheelListener }
import javax.swing.{ JLabel, JPanel, JSlider, SwingConstants }
import javax.swing.event.{ ChangeEvent, ChangeListener }

import org.nlogo.core.I18N
import org.nlogo.log.LogManager
import org.nlogo.window.Events.LoadBeginEvent

class SpeedSliderPanel(workspace: GUIWorkspace, ticksLabel: Component = null) extends JPanel
                                                                              with MouseListener
                                                                              with ChangeListener
                                                                              with LoadBeginEvent.Handler {
  implicit val prefix = I18N.Prefix("tabs.run.speedslider")

  val speedSlider = {
    val slider = new SpeedSlider(workspace.speedSliderPosition.toInt)
    slider.setFocusable(false)
    slider.addChangeListener(this)
    slider.addMouseListener(this)
    slider.setOpaque(false)
    slider
  }

  setOpaque(false)
  setLayout(new GridBagLayout)

  val slower = new JLabel(I18N.gui("slower"))
  val faster = new JLabel(I18N.gui("faster"), SwingConstants.RIGHT)
  val modelSpeed = new JLabel(I18N.gui("modelSpeed"), SwingConstants.CENTER)

  slower.setFont(slower.getFont.deriveFont(10f))
  faster.setFont(faster.getFont.deriveFont(10f))

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.gridy = 0
    c.weightx = 1
    c.anchor = GridBagConstraints.SOUTHWEST

    add(slower, c)

    c.gridx = 1
    c.anchor = GridBagConstraints.CENTER
    c.insets = new Insets(0, 12, 0, 12)

    add(modelSpeed, c)

    c.gridx = 2
    c.anchor = GridBagConstraints.SOUTHEAST
    c.insets = new Insets(0, 0, 0, 0)

    add(faster, c)

    c.gridx = 0
    c.gridy = 1
    c.gridwidth = 3
    c.fill = GridBagConstraints.HORIZONTAL
    c.anchor = GridBagConstraints.CENTER

    add(speedSlider, c)

    if (ticksLabel != null) {
      c.gridy = 2

      add(ticksLabel, c)
    }
  }

  override def setEnabled(enabled: Boolean): Unit = {
    speedSlider.setEnabled(enabled)
    // if we do setVisible() on the label, that changes the layout
    // which is a bit jarring, so do this instead - ST 9/16/08
    if (enabled)
      stateChanged(null)
    // else
    //   speedLabel.setText(" ")
  }

  def stateChanged(e: ChangeEvent): Unit = {
    val value = speedSlider.getValue
    // adjust the speed reported to the workspace
    // so there isn't a big gap between the snap area
    // and outside the snap area. ev 2/22/07
    val adjustedValue =
      if (value < -10)     value + 10
      else if (value > 10) value - 10
      else                 0

    workspace.speedSliderPosition(adjustedValue.toDouble / 2);

    LogManager.speedSliderChanged(adjustedValue)
    workspace.updateManager.nudgeSleeper()
  }

  // mouse listener junk
  def mouseClicked(e: MouseEvent): Unit = { }
  def mousePressed(e: MouseEvent): Unit = { }
  def mouseEntered(e: MouseEvent): Unit = { }
  def mouseExited(e: MouseEvent): Unit = { }

  // when we release the mouse if it's kinda close to the
  // center snappy snap.  ev 2/22/07
  def mouseReleased(e: MouseEvent): Unit = {
    speedSlider.getValue match {
      case value if (value <= 10 && value > 0) || (value >= -10 && value < 0) =>
        speedSlider.setValue(0)
      case _ =>
    }
  }

  def handle(e: LoadBeginEvent): Unit = {
    speedSlider.reset()
  }

  def setValue(speed: Int): Unit = {
    if (speedSlider.getValue != speed)
      speedSlider.setValue(speed)
  }

  def getValue: Int = speedSlider.getValue

  def getMaximum: Int = speedSlider.getMaximum

  override def isEnabled: Boolean = speedSlider.isEnabled

  override def paintComponent(g: Graphics) {
    slower.setForeground(InterfaceColors.TOOLBAR_TEXT)
    faster.setForeground(InterfaceColors.TOOLBAR_TEXT)
    modelSpeed.setForeground(InterfaceColors.TOOLBAR_TEXT)

    super.paintComponent(g)
  }

  class SpeedSlider(defaultSpeed: Int) extends JSlider(-110, 112, defaultSpeed) with MouseWheelListener {
    setExtent(1)
    setToolTipText(I18N.gui("tooltip"))
    addMouseWheelListener(this)

    override def getPreferredSize: Dimension =
      new Dimension(180, super.getPreferredSize.height)

    override def getMinimumSize: Dimension =
      new Dimension(60, super.getPreferredSize.height)

    def reset(): Unit = {
      setValue(0)
    }

    def mouseWheelMoved(e: MouseWheelEvent): Unit = {
      setValue(getValue - e.getWheelRotation)
    }

    override def paint(g: Graphics): Unit = {
      val bounds = getBounds()
      val x = bounds.x + (bounds.width / 2) - 1
      g.setColor(Color.gray)
      g.drawLine(x, bounds.y - (bounds.height / 2), x, bounds.y - (bounds.height / 4))
      super.paint(g)
    }
  }
}
