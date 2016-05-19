// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.widget

import org.nlogo.api.Editable
import org.nlogo.core.{ I18N, Switch => CoreSwitch }
import org.nlogo.window.Widget

class DummySwitchWidget extends Switch with Editable {

  type WidgetModel = CoreSwitch

  override def classDisplayName = I18N.gui.get("tabs.run.widgets.switch")
  def propertySet = Properties.dummySwitch

  // we never update constraints in a dummy widget -- CLB
  override def updateConstraints() {}

  // do nothing
  def handle(e:org.nlogo.window.Events.AfterLoadEvent) {}

  /// load and save
  override def load(model: WidgetModel): AnyRef = {
    super.name = model.varName
    isOn = model.on
    setSize(model.right - model.left, model.bottom - model.top)
    this
  }

  override def model: WidgetModel = {
    val b = getBoundsTuple
    val varName = if (_name != null && _name.trim != "") Some(_name) else None
    CoreSwitch(display = varName,
      left = b._1, top = b._2, right = b._3, bottom = b._4,
      variable = varName, on = isOn)
  }
}
