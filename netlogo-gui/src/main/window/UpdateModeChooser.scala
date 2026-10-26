// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.EventQueue

import org.nlogo.api.AppInterface
import org.nlogo.core.{ I18N, UpdateMode }
import org.nlogo.swing.ComboBox

class UpdateModeChooser(app: AppInterface, workspace: GUIWorkspace) extends ComboBox[String] {
  implicit val prefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("tabs.run.viewUpdates")

  setToolTipText(I18N.gui("dropdown.tooltip"))

  setItems(List(I18N.gui("dropdown.continuous"), I18N.gui("dropdown.onticks")))

  addItemListener(_ => {
    getSelectedItem match {
      case Some(text) if text == I18N.gui("dropdown.continuous") =>
        workspace.updateMode(UpdateMode.Continuous)
      case _ =>
        workspace.updateMode(UpdateMode.TickBased)
    }

    if (EventQueue.isDispatchThread) {
      app.dirtyMonitor.setDirty(true)
    } else {
      EventQueue.invokeAndWait(() => {
        app.dirtyMonitor.setDirty(true)
      })
    }
  })

  def refreshSelection(): Unit = {
    workspace.updateMode() match {
      case UpdateMode.Continuous => setSelectedItem(I18N.gui("dropdown.continuous"))
      case UpdateMode.TickBased => setSelectedItem(I18N.gui("dropdown.onticks"))
    }
  }
}
