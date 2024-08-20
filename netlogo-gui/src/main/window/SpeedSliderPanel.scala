// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Component, Dimension, Graphics, GridBagConstraints, GridBagLayout }
import java.awt.event.{ ComponentEvent, ComponentListener, MouseEvent, MouseListener, MouseWheelEvent,
                        MouseWheelListener }
import javax.swing.{ JLabel, JPanel, JSlider }
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

  private val speedLabel = {
    val label = new SpeedLabel(I18N.gui("normalspeed"), (i: Int) => (i / 2, i / 2))
    label.resizeWithComponent(speedSlider)
    label
  }

  setOpaque(false)
  setLayout(new GridBagLayout)

  locally {
    val c = new GridBagConstraints

    c.fill = GridBagConstraints.VERTICAL
    c.gridwidth = 1
    c.gridheight = 1
    c.gridx = 0
    c.anchor = GridBagConstraints.PAGE_START

    add(speedLabel, c)

    c.anchor = GridBagConstraints.CENTER
    c.weighty = 0.25

    add(speedSlider, c)

    if (ticksLabel != null) {
      c.weighty = 0

      add(ticksLabel, c)
    }

    enableLabels(0)
  }

  override def setEnabled(enabled: Boolean): Unit = {
    speedSlider.setEnabled(enabled)
    // if we do setVisible() on the label, that changes the layout
    // which is a bit jarring, so do this instead - ST 9/16/08
    if (enabled)
      stateChanged(null)
    else
      speedLabel.setText(" ")
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
    enableLabels(adjustedValue)
    workspace.updateManager.nudgeSleeper()
  }

  private[window] def enableLabels(value: Int): Unit = {
    val (labelText, spaceRatios) =
      if (value == 0)     (I18N.gui("normalspeed"), (i: Int) => (i / 2, i / 2))
      else if (value < 0) (I18N.gui("slower"),      (i: Int) => (i / 5, i * 4 / 5))
      else                (I18N.gui("faster"),      (i: Int) => (i * 4 / 5, i / 5))
    speedLabel.setUnpaddedText(labelText, spaceRatios)
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

    enableLabels(workspace.speedSliderPosition().toInt)
  }

  def getValue: Int = speedSlider.getValue

  def getMaximum: Int = speedSlider.getMaximum

  override def isEnabled: Boolean = speedSlider.isEnabled

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

  private class SpeedLabel(label: String, private var spaceRatios: Int => (Int, Int)) extends JLabel(label) with ComponentListener {
    private def fontMetrics = getFontMetrics(getFont)

    private var desiredWidth = 180

    override def getPreferredSize: Dimension = getMinimumSize

    override def getMinimumSize:   Dimension =
      new Dimension(desiredWidth, super.getPreferredSize.height)

    override def paint(g: Graphics): Unit = {
      val width = getBounds().width
      val (spaceBefore, _) = spaceRatios(width - fontMetrics.stringWidth(getText))
      g.translate(spaceBefore, 0)
      super.paint(g)
    }

    def setUnpaddedText(labelText: String, newSpaceRatios: Int => (Int, Int)): Unit = {
      setText(labelText)
      spaceRatios = newSpaceRatios
    }

    def resizeWithComponent(component: Component): Unit = {
      desiredWidth = component.getPreferredSize.width
      component.addComponentListener(this)
    }

    def componentResized(e: ComponentEvent): Unit = {
      desiredWidth = e.getComponent.getPreferredSize.width
    }
    def componentMoved(e: ComponentEvent): Unit = {}
    def componentShown(e: ComponentEvent): Unit = {}
    def componentHidden(e: ComponentEvent): Unit = {}
  }
}
