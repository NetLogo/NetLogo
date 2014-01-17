// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Color.BLACK
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON

import org.nlogo.mirror.ModelRun
import org.nlogo.mirror.WidgetKinds.Monitor
import org.nlogo.mirror.WidgetKinds.Monitor.Variables.ValueString
import org.nlogo.swing.Utils.createWidgetBorder
import org.nlogo.window.InterfaceColors.MONITOR_BACKGROUND
import org.nlogo.window.MonitorPainter

class MonitorPanel(
  val panelBounds: java.awt.Rectangle,
  val originalFont: java.awt.Font,
  displayName: String,
  val run: ModelRun,
  val index: Int)
  extends WidgetPanel
  with MirroredWidget {

  override val kind = Monitor

  setBorder(createWidgetBorder)
  setOpaque(true)
  setBackground(MONITOR_BACKGROUND)

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    val value = mirroredVar[String](ValueString.id).getOrElse("")
    MonitorPainter.paint(g, getSize, BLACK, displayName, value)
  }
}
