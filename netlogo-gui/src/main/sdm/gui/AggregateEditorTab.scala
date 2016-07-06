// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.awt.{ BorderLayout, Component }

import javax.swing.{ JPanel, JScrollPane, ScrollPaneConstants }

import org.nlogo.core.CompilerException
import org.nlogo.window.ErrorLabel

class AggregateEditorTab(toolbar: AggregateModelEditorToolBar, contents: Component) extends JPanel {
  private val errorLabel = new org.nlogo.window.ErrorLabel()

  locally {
    setAlignmentX(Component.LEFT_ALIGNMENT)
    setAlignmentY(Component.TOP_ALIGNMENT)
    setLayout(new BorderLayout)

    val toolbarPanel = new JPanel(new java.awt.BorderLayout)
    toolbarPanel.add(toolbar, BorderLayout.CENTER)
    toolbarPanel.add(errorLabel, BorderLayout.SOUTH)

    add(toolbarPanel, BorderLayout.NORTH)
    val scrollPane = new JScrollPane(contents,
      ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
    add(scrollPane, java.awt.BorderLayout.CENTER)
  }

  def setError(e: CompilerException, offset: Int): Unit = {
    errorLabel.setError(e, offset)
  }

  def clearError(): Unit = {
    errorLabel.setError(null, 0)
  }
}
