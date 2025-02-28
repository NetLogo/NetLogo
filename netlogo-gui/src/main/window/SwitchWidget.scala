// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.Editable
import org.nlogo.core.I18N
import org.nlogo.core.{ Switch => CoreSwitch }

class SwitchWidget extends Switch with Editable with InterfaceGlobalWidget
  with Events.PeriodicUpdateEvent.Handler {

  type WidgetModel = CoreSwitch

  override def classDisplayName = I18N.gui.get("tabs.run.widgets.switch")
  override def propertySet = Properties.switch

  def valueObject: AnyRef = constraint.defaultValue
  def valueObject(value: AnyRef) {
    if (value.isInstanceOf[Boolean]) {
      isOn = value.asInstanceOf[Boolean]
    }
  }

  def nameWrapper = this._name
  def nameWrapper(newName: String) {
    nameChanged = newName != this._name || nameChanged
    name(newName, false)
  }

  override def editFinished(): Boolean = {
    super.editFinished()
    name(this._name, nameChanged)
    updateConstraints()
    nameChanged = false
    true
  }

  def name(newName: String, sendEvent: Boolean) {
    super.name = newName
    if (sendEvent) {
      new Events.InterfaceGlobalEvent(this, true, true, false, false).raise(this)
    }
  }

  override def isOn_=(on: Boolean) {
    if (on != super.isOn) {
      super.isOn = on
      new Events.InterfaceGlobalEvent(this, false, false, true, false).raise(this)
    }
  }

  def handle(e: Events.PeriodicUpdateEvent) {
    new Events.InterfaceGlobalEvent(this, false, true, false, false).raise(this)
  }

  override def model: WidgetModel = {
    val b = getUnzoomedBounds
    val savedDisplayName =
      if (displayName != null && displayName != "") Some(displayName) else None
    val varName =
      if (_name != null && _name.trim != "") Some(_name) else None
    CoreSwitch(display = savedDisplayName,
      x = b.x, y = b.y, width = b.width, height = b.height,
      oldSize = _oldSize,
      variable = varName, on = isOn)
  }

  override def load(model: WidgetModel): AnyRef = {
    name(model.varName, true)
    isOn = model.on
    oldSize = model.oldSize
    setSize(model.width, model.height)
    this
  }

  def handle(e: Events.AfterLoadEvent) { updateConstraints() }
}
