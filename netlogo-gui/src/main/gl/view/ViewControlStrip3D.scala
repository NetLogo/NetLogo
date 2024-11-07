// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import java.awt.BorderLayout
import javax.swing.JPanel

import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ DisplaySwitch, GUIWorkspace, TickCounterLabel, ViewUpdatePanel }

class ViewControlStrip3D(workspace: GUIWorkspace, val tickCounter: TickCounterLabel)
  extends JPanel(new BorderLayout) with ThemeSync {

  val displaySwitch = new DisplaySwitch(workspace)
  val controls = new ViewUpdatePanel(workspace, displaySwitch, tickCounter)

  add(controls, BorderLayout.CENTER)

  updateTicks()

  def updateTicks() {
    val width = tickCounter.getMinimumSize.width
    // don't think this should be necessary but I couldn't get it to work otherwise ev 8/28/07
    if (width != getMinimumSize.width)
      doLayout()
  }

  def syncTheme() {
    setBackground(InterfaceColors.TOOLBAR_BACKGROUND)

    displaySwitch.syncTheme()
    controls.syncTheme()
  }
}
