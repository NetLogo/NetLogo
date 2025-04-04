// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Graphics, RadialGradientPaint }
import java.awt.event.{ MouseAdapter, MouseEvent, MouseMotionAdapter }
import javax.swing.JSlider
import javax.swing.plaf.basic.BasicSliderUI

import org.nlogo.swing.Utils
import org.nlogo.theme.InterfaceColors

class SliderWidgetUI(widget: AbstractSliderWidget, slider: JSlider) extends BasicSliderUI(slider) {
  private var hover = false
  private var pressed = false

  slider.setOpaque(false)

  slider.addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = {
      if (thumbRect.contains(e.getPoint)) {
        hover = false
        pressed = true

        widget.repaint()
      }
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      hover = thumbRect.contains(e.getPoint)
      pressed = false

      widget.repaint()
    }

    override def mouseExited(e: MouseEvent): Unit = {
      hover = false
      pressed = false

      widget.repaint()
    }
  })

  slider.addMouseMotionListener(new MouseMotionAdapter {
    override def mouseMoved(e: MouseEvent): Unit = {
      hover = thumbRect.contains(e.getPoint) && !pressed

      widget.repaint()
    }
  })

  override def paintTrack(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)
    val thickness = (6 * widget.getZoomFactor).toInt
    if (widget.vertical) {
      val startX = trackRect.x + trackRect.width / 2 - thickness / 2
      g2d.setColor(InterfaceColors.sliderBarBackgroundFilled)
      g2d.fillRoundRect(startX, thumbRect.y, thickness, trackRect.height - thumbRect.y, thickness, thickness)
      g2d.setColor(InterfaceColors.sliderBarBackground)
      g2d.fillRoundRect(startX, trackRect.y, thickness, thumbRect.y, thickness, thickness)
    } else {
      val startY = trackRect.y + trackRect.height / 2 - thickness / 2
      g2d.setColor(InterfaceColors.sliderBarBackgroundFilled)
      g2d.fillRoundRect(trackRect.x, startY, thumbRect.x, thickness, thickness, thickness)
      g2d.setColor(InterfaceColors.sliderBarBackground)
      g2d.fillRoundRect(thumbRect.x, startY, trackRect.width - thumbRect.x, thickness, thickness, thickness)
    }
  }

  override def paintThumb(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)
    if (hover) {
      if (widget.vertical) {
        g2d.setPaint(new RadialGradientPaint(thumbRect.getCenterX.toInt, thumbRect.getCenterY.toInt + 3,
                                            getThumbSize.height / 2f, Array[Float](0, 1),
                                            Array(InterfaceColors.widgetHoverShadow, InterfaceColors.Transparent)))
        g2d.fillOval(thumbRect.x, thumbRect.y + getThumbSize.width / 2 - getThumbSize.height / 2 + 3,
                    getThumbSize.height, getThumbSize.height)
      } else {
        g2d.setPaint(new RadialGradientPaint(thumbRect.getCenterX.toInt, thumbRect.getCenterY.toInt + 3,
                                            getThumbSize.width / 2f, Array[Float](0, 1),
                                            Array(InterfaceColors.widgetHoverShadow, InterfaceColors.Transparent)))
        g2d.fillOval(thumbRect.x, thumbRect.y + getThumbSize.height / 2 - getThumbSize.width / 2 + 3,
                    getThumbSize.width, getThumbSize.width)
      }
    }
    if (widget.vertical) {
      val height = (getThumbSize.height * widget.getZoomFactor).toInt
      val startX = thumbRect.getCenterX.toInt - height / 2
      g2d.setColor(InterfaceColors.sliderThumbBorder)
      g2d.fillOval(startX, thumbRect.y + thumbRect.height / 2 - height / 2, height, height)
      if (pressed) {
        g2d.setColor(InterfaceColors.sliderThumbBackgroundPressed)
      } else {
        g2d.setColor(InterfaceColors.sliderThumbBackground)
      }
      g2d.fillOval(startX + 1, thumbRect.y + thumbRect.height / 2 - height / 2 + 1, height - 2, height - 2)
    } else {
      val width = (getThumbSize.width * widget.getZoomFactor).toInt
      val startY = thumbRect.getCenterY.toInt - width / 2
      g2d.setColor(InterfaceColors.sliderThumbBorder)
      g2d.fillOval(thumbRect.x + thumbRect.width / 2 - width / 2, startY, width, width)
      if (pressed) {
        g2d.setColor(InterfaceColors.sliderThumbBackgroundPressed)
      } else {
        g2d.setColor(InterfaceColors.sliderThumbBackground)
      }
      g2d.fillOval(thumbRect.x + thumbRect.width / 2 - width / 2 + 1, startY + 1, width - 2, width - 2)
    }
  }

  override def paintFocus(g: Graphics): Unit = {
    // don't paint a focus box for sliders (Isaac B 2/8/25)
  }

  override def scrollDueToClickInTrack(dir: Int): Unit = {
    // implemented in track listener (Isaac B 2/8/25)
  }

  override def createTrackListener(slider: JSlider): TrackListener =
    new TrackListener {
      override def mousePressed(e: MouseEvent): Unit = {
        if (thumbRect.contains(e.getPoint)) {
          super.mousePressed(e)
        } else if (e.getButton == MouseEvent.BUTTON1) {
          slider.requestFocus()

          if (widget.vertical) {
            slider.setValue(valueForYPosition(e.getPoint.y))
          } else {
            slider.setValue(valueForXPosition(e.getPoint.x))
          }

          widget.updateValue()
        }
      }
    }
}
