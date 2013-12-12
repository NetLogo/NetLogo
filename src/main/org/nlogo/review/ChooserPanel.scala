// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Graphics
import java.awt.Rectangle

import org.nlogo.mirror.ModelRun
import org.nlogo.mirror.WidgetKinds.Chooser
import org.nlogo.mirror.WidgetKinds.Chooser.Variables.ValueObject
import org.nlogo.swing.Utils.createWidgetBorder
import org.nlogo.window.ChooserPainter
import org.nlogo.window.InterfaceColors.SLIDER_BACKGROUND

import javax.swing.JPanel

class ChooserPanel(
  val panelBounds: Rectangle,
  val originalFont: java.awt.Font,
  val margin: Int,
  val name: String,
  val run: ModelRun,
  val index: Int)
  extends WidgetPanel
  with MirroredWidget {

  val kind = Chooser

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

  def value: AnyRef = mirroredVar[AnyRef](ValueObject.id).get
  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    ChooserPainter.paint(g, this, margin, control.getBounds, name, value)
  }
}
