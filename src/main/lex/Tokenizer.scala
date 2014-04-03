// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.core, core.{ Token, TokenType }

// caller's responsibility to check for TokenType.Bad!

object Tokenizer extends core.TokenizerInterface {

  def tokenizeString(source: String, filename: String = ""): Iterator[Token] =
    tokenize(new java.io.StringReader(source), filename)

  def tokenize(reader: java.io.Reader, filename: String = ""): Iterator[Token] = {
    val yy = new TokenLexer(reader, filename)
    val results =
      Iterator.continually(yy.yylex())
        .takeWhile(_ != null)
    results ++ Iterator(Token.Eof)
  }

}
