// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import org.nlogo.api.Dump
import org.nlogo.window.{ GUIWorkspace, ViewUpdatePanel }
import java.awt.BorderLayout
import javax.swing.JPanel

class ViewControlStrip3D(workspace: GUIWorkspace) extends JPanel {

  val tickCounter = new TickCounterLabel
  val displaySwitch = new org.nlogo.window.DisplaySwitch(workspace)
  updateTicks()
  val controls = new ViewUpdatePanel(workspace, displaySwitch, true)
  org.nlogo.awt.Fonts.adjustDefaultFont(tickCounter)
  setLayout(new java.awt.BorderLayout)
  add(tickCounter, BorderLayout.WEST)
  controls.setOpaque(false)
  add(controls, BorderLayout.CENTER)

  def updateTicks() {
    tickCounter.setVisible(workspace.viewWidget.showTickCounter)
    val width = tickCounter.getMinimumSize.width
    val ticks = workspace.world.tickCounter.ticks
    val tickText =
      if(ticks == -1) ""
      else Dump.number(math.floor(ticks))
    tickCounter.setText(
      "     " + workspace.viewWidget.tickCounterLabel + ": " + tickText)
    // don't think this should be necessary but I couldn't get it to work otherwise ev 8/28/07
    if(width != getMinimumSize.width)
      doLayout()
  }

  class TickCounterLabel extends javax.swing.JLabel {
    override def getPreferredSize = getMinimumSize
    override def getMinimumSize = {
      val d = super.getMinimumSize
      val fontMetrics = getFontMetrics(getFont)
      d.width = d.width max fontMetrics.stringWidth(
        workspace.viewWidget.tickCounterLabel + ": 00000000")
      d
    }
  }

}
