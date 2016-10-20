// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.text.PlainDocument

import org.scalatest.FunSuite

import org.nlogo.editor.RichDocument._

class RichDocumentTest extends FunSuite {
  trait Helper {
    val text: String

    lazy val doc = {
      val d = new PlainDocument()
      d.insertString(0, text, null)
      d
    }
  }

  test("offsetToLine: empty") { new Helper {
    val text = ""
    assert(doc.offsetToLine(0) == 0)
    assert(doc.offsetToLine(1) == 0)
  } }

  test("offsetToLine: single line") { new Helper {
    val text = "abc def"
    assert(doc.offsetToLine(0) == 0)
    assert(doc.offsetToLine(6) == 0)
  } }

  test("offsetToLine: two lines") { new Helper {
    val text = "abc def\nghi"
    assert(doc.offsetToLine(0) == 0)
    assert(doc.offsetToLine(6) == 0)
    assert(doc.offsetToLine(7) == 0)
    assert(doc.offsetToLine(8) == 1)
    assert(doc.offsetToLine(11) == 1)
  } }

  test("lineToStartOffset: empty") { new Helper {
    val text = ""
    assert(doc.lineToStartOffset(0) == 0)
    assert(doc.lineToStartOffset(-1) == -1)
    assert(doc.lineToStartOffset(1) == -1)
    assert(doc.lineToStartOffset(2) == -1)
  } }

  test("lineToStartOffset: single line") { new Helper {
    val text = "abc"
    assert(doc.lineToStartOffset(0) == 0)
    assert(doc.lineToStartOffset(1) == -1)
  } }

  test("lineToStartOffset: two lines") { new Helper {
    val text = "abc\ndef"
    assert(doc.lineToStartOffset(0) == 0)
    assert(doc.lineToStartOffset(1) == 4)
    assert(doc.lineToStartOffset(2) == -1)
  } }

  test("lineToEndOffset: empty") { new Helper {
    val text = ""
    assert(doc.lineToEndOffset(0) == 1)
    assert(doc.lineToEndOffset(1) == -1)
  } }

  test("lineToEndOffset: single line") { new Helper {
    val text = "abc"
    assert(doc.lineToEndOffset(0) == 4)
    assert(doc.lineToEndOffset(1) == -1)
  } }

  test("lineToEndOffset: two lines") { new Helper {
    val text = "abc\ndef"
    assert(doc.lineToEndOffset(0) == 4)
    assert(doc.lineToEndOffset(1) == 8)
    assert(doc.lineToEndOffset(2) == -1)
  } }

  test("getLineText: empty") { new Helper {
    val text = ""
    assert(doc.getLineText(-1) == None)
    assert(doc.getLineText(0) == Some("\n"))
    assert(doc.getLineText(1) == None)
  } }

  test("getLineText: single line") { new Helper {
    val text = "abc"
    assert(doc.getLineText(0) == Some("abc\n"))
    assert(doc.getLineText(1) == None)
  } }

  test("getLineText: two lines") { new Helper {
    val text = "abc\ndef"
    assert(doc.getLineText(0) == Some("abc\n"))
    assert(doc.getLineText(1) == Some("def\n"))
  } }

  test("insertBeforeLinesInRange: empty") { new Helper {
    val text = ""
    doc.insertBeforeLinesInRange(0, 0, ";")
    assert(doc.getLineText(0) == Some(";\n"))
  } }

  test("insertBeforeLinesInRange: single line") { new Helper {
    val text = "abc"
    doc.insertBeforeLinesInRange(0, 0, ";")
    assert(doc.getLineText(0) == Some(";abc\n"))
  } }

  test("insertBeforeLinesInRange: two line") { new Helper {
    val text = "abc\ndef"
    doc.insertBeforeLinesInRange(0, 1, ";")
    assert(doc.getLineText(1) == Some(";def\n"))
  } }

  test("insertBeforeLinesInRange: invalid ranges") { new Helper {
    val text = "abc"
    doc.insertBeforeLinesInRange(1, 1, ";")
    doc.insertBeforeLinesInRange(-11, 1, ";")
  } }

  test("selectionLineRange: single line") { new Helper {
    val text = ""
    assertResult((0, 0))(doc.selectionLineRange(-1, 6))
    assertResult((0, 0))(doc.selectionLineRange(0, 0))
  } }

  test("selectionLineRange: one line") { new Helper {
    val text = "abc"
    assertResult((0, 0))(doc.selectionLineRange(0, 0))
    assertResult((0, 0))(doc.selectionLineRange(0, 3))
  } }

  test("selectionLineRange: two lines") { new Helper {
    val text = "abc\ndef"
    assertResult((1, 1))(doc.selectionLineRange(4, 4))
    assertResult((0, 0))(doc.selectionLineRange(0, 3))
    // in this case we want to avoid selecting the top line,
    // since the top-line selection only includes only the newline
    assertResult((1, 1))(doc.selectionLineRange(3, 7))
    // in this case we want to avoid selecting the bottom line,
    // since the bottom-line selection includes only the newline
    assertResult((0, 0))(doc.selectionLineRange(0, 4))
  } }

  test("selectionLineRange: middle line") { new Helper {
    val text = "abc\ndef\nghi"
    assertResult((1, 1))(doc.selectionLineRange(3, 7))
  } }
}
