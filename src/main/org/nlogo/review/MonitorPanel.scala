// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Color.BLACK
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON

import org.nlogo.mirror.AgentKey
import org.nlogo.mirror.Mirrorables.MirrorableWidgetValue.wvvValueString
import org.nlogo.mirror.Mirrorables.WidgetValue
import org.nlogo.mirror.ModelRun
import org.nlogo.window.InterfaceColors.MONITOR_BACKGROUND
import org.nlogo.window.MonitorPainter

class MonitorPanel(
  val panelBounds: java.awt.Rectangle,
  val originalFont: java.awt.Font,
  displayName: String,
  run: ModelRun,
  index: Int)
  extends WidgetPanel {
  setBackground(MONITOR_BACKGROUND)

  override def paintComponent(g: Graphics): Unit = {
    g.asInstanceOf[Graphics2D].setRenderingHint(
      KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
    super.paintComponent(g)
    for {
      frame <- run.currentFrame
      variables <- frame.mirroredState.get(AgentKey(WidgetValue, index))
      value = variables(wvvValueString).asInstanceOf[String]
    } {
      MonitorPainter.paint(
        g, getSize, BLACK, displayName, value)
    }
  }
}