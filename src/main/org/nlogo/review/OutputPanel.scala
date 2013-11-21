// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.BorderLayout

import org.nlogo.mirror.ModelRun
import org.nlogo.mirror.WidgetKinds.Output
import org.nlogo.mirror.WidgetKinds.Output.Variables._
import org.nlogo.swing.Utils.createWidgetBorder
import org.nlogo.window.InterfaceColors.MONITOR_BACKGROUND
import org.nlogo.window.OutputArea

import javax.swing.BorderFactory.createEmptyBorder
import javax.swing.JPanel

class OutputPanel(
  val panelBounds: java.awt.Rectangle,
  val originalFont: java.awt.Font,
  val displayName: String,
  val run: ModelRun,
  val index: Int)
  extends WidgetPanel
  with MirroredWidget {

  override val kind = Output
  
  setLayout(new java.awt.BorderLayout())
  setBorder(createWidgetBorder)
  setBackground(MONITOR_BACKGROUND)

  override def doLayout() {
    super.doLayout()
    outputArea.text.setText(mirroredVar[String](Text.id).getOrElse(""))
    outputArea.scrollToEnd()
  }

  val outputArea = new OutputArea
  val outputAreaPanel = new JPanel {
    setLayout(new java.awt.BorderLayout())
    setBackground(MONITOR_BACKGROUND)
    setBorder(createEmptyBorder(2, 2, 2, 2))
    add(outputArea, BorderLayout.CENTER)
  }

  add(outputAreaPanel, BorderLayout.CENTER)
}
