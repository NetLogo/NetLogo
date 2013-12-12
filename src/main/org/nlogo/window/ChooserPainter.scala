// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Rectangle
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON

import org.nlogo.api.Dump
import org.nlogo.awt.Fonts.shortenStringToFit

import javax.swing.JComponent
import javax.swing.JPanel

object ChooserPainter {

  def paint(
    g: java.awt.Graphics,
    panel: JPanel,
    margin: Int,
    clickControlBounds: Rectangle,
    name: String,
    value: AnyRef) {
    g.asInstanceOf[java.awt.Graphics2D]
      .setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
    val size = panel.getSize()
    val cb = clickControlBounds
    g.setColor(panel.getForeground)
    val metrics = g.getFontMetrics
    val fontAscent = metrics.getMaxAscent
    val fontHeight = fontAscent + metrics.getMaxDescent
    val shortenedName =
      shortenStringToFit(
        name,
        size.width - 2 * margin,
        metrics)
    g.drawString(
      shortenedName,
      margin,
      margin + (cb.y - margin - fontHeight) / 2 + fontAscent)
    val triangleSize = cb.height / 2 - margin
    val shortenedValue =
      shortenStringToFit(
        Dump.logoObject(value),
        cb.width - margin * 3 - triangleSize - 2,
        metrics)
    g.drawString(
      shortenedValue,
      cb.x + margin,
      cb.y + (cb.height - fontHeight) / 2 + fontAscent)
    filledDownTriangle(
      g,
      cb.x + cb.width - margin - triangleSize - 2,
      cb.y + (cb.height - triangleSize) / 2 + 1,
      triangleSize)
  }

  private def filledDownTriangle(
    g: java.awt.Graphics,
    x: Int,
    y: Int,
    size: Int) {
    val shadowTriangle = new java.awt.Polygon()
    shadowTriangle.addPoint(x + size / 2, y + size + 2)
    shadowTriangle.addPoint(x - 1, y - 1)
    shadowTriangle.addPoint(x + size + 2, y - 1)
    g.setColor(java.awt.Color.DARK_GRAY)
    g.fillPolygon(shadowTriangle)
    val downTriangle = new java.awt.Polygon()
    downTriangle.addPoint(x + size / 2, y + size)
    downTriangle.addPoint(x, y)
    downTriangle.addPoint(x + size, y)
    g.setColor(InterfaceColors.SLIDER_HANDLE)
    g.fillPolygon(downTriangle)
  }

  def doLayout(chooser: JComponent, control: JComponent, margin: Int) {
    val controlHeight = chooser.getHeight / 2
    control.setBounds(
      margin,
      chooser.getHeight - margin - controlHeight,
      chooser.getWidth - 2 * margin,
      controlHeight)
  }
}
