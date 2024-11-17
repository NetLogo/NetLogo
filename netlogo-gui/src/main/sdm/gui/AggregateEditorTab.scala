// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.awt.{ BorderLayout, Component }

import javax.swing.{ JPanel, JScrollPane, ScrollPaneConstants }

import org.nlogo.core.CompilerException
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.ErrorLabel

class AggregateEditorTab(toolbar: AggregateModelEditorToolBar, contents: Component) extends JPanel with ThemeSync {
  private val errorLabel = new ErrorLabel()

  private val scrollPane = new JScrollPane(contents, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                           ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)

  locally {
    setAlignmentX(Component.LEFT_ALIGNMENT)
    setAlignmentY(Component.TOP_ALIGNMENT)
    setLayout(new BorderLayout)

    val toolbarPanel = new JPanel(new BorderLayout)
    toolbarPanel.add(toolbar, BorderLayout.CENTER)
    toolbarPanel.add(errorLabel, BorderLayout.SOUTH)

    add(toolbarPanel, BorderLayout.NORTH)
    add(scrollPane, BorderLayout.CENTER)
  }

  def setError(e: CompilerException, offset: Int): Unit = {
    errorLabel.setError(e, offset)
  }

  def clearError(): Unit = {
    errorLabel.setError(null, 0)
  }

  def syncTheme() {
    scrollPane.getHorizontalScrollBar.setBackground(InterfaceColors.INTERFACE_BACKGROUND)
    scrollPane.getVerticalScrollBar.setBackground(InterfaceColors.INTERFACE_BACKGROUND)
  }
}
