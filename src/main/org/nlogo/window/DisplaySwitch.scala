// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.event.{ ActionEvent, ActionListener }
import javax.swing.JCheckBox
import org.nlogo.api.I18N

class DisplaySwitch(workspace: GUIWorkspace)
    extends JCheckBox(I18N.gui.get("tabs.run.viewUpdates.checkbox")) with ActionListener {
  
  setFocusable(false)
  setToolTipText(I18N.gui.get("tabs.run.viewUpdates.checkbox.tooltip"))
  addActionListener(this)

  def actionPerformed(e: ActionEvent) = setOn(isSelected)

  def setOn(on: Boolean) = {
    if(on != isSelected)
      setSelected(on)
    if(on) {
      workspace.view.thaw()
      workspace.viewManager.incrementalUpdateFromEventThread()
    } else {
      workspace.view.freeze()
    }
  }
}
