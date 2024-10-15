// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

// the code in this package is based on the sample code sketched out
// in the second half of this page:
// http://www.cs.bris.ac.uk/Teaching/Resources/COMS30122/tools/highlight/design.html
// see also http://www.cs.bris.ac.uk/Teaching/Resources/COMS30122/tools/index.html
// for related editor stuff - ST 6/28/03

package org.nlogo.editor

import java.awt.{ Component, Dimension, Point, Toolkit }
import java.awt.datatransfer.DataFlavor
import java.awt.event.{ FocusListener, KeyAdapter, KeyEvent, MouseAdapter, MouseEvent }
import javax.swing.{ Action, JMenuItem, JEditorPane, JPopupMenu }
import javax.swing.text.{ Document, TextAction, PlainDocument, BadLocationException }

import KeyBinding.keystroke

object EditorArea {
  def defaultSize = new Dimension(400, 400)
}

import EditorArea.defaultSize

class EditorArea(val configuration: EditorConfiguration)
  extends JEditorPane
  with AbstractEditorArea
  with FocusTraversable
  with FocusListener {

  val rows      = configuration.rows
  val columns   = configuration.columns
  val colorizer = configuration.colorizer

  private var indenter: Option[Indenter] = None
  private val contextMenu: JPopupMenu = new EditorContextMenu(colorizer)
  contextMenu.addPopupMenuListener(new SuspendCaretPopupListener(this))
  private val bracketMatcher = new BracketMatcher(colorizer)
  private val undoManager: UndoManager = new UndoManager()

  private val caret = new DoubleClickCaret(colorizer, bracketMatcher)

  private val defaultSelectionColor = getSelectionColor

  locally {
    enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK)
    addFocusListener(this)
    addCaretListener(bracketMatcher)
    val blinkRate: Int = getCaret.getBlinkRate
    // I don't really understand why, but if we don't set the blink rate,
    // it doesn't blink, even though the normal caret does - ST 6/9/04
    caret.setBlinkRate(blinkRate)
    setCaret(caret)
    setDragEnabled(false)

    undoManager.watch(this)

    // add key bindings for undo and redo so they work even in modal dialogs
    val mask: Int = if(java.awt.GraphicsEnvironment.isHeadless) 0 else getToolkit.getMenuShortcutKeyMaskEx

    getKeymap.addActionForKeyStroke(keystroke(KeyEvent.VK_Z, mask), UndoManager.undoAction())
    getKeymap.addActionForKeyStroke(keystroke(KeyEvent.VK_Y, mask), UndoManager.redoAction())

    configuration.configureEditorArea(this)
  }

  private var bracketMatcherEnabled: Boolean = true

  override def enableBracketMatcher(enabled: Boolean): Unit = {
    if (bracketMatcherEnabled != enabled) {
      if (enabled)
        addCaretListener(bracketMatcher)
      else
        removeCaretListener(bracketMatcher)
      bracketMatcherEnabled = enabled
    }
  }

  def setIndenter(newIndenter: Indenter): Unit = {
    indenter = Some(newIndenter)
    newIndenter.addActions(configuration, getInputMap)
  }

  override def getActions: Array[Action] = {
    val extraActions =
      (configuration.editorOnlyActions :+ new MouseQuickHelpAction(colorizer)).toArray[Action]
    TextAction.augmentList(super.getActions, extraActions)
  }

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
    getFontMetrics(getFont).getHeight

  def resetUndoHistory() {
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
    indenter.foreach(_.handleTab())
  }

  def lineToStartOffset(doc: Document, line: Int): Int =
    doc.getDefaultRootElement.getElement(line).getStartOffset

  def lineToEndOffset(doc: Document, line: Int): Int =
    doc.getDefaultRootElement.getElement(line).getEndOffset

  def offsetToLine(doc: Document, offset: Int): Int =
    doc.getDefaultRootElement.getElementIndex(offset)

  private var _selectionActive = true

  def selectionActive = _selectionActive

  def setSelection(s: Boolean): Unit = {
    _selectionActive = s
  }

  def selectError(start: Int, end: Int) {
    setSelectionColor(AbstractEditorArea.ERROR_HIGHLIGHT)

    select(start, end)
  }

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent) {
      setSelectionColor(defaultSelectionColor)
    }
  })

  addKeyListener(new KeyAdapter {
    override def keyPressed(e: KeyEvent) {
      setSelectionColor(defaultSelectionColor)
    }
  })

  def focusGained(fe: java.awt.event.FocusEvent): Unit = { }

  def focusLost(fe: java.awt.event.FocusEvent): Unit = {
    bracketMatcher.focusLost(this)
    colorizer.reset()
  }

  // this is used for quick help, when QH is triggered
  // by the context menu we want to look up the word under
  // the mouse pointer without moving the cursor ev 7/3/07
  private var mousePos: Int = 0

  def getMousePos: Int = mousePos

  override def processMouseEvent(me: MouseEvent): Unit = {
    if (me.isPopupTrigger() && ! contextMenu.isShowing()) {
      mousePos = caret.getMousePosition(me)
      doPopup(me)
    } else
      super.processMouseEvent(me)
  }

  private def doPopup(e: MouseEvent): Unit = {
    contextMenu.show(this, e.getX, e.getY)
  }

  private class EditorContextMenu(colorizer: Colorizer) extends JPopupMenu {
    val copyItem  = new JMenuItem(Actions.CopyAction)
    val cutItem   = new JMenuItem(Actions.CutAction)
    val pasteItem = new JMenuItem(Actions.PasteAction)

    locally {
      add(copyItem)
      add(cutItem)
      add(pasteItem)
      addSeparator()
      for (item <- configuration.contextActions) {
        add(new JMenuItem(item))
      }
    }

    override def show(invoker: Component, x: Int, y: Int): Unit = {
      val text = EditorArea.this.getSelectedText
      val isTextSelected = Option(text).exists(_.length > 0)
      val point = new Point(invoker.getLocationOnScreen)
      copyItem.setEnabled(isTextSelected)
      cutItem.setEnabled(isTextSelected)
      pasteItem.setEnabled(
        Toolkit.getDefaultToolkit.getSystemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
      point.translate(x, y)
      configuration.contextActions.foreach {
        case e: EditorAwareAction => e.updateEditorInfo(EditorArea.this, point, mousePos)
        case _ =>
      }
      super.show(invoker, x, y)
    }
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
      indenter.foreach(_.handleInsertion(selection))
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

  def undoAction = UndoManager.undoAction
  def redoAction = UndoManager.redoAction
}
