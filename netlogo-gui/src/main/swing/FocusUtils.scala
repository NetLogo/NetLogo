// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BasicStroke, Color, Graphics, KeyboardFocusManager, Stroke }
import java.awt.event.{ FocusEvent, FocusListener, KeyAdapter, KeyEvent }

trait FocusUtils extends Zoomable {
  protected var focusColor: Color = Color.WHITE

  private var focusDiameter = 0
  private var paintFocusOnClick = false

  private var primaryAction: Option[() => Unit] = None
  private var secondaryAction: Option[() => Unit] = None

  protected var shouldPaintFocus = false

  addFocusListener(new FocusListener {
    override def focusGained(e: FocusEvent): Unit = {
      import FocusEvent.Cause._

      shouldPaintFocus = e.getCause match {
        case TRAVERSAL | TRAVERSAL_BACKWARD | TRAVERSAL_DOWN | TRAVERSAL_FORWARD | TRAVERSAL_UP =>
          true

        case MOUSE_EVENT if paintFocusOnClick =>
          true

        case _ =>
          false
      }

      repaint()
    }

    override def focusLost(e: FocusEvent): Unit = {
      repaint()
    }
  })

  // this enables non-button components with click actions to activate those actions without
  // clicking when focused. for example, opening a custom ComboBox, or opening the Interface
  // Tab context menu. (Isaac B 3/5/26)
  addKeyListener(new KeyAdapter {
    override def keyReleased(e: KeyEvent): Unit = {
      if (hasFocus && shouldPaintFocus) {
        e.getKeyCode match {
          case KeyEvent.VK_SPACE =>
            if (e.isShiftDown) {
              secondaryAction.foreach(_())
            } else {
              primaryAction.foreach(_())
            }

          case KeyEvent.VK_ENTER if !FocusUtils.this.isInstanceOf[FocusRoot] =>
            if (e.isShiftDown) {
              secondaryAction.foreach(_())
            } else {
              primaryAction.foreach(_())
            }

          case KeyEvent.VK_ESCAPE =>
            Option(KeyboardFocusManager.getCurrentKeyboardFocusManager.getCurrentFocusCycleRoot)
              .foreach(_.requestFocus())

          case _ =>
        }
      }
    }
  })

  protected def setFocusColor(color: Color): Unit = {
    focusColor = color
  }

  protected def setFocusDiameter(diameter: Int): Unit = {
    focusDiameter = diameter
  }

  protected def setPaintFocusOnClick(enabled: Boolean): Unit = {
    paintFocusOnClick = enabled
  }

  protected def setPrimaryAction(action: () => Unit): Unit = {
    primaryAction = Option(action)
  }

  protected def setSecondaryAction(action: () => Unit): Unit = {
    secondaryAction = Option(action)
  }

  protected def paintFocus(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)

    val stroke: Stroke = g2d.getStroke

    g2d.setStroke(new BasicStroke(zoomClamped(2f)))
    g2d.setColor(focusColor)

    val topInset: Int = zoomClamped(1)
    val bottomInset: Int = zoom(2f).toInt.max(3)
    val diameter: Int = zoom(focusDiameter)

    if (diameter > 0) {
      g2d.drawRoundRect(topInset, topInset, getWidth - bottomInset, getHeight - bottomInset, diameter, diameter)
    } else {
      g2d.drawRect(topInset, topInset, getWidth - bottomInset, getHeight - bottomInset)
    }

    g2d.setStroke(stroke)
  }

  override def paint(g: Graphics): Unit = {
    super.paint(g)

    if (hasFocus && shouldPaintFocus)
      paintFocus(g)
  }
}
