// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{CompilerException, File, FileMode}

// This exists to support the file-read primitive, which uses LiteralParser.
// This tracks the position of the file against the position of the Iterator[token]

class TokenReader(file: File, tokenizer: core.TokenizerInterface)
extends Iterator[core.Token] {

  // code elsewhere is expected to detect eof for us
  def hasNext = true

  val reader = file.reader
  val tokenIterator = tokenizer.tokenizeSkippingTrailingWhitespace(reader)

  def next(): core.Token = {
    val (t, ws) = tokenIterator.next()
    if (t.tpe == core.TokenType.Bad)
      throw new CompilerException(t)
    file.pos += (t.end - t.start + ws)
    t
  }

}
