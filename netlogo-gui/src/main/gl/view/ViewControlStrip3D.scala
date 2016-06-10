// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import org.nlogo.api.Dump
import org.nlogo.window.{ GUIWorkspace, TickCounterLabel, ViewUpdatePanel }
import java.awt.BorderLayout
import javax.swing.JPanel

class ViewControlStrip3D(workspace: GUIWorkspace, val tickCounter: TickCounterLabel) extends JPanel {

  val displaySwitch = new org.nlogo.window.DisplaySwitch(workspace)
  updateTicks()
  val controls = new ViewUpdatePanel(workspace, displaySwitch, true, tickCounter)
  org.nlogo.awt.Fonts.adjustDefaultFont(tickCounter)
  setLayout(new java.awt.BorderLayout)
  add(controls, BorderLayout.CENTER)

  def updateTicks() {
    val width = tickCounter.getMinimumSize.width
    // don't think this should be necessary but I couldn't get it to work otherwise ev 8/28/07
    if (width != getMinimumSize.width)
      doLayout()
  }
}
