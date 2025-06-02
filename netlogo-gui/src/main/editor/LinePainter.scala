// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.{ geom, Color, Graphics, Rectangle, Shape }, geom.Rectangle2D
import javax.swing.event.{CaretEvent, CaretListener}
import javax.swing.text.{ BadLocationException, Highlighter, JTextComponent }

class LinePainter(component: JTextComponent) extends Highlighter.HighlightPainter with CaretListener {

  private var lastView: Rectangle2D = new Rectangle(0, 0, 0, 0)
  private val color = new Color(255, 249, 228, 100)
  component.addCaretListener(this)
  try {
    component.getHighlighter.addHighlight(0, 0, this)
  } catch {
    case ble: BadLocationException =>
  }

  def paint(g: Graphics,
    p0: Int,
    p1: Int,
    bounds: Shape,
    c: JTextComponent): Unit = {
      try {
        val r = c.modelToView2D(c.getCaretPosition)
        g.setColor(color)
        g.fillRect(0, r.getY.toInt, c.getWidth, r.getHeight.toInt)
        if (lastView == null) lastView = r
      } catch {
        case ble: BadLocationException => println(ble)
      }
  }

  private def resetHighlight(): Unit = {
    try {
      val offset = component.getCaretPosition
      val currentView = component.modelToView2D(offset)
      if (currentView != null && lastView.getY != currentView.getY) {
        component.repaint(0, lastView.getY.toInt, component.getWidth, lastView.getHeight.toInt)
        lastView = currentView
      }
    } catch {
      case ble: BadLocationException =>
    }
  }

  def caretUpdate(e: CaretEvent): Unit = {
    resetHighlight()
  }
}
