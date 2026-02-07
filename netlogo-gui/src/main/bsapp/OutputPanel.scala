// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.bsapp

import javax.swing.{ Box, BoxLayout, JLabel, JPanel }
import javax.swing.border.EmptyBorder

import org.nlogo.agent.OutputObject
import org.nlogo.core.I18N
import org.nlogo.swing.{ Button, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.OutputArea

// this class is a variant of the command center that allows output but doesn't allow
// any user input that could mess up the model while an experiment is running. (Isaac B 2/6/25)
class OutputPanel extends JPanel with ThemeSync {
  private val label = new JLabel(s"<html><b>${I18N.gui.get("tabs.run.commandcenter")}</b></html>")

  private val clearButton = new Button(I18N.gui.get("tabs.run.commandcenter.clearButton"), clear)

  private val outputArea = new OutputArea(new OutputArea.DefaultTextArea)

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new EmptyBorder(6, 6, 6, 6))

  add(new JPanel with Transparent {
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

    add(label)
    add(Box.createHorizontalGlue)
    add(clearButton)
  })

  add(Box.createVerticalStrut(6))
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
