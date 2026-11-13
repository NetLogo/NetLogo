// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.{ LocaleContext, UpdateMode }
import org.nlogo.swing.ComboBox

class UpdateModeChooser(workspace: GUIWorkspace) extends ComboBox[String] {
  setToolTipText(loc"change view update mode")

  setItems(List(loc"Continuous", loc"On Ticks"))

  addItemListener(_ => {
    getSelectedItem match {
      case Some(text) if text == loc"Continuous" =>
        workspace.updateMode(UpdateMode.Continuous)
      case _ =>
        workspace.updateMode(UpdateMode.TickBased)
    }

    new Events.UpdateModeChangedEvent().raiseOnEDT(workspace.getFrame)
  })

  def refreshSelection(): Unit = {
    workspace.updateMode() match {
      case UpdateMode.Continuous => setSelectedItem(loc"Continuous")
      case UpdateMode.TickBased => setSelectedItem(loc"On Ticks")
    }
  }
}
