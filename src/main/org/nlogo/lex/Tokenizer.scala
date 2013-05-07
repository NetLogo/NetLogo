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
        .map(handleSpecialIdentifiers)
    results ++ Iterator(Token.eof)
  }

  // this could be part of Namer, even. handling it here for
  // now, pending a total rewrite of Namer - ST 5/6/13
  private def handleSpecialIdentifiers(t: Token): Token =
    if (Keywords.isKeyword(t.text))
      t.copy(tpe = TokenType.Keyword)
    else Constants.get(t.text) match {
      case Some(value) =>
        t.copy(tpe = TokenType.Literal, value = value)
      case None =>
        t
    }

}
