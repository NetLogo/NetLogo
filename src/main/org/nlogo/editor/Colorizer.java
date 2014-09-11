// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor;

import java.util.List;

public interface Colorizer<TokenType> {
  java.awt.Color[] getCharacterColors(String line);

  List<TokenType> getCharacterTokenTypes(String line);

  boolean isMatch(TokenType token1, TokenType token2);

  boolean isOpener(TokenType token);

  boolean isCloser(TokenType token);

  void reset(); // forget any cached information

  // uck. this doesn't really have to do with colorizing
  // but it deals with the tokenizer and we need it in the
  // same spots.  ev 1/17/07
  String getTokenAtPosition(String text, int position);

  // this isn't colorizing either.. - ST 3/28/08
  void doHelp(java.awt.Component comp, String name);

  boolean jumpToDefinition(EditorArea editor, String name);
}
