// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.widget

import org.nlogo.api.{Editable, I18N}
import org.nlogo.window.Widget

class DummySwitchWidget extends Switch with Editable {

  override def classDisplayName = I18N.gui.get("tabs.run.widgets.switch")
  def propertySet = Properties.dummySwitch

  // we never update constraints in a dummy widget -- CLB
  override def updateConstraints() {}

  // do nothing
  def handle(e:org.nlogo.window.Events.AfterLoadEvent) {}

  /// load and save
  override def load(strings:Array[String], helper:Widget.LoadHelper) = {
    super.name = (org.nlogo.api.ModelReader.restoreLines(strings(6)))
    isOn = strings(7).toDouble == 0
    val Array(x1,y1,x2,y2) = strings.drop(1).take(4).map(_.toInt)
    setSize(x2 - x1, y2 - y1)
    this
  }

  override def save: String = {
    val s = new StringBuilder
    s.append("SWITCH\n")
    s.append(getBoundsString)
    if ((null != displayName) && (displayName.trim!="")) s.append(displayName + "\n")
    else s.append("NIL\n")
    if ((null != name) && (name.trim != "")) s.append(name + "\n")
    else s.append("NIL\n")
    if (isOn) s.append(0 + "\n") else s.append(1 + "\n")
    s.append(1 + "\n")  // for compatibility
    s.append(-1000 + "\n") // for compatibility
    s.toString
  }
}
