// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Component, Dimension, Graphics, GridBagConstraints, GridBagLayout }
import java.awt.event.{ InputEvent, MouseEvent, MouseListener, MouseWheelEvent, MouseWheelListener }
import java.util.prefs.Preferences
import javax.swing.{ JLabel, JPanel, JSlider, SwingConstants }
import javax.swing.event.{ ChangeEvent, ChangeListener }
import javax.swing.plaf.basic.BasicSliderUI

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
  implicit val prefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("tabs.run.speedslider")

  private val prefs = Preferences.userRoot.node("/org/nlogo/NetLogo")

  val speedSlider = {
    val slider = new SpeedSlider(workspace.speedSliderPosition().toInt)
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

    override def paintComponent(g: Graphics): Unit = {
      super.paintComponent(g)

      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.toolbarText())
      g2d.fillRect(6, 9, 7, 1)
    }
  }

  val faster = new Button("", () => speedSlider.setValue(speedSlider.getValue + 11)) {
    override def getPreferredSize: Dimension =
      new Dimension(19, 19)

    override def paintComponent(g: Graphics): Unit = {
      super.paintComponent(g)

      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.toolbarText())
      g2d.fillRect(6, 9, 7, 1)
      g2d.fillRect(9, 6, 1, 7)
    }
  }

  val modelSpeed = new JLabel(I18N.gui("modelSpeed"), SwingConstants.CENTER)

  private var jumpOnClick = prefs.getBoolean("jumpOnClick", true)

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

  def setJumpOnClick(value: Boolean): Unit = {
    jumpOnClick = value
  }

  override def syncTheme(): Unit = {
    slower.syncTheme()
    faster.syncTheme()

    modelSpeed.setForeground(InterfaceColors.toolbarText())
  }

  class SpeedSlider(defaultSpeed: Int) extends JSlider(-110, 112, defaultSpeed) with MouseWheelListener {
    private val sliderUI = new SpeedSliderUI
    private var lastThumbLocation = 0

    setExtent(1)
    setToolTipText(I18N.gui("tooltip"))
    setUI(sliderUI)

    addMouseWheelListener(this)

    override def getPreferredSize: Dimension =
      new Dimension(180, super.getPreferredSize.height)

    override def getMinimumSize: Dimension =
      new Dimension(60, super.getPreferredSize.height)

    def reset(): Unit = {
      setValue(0)
    }

    def mouseWheelMoved(e: MouseWheelEvent): Unit = {
      setValue(this.getValue - e.getWheelRotation)
    }

    override def setValue(value: Int): Unit = {
      lastThumbLocation = sliderUI.getThumbLocation

      super.setValue(value)
    }

    override def paintComponent(g: Graphics): Unit = {
      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(Color.GRAY)
      g2d.drawLine(getWidth / 2 - 1, getHeight / 4, getWidth / 2 - 1, getHeight * 3 / 4)

      super.paintComponent(g)
    }

    private class SpeedSliderUI extends BasicSliderUI(this) {
      def getThumbLocation: Int =
        thumbRect.x

      override def getThumbSize: Dimension =
        new Dimension(12, 12)

      override def paintTrack(g: Graphics): Unit = {
        val g2d = Utils.initGraphics2D(g)

        val startY = trackRect.y + trackRect.height / 2 - 1

        if (SpeedSlider.this.isEnabled) {
          g2d.setColor(InterfaceColors.speedSliderBarBackgroundFilled())
        } else {
          g2d.setColor(InterfaceColors.speedSliderBarBackground())
        }

        g2d.fillRoundRect(trackRect.x, startY, thumbRect.x, 2, 2, 2)
        g2d.setColor(InterfaceColors.speedSliderBarBackground())
        g2d.fillRoundRect(thumbRect.x, startY, trackRect.width - thumbRect.x, 2, 2, 2)
      }

      override def paintThumb(g: Graphics): Unit = {
        if (!SpeedSlider.this.isEnabled)
          setThumbLocation(lastThumbLocation, 0)

        val g2d = Utils.initGraphics2D(g)

        val width = getThumbSize.width
        val startY = thumbRect.getCenterY.toInt - width / 2

        if (SpeedSlider.this.isEnabled) {
          g2d.setColor(InterfaceColors.speedSliderThumb())
        } else {
          g2d.setColor(InterfaceColors.speedSliderThumbDisabled())
        }

        g2d.fillOval(thumbRect.x + thumbRect.width / 2 - width / 2, startY, width, width)
      }

      override def scrollDueToClickInTrack(dir: Int): Unit = {
        // implemented in track listener (Isaac B 2/8/25)
      }

      override def createTrackListener(slider: JSlider): TrackListener =
        new TrackListener {
          override def mousePressed(e: MouseEvent): Unit = {
            if (e.getButton == MouseEvent.BUTTON1) {
              slider.requestFocus()

              if (jumpOnClick) {
                slider.setValue(valueForXPosition(e.getX))
              } else if (valueForXPosition(e.getX) > slider.getValue) {
                slider.setValue(slider.getValue + 11)
              } else {
                slider.setValue(slider.getValue - 11)
              }
            }
          }

          override def mouseDragged(e: MouseEvent): Unit = {
            if ((e.getModifiersEx & InputEvent.BUTTON1_DOWN_MASK) == InputEvent.BUTTON1_DOWN_MASK &&
                (jumpOnClick || isDragging)) {

              slider.setValue(valueForXPosition(e.getPoint.x))
            }
          }
        }
    }
  }
}
