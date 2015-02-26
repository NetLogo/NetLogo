// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor;

import org.nlogo.api.Token;

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
  String getTokenStringAtPosition(String text, int position);
  Token getTokenAtPosition(String text, int position);

  // this isn't colorizing either.. - ST 3/28/08
  void doHelp(java.awt.Component comp, String name);

  // Let's just keep rocking out adding non colorizer things
  void raiseJumpToDefinitionEvent(java.awt.Component comp, String name);

  // Ok, at this point we should really rename this class - FD 2/26/15
  void doCodeCompletion(EditorArea<?> editor);
}
