// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.api.{ CompilerException, ExtensionManager, Token, TokenizerInterface, TokenType }

object Tokenizer2D extends Tokenizer(TokenMapper2D)

class Tokenizer(tokenMapper: TokenMapper) extends TokenizerInterface {

  // this method never throws CompilerException, but use TokenType.BAD
  // instead, because if there's an error we want to still
  // keep going, so that findProcedurePositions and AutoConverter won't
  // be useless even if there's a tokenization error in the code
  // - ST 4/21/03, 5/24/03, 6/29/06
  def tokenizeRobustly(source: String): Seq[Token] =
    doTokenize(source, false, false, "", false)

  // and here's versions that throw CompilerException as soon as they hit a bad token - ST 2/20/08
  def tokenize(source: String): Seq[Token] =
    tokenize(source, "")
  def tokenize(source: String, fileName: String): Seq[Token] = {
    val result = doTokenize(source, false, false, fileName, true)
    result.find(_.tpe == TokenType.BAD) match {
      case Some(badToken) => throw new CompilerException(badToken)
      case None => result
    }
  }

  // this is used e.g. when colorizing
  private def tokenizeIncludingComments(source: String): Seq[Token] =
    doTokenize(source, true, false, "", false)

  // includeCommentTokens is used for syntax highlighting in the editor; allowRemovedPrimitives is
  // used when doing auto-conversions that require the parser - ST 7/7/06
  private def doTokenize(source: String, includeCommentTokens: Boolean, allowRemovedPrimitives: Boolean,
                         fileName: String, stopAtFirstBadToken: Boolean): Seq[Token] =
  {
    val yy = new TokenLexer(
      new java.io.StringReader(source), tokenMapper, fileName, allowRemovedPrimitives)
    val eof = new Token("", TokenType.EOF, "")(0, 0, "")
    def yystream: Stream[Token] = {
      val t = yy.yylex()
      if (t == null)
        Stream(eof)
      else if (stopAtFirstBadToken && t.tpe == TokenType.BAD)
        Stream(t, eof)
      else
        Stream.cons(t, yystream)
    }
    yystream.filter(includeCommentTokens || _.tpe != TokenType.COMMENT).toList
  }

  def nextToken(reader: java.io.BufferedReader): Token =
    new TokenLexer(reader, tokenMapper, null, false).yylex()

  def getTokenAtPosition(source: String, position: Int): Token = {
    // if the cursor is between two adjacent tokens we'll need to pick the token
    // the user probably wants for F1 purposes. see bug #139 - ST 5/2/12
    val interestingTokenTypes =
      List(TokenType.CONSTANT, TokenType.IDENT, TokenType.COMMAND, TokenType.REPORTER,
           TokenType.KEYWORD, TokenType.VARIABLE)
    val candidates =
      tokenizeIncludingComments(source)
        .dropWhile(_.endPos < position)
        .takeWhile(_.startPos <= position)
        .take(2) // be robust against EOF tokens, etc.
    candidates match {
      case Seq() => null
      case Seq(t) => t
      case Seq(t1, t2) =>
        if (interestingTokenTypes.contains(t2.tpe))
          t2 else t1
    }
  }

  def isValidIdentifier(ident: String): Boolean =
    tokenizeRobustly(ident) match {
      case Seq(
        Token(_, TokenType.IDENT, _),
        Token(_, TokenType.EOF, _)) => true
      case _ => false
    }

  // this is for the syntax-highlighting editor in the HubNet client, where we don't have
  // an extension manager.
  def tokenizeForColorization(source: String): Array[Token] =
    tokenizeIncludingComments(source).takeWhile(_.tpe != TokenType.EOF).toArray

  // this is for the syntax-highlighting editor
  def tokenizeForColorization(source: String, extensionManager: ExtensionManager): Array[Token] = {
    // In order for extension primitives to be the right color, we need to change
    // the type of the token from TokenType.IDENT to TokenType.COMMAND or TokenType.REPORTER
    // if the identifier is recognized by the extension.
    def replaceImports(token: Token): Token =
      if (!extensionManager.anyExtensionsLoaded || token.tpe != TokenType.IDENT)
        token
      // look up the replacement.
      else extensionManager.replaceIdentifier(token.value.asInstanceOf[String]) match {
        case null => token
        case prim =>
          val newType =
            if (prim.isInstanceOf[org.nlogo.api.Command])
              TokenType.COMMAND
            else TokenType.REPORTER
          new Token(token.name, newType, token.value)(
            token.startPos, token.endPos, token.fileName)
      }
    tokenizeForColorization(source).map(replaceImports)
  }

  def checkInstructionMaps() { tokenMapper.checkInstructionMaps() }

}
