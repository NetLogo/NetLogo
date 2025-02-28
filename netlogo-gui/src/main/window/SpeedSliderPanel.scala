// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Component, Dimension, Graphics, GridBagConstraints, GridBagLayout }
import java.awt.event.{ MouseEvent, MouseListener, MouseWheelEvent, MouseWheelListener }
import javax.swing.{ JLabel, JPanel, JSlider, SwingConstants }
import javax.swing.event.{ ChangeEvent, ChangeListener }

import org.nlogo.core.I18N
import org.nlogo.log.LogManager
import org.nlogo.swing.{ Button, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Events.LoadBeginEvent

class SpeedSliderPanel(workspace: GUIWorkspace, ticksLabel: Component = null) extends JPanel
                                                                              with MouseListener
                                                                              with ChangeListener
                                                                              with LoadBeginEvent.Handler
                                                                              with ThemeSync {
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

  val slower = new Button("", () => speedSlider.setValue(speedSlider.getValue - 11)) {
    override def getPreferredSize: Dimension =
      new Dimension(19, 19)

    override def paintComponent(g: Graphics) {
      super.paintComponent(g)

      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.toolbarText)
      g2d.fillRect(6, 9, 7, 1)
    }
  }

  val faster = new Button("", () => speedSlider.setValue(speedSlider.getValue + 11)) {
    override def getPreferredSize: Dimension =
      new Dimension(19, 19)

    override def paintComponent(g: Graphics) {
      super.paintComponent(g)

      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.toolbarText)
      g2d.fillRect(6, 9, 7, 1)
      g2d.fillRect(9, 6, 1, 7)
    }
  }

  val modelSpeed = new JLabel(I18N.gui("modelSpeed"), SwingConstants.CENTER)

  slower.setFont(slower.getFont.deriveFont(10f))
  faster.setFont(faster.getFont.deriveFont(10f))

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.gridy = 1

    add(slower, c)

    c.gridx = 1
    c.gridy = 0
    c.weightx = 1

    add(modelSpeed, c)

    c.gridx = 2
    c.gridy = 1

    add(faster, c)

    c.gridx = 1
    c.fill = GridBagConstraints.HORIZONTAL
    c.anchor = GridBagConstraints.CENTER

    add(speedSlider, c)

    if (ticksLabel != null) {
      c.gridy = 2
      c.fill = GridBagConstraints.NONE

      add(ticksLabel, c)
    }
  }

  override def setEnabled(enabled: Boolean): Unit = {
    speedSlider.setEnabled(enabled)
    // if we do setVisible() on the label, that changes the layout
    // which is a bit jarring, so do this instead - ST 9/16/08
    if (enabled)
      stateChanged(null)
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

  override def syncTheme(): Unit = {
    slower.syncTheme()
    faster.syncTheme()

    modelSpeed.setForeground(InterfaceColors.toolbarText)
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

    override def paintComponent(g: Graphics): Unit = {
      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(Color.GRAY)
      g2d.drawLine(getWidth / 2 - 1, getHeight / 4, getWidth / 2 - 1, getHeight * 3 / 4)

      super.paintComponent(g)
    }
  }
}
