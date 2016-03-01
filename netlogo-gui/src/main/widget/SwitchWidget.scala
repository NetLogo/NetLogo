// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.widget

import org.nlogo.api.Editable
import org.nlogo.core.I18N
import org.nlogo.window.{Events, Widget, InterfaceGlobalWidget}

class SwitchWidget extends Switch with Editable with InterfaceGlobalWidget
  with org.nlogo.window.Events.PeriodicUpdateEvent.Handler {

  override def classDisplayName= I18N.gui.get("tabs.run.widgets.switch")
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

  def save: String = {
    val s: StringBuilder = new StringBuilder
    s.append("SWITCH\n")
    s.append(getBoundsString)
    if ((null != displayName) && (!displayName.trim.equals(""))) s.append(displayName + "\n")
    else s.append("NIL\n")
    if ((null != this._name) && (!this._name.trim.equals(""))) s.append(this._name + "\n")
    else s.append("NIL\n")
    if (isOn) s.append(0 + "\n")
    else s.append(1 + "\n")
    s.append(1 + "\n")
    s.append(-1000 + "\n")
    s.toString
  }

  def load(strings: Array[String], helper: Widget.LoadHelper): AnyRef = {
    name(org.nlogo.api.ModelReader.restoreLines(strings(6)), true)
    isOn = strings(7).toDouble == 0
    val Array(x1,y1,x2,y2) = strings.drop(1).take(4).map(_.toInt)
    setSize(x2 - x1, y2 - y1)
    this
  }

  def handle(e: Events.AfterLoadEvent) { updateConstraints() }
}
