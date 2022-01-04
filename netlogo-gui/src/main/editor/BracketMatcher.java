// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import java.util.List;

import org.nlogo.core.TokenType;

/**
 * Highlights two corresponding parentheses/brackets and identifies if they are
 * of the same type.  Two different colors are used to indicate "good"
 * matches and "bad" matches (where bad means that the parentheses/brackets
 * don't balance correctly).
 * <p/>
 * Created by EditorArea class.  Some methods are static because they are
 * also used by DoubleClickCaret.
 */

strictfp class BracketMatcher
    implements javax.swing.event.CaretListener {

  private static final java.awt.Color GOOD_COLOR = java.awt.Color.GRAY;
  private static final java.awt.Color BAD_COLOR = java.awt.Color.RED;

  // this object breaks the text up into tokens for us
  private final Colorizer colorizer;

  // remember what the whole document text was so we only retokenize when it changes
  private String oldText = "";
  private List<TokenType> tokenTypes = null;
  // remember what the text on current line was so we only retokenize when it changes
  private String oldLineText = "";
  private List<TokenType> lineTokenTypes = null;

  private final BracketHighlightPainter goodPainter;
  private final BracketHighlightPainter badPainter;

  BracketMatcher(Colorizer colorizer) {
    this.colorizer = colorizer;
    goodPainter = new BracketHighlightPainter(GOOD_COLOR);
    badPainter = new BracketHighlightPainter(BAD_COLOR);
  }

  ///

  /**
   * This is the single method we provide in order to implement CaretListener.
   * Listens for changes in caret position of text.
   * Checks current location of caret for parenthesis. If found, tries to find
   * matching parenthesis.
   */
  // The code gets complicated because of the need to be efficient.
  // We don't want to call the tokenizer unless necessary.  We especially
  // don't want to retokenize the whole document unless necessary.
  public void caretUpdate(javax.swing.event.CaretEvent e) {
    EditorArea source = (EditorArea) e.getSource();
    Highlighter highlighter = source.getHighlighter();
    removeOldHighlights(highlighter);
    int dot = e.getDot();
    // only highlight if there is no selection and there is
    // a character to the left
    if (dot != e.getMark() || dot == 0) {
      return;
    }
    try {
      // first we tokenize only the current line (if it changed,
      // otherwise use cached info) in order to find out
      // whether the cursor is to the right of an opener or closer
      String lineText = source.getLineText(dot);
      if (!lineText.equals(oldLineText)) {
        // line text changed, must retokenize
        lineTokenTypes = colorizer.getCharacterTokenTypes(lineText);
        oldLineText = lineText;
      }
      javax.swing.text.PlainDocument doc =
          (javax.swing.text.PlainDocument) source.getDocument();
      int lineNumber = source.offsetToLine(doc, dot);
      int lineDot = dot - source.lineToStartOffset(doc, lineNumber);
      if (lineDot == 0 ||
          // look left of cursor for opening paren
          !colorizer.isOpener(lineTokenTypes.get(lineDot - 1)) &&
              // look left of cursor for closing paren
              !colorizer.isCloser(lineTokenTypes.get(lineDot - 1))) {
        return;
      }
      String text = source.getText();
      if (!text.equals(oldText)) {
        // Text changed, so retokenize the whole document.
        // It'd be better if we could find a way to retokenize
        // only as much as needed to match!  This would be possible
        // to arrange, but tricky. - ST 10/29/04
        tokenTypes = colorizer.getCharacterTokenTypes(text);
        oldText = text;
      }
      doHighlighting(highlighter,
          colorizer.isOpener(lineTokenTypes.get(lineDot - 1)),
          tokenTypes, dot);
    } catch (BadLocationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  void focusLost(EditorArea source) {
    removeOldHighlights(source.getHighlighter());
    oldText = "";
    tokenTypes = null;
    oldLineText = "";
    lineTokenTypes = null;
  }

  /// now for the actual matching logic

  private void doHighlighting(Highlighter highlighter, boolean isOpener,
                              List<TokenType> tokens, int dot)
      throws BadLocationException {
    if (isOpener) {
      int closer = findCloser(colorizer, tokens, dot - 1);
      if (closer != -1 &&
          // candidate found, but is it a good match?
          colorizer.isMatch(tokens.get(dot - 1),
              tokens.get(closer))) {
        highlightGood(highlighter, closer);
      }
      // don't call highlightBad here because they
      // probably just haven't typed the closer yet
    } else {
      int opener = findOpener(colorizer, tokens, dot - 1);
      if (opener != -1) // success!
      {
        // candidate found, but is it a good match?
        if (colorizer.isMatch(tokens.get(opener),
            tokens.get(dot - 1))) {
          highlightGood(highlighter, opener);
        } else {
          highlightBad(highlighter, opener);
        }
      } else {
        highlightBad(highlighter, dot - 1);
      }
    }
  }

  /**
   * Given location of an open parenthesis, will search subsequent tokens
   * for corresponding close parenthesis. Will account for other open parentheses
   * that it encounters and their corresponding close parentheses.
   * Declared static so it can be used from DoubleClickCaret.
   *
   * @return location of close paren in document, or -1 if none found
   */
  int findCloser(Colorizer colorizer, List<TokenType> tokens, int opener) {
    int parenCount = 1;
    for (int i = opener + 1; i < tokens.size(); i++) {
      if (colorizer.isOpener(tokens.get(i))) {
        parenCount++;
      } else if (colorizer.isCloser(tokens.get(i))) {
        parenCount--;
      }
      if (parenCount == 0) {
        return i;
      }
    }
    return -1; // failure
  }

  /**
   * Given location of a close parenthesis, will search previous tokens
   * for corresponding open parenthesis. Will account for other close parentheses
   * that it encounters and their corresponding open parentheses.
   * Declared static so it can be used from DoubleClickCaret.
   *
   * @return location of open paren in document, or -1 if none found
   */
  int findOpener(Colorizer colorizer, List<TokenType> tokens, int closer) {
    int parenCount = 1;
    for (int i = closer - 1; i >= 0; i--) {
      if (colorizer.isCloser(tokens.get(i))) {
        parenCount++;
      } else if (colorizer.isOpener(tokens.get(i))) {
        parenCount--;
      }
      if (parenCount == 0) {
        return i;
      }
    }
    return -1; // failure
  }

  /// methods for creating & removing highlights

  private void removeOldHighlights(Highlighter highlighter) {
    // we can't just remove all highlights because when you have a
    // selection, that's considered a highlight as well, so we need
    // to look for our own highlights
    Highlighter.Highlight[] highlights = highlighter.getHighlights();
    for (int i = 0; i < highlights.length; i++) {
      if (highlights[i].getPainter() == goodPainter ||
          highlights[i].getPainter() == badPainter) {
        highlighter.removeHighlight(highlights[i]);
      }
    }
  }

  private void highlightGood(Highlighter highlighter, int pos)
      throws BadLocationException {
    highlighter.addHighlight
        (pos, pos + 1, goodPainter);
  }

  private void highlightBad(Highlighter highlighter, int pos)
      throws BadLocationException {
    highlighter.addHighlight(pos, pos + 1, badPainter);
  }

  ///

  private class BracketHighlightPainter
      implements Highlighter.HighlightPainter {
    private final java.awt.Color color;

    BracketHighlightPainter(java.awt.Color color) {
      this.color = color;
    }

    public void paint(java.awt.Graphics g, int p0, int p1,
                      java.awt.Shape bounds, JTextComponent c) {
      try {
        g.setColor(color);
        java.awt.geom.Rectangle2D rect =
            c.getUI().modelToView2D(c, p0, Position.Bias.Forward)
                .createUnion
                    (c.getUI().modelToView2D(c, p1, Position.Bias.Forward));
        g.drawRect((int) rect.getX(), (int)rect.getY(),
            (int) rect.getWidth() - 1, (int) rect.getHeight() - 1);
      } catch (javax.swing.text.BadLocationException ex) {
        throw new IllegalStateException(ex);
      }
    }
  }

}
