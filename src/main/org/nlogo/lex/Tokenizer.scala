// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.api, api.{ Token, TokenType }

object Tokenizer extends api.TokenizerInterface {

  // this method never throws CompilerException, but use TokenType.BAD
  // instead, because if there's an error we want to still
  // keep going, so that findProcedurePositions and AutoConverter won't
  // be useless even if there's a tokenization error in the code
  // - ST 4/21/03, 5/24/03, 6/29/06
  def tokenizeRobustly(source: String): Seq[Token] =
    doTokenize(source, false, "", false)

  // and here's one that throws CompilerException as soon as it hits a bad token - ST 2/20/08
  def tokenize(source: String, fileName: String): Seq[Token] = {
    val result = doTokenize(source, false, fileName, true)
    result.find(_.tpe == TokenType.Bad) match {
      case Some(badToken) =>
        throw new api.CompilerException(badToken)
      case None =>
        result
    }
  }

  // this is used e.g. when colorizing
  private def tokenizeIncludingComments(source: String): Seq[Token] =
    doTokenize(source, true, "", false)

  // includeCommentTokens is used for syntax highlighting in the editor - ST 7/7/06
  private def doTokenize(source: String, includeCommentTokens: Boolean,
                         fileName: String, stopAtFirstBadToken: Boolean): Seq[Token] =
  {
    val yy = new TokenLexer(
      new java.io.StringReader(source),
      fileName)
    val eof = new Token("", TokenType.EOF, "")(0, 0, "")
    def yystream: Stream[Token] = {
      val t = yy.yylex()
      if (t == null)
        Stream(eof)
      else if (stopAtFirstBadToken && t.tpe == TokenType.Bad)
        Stream(t, eof)
      else
        Stream.cons(t, yystream)
    }
    yystream
      .filter(includeCommentTokens || _.tpe != TokenType.Comment)
      .map(handleSpecialIdentifiers)
      .toList
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

  def isValidIdentifier(ident: String): Boolean =
    tokenizeRobustly(ident).take(2).map(_.tpe) ==
      Seq(TokenType.Ident, TokenType.EOF)

}
