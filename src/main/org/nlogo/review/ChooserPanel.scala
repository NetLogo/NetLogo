// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Graphics
import java.awt.Rectangle

import org.nlogo.api.ModelRun
import org.nlogo.swing.Utils.createWidgetBorder
import org.nlogo.window.ChooserPainter
import org.nlogo.window.InterfaceColors.SLIDER_BACKGROUND

import javax.swing.JPanel

class ChooserPanel(
  val panelBounds: Rectangle,
  val originalFont: java.awt.Font,
  val margin: Int,
  val name: String,
  run: ModelRun,
  index: Int)
  extends WidgetPanel {

  setBorder(createWidgetBorder)
  setBackground(SLIDER_BACKGROUND)
  setOpaque(true)
  setLayout(null)

  val control = new JPanel()
  control.setBackground(SLIDER_BACKGROUND)
  control.setBorder(createWidgetBorder)
  control.setOpaque(false)
  add(control)

  ChooserPainter.doLayout(this, control, margin)

  def value: AnyRef = "XXXXXXXX" // TODO
  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    ChooserPainter.paint(g, this, margin, control.getBounds, name, value)
  }
}
