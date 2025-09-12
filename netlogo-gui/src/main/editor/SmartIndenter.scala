// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.util.Locale

import org.nlogo.api.{ CompilerServices, EditorAreaInterface}
import org.nlogo.core.{ Token, TokenType }

import scala.collection.BufferedIterator

class SmartIndenter(code: EditorAreaInterface, compiler: CompilerServices) extends Indenter {
  private val TAB_WIDTH = 2

  // TokenizedLine represents a line that has been tokenized and is ready to be evaluated
  // for indentation.
  case class TokenizedLine(lineNum: Int, lineStart: Int, lineEnd: Int, text: String, leadingSpaces: Int, tokens: Seq[Token]) {
    // bracketDelta lists the change in number of brackets.
    // "" = 0, "[" = 1, "]" = -1, "[]" = 0, "[[" = 2, &c
    lazy val bracketDelta = tokens.map(_.tpe).foldLeft(0) {
      case (i, (TokenType.OpenBracket | TokenType.OpenParen))  => i + 1
      case (i, (TokenType.CloseBracket | TokenType.CloseParen)) => i - 1
      case (i, _) => i
    }
    // bracketsClosed lists the number of unmatched closing brackets on this line.
    // "" = 0, "[" = 0, "]" = 1, "[]" = 0, "]]" = 2, "[]]" = 1, &c
    lazy val bracketsClosed: Int = {
      val (_, closedCount) = tokens.map(_.tpe).foldLeft((0, 0)) {
        case ((currentDelta, maxClosed), (TokenType.CloseBracket | TokenType.CloseParen)) =>
          (currentDelta + 1, maxClosed max (currentDelta + 1))
        case ((currentDelta, maxClosed), (TokenType.OpenBracket | TokenType.OpenParen)) =>
          (currentDelta - 1, maxClosed)
        case ((currentDelta, maxClosed), _) => (currentDelta, maxClosed)
      }
      closedCount
    }
    lazy val finalCommentStart: Option[Int] =
      tokens.lastOption.filter(_.tpe == TokenType.Comment).map(_.start - lineStart)
    lazy val isOnlyComment: Boolean = tokens.length == 1 && tokens.head.tpe == TokenType.Comment
  }

  // LineIndent and the classes that extend it track operations needing to be performed on
  // a particular line.
  sealed trait LineIndent {
    def lineNum: Int
    def lineStart: Int
    def lineEnd: Int
    def text: String
    def delta: Int
    def caretOffset: Int
  }
  case class AddIndent(lineNum: Int, lineStart: Int, lineEnd: Int, text: String, delta: Int, caretOffset: Int)
    extends LineIndent
  case class MaintainIndent(lineNum: Int, lineStart: Int, lineEnd: Int, text: String, caretOffset: Int)
    extends LineIndent {
    def delta: Int = 0
  }
  case class RemoveIndent(lineNum: Int, lineStart: Int, lineEnd: Int, text: String, delta: Int, caretOffset: Int)
    extends LineIndent

  /// first, the five handle* methods in IndenterInterface
  def handleTab(): Unit = {
    code.beginCompoundEdit()
    val indentations = lineIndentation(code.getSelectionStart, code.getSelectionEnd)
    if (indentations.nonEmpty) {
      executeIndentations(indentations, Some(code.getCaretPosition))
    }
    code.endCompoundEdit()
  }

  def handleUntab(): Unit = {
    code.beginCompoundEdit()
    val indentations = lineIndentation(code.getSelectionStart, code.getSelectionEnd)
    if (indentations.nonEmpty) {
      executeIndentations(indentations, Some(code.getCaretPosition))
    }
    code.endCompoundEdit()
  }

  def handleEnter(): Unit = {
    code.beginCompoundEdit()
    code.replaceSelection("\n")
    val originalCaretPosition = code.getCaretPosition
    val line = code.offsetToLine(code.getSelectionEnd)
    val start = code.lineToStartOffset(line)
    val end = code.lineToEndOffset(line)
    // end - 1 because the editor includes newlines as part of the text given in a line
    val indentations = lineIndentation(start, end - 1)
    if (indentations.nonEmpty) {
      executeIndentations(indentations, Some(originalCaretPosition))
    }
    code.endCompoundEdit()
  }

  def handleInsertion(s: String): Unit = {
    code.beginCompoundEdit()
    if(List("e", "n", "d").contains(s.toLowerCase)) {
      val lineNum = code.offsetToLine(code.getSelectionStart)
      if(code.getLineOfText(lineNum).trim.equalsIgnoreCase("end"))
        indentSelectedLine()
    }
    code.endCompoundEdit()
  }

  def handleCloseBracket(): Unit = {
    code.beginCompoundEdit()
    code.replaceSelection("]")
    indentSelectedLine()
    code.endCompoundEdit()
  }

  def indentSelectedLine(): Unit = {
    val indentations = lineIndentation(code.getSelectionStart, code.getSelectionEnd)
    if (indentations.nonEmpty) {
      executeIndentations(indentations, Some(code.getCaretPosition))
    }
  }

  /// private helpers
  private def executeIndentations(indentations: Seq[LineIndent], caretPosition: Option[Int]): Unit = {
    val start = indentations.head.lineStart
    val finish = indentations.last.lineEnd
    val builder = new StringBuilder(finish - start)
    indentations.foldLeft(0) { (offset: Int, indent: LineIndent) =>
      val newOffset = (offset, indent) match {
        case (offset, AddIndent(_, _, _, text, delta, _)) =>
          builder.append(spaces(delta))
          builder.append(text)
          (offset + delta)
        case (offset, RemoveIndent(_, _, _, text, delta, _)) =>
          if (text.length > - delta) {
            builder.append(text.substring(- delta, text.length))
          }
          (offset + delta)
        case (offset, MaintainIndent(_, _, _, text, _)) =>
          builder.append(text)
          offset
      }
      builder.append("\n")
      newOffset
    }
    // Remove trailing newline
    builder.delete(builder.length - 1, builder.length)
    if (caretPosition.isDefined) {
      caretPosition.foreach { startCaretPosition =>
        val startLine = code.offsetToLine(startCaretPosition)
        val finalCaretPosition =
          if (startCaretPosition >= start) {
            val caretOffset = indentations.foldLeft(0) {
              case (offset, indent) if indent.lineStart <= startCaretPosition + 1 =>
                offset + indent.delta + indent.caretOffset
              case (offset, indent) => offset
            }
            startCaretPosition + caretOffset max 0
          } else {
            startCaretPosition
          }
        code.replace(start, finish - start, builder.toString)
        code.setCaretPosition(finalCaretPosition.max(code.lineToStartOffset(startLine)))
      }
    } else {
      code.replace(start, finish - start, builder.toString)
    }
  }

  private def lineIndentation(startOffset: Int, endOffset: Int): Seq[LineIndent] = {
    val endLine = code.offsetToLine(endOffset)
    // we actually indent to the end of the endLine
    val endOfEndLine = code.lineToEndOffset(endLine)
    val tokensFromZero = compiler.tokenizeForColorizationIterator(code.getText(0, endOfEndLine)).buffered
    val tokLineIter = new TokenLineIterator(tokensFromZero, endLine)

    // this fold has several accumulation variables
    // _1: List[Int] - indent levels, stored as a stack. Initially empty, changed by the values present in each line
    // _2: Option[TokenizedLine] - priorLine. The priorLine is sometimes needed to compute line spaces.
    // _3: Seq[LineIndent] - accumulator of line indents. This is accumulated last->first and reversed after folding
    val initialFoldParams = (List.empty[Int], Option.empty[TokenizedLine], Seq.empty[LineIndent])
    val (_, _, lineIndents) = tokLineIter.foldLeft(initialFoldParams)(foldLineIndentations)
    lineIndents.reverse.dropWhile(_.lineEnd < startOffset)
  }

  private def foldLineIndentations(acc: (List[Int], Option[TokenizedLine], Seq[LineIndent]), line: TokenizedLine): (List[Int], Option[TokenizedLine], Seq[LineIndent]) = {
    val (indentLevels, priorLine, indentAcc) = acc
    val thisIndent = lineIndentationLevel(indentLevels, line, priorLine, indentAcc.headOption)
    val newIndentLevels = indentationChange(indentLevels, line, priorLine, indentAcc.headOption)
    val caretOffset = {
      if (line.lineStart <= code.getCaretPosition && line.lineEnd > code.getCaretPosition && line.tokens.isEmpty) {
        line.leadingSpaces - (code.getCaretPosition - line.lineStart)
      } else {
        0
      }
    }
    val newIndentAcc: Seq[LineIndent] = {
      if ((thisIndent - line.leadingSpaces) == 0) {
        MaintainIndent(line.lineNum, line.lineStart, line.lineEnd, line.text, caretOffset) +: indentAcc
      } else if (thisIndent - line.leadingSpaces > 0) {
        AddIndent(line.lineNum, line.lineStart, line.lineEnd, line.text, thisIndent - line.leadingSpaces, caretOffset)
          +: indentAcc
      } else {
        RemoveIndent(line.lineNum, line.lineStart, line.lineEnd, line.text, thisIndent - line.leadingSpaces,
                     caretOffset) +: indentAcc
      }
    }
    (newIndentLevels, Some(line), newIndentAcc)
  }

  private def lineIndentationLevel(indentLevels: List[Int], line: TokenizedLine, priorLine: Option[TokenizedLine], priorAdjustment: Option[LineIndent]): Int = {
    val priorIndentLevel = indentLevels.headOption.getOrElse(0)
    line.tokens.headOption.map(_.tpe) match {
      case Some(TokenType.Keyword) => 0
      case Some(TokenType.CloseBracket) if line.bracketDelta == 0 =>
        (priorIndentLevel - TAB_WIDTH) max 0
      case Some((TokenType.CloseBracket | TokenType.CloseParen)) if line.bracketDelta < 0 =>
        if (indentLevels.length > line.bracketsClosed)
          (indentLevels(line.bracketsClosed) max 0)
        else
          (indentLevels.lastOption.getOrElse(2) - TAB_WIDTH)
      case Some(TokenType.Comment) if line.isOnlyComment =>
        (for {
          lastLine <- priorLine
          adjustment <- priorAdjustment
          finalCommentStart <- lastLine.finalCommentStart
        } yield (finalCommentStart + adjustment.delta)).getOrElse(priorIndentLevel)
      case Some(TokenType.OpenBracket) =>
        preIndentationAmount(priorIndentLevel, line, priorLine, priorAdjustment)
          .getOrElse(priorIndentLevel)
      case _ => priorIndentLevel
    }
  }

  private def preIndentationAmount(priorIndentLevel: Int, line: TokenizedLine, priorLine: Option[TokenizedLine], priorAdjustment: Option[LineIndent]): Option[Int] = {
    if (line.leadingSpaces > priorIndentLevel) {
      for {
        lineAbove <- priorLine if lineAbove.tokens.headOption.exists(_.tpe == TokenType.Command) && lineAbove.bracketDelta == 0
        adjustment <- priorAdjustment
        leadingSpaces = line.leadingSpaces + adjustment.delta
      } yield {
        if (leadingSpaces % TAB_WIDTH == 1) leadingSpaces + (leadingSpaces % TAB_WIDTH)
        else leadingSpaces
      }
    } else
      None
  }

  private def indentationChange(indentLevels: List[Int], line: TokenizedLine, priorLine: Option[TokenizedLine],
                                priorAdjustment: Option[LineIndent]): List[Int] = {
    val priorIndentLevel = indentLevels.headOption.getOrElse(0)
    line.tokens.headOption.map(t => (t.tpe, t.text.toUpperCase(Locale.ENGLISH))) match {
      case Some((TokenType.Keyword, ("TO" | "TO-REPORT"))) => List(TAB_WIDTH)
      case Some((TokenType.Keyword, _)) if line.bracketDelta == 0 => List()
      case Some((TokenType.OpenBracket, _)) if line.bracketDelta > 0 =>
        preIndentationAmount(priorIndentLevel, line, priorLine, priorAdjustment)
          .map(_ + TAB_WIDTH)
          .map(i => List.fill(line.bracketDelta)(i) ++ indentLevels)
          .getOrElse(List.fill(line.bracketDelta)(priorIndentLevel + TAB_WIDTH) ++ indentLevels)
      case Some(_) if line.bracketDelta > 0 => List.fill(line.bracketDelta)(priorIndentLevel + TAB_WIDTH) ++ indentLevels
      case Some(_) if line.bracketDelta < 0 => indentLevels.drop(- line.bracketDelta)
      case _ => indentLevels
    }
  }

  class TokenLineIterator(tokens: BufferedIterator[Token], endLine: Int) extends Iterator[TokenizedLine] {
    var currentLine = 0
    var priorEnd = -1
    def hasNext = currentLine <= endLine
    def next(): TokenizedLine = {
      val lineStart = code.lineToStartOffset(currentLine)
      val rawEnd = code.lineToEndOffset(currentLine)
      while (tokens.head.end < lineStart) {
        tokens.next()
      }
      val rawText = code.getText(lineStart, rawEnd - lineStart)
      val (text, lineEnd) =
        if (rawText.nonEmpty && rawText.last == '\n')
          (rawText.substring(0, rawEnd - (lineStart + 1)), rawEnd - 1)
        else (rawText, rawEnd)
      val spaces = countLeadingSpaces(text)
      val tokLine =
        if (tokens.head.start > lineEnd) {
          TokenizedLine(currentLine, lineStart, lineEnd, text, spaces, Seq.empty[Token])
        } else {
          var lineTokensReversed = Seq.empty[Token]
          while (tokens.hasNext && tokens.head.start < lineEnd) {
            lineTokensReversed = tokens.next() +: lineTokensReversed
          }
          TokenizedLine(currentLine, lineStart, lineEnd, text, spaces, lineTokensReversed.reverse)
        }
      priorEnd = lineEnd
      currentLine += 1
      tokLine
    }
  }

  private def countLeadingSpaces(s: String) = s.takeWhile(_ == ' ').size
  private def spaces(n: Int) = List.fill(n)(' ').mkString
}
