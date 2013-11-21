// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Color.BLACK
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON

import org.nlogo.mirror.AgentKey
import org.nlogo.mirror.WidgetKinds
import org.nlogo.mirror.ModelRun
import org.nlogo.window.InterfaceColors.MONITOR_BACKGROUND
import org.nlogo.window.MonitorPainter
import org.nlogo.swing.Utils.createWidgetBorder

class MonitorPanel(
  val panelBounds: java.awt.Rectangle,
  val originalFont: java.awt.Font,
  displayName: String,
  run: ModelRun,
  index: Int)
  extends WidgetPanel {
  
  val agentKey = AgentKey(WidgetKinds.Monitor, index)

  setBorder(createWidgetBorder)
  setBackground(MONITOR_BACKGROUND)

  override def paintComponent(g: Graphics): Unit = {
    g.asInstanceOf[Graphics2D].setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
    super.paintComponent(g)
    val value = for {
      frame <- run.currentFrame
      variables <- frame.mirroredState.get(agentKey)
    } yield variables(WidgetKinds.Monitor.Variables.ValueString.id).asInstanceOf[String]
    MonitorPainter.paint(g, getSize, BLACK, displayName, value.getOrElse(""))
  }
}