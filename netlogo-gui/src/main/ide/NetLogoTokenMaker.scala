// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import javax.swing.text.Segment

import org.fife.ui.rsyntaxtextarea.{ TokenMakerBase }
import org.fife.ui.rsyntaxtextarea.{ Token => RstaToken, TokenImpl, TokenTypes }

import org.nlogo.api.{ NetLogoLegacyDialect, NetLogoThreeDDialect }
import org.nlogo.core.{ Dialect, Femto, Nobody, Token, TokenizerInterface, TokenType }
import org.nlogo.nvm.ExtensionManager

trait NetLogoTokenMaker extends TokenMakerBase {
  def extensionManager: Option[ExtensionManager]
  def dialect: Dialect

  val tokenizer = Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer")
  val namer = Femto.scalaSingleton[Token => Token]("org.nlogo.parse.Namer0")

  def literalToken(value: AnyRef): Int =
    value match {
      case s: String            => TokenTypes.LITERAL_STRING_DOUBLE_QUOTE
      case d: java.lang.Double  => TokenTypes.LITERAL_NUMBER_FLOAT
      case b: java.lang.Boolean => TokenTypes.LITERAL_BOOLEAN
      case Nobody               => TokenTypes.LITERAL_BACKQUOTE
      case _                    => TokenTypes.IDENTIFIER
    }

  def rstaTokenType(t: Token, firstOnLine: Boolean): Int = {
    import TokenType._
    def isBreedVariable = {
      (dialect.agentVariables.implicitObserverVariableTypeMap.keySet ++
      dialect.agentVariables.implicitTurtleVariableTypeMap.keySet ++
      dialect.agentVariables.implicitPatchVariableTypeMap.keySet ++
      dialect.agentVariables.implicitLinkVariableTypeMap.keySet).contains(t.text.toUpperCase)
    }

    def tagToken(t: Token): Token = {
      val named = namer(t)
      named.tpe match {
        case TokenType.Ident =>
          extensionManager.flatMap { manager =>
            manager.cachedType(t.text.toUpperCase)
              .map(tokenType => t.copy(tpe = tokenType)())
          }.getOrElse(t)
        case _ => named
      }
    }

    val punctType = TokenTypes.SEPARATOR
    t.tpe match {
      case OpenParen | CloseParen | OpenBracket | CloseBracket | OpenBrace | CloseBrace | Comma => punctType
      case Literal    => literalToken(t.value)
      case Ident      =>
        val namedToken = tagToken(t)
        namedToken.tpe match {
          case Command  => TokenTypes.OPERATOR
          case Reporter => TokenTypes.FUNCTION
          case Keyword  => TokenTypes.RESERVED_WORD
          case Literal  => literalToken(namedToken.value)
          case _        =>
            if (dialect.tokenMapper.getCommand(t.text.toUpperCase).isDefined)
              TokenTypes.OPERATOR
            else if (dialect.tokenMapper.getReporter(t.text.toUpperCase).isDefined)
              TokenTypes.FUNCTION
            else if (t.text.equalsIgnoreCase("BREED") && firstOnLine)
              TokenTypes.RESERVED_WORD
            else if (isBreedVariable)
              TokenTypes.FUNCTION
            else
              TokenTypes.IDENTIFIER
        }
      case Command    => TokenTypes.OPERATOR
      case Reporter   => TokenTypes.FUNCTION
      case Keyword    => TokenTypes.RESERVED_WORD
      case Comment    => TokenTypes.COMMENT_KEYWORD
      case Bad        => TokenTypes.ERROR_IDENTIFIER
      case Extension  => TokenTypes.IDENTIFIER
      case Whitespace => TokenTypes.WHITESPACE
      case Eof        => TokenTypes.NULL
    }
  }

  // trying to use `resetTokenList()` directly causes the following scala compiler error:
  // "Implementation restriction: trait NetLogoTokenMaker accesses protected method resetTokenList inside a concrete trait method."
  // Adding this proxy method to each class extending class TokenMakerBase works around the issue.
  def _resetTokenList(): Unit

  def getTokenList(seg: Segment, initialTokenType: Int, offset: Int): RstaToken = {
    _resetTokenList()

    val offsetShift = - seg.offset + offset

    seg.setIndex(seg.getBeginIndex) // reset Segment

    def netlogoTokenToRstaToken(netLogoToken: Token, lastToken: Option[TokenImpl]): TokenImpl = {
      val next = new TokenImpl(seg, netLogoToken.start, netLogoToken.end - 1, offsetShift + netLogoToken.start, rstaTokenType(netLogoToken, lastToken.isEmpty), 0)
      lastToken.foreach { last =>
        last.setNextToken(next)
      }
      next
    }

    val tokens = tokenizer.tokenizeWithWhitespace(seg, "").filter(_.tpe != TokenType.Eof)

    val firstRstaToken =
      if (tokens.hasNext) netlogoTokenToRstaToken(tokens.next(), None)
      else new TokenImpl(seg, seg.getIndex, seg.getIndex, offset, TokenTypes.NULL, 0)

    tokens.foldLeft(firstRstaToken) {
      case (rstaToken, nlToken) => netlogoTokenToRstaToken(nlToken, Some(rstaToken))
    }

    firstRstaToken
  }
}

class NetLogoTwoDTokenMaker(val extensionManager: Option[ExtensionManager]) extends NetLogoTokenMaker {
  def this() = this(None)

  def dialect = NetLogoLegacyDialect

  def _resetTokenList(): Unit = { super.resetTokenList() }
}

class NetLogoThreeDTokenMaker(val extensionManager: Option[ExtensionManager]) extends NetLogoTokenMaker {
  def this() = this(None)

  def dialect = NetLogoThreeDDialect

  def _resetTokenList(): Unit = { super.resetTokenList() }
}
