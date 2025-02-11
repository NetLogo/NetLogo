// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Insets
import java.awt.event.{ FocusEvent, FocusListener, MouseAdapter, MouseEvent }
import javax.swing.JTextField
import javax.swing.border.LineBorder
import javax.swing.text.Document

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class TextField(columns: Int = 0, text: String = null, document: Document = null)
  extends JTextField(document, text, columns) with ThemeSync {

  private var mouseEvent = false

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = {
      mouseEvent = true
    }
  })

  addFocusListener(new FocusListener {
    def focusGained(e: FocusEvent): Unit = {
      if (!mouseEvent) {
        // this is like selectAll(), but it leaves the
        // caret at the beginning rather than the start;
        // this prevents the enclosing scrollpane from
        // scrolling to the end to make the caret
        // visible; it's nicer to keep the scroll at the
        // start - ST 12/20/04
        setCaretPosition(getText.size)
        moveCaretPosition(0)
      }
    }

    def focusLost(e: FocusEvent): Unit = {
      mouseEvent = e.isTemporary
    }
  })

  syncTheme()

  override def getInsets: Insets =
    new Insets(0, 3, 0, 0)

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.textAreaBackground)
    setForeground(InterfaceColors.textAreaText)
    setCaretColor(InterfaceColors.textAreaText)
    setBorder(new LineBorder(InterfaceColors.textAreaBorderEditable))
  }
}
