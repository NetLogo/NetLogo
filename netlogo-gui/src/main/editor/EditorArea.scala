// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

// the code in this package is based on the sample code sketched out
// in the second half of this page:
// http://www.cs.bris.ac.uk/Teaching/Resources/COMS30122/tools/highlight/design.html
// see also http://www.cs.bris.ac.uk/Teaching/Resources/COMS30122/tools/index.html
// for related editor stuff - ST 6/28/03

package org.nlogo.editor

import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.KeyStroke
import javax.swing.text.DefaultEditorKit
import javax.swing.text.TextAction
import javax.swing.text.PlainDocument
import javax.swing.text.BadLocationException
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.datatransfer.DataFlavor
import java.awt.event.ActionEvent
import java.awt.event.FocusListener
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.Component


object EditorArea {
  def emptyMap = Map[KeyStroke, TextAction]()
  def defaultSize = new java.awt.Dimension(400, 400)
}

import EditorArea._

class EditorArea(
  rows: Int,
  columns: Int,
  font: java.awt.Font,
  enableFocusTraversalKeys: Boolean,
  listener: java.awt.event.TextListener,
  colorizer: Colorizer,
  i18n: String => String,
  actionMap: Map[KeyStroke, TextAction] = EditorArea.emptyMap)
  extends AbstractEditorArea
   with java.awt.event.FocusListener {

  private var indenter: IndenterInterface = new DumbIndenter(this)
  private var contextMenu: JPopupMenu = editorContextMenu(colorizer, i18n)
  private val bracketMatcher = new BracketMatcher(colorizer)
  private val undoManager: UndoManager = new UndoManager()
  private val caret = new DoubleClickCaret(colorizer, bracketMatcher)

  locally {
    import java.awt.event.{ KeyEvent => Key }
    import InputEvent.{ SHIFT_MASK => ShiftKey }
    enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK)
    addFocusListener(this)
    addCaretListener(bracketMatcher)
    val blinkRate: Int = getCaret.getBlinkRate
    // I don't really understand why, but if we don't set the blink rate,
    // it doesn't blink, even though the normal caret does - ST 6/9/04
    caret.setBlinkRate(blinkRate)
    setCaret(caret)
    setDragEnabled(false)
    setFocusTraversalKeysEnabled(enableFocusTraversalKeys)
    if (enableFocusTraversalKeys) {
      getInputMap.put(keystroke(Key.VK_TAB),           new TransferFocusAction())
      getInputMap.put(keystroke(Key.VK_TAB, ShiftKey), new TransferFocusBackwardAction())
    } else {
      getInputMap.put(keystroke(Key.VK_TAB),           Actions.tabKeyAction)
      getInputMap.put(keystroke(Key.VK_TAB, ShiftKey), Actions.shiftTabKeyAction)
    }
    setFont(font)
    setEditorKit(new HighlightEditorKit(listener, colorizer))
    getInputMap.put(keystroke(Key.VK_ENTER), new EnterAction())

    getInputMap.put(charKeystroke(']'), new CloseBracketAction())
    getDocument.putProperty(PlainDocument.tabSizeAttribute, Int.box(2))

    // on Windows, prevent save() from outputting ^M characters - ST 2/23/04
    getDocument.putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n")
    getDocument.addUndoableEditListener(undoManager)

    // add key bindings for undo and redo so they work even in modal dialogs
    val mask: Int = getToolkit.getMenuShortcutKeyMask
    getKeymap.addActionForKeyStroke(keystroke(Key.VK_Z, mask), UndoManager.undoAction())
    getKeymap.addActionForKeyStroke(keystroke(Key.VK_Y, mask), UndoManager.redoAction())

    // add key binding, for getting quick "contexthelp", based on where
    // the cursor is...
    getInputMap.put(keystroke(Key.VK_F1, 0), Actions.quickHelpAction(colorizer, i18n))
    actionMap.foreach {
      case (k, v) => getInputMap.put(k, v)
    }
  }

  def charKeystroke(char: Char, mask: Int = 0): KeyStroke =
    KeyStroke.getKeyStroke(Character.valueOf(char), mask)

  def keystroke(key: Int, mask: Int = 0): KeyStroke =
    KeyStroke.getKeyStroke(key, mask)

  override def paintComponent(g: Graphics): Unit = {
    val g2d = g.asInstanceOf[Graphics2D]
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    super.paintComponent(g)
  }

  private var bracketMatcherEnabled: Boolean = true

  override def enableBracketMatcher(enabled: Boolean): Unit = {
    if (bracketMatcherEnabled!= enabled) {
      if (enabled)
        addCaretListener(bracketMatcher)
      else
        removeCaretListener(bracketMatcher)
      bracketMatcherEnabled = enabled
    }
  }

  def setIndenter(newIndenter: IndenterInterface): Unit = {
    indenter = newIndenter
  }

  class EnterAction extends TextAction("enter") {
    def actionPerformed(e: ActionEvent): Unit = {
      indenter.handleEnter()
    }
  }

  class CloseBracketAction extends TextAction("close-bracket") {
    def actionPerformed(e: ActionEvent): Unit = {
      replaceSelection("]")
      indenter.handleCloseBracket()
    }
  }

  override def getActions: Array[Action] =
    TextAction.augmentList(super.getActions,
      Array[Action](
        Actions.commentAction, Actions.uncommentAction,
        Actions.shiftLeftAction, Actions.shiftRightAction,
        Actions.quickHelpAction(colorizer, i18n)))

  override def getPreferredScrollableViewportSize: Dimension = {
    val dimension =
      Option(super.getPreferredScrollableViewportSize).getOrElse(defaultSize)
    dimension.width  = if (columns != 0) columns * getColumnWidth else dimension.width
    dimension.height = if (rows != 0)    rows    * getRowHeight   else dimension.height
    dimension
  }

  override def getPreferredSize: Dimension = {
    val dimension =
      Option(super.getPreferredSize).getOrElse(defaultSize)

    if (columns != 0) { dimension.width  = dimension.width  max (columns * getColumnWidth) }
    if (rows != 0)    { dimension.height = dimension.height max (rows    * getRowHeight) }

    dimension
  }

  private def getColumnWidth: Int =
    getFontMetrics(getFont).charWidth('m')

  private def getRowHeight: Int =
    return getFontMetrics(getFont).getHeight

  override def setText(text: String): Unit =
    // not sure if this really needed - ST 8/27/03
    if (text != getText()) {
      super.setText(text)
      undoManager.discardAllEdits()
    }

  @throws(classOf[BadLocationException])
  def getLineText(offset: Int): String = {
    val doc         = getDocument.asInstanceOf[PlainDocument]
    val currentLine = offsetToLine(doc, offset)
    val lineStart   = lineToStartOffset(doc, currentLine)
    val lineEnd     = lineToEndOffset(doc, currentLine)
    doc.getText(lineStart, lineEnd - lineStart)
  }

  def indentSelection(): Unit = {
    indenter.handleTab()
  }

  def lineToStartOffset(doc: PlainDocument, line: Int): Int =
    doc.getDefaultRootElement.getElement(line).getStartOffset

  def lineToEndOffset(doc: PlainDocument, line: Int): Int =
    doc.getDefaultRootElement.getElement(line).getEndOffset

  def offsetToLine(doc: PlainDocument, offset: Int): Int =
    doc.getDefaultRootElement.getElementIndex(offset)

  private def currentSelectionProperties: (PlainDocument, Int, Int) = {
    val doc = getDocument.asInstanceOf[PlainDocument]
    var currentLine = offsetToLine(doc, getSelectionStart)
    var endLine     = offsetToLine(doc, getSelectionEnd)
    // The two following cases are to take care of selections that include
    // only the very edge of a line of text, either at the top or bottom
    // of the selection.  Because these lines do not have *any* highlighted
    // text, it does not make sense to modify these lines. ~Forrest (9/22/2006)
    if (endLine > currentLine &&
        getSelectionEnd == lineToStartOffset(doc, endLine)) {
      endLine -= 1
    }
    if (endLine > currentLine &&
        getSelectionStart == (lineToEndOffset(doc, currentLine) - 1)) {
      currentLine += 1
    }
    (doc, currentLine, endLine)
  }

  def insertBeforeEachSelectedLine(insertion: String): Unit = {
    try {
      val (doc, currentLine, endLine) = currentSelectionProperties

      for (line <- currentLine to endLine) {
        doc.insertString(lineToStartOffset(doc, line), insertion, null)
      }
    } catch {
      case ex: BadLocationException => throw new IllegalStateException(ex)
    }
  }

  def uncomment(): Unit = {
    try {
      val (doc, currentLine, endLine) = currentSelectionProperties

      for (line <- currentLine to endLine) {
        val lineStart = lineToStartOffset(doc, line)
        val lineEnd   = lineToEndOffset(doc, line)
        val text      = doc.getText(lineStart, lineEnd - lineStart)
        val semicolonPos = text.indexOf(';')
        if (semicolonPos != -1) {
          val allSpaces = (0 until semicolonPos)
            .forall(i => Character.isWhitespace(text.charAt(i)))
          if (allSpaces)
            doc.remove(lineStart + semicolonPos, 1)
        }
      }
    } catch {
      case ex: BadLocationException => throw new IllegalStateException(ex)
    }
  }

  def shiftLeft(): Unit = {
    try {
      val (doc, currentLine, endLine) = currentSelectionProperties

      for (line <- currentLine to endLine) {
        val lineStart = lineToStartOffset(doc, line)
        val lineEnd   = lineToEndOffset(doc, line)
        val text      = doc.getText(lineStart, lineEnd - lineStart)
        if (text.length > 0 && text.charAt(0) == ' ')
          doc.remove(lineStart, 1)
      }
    } catch {
      case ex: BadLocationException => throw new IllegalStateException(ex)
    }
  }


  /// select-all-on-focus stuff copied from org.nlogo.swing.TextField

  private var mouseEvent = false

  private var selectionActive = true

  def setSelection(s: Boolean): Unit = {
    selectionActive = s
  }

  def focusGained(fe: java.awt.event.FocusEvent): Unit = {
    if (!mouseEvent && enableFocusTraversalKeys && selectionActive) {
      // this is like selectAll(), but it leaves the
      // caret at the beginning rather than the start;
      // this prevents the enclosing scrollpane from
      // scrolling to the end to make the caret
      // visible; it's nicer to keep the scroll at the
      // start - ST 12/20/04
      setCaretPosition(getText().length)
      moveCaretPosition(0)
    }
    Actions.setEnabled(true)
    UndoManager.setCurrentManager(undoManager)
  }

  def focusLost(fe: java.awt.event.FocusEvent): Unit = {
    // On Windows (and perhaps Linux? not sure), putting
    // the focus elsewhere leaves the text selected in the
    // now-unfocused field.  This causes the text to be drawn
    // in different colors even though the selection isn't
    // visible.  I suppose we could make HighlightView smarter
    // about that, but instead let's just force the Mac-like
    // behavior and be done with it for now - ST 11/3/03
    if (enableFocusTraversalKeys)
      select(0, 0)
    mouseEvent = fe.isTemporary
    bracketMatcher.focusLost(this)
    colorizer.reset()
    if (!fe.isTemporary) {
      Actions.setEnabled(false)
      UndoManager.setCurrentManager(null)
    }
  }

  // this is used for quick help, when QH is triggered
  // by the context menu we want to look up the word under
  // the mouse pointer without moving the cursor ev 7/3/07
  private var mousePos: Int = 0

  def getMousePos: Int = mousePos

  override def processMouseEvent(me: java.awt.event.MouseEvent): Unit = {
    if (me.getID == java.awt.event.MouseEvent.MOUSE_PRESSED) {
      mouseEvent = true
    }
    if (me.isPopupTrigger() && ! contextMenu.isShowing()) {
      mousePos = caret.getMousePosition(me)
      doPopup(me)
    } else
      super.processMouseEvent(me)
  }

  private class EditorContextMenu(colorizer: Colorizer, i18n: String => String) extends JPopupMenu {

    val copyItem  = new JMenuItem(Actions.COPY_ACTION)
    val cutItem   = new JMenuItem(Actions.CUT_ACTION)
    val pasteItem = new JMenuItem(Actions.PASTE_ACTION)

    locally {
      add(copyItem)
      Actions.COPY_ACTION.putValue(Action.NAME, i18n.apply("menu.edit.copy"))
      add(cutItem)
      Actions.CUT_ACTION.putValue(Action.NAME, i18n.apply("menu.edit.cut"))
      add(pasteItem)
      Actions.PASTE_ACTION.putValue(Action.NAME, i18n.apply("menu.edit.paste"))
      addSeparator()
      add(new JMenuItem(Actions.mouseQuickHelpAction(colorizer, i18n)))
    }

    override def show(invoker: Component, x: Int, y: Int): Unit = {
      val text = EditorArea.this.getSelectedText
      val isTextSelected = Option(text).exists(_.length > 0)
      copyItem.setEnabled(isTextSelected)
      cutItem.setEnabled(isTextSelected)
      pasteItem.setEnabled(
        Toolkit.getDefaultToolkit.getSystemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
      super.show(invoker, x, y)
    }
  }

  private def editorContextMenu(colorizer: Colorizer, i18n: String => String): JPopupMenu = {
    new EditorContextMenu(colorizer, i18n)
  }

  private def doPopup(e: MouseEvent): Unit = {
    contextMenu.show(this, e.getX, e.getY)
  }

  override def replaceSelection(s: String): Unit = {
    // we got a bug report (#917) from a guy in Denmark who was getting NullPointerExceptions
    // when he types the ^ character on Mac OS X 10.6.2.  Dunno what's that about, have not
    // seen it locally, but perhaps we can avoid the exception as follows. - ST 11/25/09
    if (s == null) {
      super.replaceSelection(s)
    } else {
    var selection = s
      // on Macs we're having problems with pasted text from other
      // apps having some weird nonstandard character at the
      // beginning we need to ignore - ST 1/3/06
      if (s.length > 0 &&
          Character.getType(s.charAt(0)) == Character.FORMAT) {
        selection = selection.drop(1)
      }
      // Let's turn all tabs into spaces, because tabs are icky
      // and smartTabbing isn't happy with them. ~Forrest (10/4/2006)
      selection = selection.replaceAllLiterally("\t", "  ")
      super.replaceSelection(selection)
      indenter.handleInsertion(selection)
    }
  }

  override def getText(start: Int, end: Int): String =
    try {
      getDocument.getText(start, end)
    } catch {
      case ex: BadLocationException => throw new IllegalStateException(ex)
    }

  ///

  def getHelpTarget(startition: Int): Option[String] = {
    // determine the current "word" that the cursor is on
    val doc = getDocument.asInstanceOf[PlainDocument]
    try {
      val currentLine      = offsetToLine(doc, startition)
      val startLineOffset  = lineToStartOffset(doc, currentLine)
      val lineLength       = lineToEndOffset(doc, currentLine) - startLineOffset
      val lineText         = doc.getText(startLineOffset, lineLength)
      val selStartInString = startition - startLineOffset
      colorizer.getTokenAtPosition(lineText, selStartInString)
    } catch  {
      case ex: BadLocationException => throw new IllegalStateException(ex)
    }
  }

  ///

  protected class TransferFocusAction extends AbstractAction {
    def actionPerformed(e: java.awt.event.ActionEvent): Unit = {
      transferFocus()
    }
  }

  protected class TransferFocusBackwardAction extends AbstractAction {
    def actionPerformed(e: java.awt.event.ActionEvent): Unit = {
      transferFocusBackward()
    }
  }

}
