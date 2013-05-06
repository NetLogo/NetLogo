// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex;

import org.nlogo.api.TokenType;

class TokenTypeJ {

  // this class is not instantiable
  private TokenTypeJ() {
    throw new IllegalStateException();
  }

  // annoying, but I can't figure out any other way to get at the
  // Scala inner objects from Java - ST 7/7/11
  static final TokenType OpenParen    = byName("OpenParen");
  static final TokenType CloseParen   = byName("CloseParen");
  static final TokenType OpenBracket  = byName("OpenBracket");
  static final TokenType CloseBracket = byName("CloseBracket");
  static final TokenType OpenBrace    = byName("OpenBrace");
  static final TokenType CloseBrace   = byName("CloseBrace");
  static final TokenType Literal      = byName("Literal");
  static final TokenType Ident        = byName("Ident");
  static final TokenType Comma        = byName("Comma");
  static final TokenType Comment      = byName("Comment");
  static final TokenType Bad          = byName("Bad");

  private static TokenType byName(String name) {
    try {
      return (TokenType)
        Class.forName("org.nlogo.api.TokenType$" + name + "$")
        .getField("MODULE$").get(null);
    }
    catch(IllegalAccessException ex) {
      throw new IllegalStateException(ex);
    }
    catch(NoSuchFieldException ex) {
      throw new IllegalStateException(ex);
    }
    catch(ClassNotFoundException ex) {
      throw new IllegalStateException(ex);
    }
  }

}
