// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.Editable
import org.nlogo.core.{ I18N, Switch => CoreSwitch }

class DummySwitchWidget extends Switch with Editable {

  type WidgetModel = CoreSwitch

  override def classDisplayName = I18N.gui.get("tabs.run.widgets.switch")
  def propertySet = Properties.dummySwitch

  // we never update constraints in a dummy widget -- CLB
  override def updateConstraints() {}

  // do nothing
  def handle(e: Events.AfterLoadEvent) {}

  /// load and save
  override def load(model: WidgetModel): AnyRef = {
    super.name = model.varName
    isOn = model.on
    oldSize = model.oldSize
    setSize(model.width, model.height)
    this
  }

  override def model: WidgetModel = {
    val b = getUnzoomedBounds
    val varName = if (_name != null && _name.trim != "") Some(_name) else None
    CoreSwitch(display = varName,
      x = b.x, y = b.y, width = b.width, height = b.height,
      oldSize = _oldSize,
      variable = varName, on = isOn)
  }
}
