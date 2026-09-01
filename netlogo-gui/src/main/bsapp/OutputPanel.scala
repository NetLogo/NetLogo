// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.bsapp

import javax.swing.{ Box, JLabel }

import org.nlogo.agent.OutputObject
import org.nlogo.core.I18N
import org.nlogo.editor.EditorConfiguration
import org.nlogo.swing.{ BoxColumn, BoxRow, Button, ZoomableBorder }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.OutputArea

// this class is a variant of the command center that allows output but doesn't allow
// any user input that could mess up the model while an experiment is running. (Isaac B 2/6/25)
class OutputPanel extends BoxColumn(6) with ThemeSync {
  private val label = new JLabel(s"<html><b>${I18N.gui.get("tabs.run.commandcenter")}</b></html>")

  private val clearButton = new Button(I18N.gui.get("tabs.run.commandcenter.clearButton"), clear)

  private val outputArea = new OutputArea(new OutputArea.DefaultTextArea) {
    setFont(EditorConfiguration.getCodeFont)
  }

  setOpaque(true)
  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(new BoxRow(Seq(
    label,
    Box.createHorizontalGlue,
    clearButton
  )))

  add(outputArea)

  def clear(): Unit = {
    outputArea.clear()
  }

  def append(oo: OutputObject, wrapLines: Boolean): Unit = {
    outputArea.append(oo, wrapLines)
  }

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.commandCenterBackground())

    label.setForeground(InterfaceColors.commandCenterText())

    clearButton.syncTheme()
    outputArea.syncTheme()
  }
}
