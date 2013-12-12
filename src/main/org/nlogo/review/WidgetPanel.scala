// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON

import javax.swing.JPanel

trait WidgetPanel extends JPanel {

  val panelBounds: java.awt.Rectangle
  val originalFont: java.awt.Font

  setBounds(panelBounds)
  setFont(originalFont)

  override def paintComponent(g: Graphics) {
    super.paintComponent(g)
    g.asInstanceOf[Graphics2D]
      .setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
  }
}
