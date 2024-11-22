// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Insets
import java.awt.event.{ FocusEvent, FocusListener, MouseAdapter, MouseEvent }
import javax.swing.JTextField
import javax.swing.border.LineBorder
import javax.swing.text.Document

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class TextField(document: Document, text: String, columns: Int)
  extends JTextField(document, text, columns) with ThemeSync {
  
  def this(text: String, columns: Int) = this(null, text, columns)
  def this(columns: Int) = this(null, null, columns)
  def this() = this(null, null, 0)

  private var mouseEvent = false

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent) {
      mouseEvent = true
    }
  })

  addFocusListener(new FocusListener {
    def focusGained(e: FocusEvent) {
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

    def focusLost(e: FocusEvent) {
      mouseEvent = e.isTemporary
    }
  })

  syncTheme()

  override def getInsets: Insets =
    new Insets(0, 3, 0, 0)

  def syncTheme() {
    setBackground(InterfaceColors.TEXT_AREA_BACKGROUND)
    setForeground(InterfaceColors.TEXT_AREA_TEXT)
    setCaretColor(InterfaceColors.TEXT_AREA_TEXT)
    setBorder(new LineBorder(InterfaceColors.TEXT_AREA_BORDER_EDITABLE))
  }
}
