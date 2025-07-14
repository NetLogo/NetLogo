// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import javax.swing.JTextArea
import javax.swing.text.{ AttributeSet, DocumentFilter, PlainDocument }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class TextArea(rows: Int, columns: Int, text: String = "")
  extends JTextArea(text, rows, columns) with ThemeSync {

  private val TabSize = 4

  getDocument.asInstanceOf[PlainDocument].setDocumentFilter(new DocumentFilter {
    override def insertString(bypass: DocumentFilter.FilterBypass, offset: Int, text: String,
                              attributes: AttributeSet): Unit = {
      if (text == null) {
        super.insertString(bypass, offset, text, attributes)
      } else {
        super.insertString(bypass, offset, text.replace("\t", " " * TabSize), attributes)
      }
    }

    override def replace(bypass: DocumentFilter.FilterBypass, offset: Int, length: Int, text: String,
                         attributes: AttributeSet): Unit = {
      if (text == null) {
        super.replace(bypass, offset, length, text, attributes)
      } else {
        val replaced = text.replace("\t", " " * TabSize)

        super.replace(bypass, offset, length, replaced, attributes)
      }
    }
  })

  TextActions.applyToComponent(this)

  syncTheme()

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.textAreaBackground())
    setForeground(InterfaceColors.textAreaText())
    setCaretColor(InterfaceColors.textAreaText())
  }
}
