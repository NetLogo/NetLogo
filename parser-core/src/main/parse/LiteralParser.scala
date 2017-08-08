// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{ LiteralImportHandler, Token, TokenType },
  core.Fail._

/**
 * The literal parser.
 * This class contains methods which are used to parse literal NetLogo values
 * from a Iterator[Token]. (It hands off all the complicated import-world stuff
 * involving literal agents and literal agentsets to LiteralAgentParser.)
 */
class LiteralParser(importHandler: LiteralImportHandler) {

  /// all error messages used in this class
  private val ERR_EXPECTED_CLOSEPAREN = "Expected a closing parenthesis."
  private val ERR_EXPECTED_LITERAL = "Expected a literal value."
  private val ERR_EXPECTED_NUMBER = "Expected a number."
  private val ERR_EXTRA_STUFF_AFTER_LITERAL = "Extra characters after literal."
  private val ERR_EXTRA_STUFF_AFTER_NUMBER = "Extra characters after number."
  private val ERR_MISSING_CLOSEBRACKET = "No closing bracket for this open bracket."

  /**
  * reads a literal value from a token vector. The entire vector must denote a single literal
  * value; extra garbage at the end is illegal. Used for read-from-string and other things.
  * @param tokens the input tokens
  */
  def getLiteralValue(tokens: Iterator[Token]): AnyRef = {
    val result = readLiteralPrefix(tokens.next(), tokens)
    // make sure there's no extra stuff at the end...
    val extra = tokens.next()
    cAssert(extra.tpe == TokenType.Eof, ERR_EXTRA_STUFF_AFTER_LITERAL, extra)
    result
  }

  // Reads in a literal from a File.
  def getLiteralFromFile(tokens: Iterator[Token]): AnyRef =
    readLiteralPrefix(tokens.next(), tokens)

  def getNumberValue(tokens: Iterator[Token]) = {
    val token = tokens.next()
    if(token.tpe != TokenType.Literal || !token.value.isInstanceOf[java.lang.Double])
      exception(ERR_EXPECTED_NUMBER, token)
    val extra = tokens.next()
    cAssert(extra.tpe == TokenType.Eof, ERR_EXTRA_STUFF_AFTER_NUMBER, extra)
    token.value.asInstanceOf[java.lang.Double]
  }

 /**
  * reads a literal value from the beginning of a token vector. This
  * method leaves the rest of the token vector intact (i.e., extra garbage
  * after the literal is OK).
  *
  * @param tokens the input tokens
  */
  def readLiteralPrefix(token: Token, tokens: Iterator[Token]): AnyRef = {
    token.tpe match {
      case TokenType.Extension =>
        importHandler.parseExtensionLiteral(token)
      case TokenType.Literal =>
        token.value
      case TokenType.OpenBracket =>
        val (result, _) = parseLiteralList(token, tokens)
        result
      case TokenType.OpenBrace =>
        importHandler.parseLiteralAgentOrAgentSet(tokens, readLiteralPrefix)
      case TokenType.OpenParen =>
        val result = readLiteralPrefix(tokens.next(), tokens)
        // if next is anything else other than ), we complain and point to the next token
        // itself. since we don't do syntax highlighting, it doesn't matter so much what the token
        // is, and we use a message which doesn't rely on that context.
        val closeParen = tokens.next()
        cAssert(closeParen.tpe == TokenType.CloseParen, ERR_EXPECTED_CLOSEPAREN, closeParen)
        result
      case TokenType.Comment =>
        // just skip comments when reading a literal - ev 7/10/07
        readLiteralPrefix(tokens.next(), tokens)
      case _ =>
        exception(ERR_EXPECTED_LITERAL, token)
    }
  }

 /**
  * parses a literal list. Assumes the open bracket was already eaten.  Eats the list
  * contents and the close bracket; returns a LogoList and the close bracket token.
  */
  def parseLiteralList(openBracket: Token, tokens: Iterator[Token]): (core.LogoList, Token) = {
    var list = core.LogoList()
    var closeBracket: Option[Token] = None
    while(!closeBracket.isDefined) {
      val token = tokens.next()
      token.tpe match {
        case TokenType.CloseBracket => closeBracket = Some(token)
        case TokenType.Eof => exception(ERR_MISSING_CLOSEBRACKET, openBracket)
        case _ => list = list.lput(readLiteralPrefix(token, tokens))
      }
    }
    (list, closeBracket.get)
  }
}
