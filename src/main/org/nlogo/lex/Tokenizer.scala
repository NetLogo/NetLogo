// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.api, api.{ Token, TokenType }

object Tokenizer extends api.TokenizerInterface {

  // throws CompilerException when it hits a bad token
  def tokenize(source: String, filename: String = ""): Iterator[Token] =
    tokenizeRobustly(new java.io.StringReader(source), filename)
      .map{token =>
        if (token.tpe == TokenType.Bad)
          throw new api.CompilerException(token)
        else
          token}

  // no CompilerExceptions, just keeps chugging spitting out TokenType.Bad
  // as necessary
  def tokenizeRobustly(reader: java.io.Reader, filename: String = ""): Iterator[Token] = {
    val yy = new TokenLexer(reader, filename)
    val results =
      Iterator.continually(yy.yylex())
        .takeWhile(_ != null)
    results ++ Iterator(Token.Eof)
  }

}
