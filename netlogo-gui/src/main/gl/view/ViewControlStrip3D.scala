// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import java.awt.{ BorderLayout, FlowLayout }
import javax.swing.JPanel

import org.nlogo.swing.Transparent
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ DisplaySwitch, GUIWorkspace, SpeedSliderPanel, TickCounterLabel, ViewUpdatePanel }

class ViewControlStrip3D(workspace: GUIWorkspace, val tickCounter: TickCounterLabel)
  extends JPanel(new BorderLayout) with ThemeSync {

  val speedSlider = new SpeedSliderPanel(workspace, tickCounter)
  val displaySwitch = new DisplaySwitch(workspace)
  val controls = new ViewUpdatePanel(workspace, speedSlider, displaySwitch, tickCounter)

  add(new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0)) with Transparent {
    add(speedSlider)
    add(controls)
  }, BorderLayout.CENTER)

  updateTicks()

  def updateTicks() {
    val width = tickCounter.getMinimumSize.width
    // don't think this should be necessary but I couldn't get it to work otherwise ev 8/28/07
    if (width != getMinimumSize.width)
      doLayout()
  }

  def syncTheme(): Unit = {
    setBackground(InterfaceColors.TOOLBAR_BACKGROUND)

    displaySwitch.syncTheme()
    controls.syncTheme()
  }
}
