// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import java.io.{ Reader, StringReader }
import java.text.CharacterIterator

import org.nlogo.core.{ Token, TokenType, TokenizerInterface }

// caller's responsibility to check for TokenType.Bad!

object Tokenizer extends TokenizerInterface {
  def tokenizeString(source: String, filename: String = ""): Iterator[Token] =
    new TokenLexIterator(StandardLexer, WrappedInput(new StringReader(source), filename)).map(_._1)

  def tokenize(reader: Reader, filename: String = ""): Iterator[Token] =
    new TokenLexIterator(StandardLexer, WrappedInput(reader, filename)).map(_._1)

  def getTokenAtPosition(source: String, position: Int): Option[Token] = {
    val interestingTokenTypes =
      Seq(TokenType.Ident, TokenType.Command, TokenType.Keyword, TokenType.Reporter)
    tokenizeString(source)
      .sliding(2)
      .find { ts =>
        ts.head.start <= position && ts.head.end >= position && ts.head.tpe != TokenType.Eof
      }
      .map {
        case Seq(a, b) if a.end    <= position && ! interestingTokenTypes.contains(b.tpe) => a
        case Seq(a, b) if position <  a.end => a
        case Seq(a, b)                      => b
      }
  }

  def isValidIdentifier(ident: String): Boolean = {
    val is = tokenizeString(ident)
    is.next.tpe == TokenType.Ident && is.next.tpe == TokenType.Eof
  }

  def tokenizeSkippingTrailingWhitespace(reader: Reader, filename: String = ""): Iterator[(Token, Int)] = {
    var lastOffset = 0
    new TokenLexIterator(WhitespaceSkippingLexer, WrappedInput(reader, filename)).map {
      case (t, i) =>
        val r = (t, i.offset - lastOffset - (t.end - t.start))
        lastOffset = i.offset
        r
    }
  }

  // Returns an Iterator[Token] which includes tokens with tpe == TokenType.Whitespace.
  // The other tokenize methods will not include tokens of this type.
  def tokenizeWithWhitespace(reader: Reader, filename: String): Iterator[Token] =
    new TokenLexIterator(WhitespaceTokenizingLexer, WrappedInput(reader, filename)).map(_._1)

  def tokenizeWithWhitespace(source: String, filename: String): Iterator[Token] =
    new TokenLexIterator(WhitespaceTokenizingLexer, WrappedInput(new StringReader(source), filename)).map(_._1)

  def tokenizeWithWhitespace(iter: CharacterIterator, filename: String): Iterator[Token] =
    new TokenLexIterator(WhitespaceTokenizingLexer, WrappedInput(iter, filename)).map(_._1)

  private class TokenLexIterator(lexer: TokenLexer, initialInput: WrappedInput)
    extends Iterator[(Token, WrappedInput)] {
    private var lastToken = Option.empty[Token]
    private var lastInput = initialInput

    override def hasNext: Boolean = ! lastToken.contains(Token.Eof)

    override def next(): (Token, WrappedInput) = {
      val (t, i) = lexer(lastInput)
      lastToken = Some(t)
      lastInput = i
      (t, i)
    }
  }
}
