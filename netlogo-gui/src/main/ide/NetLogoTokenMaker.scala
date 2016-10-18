// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import javax.swing.text.Segment
import java.text.CharacterIterator.DONE

import org.fife.ui.rsyntaxtextarea.{ TokenMakerBase }
import org.fife.ui.rsyntaxtextarea.{ Token => RstaToken, TokenImpl, TokenTypes }

import org.nlogo.api.NetLogoLegacyDialect
import org.nlogo.core.{ Dialect, Femto, SourceLocation, Token, TokenType }
import org.nlogo.lex.{ LexPredicate, LexStates, TokenGenerator, WhitespaceTokenizingLexer, WrappedInput }

import scala.annotation.tailrec

class SegmentWrappedInput(val segment: Segment, var offset: Int) extends WrappedInput {
  def this(segment: Segment) = this(segment, segment.getIndex)
  def hasNext: Boolean = segment.current != DONE
  def filename: String = ""

  @tailrec
  private def longestPrefixTail(p: LexPredicate, acc: String): String = {
    val c = segment.current
    if (c != DONE && p(c).continue) {
      segment.next()
      longestPrefixTail(p, acc + c)
    } else
      acc
  }

  def longestPrefix(f: LexPredicate): (String, WrappedInput) =
    (longestPrefixTail(f, ""), new SegmentWrappedInput(segment))

  def assembleToken(p: LexPredicate, f: TokenGenerator): Option[(Token, WrappedInput)] = {
    val offset = segment.getIndex
    val (prefix, remainder) = longestPrefix(p)
    prefix match {
      case "" => None
      case nonEmptyString =>
        f(nonEmptyString) match {
          case None =>
            segment.setIndex(offset)
            None
          case Some((text, tpe, tval)) =>
            Some((new Token(text, tpe, tval)(SourceLocation(offset, remainder.offset, filename)), remainder))
        }
    }
  }
}

class NetLogoTokenMaker(dialect: Dialect) extends TokenMakerBase {
  def this() = this(NetLogoLegacyDialect)

  val namer = Femto.scalaSingleton[Token => Token]("org.nlogo.parse.Namer0")

  def rstaTokenType(t: Token): Int = {
    import TokenType._

    val punctType = TokenTypes.SEPARATOR
    t.tpe match {
      case OpenParen | CloseParen | OpenBracket | CloseBracket | OpenBrace | CloseBrace | Comma => punctType
      case Literal    => t.value match {
        case s: String           => TokenTypes.LITERAL_STRING_DOUBLE_QUOTE
        case d: java.lang.Double => TokenTypes.LITERAL_NUMBER_FLOAT
        case _                   => TokenTypes.IDENTIFIER
      }
      case Ident      =>
        val namedToken = namer(t)
        namedToken.tpe match {
          case Command  => TokenTypes.OPERATOR
          case Reporter => TokenTypes.FUNCTION
          case Keyword  => TokenTypes.RESERVED_WORD
          case Literal  => namedToken.value match {
            case d: java.lang.Double  => TokenTypes.LITERAL_NUMBER_FLOAT
            case s: java.lang.Boolean => TokenTypes.LITERAL_BOOLEAN
            case _ => TokenTypes.IDENTIFIER
          }
          case _        =>
            if (dialect.tokenMapper.getCommand(t.text.toUpperCase).isDefined)
              TokenTypes.OPERATOR
            else if (dialect.tokenMapper.getReporter(t.text.toUpperCase).isDefined)
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

  def getTokenList(seg: Segment, initialTokenType: Int, offset: Int): RstaToken = {
    resetTokenList()

    val offsetShift = - seg.offset + offset

    seg.setIndex(seg.getBeginIndex) // reset Segment

    def netlogoTokenToRstaToken(netLogoToken: Token, lastToken: Option[TokenImpl]): TokenImpl = {
      val next = new TokenImpl(seg, netLogoToken.start, netLogoToken.end - 1, offsetShift + netLogoToken.start, rstaTokenType(netLogoToken), 0)
      lastToken.foreach { last =>
        last.setNextToken(next)
      }
      next
    }

    var input: WrappedInput = new SegmentWrappedInput(seg)
    var lastToken = Option.empty[TokenImpl]
    var firstToken = Option.empty[TokenImpl]
    while (input.hasNext) {
      val (nextToken, nextInput) = WhitespaceTokenizingLexer(input)
      input = nextInput
      lastToken = Some(netlogoTokenToRstaToken(nextToken, lastToken))
      if (firstToken.isEmpty)
        firstToken = lastToken
    }
    firstToken.getOrElse(new TokenImpl(seg, seg.getIndex, seg.getIndex, offset, TokenTypes.NULL, 0))
  }
}
