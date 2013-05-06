// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.api, api.{ Token, TokenType }

object Tokenizer extends api.TokenizerInterface {

  // throws CompilerException as soon as it hits a bad token - ST 2/20/08
  def tokenize(source: String, fileName: String = ""): Seq[Token] = {
    val result = tokenizeRobustly(source, fileName)
    result.find(_.tpe == TokenType.Bad) match {
      case Some(badToken) =>
        throw new api.CompilerException(badToken)
      case None =>
        result.toList
    }
  }

  // never throws CompilerException, but use TokenType.BAD
  // instead, in case we want to keep going. at the moment, only used by
  // TestTokenizer - ST 5/6/13
  private[lex] def tokenizeRobustly(source: String, fileName: String = ""): Stream[Token] = {
    val yy = new TokenLexer(
      new java.io.StringReader(source),
      fileName)
    val eof = new Token("", TokenType.EOF, "")(0, 0, "")
    def yystream: Stream[Token] = {
      val t = yy.yylex()
      if (t == null)
        Stream(eof)
      else
        Stream.cons(t, yystream)
    }
    yystream
      .filter(_.tpe != TokenType.Comment)
      .map(handleSpecialIdentifiers)
  }

  // this could be part of IdentifierParser, even. handling it here for
  // now, pending a total rewrite of IdentifierParser - ST 5/6/13
  private def handleSpecialIdentifiers(t: Token): Token =
    if (Keywords.isKeyword(t.name))
      t.copy(tpe = TokenType.Keyword)
    else Constants.get(t.name) match {
      case Some(value) =>
        t.copy(tpe = TokenType.Literal, value = value)
      case None =>
        t
    }

  def nextToken(reader: java.io.BufferedReader): Token =
    handleSpecialIdentifiers(
      new TokenLexer(reader, null).yylex())

}
