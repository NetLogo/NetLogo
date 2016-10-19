// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.{ Color, Graphics, Rectangle, Shape }
import javax.swing.event.{CaretEvent, CaretListener}
import javax.swing.text.{ BadLocationException, Highlighter, JTextComponent }

class LinePainter(private var component: JTextComponent) extends Highlighter.HighlightPainter with CaretListener {

  private var lastView: Rectangle = new Rectangle(0, 0, 0, 0)
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
    c: JTextComponent) {
      try {
        val r = c.modelToView(c.getCaretPosition)
        g.setColor(color)
        g.fillRect(0, r.y, c.getWidth, r.height)
        if (lastView == null) lastView = r
      } catch {
        case ble: BadLocationException => println(ble)
      }
  }

  private def resetHighlight() {
    try {
      val offset = component.getCaretPosition
      val currentView = component.modelToView(offset)
      if (currentView != null && lastView.y != currentView.y) {
        component.repaint(0, lastView.y, component.getWidth, lastView.height)
        lastView = currentView
      }
    } catch {
      case ble: BadLocationException =>
    }
  }

  def caretUpdate(e: CaretEvent) {
    resetHighlight()
  }
}
