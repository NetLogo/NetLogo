// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import org.scalatest.FunSuite

class DummyEditorArea(_text: String) extends EditorAreaWrapper {
  def textComponent = null
  var text = _text + "\n"
  private var _caretPosition: Int = 0
  private var selectionStart = 0
  private var selectionEnd = 0
  private var _linesText = text
  private var _lines = text.split("\\n")

  override def getCaretPosition = _caretPosition
  override def setCaretPosition(pos: Int) = {
    _caretPosition = pos
  }
  override def getSelectionStart = selectionStart
  override def setSelectionStart(pos: Int): Unit = {
    selectionStart = pos
  }
  override def getSelectionEnd = selectionEnd
  override def setSelectionEnd(pos: Int): Unit = {
    selectionEnd = pos
  }
  selectAll()
  def lines: Array[String] = {
    if (_linesText eq text) {
      _lines
    } else {
      text = if (text.lastOption.contains('\n')) text else text + "\n"
      _linesText = text
      _lines = _linesText.split("\\n")
      _lines
    }
  }
  // drop the newline we are obligated to have at the end of the last bit of text
  def presentationText = text.dropRight(1)

  def selectAll() { selectionStart = 0; selectionEnd = text.size }

  override def offsetToLine(offset: Int): Int =
    lines.indices.find(lineToEndOffset(_) > offset)
      .getOrElse(lines.size - 1)

  override def lineToStartOffset(line: Int) = lines.take(line).map(_.length + 1).sum

  override def lineToEndOffset(line: Int) = lineToStartOffset(line) + lines(line).length + (if (lines.length > 1) 1 else 0)

  override def getLineOfText(lineNum: Int) = {
    val start = lineToStartOffset(lineNum)
    getText(start, (lineToEndOffset(lineNum) - 1) - start) + "\n"
  }

  override def getText(start: Int, len: Int) = text.substring(start, start + len)

  override def insertString(pos: Int, str: String) { text = text.substring(0, pos) + str + text.substring(pos, text.length) }

  override def replaceSelection(str: String) {
    text = text.take(selectionStart) + str + text.drop(selectionEnd)
    val originalStart = selectionStart
    _caretPosition = selectionStart + str.length
    selectionStart = originalStart + str.length
    selectionEnd   = originalStart + str.length
  }

  override def remove(start: Int, len: Int) { text = text.substring(0, start) + text.substring(start + len, text.length) }

  override def replace(start: Int, len: Int, str: String): Unit = {
    text = text.substring(0, start) + str + text.substring(start + len, text.length)
    _caretPosition = start + str.length
  }

  def replace(start: Int, len: Int, str: String, newCaretPosition: Int): Unit = {
    text = text.substring(0, start) + str + text.substring(start + len, text.length)
    _caretPosition = newCaretPosition
  }
}

class DummyEditorAreaTests extends FunSuite {

  // NOTE: These values were obtained empirically on Java 8u141
  // Please remeasure before adjusting
  test("behaves the same way that EditorAreaWrapper does") {
    val text = "abc\n   \nd\nhij"
    val ea = new DummyEditorArea(text)
    val lineData =
      Seq(
        ((0, 4),   "abc\n"),
        ((4, 8),   "   \n"),
        ((8, 10),  "d\n"),
        ((10, 14), "hij\n"))

    lineData.zipWithIndex.foreach {
      case (((start, end), text), lineNum) =>
        assertResult(start, "expected start offset to match")(ea.lineToStartOffset(lineNum))
        assertResult(end,   "expected end offset to match")(ea.lineToEndOffset(lineNum))
        assertResult(text,  "expected getLineOfText to match")(ea.getLineOfText(lineNum))
        assertResult(text,  "expected getText to match")(ea.getText(start, end - start))
    }
  }
}
