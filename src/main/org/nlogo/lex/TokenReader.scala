// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.api.{ CompilerException, File, FileMode, Token, TokenizerInterface, TokenReaderInterface, TokenType }
import java.io.IOException

// This exists to support the file-read primitive, which uses LiteralParser.  During normal
// compilation we just slurp all of the code into memory before doing any parsing, but it
// wouldn't be OK for file-read to slurp a whole data file, so LiteralParser uses Iterator[Token].

class TokenReader(file: File, tokenizer: TokenizerInterface) extends TokenReaderInterface {
  def hasNext = true // code elsewhere is expected to detect eof for us
  // Now for the tricky part.  This class is designed to work with api.File, which
  // provids a BufferedReader but also maintains its own notion of current position with the
  // file (LocalFile.pos).  The BufferedReader and file.pos need to be kept in sync.
  // LocalFile is very old and crufty code and it's probably full of unneeded complexity,
  // but until that gets cleaned up, here's what we do to keep LocalFile happy.
  // It may be that the approach we take is the best that can be done given LocalFile's
  // complexities, but it's also possible that even within that constraint, this stuff
  // doesn't need to be so complex either.  I really don't know. - ST 12/19/08
  def next(): Token = {
    def reader = file.reader // def not val because we close & reopen the file below
    val pos = file.pos
    // here we set an arbitrary ceiling on amount of buffered lookahead we let ourselves do.  we
    // have to set some ceiling. for reasons I don't understand, when we switched from JLex to
    // JFlex, the Grand Canyon's startup procedure (which uses file-read to a slurp a single giant
    // list, 356K characters long) slowed down by a large factor.  JLex and JFlex have their own
    // buffering so maybe JFlex does it differently somehow.  Anyway, raising this ceiling from 4K
    // to 64K made the problem go away.  It's probably still quadratic time and a large enough file
    // would become unreasonably slow again, but 356K is pretty big so I'm not going to worry about
    // it, at least until the day when the whole LocalFile mess gets straightened out. - ST 1/21/09
    reader.mark(65536)
    val t = tokenizer.nextToken(reader)
    if (t.tpe == TokenType.BAD)
      throw new CompilerException(t)
    // after Tokenizer has done its thing, we no longer know what relationship holds between
    // the BufferedReader's position and file.pos, so the following code makes sure they are
    // both correct and in sync with each other.  Above we called reader.mark() so we could
    // get back to a known position in the file; usually returning to the mark is a simple
    // matter of calling reset(), but reset() can fail -- I think it's because we might be
    // trying to return to a point which is before the beginning of the reader's buffer.
    // Originally we didn't have the close/reopen thing, but it was added when we got a bug
    // report from a user having difficulty reading a long file; there are test cases in
    // test/commands/File.txt that cover the bug.
    try { reader.reset() }
    catch {
      case ex: IOException => // token too big to mark; close and reopen file to get back where we were
        file.close(true)
        file.open(FileMode.Read)
        reader.skip(pos)
        file.pos = pos
    }
    // nextToken() makes a new TokenLexer every time, so endPos is just the size of the token,
    // not an absolute position.  so, once we've returned to our original position, before
    // the token was read, token.endPos the amount we need to move both pointers forward
    // in order to be in the position right after the token we read. - ST 12/18/08
    reader.skip(t.endPos)
    file.pos += t.endPos
    t
  }
}
