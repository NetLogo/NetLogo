// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.{ I18N, Switch => CoreSwitch, Widget => CoreWidget }

class DummySwitchWidget extends Switch with Editable {
  override def classDisplayName = I18N.gui.get("tabs.run.widgets.switch")

  override def editPanel: EditPanel = new DummySwitchEditPanel(this)

  // we never update constraints in a dummy widget -- CLB
  override def updateConstraints(): Unit = {}

  // do nothing
  def handle(e: Events.AfterLoadEvent): Unit = {}

  /// load and save
  override def load(model: CoreWidget): Unit = {
    model match {
      case switch: CoreSwitch =>
        super.setVarName(switch.varName)
        isOn = switch.on
        oldSize(switch.oldSize)
        setSize(switch.width, switch.height)

      case _ =>
    }
  }

  override def model: CoreWidget = {
    val b = getUnzoomedBounds
    val varName = if (_name != null && _name.trim != "") Some(_name) else None
    CoreSwitch(display = varName,
      x = b.x, y = b.y, width = b.width, height = b.height,
      oldSize = _oldSize,
      variable = varName, on = isOn)
  }
}
