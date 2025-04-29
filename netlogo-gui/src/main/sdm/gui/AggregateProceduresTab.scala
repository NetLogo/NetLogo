// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.awt.{ GridBagConstraints, GridBagLayout }
import javax.swing.{ JPanel, ScrollPaneConstants }

import org.nlogo.core.CompilerException
import org.nlogo.editor.{ Colorizer, EditorArea, EditorConfiguration }
import org.nlogo.swing.ScrollPane
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.EditorAreaErrorLabel

class AggregateProceduresTab(colorizer: Colorizer) extends JPanel(new GridBagLayout) with ThemeSync {
  private val editorConfiguration = EditorConfiguration.default(50, 75, colorizer)
  private val text = new EditorArea(editorConfiguration)

  text.setEditable(false)

  private val errorLabel = new EditorAreaErrorLabel(text)

  private val scrollableEditor = new ScrollPane(text, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1

    add(errorLabel, c)

    c.fill = GridBagConstraints.BOTH
    c.weighty = 1

    add(scrollableEditor, c)
  }

  def setError(e: CompilerException, offset: Int): Unit = {
    errorLabel.setError(e, offset)
  }

  def clearError(): Unit = {
    errorLabel.setError(null, 0)
  }

  def setText(newText: String): Unit = {
    text.setText(newText)
  }

  override def syncTheme(): Unit = {
    text.setBackground(InterfaceColors.codeBackground())
    text.setCaretColor(InterfaceColors.textAreaText())

    scrollableEditor.setBackground(InterfaceColors.codeBackground())
  }
}
