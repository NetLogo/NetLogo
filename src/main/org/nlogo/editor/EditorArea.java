// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

// the code in this package is based on the sample code sketched out
// in the second half of this page:
// http://www.cs.bris.ac.uk/Teaching/Resources/COMS30122/tools/highlight/design.html
// see also http://www.cs.bris.ac.uk/Teaching/Resources/COMS30122/tools/index.html
// for related editor stuff - ST 6/28/03

package org.nlogo.editor;

import java.util.ArrayList;
import java.util.Arrays;

public strictfp class EditorArea<TokenType>
    extends AbstractEditorArea
    implements java.awt.event.FocusListener {
  private final int rows;
  private final int columns;
  protected final boolean disableFocusTraversalKeys;
  private final BracketMatcher<TokenType> bracketMatcher;
  private final UndoManager undoManager = new UndoManager();
  protected final Colorizer<TokenType> colorizer;
  protected IndenterInterface indenter;
  private final scala.Function1<String, String> i18n;

  private final DoubleClickCaret<TokenType> caret;

  public EditorArea(int rows, int columns,
                    java.awt.Font font,
                    boolean disableFocusTraversalKeys,
                    java.awt.event.TextListener listener,
                    Colorizer<TokenType> colorizer,
                    scala.Function1<String, String> i18n) {
    this.rows = rows;
    this.columns = columns;
    this.disableFocusTraversalKeys = disableFocusTraversalKeys;
    this.colorizer = colorizer;
    this.i18n = i18n;
    indenter = new DumbIndenter(this);
    enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK);
    addFocusListener(this);

    bracketMatcher = new BracketMatcher<TokenType>(colorizer);
    addCaretListener(bracketMatcher);
    int blinkRate = getCaret().getBlinkRate();
    caret = new DoubleClickCaret<TokenType>(colorizer, bracketMatcher);
    // I don't really understand why, but if we don't set the blink rate,
    // it doesn't blink, even though the normal caret does - ST 6/9/04
    caret.setBlinkRate(blinkRate);
    setCaret(caret);

    setDragEnabled(false);
    setFocusTraversalKeysEnabled(!disableFocusTraversalKeys);
    if (!disableFocusTraversalKeys) {
      getInputMap().put
          (javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, 0),
              new TransferFocusAction());
      getInputMap().put
          (javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB,
              java.awt.event.InputEvent.SHIFT_MASK),
              new TransferFocusBackwardAction());
    } else {
      getInputMap().put
          (javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, 0),
              Actions.tabKeyAction());
      getInputMap().put
          (javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB,
              java.awt.event.InputEvent.SHIFT_MASK),
              Actions.shiftTabKeyAction());
    }
    setFont(font);
    setEditorKit(new HighlightEditorKit<TokenType>(listener, colorizer));
    getInputMap().put
        (javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),
            new EnterAction());

    getInputMap().put
        (javax.swing.KeyStroke.getKeyStroke(Character.valueOf(']'), 0),
            new CloseBracketAction());
    getDocument().putProperty
        (javax.swing.text.PlainDocument.tabSizeAttribute,
            Integer.valueOf(2));
    getDocument().putProperty
        (javax.swing.text.DefaultEditorKit.EndOfLineStringProperty,
            "\n"); // on Windows, prevent save() from outputting ^M characters - ST 2/23/04
    getDocument().addUndoableEditListener(undoManager);

    // add key bindings for undo and redo so they work even in modal dialogs
    int mask = getToolkit().getMenuShortcutKeyMask();
    getKeymap().addActionForKeyStroke
        (javax.swing.KeyStroke.getKeyStroke
            (java.awt.event.KeyEvent.VK_Z, mask),
            UndoManager.undoAction());
    getKeymap().addActionForKeyStroke
        (javax.swing.KeyStroke.getKeyStroke
            (java.awt.event.KeyEvent.VK_Y, mask),
            UndoManager.redoAction());

    // add key binding, for getting quick "contexthelp", based on where
    // the cursor is...
    getInputMap().put
        (javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0),
            Actions.quickHelpAction(colorizer, i18n));
  }

  @Override
  public void paintComponent(java.awt.Graphics g) {
    java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
    g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
    super.paintComponent(g);
  }

  private boolean enabled = true;

  @Override
  public void enableBracketMatcher(boolean enabled) {
    if (this.enabled != enabled) {
      if (enabled) {
        addCaretListener(bracketMatcher);
      } else {
        removeCaretListener(bracketMatcher);
      }
      this.enabled = enabled;
    }
  }

  public void setIndenter(IndenterInterface indenter) {
    this.indenter = indenter;
  }

  class EnterAction
      extends javax.swing.text.TextAction {
    EnterAction() {
      super("enter");
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      indenter.handleEnter();
    }
  }

  class CloseBracketAction
      extends javax.swing.text.TextAction {
    CloseBracketAction() {
      super("close-bracket");
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      replaceSelection("]");
      indenter.handleCloseBracket();
    }
  }

  private boolean isModalEditor() {
    java.awt.Container topLevel = getTopLevelAncestor();
    return topLevel instanceof java.awt.Dialog && ((java.awt.Dialog) topLevel).isModal();
  }

  @Override
  public javax.swing.Action[] getActions() {
    ArrayList<javax.swing.Action> actions = new ArrayList<javax.swing.Action>(
      Arrays.asList(
        Actions.commentAction(), Actions.uncommentAction(),
        Actions.shiftLeftAction(), Actions.shiftRightAction(),
        Actions.quickHelpAction(colorizer, i18n)
      ));
    if(!isModalEditor()) actions.add(Actions.jumpToDefinitionAction(colorizer, i18n));
    return javax.swing.text.TextAction.augmentList
        (super.getActions(),actions.toArray(new javax.swing.Action[actions.size()]));
  }

  @Override
  public java.awt.Dimension getPreferredScrollableViewportSize() {
    java.awt.Dimension dimension = super.getPreferredScrollableViewportSize();
    dimension = dimension != null ? dimension : new java.awt.Dimension(400, 400);
    dimension.width = columns != 0 ? columns * getColumnWidth() : dimension.width;
    dimension.height = rows != 0 ? rows * getRowHeight() : dimension.height;
    return dimension;
  }

  @Override
  public java.awt.Dimension getPreferredSize() {
    java.awt.Dimension dimension = super.getPreferredSize();
    dimension =
        (dimension == null)
            ? new java.awt.Dimension(400, 400)
            : dimension;
    if (columns != 0) {
      dimension.width = StrictMath.max(dimension.width,
          columns * getColumnWidth());
    }
    if (rows != 0) {
      dimension.height = StrictMath.max(dimension.height,
          rows * getRowHeight());
    }
    return dimension;
  }

  private int getColumnWidth() {
    return getFontMetrics(getFont()).charWidth('m');
  }

  private int getRowHeight() {
    return getFontMetrics(getFont()).getHeight();
  }

  @Override
  public void setText(String text) {
    // not sure if this really needed - ST 8/27/03
    if (!text.equals(getText())) {
      super.setText(text);
      undoManager.discardAllEdits();
    }
  }

  String getLineText(int offset)
      throws javax.swing.text.BadLocationException {
    javax.swing.text.PlainDocument doc =
        (javax.swing.text.PlainDocument) getDocument();
    int currentLine = offsetToLine(doc, offset);
    int lineStart = lineToStartOffset(doc, currentLine);
    int lineEnd = lineToEndOffset(doc, currentLine);
    return doc.getText(lineStart, lineEnd - lineStart);
  }

  void indentSelection() {
    indenter.handleTab();
  }

  int lineToStartOffset(javax.swing.text.PlainDocument doc, int line) {
    return doc.getDefaultRootElement().getElement(line).getStartOffset();
  }

  int lineToEndOffset(javax.swing.text.PlainDocument doc, int line) {
    return doc.getDefaultRootElement().getElement(line).getEndOffset();
  }

  int offsetToLine(javax.swing.text.PlainDocument doc, int offset) {
    return doc.getDefaultRootElement().getElementIndex(offset);
  }

  void insertBeforeEachSelectedLine(String insertion) {
    javax.swing.text.PlainDocument doc = (javax.swing.text.PlainDocument) getDocument();
    try {
      int currentLine = offsetToLine(doc, getSelectionStart());
      int endLine = offsetToLine(doc, getSelectionEnd());

      // The two following cases are to take care of selections that include
      // only the very edge of a line of text, either at the top or bottom
      // of the selection.  Because these lines do not have *any* highlighted
      // text, it does not make sense to modify these lines. ~Forrest (9/22/2006)
      if (endLine > currentLine &&
          getSelectionEnd() == lineToStartOffset(doc, endLine)) {
        endLine--;
      }
      if (endLine > currentLine &&
          getSelectionStart() == (lineToEndOffset(doc, currentLine) - 1)) {
        currentLine++;
      }

      while (currentLine <= endLine) {
        doc.insertString
            (lineToStartOffset(doc, currentLine), insertion, null);
        currentLine++;
      }
    } catch (javax.swing.text.BadLocationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  void uncomment() {
    javax.swing.text.PlainDocument doc = (javax.swing.text.PlainDocument) getDocument();
    try {
      int currentLine = offsetToLine(doc, getSelectionStart());
      int endLine = offsetToLine(doc, getSelectionEnd());

      // The two following cases are to take care of selections that include
      // only the very edge of a line of text, either at the top or bottom
      // of the selection.  Because these lines do not have *any* highlighted
      // text, it does not make sense to modify these lines. ~Forrest (9/22/2006)
      if (endLine > currentLine &&
          getSelectionEnd() == lineToStartOffset(doc, endLine)) {
        endLine--;
      }
      if (endLine > currentLine &&
          getSelectionStart() == (lineToEndOffset(doc, currentLine) - 1)) {
        currentLine++;
      }

      while (currentLine <= endLine) {
        int lineStart = lineToStartOffset(doc, currentLine);
        int lineEnd = lineToEndOffset(doc, currentLine);
        String text = doc.getText(lineStart, lineEnd - lineStart);
        int semicolonPos = text.indexOf(';');
        if (semicolonPos != -1) {
          boolean allSpaces = true;
          for (int i = 0; i < semicolonPos; i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
              allSpaces = false;
              break;
            }
          }
          if (allSpaces) {
            doc.remove(lineStart + semicolonPos, 1);
          }
        }
        currentLine++;
      }
    } catch (javax.swing.text.BadLocationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  void shiftLeft() {
    javax.swing.text.PlainDocument doc = (javax.swing.text.PlainDocument) getDocument();
    try {
      int currentLine = offsetToLine(doc, getSelectionStart());
      int endLine = offsetToLine(doc, getSelectionEnd());

      // The two following cases are to take care of selections that include
      // only the very edge of a line of text, either at the top or bottom
      // of the selection.  Because these lines do not have *any* highlighted
      // text, it does not make sense to modify these lines. ~Forrest (9/22/2006)
      if (endLine > currentLine &&
          getSelectionEnd() == lineToStartOffset(doc, endLine)) {
        endLine--;
      }
      if (endLine > currentLine &&
          getSelectionStart() == (lineToEndOffset(doc, currentLine) - 1)) {
        currentLine++;
      }

      while (currentLine <= endLine) {
        int lineStart = lineToStartOffset(doc, currentLine);
        int lineEnd = lineToEndOffset(doc, currentLine);
        String text = doc.getText(lineStart, lineEnd - lineStart);
        if (text.length() > 0 && text.charAt(0) == ' ') {
          doc.remove(lineStart, 1);
        }
        currentLine++;
      }
    } catch (javax.swing.text.BadLocationException ex) {
      throw new IllegalStateException(ex);
    }
  }


  /// select-all-on-focus stuff copied from org.nlogo.swing.TextField

  private boolean mouseEvent = false;

  private boolean setSelection = true;

  public void setSelection(boolean s) {
    setSelection = s;
  }

  public void focusGained(java.awt.event.FocusEvent fe) {
    if (!mouseEvent && !disableFocusTraversalKeys && setSelection) {
      // this is like selectAll(), but it leaves the
      // caret at the beginning rather than the start;
      // this prevents the enclosing scrollpane from
      // scrolling to the end to make the caret
      // visible; it's nicer to keep the scroll at the
      // start - ST 12/20/04
      setCaretPosition(getText().length());
      moveCaretPosition(0);
    }
    Actions.setEnabled(true);
    UndoManager.setCurrentManager(undoManager);
  }

  public void focusLost(java.awt.event.FocusEvent fe) {
    // On Windows (and perhaps Linux? not sure), putting
    // the focus elsewhere leaves the text selected in the
    // now-unfocused field.  This causes the text to be drawn
    // in different colors even though the selection isn't
    // visible.  I suppose we could make HighlightView smarter
    // about that, but instead let's just force the Mac-like
    // behavior and be done with it for now - ST 11/3/03
    if (!disableFocusTraversalKeys) {
      select(0, 0);
    }
    mouseEvent = fe.isTemporary();
    bracketMatcher.focusLost(this);
    colorizer.reset();
    if (!fe.isTemporary()) {
      Actions.setEnabled(false);
      UndoManager.setCurrentManager(null);
    }
  }

  // this is used for quick help, when QH is triggered
  // by the context menu we want to look up the word under
  // the mouse pointer without moving the cursor ev 7/3/07
  private int mousePos;

  public int getMousePos() {
    return mousePos;
  }

  @Override
  public void processMouseEvent(java.awt.event.MouseEvent me) {
    if (me.getID() == java.awt.event.MouseEvent.MOUSE_PRESSED) {
      mouseEvent = true;
    }
    if (me.isPopupTrigger()) {
      mousePos = caret.getMousePosition(me);
      doPopup(me);
      return;
    }
    super.processMouseEvent(me);
  }

  private void doPopup(java.awt.event.MouseEvent e) {
    javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
    menu.add(new javax.swing.JMenuItem(Actions.COPY_ACTION()));
    Actions.COPY_ACTION().putValue(javax.swing.Action.NAME, i18n.apply("menu.edit.copy"));
    menu.add(new javax.swing.JMenuItem(Actions.CUT_ACTION()));
    Actions.CUT_ACTION().putValue(javax.swing.Action.NAME, i18n.apply("menu.edit.cut"));
    menu.add(new javax.swing.JMenuItem(Actions.PASTE_ACTION()));
    Actions.PASTE_ACTION().putValue(javax.swing.Action.NAME, i18n.apply("menu.edit.paste"));
    menu.addSeparator();
    menu.add(new javax.swing.JMenuItem(Actions.mouseQuickHelpAction(colorizer, i18n)));
    if(!isModalEditor()) menu.add(new javax.swing.JMenuItem(Actions.mouseJumpToDefinitionAction(colorizer, i18n)));
    menu.show(this, e.getX(), e.getY());
  }

  @Override
  public void replaceSelection(String s) {
    // we got a bug report (#917) from a guy in Denmark who was getting NullPointerExceptions
    // when he types the ^ character on Mac OS X 10.6.2.  Dunno what's that about, have not
    // seen it locally, but perhaps we can avoid the exception as follows. - ST 11/25/09
    if (s == null) {
      super.replaceSelection(s);
      return;
    }
    // on Macs we're having problems with pasted text from other
    // apps having some weird nonstandard character at the
    // beginning we need to ignore - ST 1/3/06
    if (s.length() > 0 &&
        Character.getType(s.charAt(0)) == Character.FORMAT) {
      s = s.substring(1);
    }
    // Let's turn all tabs into spaces, because tabs are icky
    // and smartTabbing isn't happy with them. ~Forrest (10/4/2006)
    if (s.indexOf('\t') >= 0) {
      s = s.replaceAll("\t", "  ");
    }
    super.replaceSelection(s);
    indenter.handleInsertion(s);
  }

  @Override
  public String getText(int start, int end) {
    try {
      return getDocument().getText(start, end);
    } catch (javax.swing.text.BadLocationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  ///

  String getHelpTarget(int startPosition) {
    // determine the current "word" that the cursor is on
    javax.swing.text.PlainDocument doc = (javax.swing.text.PlainDocument) getDocument();
    try {
      int currentLine = offsetToLine(doc, startPosition);
      int startLineOffset = lineToStartOffset(doc, currentLine);
      int lineLength = lineToEndOffset(doc, currentLine) - startLineOffset;
      String lineText = doc.getText(startLineOffset, lineLength);
      int selStartInString = startPosition - startLineOffset;
      return colorizer.getTokenAtPosition(lineText, selStartInString);
    } catch (javax.swing.text.BadLocationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  ///

  protected class TransferFocusAction
      extends javax.swing.AbstractAction {
    public void actionPerformed(java.awt.event.ActionEvent e) {
      transferFocus();
    }
  }

  protected class TransferFocusBackwardAction
      extends javax.swing.AbstractAction {
    public void actionPerformed(java.awt.event.ActionEvent e) {
      transferFocusBackward();
    }
  }

}
