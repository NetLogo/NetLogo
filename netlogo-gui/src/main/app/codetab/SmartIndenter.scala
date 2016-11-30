// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import org.nlogo.api.{ CompilerServices, EditorAreaInterface}
import org.nlogo.core.{ Token, TokenType }
import org.nlogo.editor.Indenter

class SmartIndenter(code: EditorAreaInterface, compiler: CompilerServices)
extends Indenter {

  /// first, the four handle* methods in IndenterInterface

  def handleTab() = {
    val line1 = code.offsetToLine(code.getSelectionStart)
    val line2 = code.offsetToLine(code.getSelectionEnd)
    (line1 to line2).foreach(indentLine)
  }

  def handleEnter() = {
    code.replaceSelection("\n")
    indentLine(code.offsetToLine(code.getSelectionEnd))
  }

  def handleInsertion(s: String) = {
    if(List("e", "n", "d").contains(s.toLowerCase)) {
      val lineNum = code.offsetToLine(code.getSelectionStart)
      if(code.getLineOfText(lineNum).trim.equalsIgnoreCase("end"))
        handleCloseBracket()
    }
  }

  def handleCloseBracket() = handleTab()

  /// private helpers

  private val TAB_WIDTH = 2
  private def indentLine(lineNum: Int) {
    val currentLine = code.getLineOfText(lineNum)
    for(newSpaces <- computeNewSpaces(currentLine, lineNum)) {
      val oldSpaces = countLeadingSpaces(currentLine)
      if(newSpaces != oldSpaces) {
        val lineStart = code.lineToStartOffset(lineNum)
        if(newSpaces > oldSpaces)
          code.insertString(lineStart, spaces(newSpaces - oldSpaces))
        else
          code.remove(lineStart, oldSpaces - newSpaces)
      }
    }
  }

  // None return means "leave it where it is"
  private def computeNewSpaces(currentLine: String,lineNum: Int): Option[Int] = {
    val token = compiler.tokenizeForColorization(currentLine).headOption.orNull
    if(token != null && token.tpe == TokenType.CloseBracket) {
      // first token is close bracket, so find matching opener and set it to the same indent level
      val opener = findMatchingOpenerBackward(
        code.getText(0, code.lineToStartOffset(lineNum) + token.start + 1), 0)
      return Some(countLeadingSpaces(
        code.getLineOfText(
          code.offsetToLine(opener.start))))
    }
    // keywords should always be at the far left.  go ahead and guess if breed is the first token in
    // a line that it is the keyword. we do the same thing in EditorColorizer, sort of. I can think
    // of situations where the breed variable might be the first token in a line, however, they seem
    // quite unusual and maybe you should be formatting your code differently if you run into such a
    // situation :) ev 1/22/08
    if(token != null && (token.tpe == TokenType.Keyword ||
                         token.text.equalsIgnoreCase("breed")))
      return Some(0)
    // if it's not one of the previous two cases the position probably depends at least one line
    // previous unless it's the first line
    if(lineNum == 0) return None
    // find previous line that has a token on it. (ignoring blank lines) if this line does not start
    // with a comment also ignore lines that do cause they might be weird ev 2/25/08
    var prevLineNum = lineNum - 1
    var prevLine = code.getLineOfText(prevLineNum)
    var tokens = tokenize(prevLine)
    var i = prevLineNum - 1
    while(i >= 0 &&
          (tokens.isEmpty ||
           ((token == null || token.tpe != TokenType.Comment) &&
            tokens(0).tpe == TokenType.Comment)))
    {
      prevLineNum = i
      prevLine = code.getLineOfText(prevLineNum)
      tokens = tokenize(prevLine)
      i -= 1
    }
    if(tokens.isEmpty) return None
    // if our line starts with a comment, try to find a comment in prev line to align with
    if(token != null && token.tpe == TokenType.Comment)
      getComment(tokens).foreach(tok => return Some(tok.start))
    var result = countLeadingSpaces(prevLine)
    // if there is such a previous line if it's got an "opener" that has no closer bump this line in
    // Note that value > 0 implies there's unmatched opener but not vice versa.
    // If there's an unmatched opener, we *don't* want to indent off the last
    // command (which is what the else branch does).
    if (findUnmatchedOpener(tokens).isDefined) {
      // We may not want to bump this line if the previous line is something
      // like `  foo ] [`. That's what the following `if` checks for.
      // We drop closers at the beginning because the indenter should have
      // already taken those into account in re-indenting that line.
      if (totalValue(tokens.dropWhile(isCloser)) > 0) { result += TAB_WIDTH }
    } else {
      findCommandForLastCloser(
        prevLine, code.lineToStartOffset(prevLineNum),
        token != null && isOpener(token)).foreach(opener =>
        result = countLeadingSpaces(
          code.getLineOfText(
            code.offsetToLine(opener.start))))
      // look for the command on the previous line if we've got an opener (closed opener) look to
      // see where we are in relation to the command if we're indented past the command move to 1
      // tab stop past if we're before we move to be in line with the command
      val cmd = findCommand(prevLine)
      if (token != null && isOpener(token) && cmd.isDefined &&
        countLeadingSpaces(currentLine) > result)
        return Some(result + TAB_WIDTH)
    }
    Some(result)
  }

  private def countLeadingSpaces(s: String) = s.takeWhile(_ == ' ').size
  private def spaces(n: Int) = List.fill(n)(' ').mkString
  private def totalValue(tokens: List[Token]): Int = tokens.map(value).sum
  private def findUnmatchedOpener(tokens: List[Token]): Option[Token] = {
    def helper(ts: List[Token], stack: Int): Option[Token] = ts match {
      case h :: tail if isOpener(h) => if (stack < 0) helper(tail, stack + 1) else Some(h)
      case h :: tail if isCloser(h) =>                helper(tail, stack - 1)
      case _ :: tail                =>                helper(tail, stack)
      case _ => None
    }
    helper(tokens.reverse, 0)
  }

  private def value(tok: Token) =
    if(isOpener(tok)) 1
    else if(isCloser(tok)) -1
    else 0

  private def findMatchingOpenerBackward(s: String, startDiff: Int): Token = {
    val tokens = tokenize(s)
    var diff = startDiff
    var changedDiffYet = false
    for(tok <- tokens.reverse) {
      if(isCloser(tok)) {
        diff += 1
        changedDiffYet = true
      }
      else if(isOpener(tok)) {
        diff -= 1
        changedDiffYet = true
      }
      if(changedDiffYet && diff == 0)
        return tok
    }
    tokens(0)
  }

  private def findCommandForLastCloser(line: String, offset: Int, findOpener: Boolean): Option[Token] = {
    for(tok <- tokenize(line).reverse)
      if(isCloser(tok)) {
        val opener = findMatchingOpenerBackward(
          code.getText(0, tok.start + offset), 1)
        return Some(if(findOpener) opener
                    else findCommand(code.getText(0, opener.start)).getOrElse(opener))
      }
    None
  }

  private def findCommand(text: String): Option[Token] = {
    var diff = 0
    for(tok <- tokenize(text).reverse) {
      if(isOpener(tok))
        diff -= 1
      else if(isCloser(tok))
        diff += 1
      else if(diff == 0 && tok.tpe == TokenType.Command)
        return Some(tok)
    }
    None
  }

  private def getComment(tokens: List[Token]): Option[Token] =
    tokens.reverse.find(_.tpe == TokenType.Comment)
  private def tokenize(line: String) =
    compiler.tokenizeForColorization(line).toList
  private def isOpener(t: Token) =
    t.tpe == TokenType.OpenParen ||
    t.tpe == TokenType.OpenBracket ||
    t.tpe == TokenType.Keyword &&
      (t.text.equalsIgnoreCase("to") || t.text.equalsIgnoreCase("to-report"))
  private def isCloser(t: Token) =
    t.tpe == TokenType.CloseParen ||
    t.tpe == TokenType.CloseBracket ||
    t.tpe == TokenType.Keyword &&
      t.text.equalsIgnoreCase("end")

}
