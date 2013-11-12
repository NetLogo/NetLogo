package org.nlogo.review

import org.nlogo.mirror.ModelRun
import org.nlogo.widget.PaintableSwitch
import org.nlogo.widget.PaintableSwitchChannel
import org.nlogo.widget.PaintableSwitchDragger

class SwitchPanel(
  val panelBounds: java.awt.Rectangle,
  val originalFont: java.awt.Font,
  val displayName: String,
  run: ModelRun,
  index: Int)
  extends WidgetPanel
  with PaintableSwitch {

  override val channel = new PaintableSwitchChannel
  override val dragger = new PaintableSwitchDragger
  override def isOn: Boolean = true // TODO: get this from current frame

  add(dragger)
  add(channel)

}
