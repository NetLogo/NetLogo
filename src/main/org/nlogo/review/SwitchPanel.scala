// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Graphics

import org.nlogo.mirror.ModelRun
import org.nlogo.mirror.WidgetKinds.Switch
import org.nlogo.mirror.WidgetKinds.Switch.Variables.IsOn
import org.nlogo.widget.PaintableSwitch
import org.nlogo.widget.PaintableSwitchChannel
import org.nlogo.widget.PaintableSwitchDragger

class SwitchPanel(
  val panelBounds: java.awt.Rectangle,
  val originalFont: java.awt.Font,
  val displayName: String,
  val run: ModelRun,
  val index: Int)
  extends WidgetPanel
  with PaintableSwitch
  with MirroredWidget {

  override val kind = Switch
  override val channel = new PaintableSwitchChannel
  override val dragger = new PaintableSwitchDragger
  override def isOn: Boolean = mirroredVar[Boolean](IsOn.id).getOrElse(false)

  add(dragger)
  add(channel)
}
