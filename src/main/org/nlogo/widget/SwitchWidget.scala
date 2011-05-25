package org.nlogo.widget

import org.nlogo.api.Editable
import org.nlogo.api.I18N
import org.nlogo.window.{Events, Widget, InterfaceGlobalWidget}

class SwitchWidget extends Switch with Editable with InterfaceGlobalWidget
  with org.nlogo.window.Events.PeriodicUpdateEvent.Handler {

  override def classDisplayName= I18N.gui.get("tabs.run.widgets.switch")
  override def propertySet = Properties.swiitch

  def valueObject: AnyRef = constraint.defaultValue
  def valueObject(value: AnyRef) {
    if (value.isInstanceOf[Boolean]) {
      isOn(value.asInstanceOf[Boolean])
    }
  }

  def nameWrapper = name()
  def nameWrapper(newName: String) {
    nameChanged = newName != name() || nameChanged
    name(newName, false)
  }

  override def editFinished(): Boolean = {
    super.editFinished()
    name(name(), nameChanged)
    updateConstraints()
    nameChanged = false
    true
  }

  def name(newName: String, sendEvent: Boolean) {
    super.name(newName)
    if (sendEvent) {
      new Events.InterfaceGlobalEvent(this, true, true, false, false).raise(this)
    }
  }

  override def isOn(on: Boolean) {
    if (on != super.isOn) {
      super.isOn(on)
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
    if ((null != name()) && (!name().trim.equals(""))) s.append(name() + "\n")
    else s.append("NIL\n")
    if (isOn) s.append(0 + "\n")
    else s.append(1 + "\n")
    s.append(1 + "\n")
    s.append(-1000 + "\n")
    s.toString
  }

  def load(strings: Array[String], helper: Widget.LoadHelper): AnyRef = {
    name(org.nlogo.api.File.restoreLines(strings(6)), true)
    isOn(strings(7).toDouble == 0)
    val Array(x1,y1,x2,y2) = strings.drop(1).take(4).map(_.toInt)
    setSize(x2 - x1, y2 - y1)
    this
  }

  def handle(e: Events.AfterLoadEvent) { updateConstraints() }
}