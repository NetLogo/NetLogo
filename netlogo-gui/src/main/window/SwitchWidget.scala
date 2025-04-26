// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.lang.{ Boolean => JBoolean }

import org.nlogo.api.CompilerServices
import org.nlogo.core.{ I18N, Switch => CoreSwitch, Widget => CoreWidget }

class SwitchWidget(compiler: CompilerServices) extends Switch with Editable with InterfaceGlobalWidget
  with Events.PeriodicUpdateEvent.Handler {

  override def classDisplayName = I18N.gui.get("tabs.run.widgets.switch")

  override def editPanel: EditPanel = new SwitchEditPanel(this, compiler)

  def valueObject: AnyRef = constraint.defaultValue
  def valueObject(value: AnyRef): Unit = {
    value match {
      case b: JBoolean =>
        isOn = b

      case _ =>
    }
  }

  def nameWrapper = this._name
  def setNameWrapper(newName: String): Unit = {
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

  def name(newName: String, sendEvent: Boolean): Unit = {
    super.setVarName(newName)
    if (sendEvent) {
      new Events.InterfaceGlobalEvent(this, true, true, false, false).raise(this)
    }
  }

  override def isOn_=(on: Boolean): Unit = {
    if (on != super.isOn) {
      super.isOn = on
      new Events.InterfaceGlobalEvent(this, false, false, true, false).raise(this)
    }
  }

  def handle(e: Events.PeriodicUpdateEvent): Unit = {
    new Events.InterfaceGlobalEvent(this, false, true, false, false).raise(this)
  }

  override def model: CoreWidget = {
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

  override def load(model: CoreWidget): Unit = {
    model match {
      case switch: CoreSwitch =>
        name(switch.varName, true)
        isOn = switch.on
        oldSize(switch.oldSize)
        setSize(switch.width, switch.height)

      case _ =>
    }
  }

  def handle(e: Events.AfterLoadEvent): Unit = { updateConstraints() }
}
