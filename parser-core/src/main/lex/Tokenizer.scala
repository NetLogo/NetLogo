// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.core.{ Token, TokenizerInterface },
  TokenLexer.WrappedInput

// caller's responsibility to check for TokenType.Bad!

object Tokenizer extends TokenizerInterface {
  def tokenizeString(source: String, filename: String = ""): Iterator[Token] =
    new TokenLexIterator(StandardLexer, new java.io.StringReader(source), filename).map(_._1)

  def tokenize(reader: java.io.Reader, filename: String = ""): Iterator[Token] =
    new TokenLexIterator(StandardLexer, reader, filename).map(_._1)

  def tokenizeSkippingTrailingWhitespace(reader: java.io.Reader, filename: String = ""): Iterator[(Token, Int)] = {
    var lastOffset = 0
    new TokenLexIterator(WhitespaceSkippingLexer, reader, filename).map {
      case (t, i) =>
        val r = (t, i.offset - lastOffset - (t.end - t.start))
        lastOffset = i.offset
        r
    }
  }

  private class TokenLexIterator(lexer: TokenLexer, reader: java.io.Reader, filename: String)
    extends Iterator[(Token, WrappedInput)] {
    private var lastToken = Option.empty[Token]
    private var lastInput = lexer.wrapInput(reader, filename)

    override def hasNext: Boolean = ! lastToken.contains(Token.Eof)

    override def next(): (Token, WrappedInput) = {
      val (t, i) = lexer(lastInput)
      lastToken = Some(t)
      lastInput = i
      (t, i)
    }
  }
}
