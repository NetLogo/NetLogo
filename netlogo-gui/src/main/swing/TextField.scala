// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Graphics, Insets }
import java.awt.event.{ ActionEvent, FocusEvent, FocusListener, KeyEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, JTextField, KeyStroke }
import javax.swing.border.LineBorder
import javax.swing.text.Document
import javax.swing.undo.UndoManager

import org.nlogo.core.I18N
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class TextField(columns: Int = 0, text: String = null, document: Document = null)
  extends JTextField(document, text, columns) with ThemeSync {

  private val undoManager = new UndoManager

  private var mouseEvent = false

  TextActions.applyToComponent(this)

  getDocument.addUndoableEditListener(undoManager)

  getKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Z, getToolkit.getMenuShortcutKeyMaskEx),
                                  new UndoAction)

  getKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Y, getToolkit.getMenuShortcutKeyMaskEx),
                                  new RedoAction)

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

  override def paintComponent(g: Graphics): Unit = {
    if (isEnabled) {
      setBackground(InterfaceColors.textAreaBackground())
    } else {
      setBackground(InterfaceColors.Transparent)
    }

    super.paintComponent(g)
  }

  override def syncTheme(): Unit = {
    setForeground(InterfaceColors.textAreaText())
    setCaretColor(InterfaceColors.textAreaText())
    setBorder(new LineBorder(InterfaceColors.textAreaBorderEditable()))
  }

  private class UndoAction extends AbstractAction(I18N.gui.get("menu.edit.undo")) {
    override def actionPerformed(e: ActionEvent): Unit = {
      if (undoManager.canUndo)
        undoManager.undo()
    }
  }

  private class RedoAction extends AbstractAction(I18N.gui.get("menu.edit.redo")) {
    override def actionPerformed(e: ActionEvent): Unit = {
      if (undoManager.canRedo)
        undoManager.redo()
    }
  }
}
