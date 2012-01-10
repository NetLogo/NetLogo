// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor;

import javax.swing.text.JTextComponent;
import java.util.List;

/**
 * Highlights a section of code that lies between two matching
 * parentheses or brackets
 */
strictfp class DoubleClickCaret<TokenType>
    extends javax.swing.text.DefaultCaret {

  private final Colorizer<TokenType> colorizer;
  private final BracketMatcher<TokenType> bracketMatcher;

  DoubleClickCaret(Colorizer<TokenType> colorizer,
                   BracketMatcher<TokenType> bracketMatcher) {
    this.colorizer = colorizer;
    this.bracketMatcher = bracketMatcher;
  }

  ///

  /**
   * Overrides method in DefaultCaret.
   */
  @Override
  public void mouseClicked(java.awt.event.MouseEvent e) {
    if (!e.isConsumed() &&
        javax.swing.SwingUtilities.isLeftMouseButton(e) &&
        e.getClickCount() == 2) {
      JTextComponent source = (JTextComponent) e.getSource();

      if (!handleDoubleClick
          (source,
              colorizer.getCharacterTokenTypes(source.getText()),
              source.getUI().viewToModel(source, e.getPoint()))) {
        super.mouseClicked(e);
      }
    } else {
      super.mouseClicked(e);
    }
  }

  public int getMousePosition(java.awt.event.MouseEvent e) {
    JTextComponent source = (JTextComponent) e.getSource();

    return source.getUI().viewToModel(source, e.getPoint());
  }

  /**
   * @return whether we found something to select
   */
  private boolean handleDoubleClick(JTextComponent textComponent,
                                    List<TokenType> tokens, int dot) {
    // look on right side of cursor for opening paren
    if (dot < tokens.size() &&
        colorizer.isOpener(tokens.get(dot))) {
      int closer = bracketMatcher.findCloser(colorizer, tokens, dot);
      if (closer != -1 &&
          // candidate found, but is it a good match?
          colorizer.isMatch(tokens.get(dot), tokens.get(closer))) {
        textComponent.select(dot, closer + 1);
        return true;
      }
    }
    // look on left side of cursor for closing paren
    else if (dot > 0 &&
        colorizer.isCloser(tokens.get(dot - 1))) {
      int opener = bracketMatcher.findOpener(colorizer, tokens, dot - 1);
      if (opener != -1 &&
          // candidate found, but is it a good match?
          colorizer.isMatch(tokens.get(opener), tokens.get(dot - 1))) {
        textComponent.select(opener, dot);
        return true;
      }
    }
    return false;
  }

}
