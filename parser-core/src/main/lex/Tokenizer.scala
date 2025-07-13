// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import java.io.Reader
import java.text.CharacterIterator

import org.nlogo.core.{ Token, TokenType, TokenizerInterface }

// caller's responsibility to check for TokenType.Bad!

object Tokenizer extends TokenizerInterface {
  // WrapStringInput splits between jvm and js. JVM has access to
  // StringCharacterIterator (passed as a dependency to CharacterIteratorInput).
  // StringCharacterIterator, isn't available in JS, so we use StringReader
  // instead. If lexing is too slow, you can probably get speed (both js and jvm)
  // by writing a WrappedInput which interacts directly with string. RG 5/4/17
  def tokenizeString(source: String, filename: String = ""): Iterator[Token] =
    new TokenLexIterator(StandardLexer, WrapStringInput(source, filename)).map(_._1)

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
        case ts => throw new IllegalStateException
      }
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
    new TokenLexIterator(WhitespaceTokenizingLexer, WrapStringInput(source, filename)).map(_._1)

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
