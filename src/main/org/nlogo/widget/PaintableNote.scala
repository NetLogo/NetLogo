// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.widget

import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON

import scala.collection.JavaConverters.asScalaBufferConverter

import org.nlogo.window.InterfaceColors.TRANSPARENT

import javax.swing.JComponent

trait PaintableNote {
  self: JComponent =>

  def text: String
  def color: java.awt.Color

  setBackground(TRANSPARENT)
  setOpaque(false)

  override def paintComponent(g: Graphics) {
    g.asInstanceOf[Graphics2D].setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
    g.setFont(getFont)
    val metrics: FontMetrics = g.getFontMetrics
    val stringHeight: Int = metrics.getMaxDescent + metrics.getMaxAscent
    val stringAscent: Int = metrics.getMaxAscent
    val lines = org.nlogo.awt.LineBreaker.breakLines(text, metrics, getWidth)
    g.setColor(color)
    for ((line, i) <- lines.asScala.zipWithIndex)
      g.drawString(line, 0, i * stringHeight + stringAscent)
  }
}