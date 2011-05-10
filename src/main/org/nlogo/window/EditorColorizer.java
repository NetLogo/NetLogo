package org.nlogo.window;

import java.util.Arrays;
import java.util.List;

import org.nlogo.api.CompilerServices;
import org.nlogo.api.Token;
import org.nlogo.api.TokenType;
import org.nlogo.editor.Colorizer;

public strictfp class EditorColorizer
    implements Colorizer<TokenType> {

  private final CompilerServices compiler;

  public EditorColorizer(CompilerServices compiler) {
    this.compiler = compiler;
  }

  // cache last studied line, so we don't retokenize the same string
  // over and over again when the user isn't even doing anything
  private String lastLine = "";
  java.awt.Color[] lastColors = new java.awt.Color[]{};

  public void reset() {
    lastLine = "";
    lastColors = new java.awt.Color[]{};
  }

  public java.awt.Color[] getCharacterColors(String line) {
    if (line.equals(lastLine)) {
      return lastColors;
    }
    java.awt.Color[] result = new java.awt.Color[line.length()];
    Token[] tokens = tokenizeForColorization(line);

    for (int i = 0; i < result.length; i++) {
      result[i] = SyntaxColors.DEFAULT_COLOR;
    }
    for (int i = 0; i < tokens.length; i++) {
      TokenType type = tokens[i].tyype();
      // "breed" can be either a keyword or a turtle variable,
      // which means we can't reliably colorize it correctly;
      // so as a kludge we colorize it as a keyword if it's
      // right at the beginning of the line (position 0) - ST 7/11/06
      if (type == TokenType.VARIABLE &&
          tokens[i].startPos() == 0 &&
          tokens[i].name().equalsIgnoreCase("BREED")) {
        type = TokenType.KEYWORD;
      }
      java.awt.Color color = getTokenColor(type);
      for (int j = tokens[i].startPos();
           j < tokens[i].endPos();
           j++) {
        // guard against any bugs in tokenization causing
        // out-of-bounds positions
        if (j >= 0 && j < result.length) {
          result[j] = color;
        }
      }
    }
    lastColors = result;
    lastLine = line;
    return result;
  }

  // This is used for bracket matching and word selection (double clicking)
  // and not for colorization, so we don't need to bother with the
  // TYPE_KEYWORD hack for "breed" here. - ST 7/11/06
  public List<TokenType> getCharacterTokenTypes(String line) {
    TokenType[] result = new TokenType[line.length()];
    Token[] tokens = tokenizeForColorization(line);
    for (int i = 0; i < tokens.length; i++) {
      for (int j = tokens[i].startPos();
           j < tokens[i].endPos();
           j++) {
        // guard against any bugs in tokenization causing
        // out-of-bounds positions
        if (j >= 0 && j < result.length) {
          result[j] = tokens[i].tyype();
        }
      }
    }
    return Arrays.asList(result);
  }

  public boolean isMatch(TokenType token1, TokenType token2) {
    return
        (token1 == TokenType.OPEN_PAREN &&
            token2 == TokenType.CLOSE_PAREN) ||
            (token1 == TokenType.OPEN_BRACKET &&
                token2 == TokenType.CLOSE_BRACKET);
  }

  public boolean isOpener(TokenType token) {
    return
        token == TokenType.OPEN_PAREN ||
            token == TokenType.OPEN_BRACKET;
  }

  public boolean isCloser(TokenType token) {
    return
        token == TokenType.CLOSE_PAREN ||
            token == TokenType.CLOSE_BRACKET;
  }

  public Token[] tokenizeForColorization(String line) {
    return compiler.tokenizeForColorization(line);
  }

  ///

  private static java.awt.Color getTokenColor(TokenType type) {
    switch (type) {
      case CONSTANT:
        return SyntaxColors.CONSTANT_COLOR;
      case COMMAND:
        return SyntaxColors.COMMAND_COLOR;
      case REPORTER:
        return SyntaxColors.REPORTER_COLOR;
      case VARIABLE:
        return SyntaxColors.REPORTER_COLOR;
      case KEYWORD:
        return SyntaxColors.KEYWORD_COLOR;
      case COMMENT:
        return SyntaxColors.COMMENT_COLOR;
      default:
        return SyntaxColors.DEFAULT_COLOR;
    }
  }

  public String getTokenAtPosition(String text, int position) {
    Token token = compiler.getTokenAtPosition(text, position);
    if (token != null) {
      return token.name();
    }
    return null;
  }

  /// quick help

  public void doHelp(java.awt.Component comp, String name) {
    QuickHelp.doHelp(comp, name);
  }

}
