// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Graphics, Point, RadialGradientPaint }
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.{ JPanel, SwingConstants }

import org.nlogo.swing.{ Transparent, Utils }
import org.nlogo.theme.InterfaceColors

// custom slider implementation to work around the fact that JSlider only supports integer values (Isaac B 1/2/26)
class Slider(private var minimum: Double, private var increment: Double, private var maximum: Double,
             widget: AbstractSliderWidget) extends JPanel with Transparent {

  private var orientation = SwingConstants.HORIZONTAL
  private var value = 0.0

  private var hover = false
  private var pressed = false

  setLayout(null)

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = {
      if (e.getButton == MouseEvent.BUTTON1 && !pressed) {
        pressed = onThumb(e.getPoint)

        if (widget.jumpOnClick) {
          pressed = true

          if (orientation == SwingConstants.HORIZONTAL) {
            widget.setValue(valueForX(e.getX))
          } else {
            widget.setValue(valueForY(e.getY))
          }
        } else if (!pressed) {
          val thumb = thumbPos()

          if (orientation == SwingConstants.HORIZONTAL) {
            if (e.getX > thumb.x) {
              increase()
            } else if (e.getX < thumb.x) {
              decrease()
            }
          } else {
            if (e.getY < thumb.y) {
              increase()
            } else if (e.getY > thumb.y) {
              decrease()
            }
          }

          widget.setValue(value)
        }

        repaint()
      }
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      if (e.getButton == MouseEvent.BUTTON1 && pressed) {
        pressed = false

        repaint()
      }
    }
  })

  addMouseMotionListener(new MouseAdapter {
    override def mouseMoved(e: MouseEvent): Unit = {
      val old = hover

      hover = onThumb(e.getPoint)

      if (hover != old)
        repaint()
    }

    override def mouseDragged(e: MouseEvent): Unit = {
      if (pressed) {
        hover = onThumb(e.getPoint)

        if (orientation == SwingConstants.HORIZONTAL) {
          widget.setValue(valueForX(e.getX))
        } else {
          widget.setValue(valueForY(e.getY))
        }
      }
    }

    override def mouseExited(e: MouseEvent): Unit = {
      if (hover) {
        hover = false

        repaint()
      }
    }
  })

  def setOrientation(value: Int): Unit = {
    orientation = value
  }

  def setMinimum(value: Double): Unit = {
    minimum = value
  }

  def setIncrement(value: Double): Unit = {
    increment = value
  }

  def setMaximum(value: Double): Unit = {
    maximum = value
  }

  def getValue: Double =
    value

  def setValue(value: Double): Unit = {
    this.value = value
  }

  def increase(): Unit = {
    value = (value + increment).max(minimum).min(maximum)
  }

  def decrease(): Unit = {
    value = (value - increment).max(minimum).min(maximum)
  }

  private def constrain(value: Double): Double =
    ((value / increment).round * increment).max(minimum).min(maximum)

  private def valueForX(x: Double): Double = {
    val radius = thumbRadius

    constrain(((x - radius) / (getWidth - radius * 2)) * (maximum - minimum) + minimum)
  }

  private def valueForY(y: Double): Double = {
    val radius = thumbRadius

    constrain(((y - radius) / (getHeight - radius * 2)) * (maximum - minimum) + minimum)
  }

  private def trackThickness: Int =
    widget.zoom(6)

  private def thumbRadius: Int =
    trackThickness

  private def thumbPos(): Point = {
    val radius = thumbRadius

    if (orientation == SwingConstants.HORIZONTAL) {
      val x = ((getWidth - radius * 2) * (value - minimum) / (maximum - minimum) + radius).toInt

      new Point(x.max(radius).min(getWidth - radius), getHeight - radius * 3 / 2)
    } else {
      val y = ((getHeight - radius * 2) * (value - minimum) / (maximum - minimum) + radius).toInt

      new Point(getWidth - radius * 3 / 2, y.max(radius).min(getHeight - radius))
    }
  }

  private def onThumb(point: Point): Boolean = {
    val thumb = thumbPos()
    val radius = thumbRadius

    (thumb.x - point.x).abs < radius && (thumb.y - point.y).abs < radius
  }

  override def getPreferredSize: Dimension = {
    if (orientation == SwingConstants.HORIZONTAL) {
      new Dimension(super.getPreferredSize.width, trackThickness * 3)
    } else {
      new Dimension(trackThickness * 3, super.getPreferredSize.height)
    }
  }

  override def paintComponent(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)

    val thickness = trackThickness
    val radius = thumbRadius
    val diameter = radius * 2
    val border = widget.zoom(1)
    val thumb = thumbPos()

    // track

    if (orientation == SwingConstants.HORIZONTAL) {
      val startY = getHeight - radius * 2

      g2d.setColor(InterfaceColors.sliderBarBackgroundFilled())
      g2d.fillRoundRect(0, startY, thumb.x, thickness, thickness, thickness)
      g2d.setColor(InterfaceColors.sliderBarBackground())
      g2d.fillRoundRect(thumb.x, startY, getWidth - thumb.x, thickness, thickness, thickness)
    } else {
      val startX = getWidth - radius * 2

      g2d.setColor(InterfaceColors.sliderBarBackgroundFilled())
      g2d.fillRoundRect(startX, thumb.y, thickness, getHeight - thumb.y, thickness, thickness)
      g2d.setColor(InterfaceColors.sliderBarBackground())
      g2d.fillRoundRect(startX, 0, thickness, thumb.y, thickness, thickness)
    }

    // thumb hover shadow

    if (hover) {
      val shadow: Point = {
        if (orientation == SwingConstants.HORIZONTAL) {
          new Point(thumb.x, thumb.y + widget.zoom(3))
        } else {
          new Point(thumb.x + widget.zoom(3), thumb.y)
        }
      }

      g2d.setPaint(new RadialGradientPaint(shadow, radius.toFloat, Array(0f, 1f),
                                           Array(InterfaceColors.widgetHoverShadow(), InterfaceColors.Transparent)))
      g2d.fillOval(shadow.x - radius, shadow.y - radius, diameter, diameter)
    }

    // thumb

    g2d.setColor(InterfaceColors.sliderThumbBorder())
    g2d.fillOval(thumb.x - radius, thumb.y - radius, diameter, diameter)

    if (pressed) {
      g2d.setColor(InterfaceColors.sliderThumbBackgroundPressed())
    } else {
      g2d.setColor(InterfaceColors.sliderThumbBackground())
    }

    g2d.fillOval(thumb.x - radius + border, thumb.y - radius + border, diameter - border * 2, diameter - border * 2)
  }
}
