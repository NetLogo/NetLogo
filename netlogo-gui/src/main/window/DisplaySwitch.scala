// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.event.{ ActionEvent, ActionListener }

import org.nlogo.core.LocaleContext
import org.nlogo.swing.CheckBox

class DisplaySwitch(workspace: GUIWorkspace) extends CheckBox(loc"View Updates") with ActionListener {
  setToolTipText(loc"Freeze or unfreeze view")

  addActionListener(this)

  def actionPerformed(e: ActionEvent): Unit = {
    setOn(isSelected)
  }

  def setOn(on: Boolean): Unit = {
    if (on != isSelected)
      setSelected(on)

    if (on) {
      workspace.view.thaw()
      workspace.viewManager.incrementalUpdateFromEventThread()
    } else {
      workspace.view.freeze()
    }
  }
}
