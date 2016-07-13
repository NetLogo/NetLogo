// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import java.io.{Reader => JReader, BufferedReader}

import org.nlogo.core.{ NumberParser, SourceLocation, StringEscaper, Token, TokenType }
import TokenLexer.WrappedInput
import LexOperations._

import scala.annotation.tailrec

class TokenLexer {
  import LexOperations.PrefixConversions._
  import TokenLexer._
  import Charset.validIdentifierChar

  private val punctuation = Map(
    "," -> TokenType.Comma,
    "{" -> TokenType.OpenBrace,
    "}" -> TokenType.CloseBrace,
    "(" -> TokenType.OpenParen,
    ")" -> TokenType.CloseParen,
    "[" -> TokenType.OpenBracket,
    "]" -> TokenType.CloseBracket
  )

  def lexerOrdering: Seq[(LexPredicate, TokenGenerator)] =
    Seq(extensionLiteral, punct, comment, numericLiteral, string, ident, illegalCharacter)

  def apply(input: WrappedInput): (Token, WrappedInput) = {
    if (input.hasNext) {
      val r = lexerOrdering.foldLeft((Option.empty[Token], input)) {
        case ((Some(token), remaining), (prefixDetector, tokenizer)) => (Some(token), remaining)
        case ((None,        remaining), (prefixDetector, tokenizer)) =>
          remaining.assembleToken(prefixDetector, tokenizer)
            .map(o => (Some(o._1), o._2))
            .getOrElse((None, remaining))
      }
      (r._1.get, r._2)
    } else
      (Token.Eof, input)
  }

  def fastForwardWhitespace(input: WrappedInput): (Int, WrappedInput) = {
    val (spaces, remainder) = input.longestPrefix({
      c => if (Character.isWhitespace(c)) Accept else Finished
    })
    (spaces.length, remainder)
  }

  class DoubleBracePairMatcher extends LexPredicate {
    var lastChar = Option.empty[Char]
    var nesting = 0
    val detectEnd = withFeedback[(Option[Char], Int)]((lastChar, nesting)) {
      case ((None, 0), '{')              => ((Some('{'), 0), Accept)
      case ((_, n), _) if n < 0          => ((None, n), Error)
      case ((_, n), c) if c == '\r' || c == '\n' => ((Some(c), n), Finished)
      case ((Some('}'), 1), '}')         => ((None, 0), Finished)
      case ((Some('{'), n), '{')         => ((None, n + 1), Accept)
      case ((Some('}'), n), '}')         => ((None, n - 1), Accept)
      case ((Some(c), 0), _) if c != '{' => ((None, 0), Error) // may be able to clean this up
      case ((_, n), c)                   => ((Some(c), n), Accept)
      case _                             => ((None, 0), Error)
    }

    def apply(c: Char): DetectorStates =
      detectEnd(c)
  }

  def extensionLiteral: (LexPredicate, TokenGenerator) = {
    val innerMatcher = new DoubleBracePairMatcher
    (chain(innerMatcher, anyOf('}', '\n', '\r')), tokenizeExtensionLiteral(innerMatcher))
  }

  def punct: (LexPredicate, TokenGenerator) =
    (characterMatching(c => punctuation.isDefinedAt(c.toString)),
      p => punctuation.get(p).map(tpe => (p, tpe, null)))

  def string: (LexPredicate, TokenGenerator) =
    (chain('"', stringLexer, '"') , tokenizeString)

  def stringLexer: LexPredicate =
    withFeedback[Option[Char]](Some('"')) {
      case (Some('\\'), '"') => (Some('"'), Accept)
      case (Some('\\'), '\\') => (None, Accept)
      case (_, '"') => (Some('"'), Finished)
      case (_, c) => (Some(c), Accept)
    }

  def comment: (LexPredicate, TokenGenerator) =
    (chain(';', zeroOrMore(c => c != '\r' && c != '\n')),
      (s => Some((s, TokenType.Comment, null))))

  def numericLiteral: (LexPredicate, TokenGenerator) =
    (chain(
      anyOf(characterMatching(Character.isDigit),
        chain(anyOf('.', '-'),
          anyOf(characterMatching(Character.isDigit), chain('.', characterMatching(Character.isDigit))))),
      zeroOrMore(c => validIdentifierChar(c))),
      tokenizeLiteral)

  def ident: (LexPredicate, TokenGenerator) =
    (oneOrMore(validIdentifierChar), tokenizeIdent)

  def illegalCharacter: (LexPredicate, TokenGenerator) =
    // if we've gotten to this point, we have a bad character
    (aSingle(c => Accept), {s => Some((s, TokenType.Bad, "This non-standard character is not allowed.")) } )

  private def tokenizeExtensionLiteral(innerMatcher: DoubleBracePairMatcher)(literalString: String): Option[(String, TokenType, AnyRef)] =
    if (literalString.take(2) != "{{")
      None
    else if (literalString.last == '\n' || literalString.last == '\r')
      Some(("", TokenType.Bad, "End of line reached unexpectedly"))
    else if (literalString.foldLeft((0, Option.empty[Char])) {
          case ((nesting, lastChar), currentChar) =>
            (lastChar, currentChar) match {
              case (Some('{'), '{') => (nesting + 1, None)
              case (Some('}'), '}') => (nesting - 1, None)
              case _ => (nesting, Some(currentChar))
            }
        }._1 > 0)
      Some(("", TokenType.Bad, "End of file reached unexpectedly"))
    else
      Some((literalString, TokenType.Extension, literalString))

  private def tokenizeLiteral(literalString: String): Option[(String, TokenType, AnyRef)] =
    if (literalString.exists(Character.isDigit))
      NumberParser.parse(literalString) match {
        case Left(error) => Some((literalString, TokenType.Bad, error))
        case Right(literal) => Some((literalString, TokenType.Literal, literal))
      }
    else
      None

  private def tokenizeIdent(identString: String): Option[(String, TokenType, AnyRef)] =
    Some((identString, TokenType.Ident, identString.toUpperCase))

  private def tokenizeString(stringText: String): Option[(String, TokenType, AnyRef)] = {
    val lastCharEscaped = stringText.dropRight(1).foldLeft(false) {
      case (true, '\\') => false
      case (_, '\\') => true
      case _ => false
    }
    try {
      if (stringText.last != '"' || lastCharEscaped)
        Some((s"""$stringText""", TokenType.Bad, "Closing double quote is missing"))
      else {
        val unescapedText = StringEscaper.unescapeString(stringText.drop(1).dropRight(1))
        Some(( s"""$stringText""", TokenType.Literal, unescapedText))
      }
    } catch {
      case ex: IllegalArgumentException => Some(( s"""$stringText""", TokenType.Bad, "Illegal character after backslash"))
    }
  }

  def wrapInput(reader: JReader, filename: String): WrappedInput =
    reader match {
      case br: BufferedReader => new BufferedInputWrapper(br, 0, filename)
      case r => new BufferedInputWrapper(reader, 0, filename)
    }

  class AutoGrowingBufferedReader(reader: BufferedReader) {
    private var markSize: Int = 65536
    private var remainingMark: Int = 0

    def mark(): Unit = {
      reader.mark(markSize)
      remainingMark = markSize
    }

    def reset(): Unit = {
      reader.reset()
      remainingMark = markSize
    }

    def skip(l: Long): Unit = {
      reader.skip(l)
      remainingMark -= l.toInt
    }

    def read(): Int = {
      if (remainingMark == 0) {
        reader.reset()
        reader.mark(markSize * 2)
        reader.skip(markSize)
        remainingMark = markSize
        markSize = markSize * 2
      }
      val i = reader.read()
      remainingMark -= 1
      i
    }
  }

  class BufferedInputWrapper(buffReader: AutoGrowingBufferedReader, var offset: Int, val filename: String) extends WrappedInput {
    def this(input: BufferedReader, offset: Int, filename: String) = {
      this(new AutoGrowingBufferedReader(input), offset, filename)
    }
    def this(input: JReader, offset: Int, filename: String) = {
      this(new BufferedReader(input, 65536), offset, filename)
    }

    def nextChar: Option[Char] = {
      val readChar = buffReader.read()
      if (readChar == -1)
        None
      else
        Some(readChar.asInstanceOf[Char])
    }

    @tailrec private def longestPrefixTail(p: LexPredicate, acc: String): String =
      nextChar match {
        case Some(c) if p(c).continue => longestPrefixTail(p, acc + c)
        case _ => acc
      }

    override def hasNext: Boolean = {
      buffReader.mark()
      val r = buffReader.read() != -1
      buffReader.reset()
      r
    }

    override def assembleToken(p: LexPredicate, f: TokenGenerator): Option[(Token, WrappedInput)] = {
      val originalOffset = offset
      val (prefix, remainder) = longestPrefix(p)
      (prefix match {
        case "" => None
        case nonEmptyString => f(nonEmptyString).map {
          case (text, tpe, tval) => (new Token(text, tpe, tval)(SourceLocation(originalOffset, remainder.offset, filename)), this)
        }
      }) orElse {
        buffReader.reset()
        offset = originalOffset
        None
      }
    }

    override def longestPrefix(f: LexPredicate): (String, WrappedInput) = {
      buffReader.mark()
      val (a, b) = (longestPrefixTail(f, ""), this)
      buffReader.reset()
      buffReader.skip(a.length) // we always go "one too far", so we have to backup
      offset += a.length
      (a, b)
    }
  }
}

trait StandardLexer extends TokenLexer {
  override def apply(input: WrappedInput): (Token, WrappedInput) = {
    val (wsCount, remainder) = fastForwardWhitespace(input)
    super.apply(remainder)
  }
}

object StandardLexer extends StandardLexer

object WhitespaceSkippingLexer extends StandardLexer {
  override def apply(input: WrappedInput): (Token, WrappedInput) = {
    val (t, endOfToken) = super.apply(input)
    val (_, beginningOfNextToken) = fastForwardWhitespace(endOfToken)
    (t, beginningOfNextToken)
  }
}

object WhitespaceTokenizingLexer extends TokenLexer {
  override def lexerOrdering: Seq[(LexPredicate, TokenGenerator)] =
    whitespace +: super.lexerOrdering

  def whitespace: (LexPredicate, TokenGenerator) =
    (oneOrMore(Character.isWhitespace _), (s) => if (s.nonEmpty) Some((s, TokenType.Whitespace, s)) else None)
}

object TokenLexer {
  trait WrappedInput {
    def hasNext: Boolean
    def offset: Int
    def filename: String
    def longestPrefix(f: LexPredicate): (String, WrappedInput)
    def assembleToken(p: LexPredicate, f: TokenGenerator): Option[(Token, WrappedInput)]
  }
}
