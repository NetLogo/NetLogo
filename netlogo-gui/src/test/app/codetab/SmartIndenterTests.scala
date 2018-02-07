// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import org.nlogo.api.{ EditorAreaInterface, NetLogoLegacyDialect }
import org.nlogo.nvm.{ PresentationCompilerInterface, DefaultCompilerServices }
import org.nlogo.core.Femto
import org.nlogo.api.FileIO.fileToString

import org.nlogo.util.{ ArityIndependent, TaggedFunSuite }

import scala.util.matching.Regex

object SmartIndenterTests {
  class Scaffold(var text: String) extends EditorAreaInterface {
    private var _caretPosition: Int = 0
    private var selectionStart = 0
    private var selectionEnd = 0
    private var _linesText = text
    private var _lines = text.split("\\n")

    def getCaretPosition = _caretPosition
    def setCaretPosition(pos: Int) = {
      _caretPosition = pos
    }
    def getSelectionStart = selectionStart
    def setSelectionStart(pos: Int): Unit = {
      selectionStart = pos
    }
    def getSelectionEnd = selectionEnd
    def setSelectionEnd(pos: Int): Unit = {
      selectionEnd = pos
    }
    selectAll()
    def lines: Array[String] = {
      if (_linesText eq text) {
        _lines
      } else {
        _linesText = text
        _lines = _linesText.split("\\n")
        _lines
      }
    }

    def selectAll() { selectionStart = 0; selectionEnd = text.size }

    def offsetToLine(offset: Int): Int = lines.indices.find(lineToEndOffset(_) > offset)
            .getOrElse(lines.size - 1)

    def lineToStartOffset(line: Int) = lines.take(line).map(_.length + 1).sum

    def lineToEndOffset(line: Int) = lineToStartOffset(line) + lines(line).length

    def getLineOfText(lineNum: Int) = {
      val start = lineToStartOffset(lineNum)
      getText(start, lineToEndOffset(lineNum) - start)
    }

    def getText(start: Int, len: Int) = text.substring(start, start + len)

    def insertString(pos: Int, str: String) { text = text.substring(0, pos) + str + text.substring(pos, text.length) }

    def replaceSelection(str: String) {
      text = text.take(selectionStart) + str + text.drop(selectionEnd)
      val originalStart = selectionStart
      _caretPosition = selectionStart + str.length
      selectionStart = originalStart + str.length
      selectionEnd   = originalStart + str.length
    }

    def remove(start: Int, len: Int) { text = text.substring(0, start) + text.substring(start + len, text.length) }

    def replace(start: Int, len: Int, str: String): Unit = {
      text = text.substring(0, start) + str + text.substring(start + len, text.length)
      _caretPosition = start + str.length
    }

    def replace(start: Int, len: Int, str: String, newCaretPosition: Int): Unit = {
      text = text.substring(0, start) + str + text.substring(start + len, text.length)
      _caretPosition = newCaretPosition
    }
  }
}

import SmartIndenterTests.Scaffold

class SmartIndenterTests extends TaggedFunSuite(ArityIndependent) {
  val path = "test/indent.txt" // where the actual test cases live

  // read tests from file
  val data: Seq[Array[String]] = {

    def replaceLeading(s: String, c: Char, replacement: String) =
      s.toList.span(_ == c) match {
        case (leading, rest) => leading.map(_ => replacement).mkString + rest.mkString
      }

    def spec2in(spec: String) = replaceLeading(replaceLeading(spec, '+', ""), '-', " ")

    def spec2out(spec: String) = replaceLeading(replaceLeading(spec, '-', ""), '+', " ")

    fileToString(path)
            .split("\n")
            .filter(!_.startsWith("#"))
            .dropWhile(_.isEmpty)
            .mkString("\n")
            .split("\n\n")
            .filter(!_.trim.isEmpty)
            .map {
              _.split("\n") match {
                case Array(name: String, spec@_*) =>
                  Array[String](name,
                                spec.map(spec2in).mkString("\n"),
                                spec.map(spec2out).mkString("\n"))
              }
            }
  }

  val compiler = new DefaultCompilerServices(
    Femto.get[PresentationCompilerInterface]("org.nlogo.compile.Compiler", NetLogoLegacyDialect))

  test("handleCloseBracket adds closing bracket") {
    val code = new Scaffold("")
    new SmartIndenter(code, compiler).handleCloseBracket()
    assert("]" == code.text)
  }

  test("handleCloseBracket correctly indents line") {
    val code = new Scaffold("[\n  abc\n  ")
    code.setSelectionStart(10)
    code.setSelectionEnd(10)
    code.setCaretPosition(10)
    new SmartIndenter(code, compiler).handleCloseBracket()
    assert("[\n  abc\n]" == code.text)
  }

  // call FunSuite's test method for each test read
  for(Array(name, in, out) <- data) {
    test("indent all lines in " + name) {
      val code = new Scaffold(in)
      new SmartIndenter(code, compiler).handleTab()
      assert(out === code.text)
    }

    test("indent a single line in " + name) {
      val lineCount = out.lines.length
      for {
        lineNumber <- (0 until lineCount)
      } {
        val code = new Scaffold(in)
        val indenter = new SmartIndenter(code, compiler)
        code.setSelectionStart(code.lineToStartOffset(lineNumber))
        code.setSelectionEnd(code.lineToEndOffset(lineNumber) - 1)
        indenter.handleTab()
        assert(out.lines.toSeq(lineNumber) === code.lines(lineNumber))
        if (lineNumber > 0) {
          assert(in.lines.toSeq(lineNumber - 1) === code.lines(lineNumber - 1))
        }
        if (lineNumber < lineCount - 2) {
          assert(in.lines.toSeq(lineNumber + 1) === code.lines(lineNumber + 1))
        }
      }
    }

    test("handleEnter does not mangle next line " + name) {
      val newline = new Regex("\\n")
      for {
        (newLine, i) <- (newline.findAllMatchIn(in).toSeq.zipWithIndex)
      } {
        if (i < out.lines.length) {
          val code = new Scaffold(in)
          val indenter = new SmartIndenter(code, compiler)
          code.setSelectionStart(newLine.start)
          code.setSelectionEnd(newLine.end)
          indenter.handleEnter()
          assert(out.lines.toSeq(i + 1) === code.lines(i + 1))
        }
      }
    }

    test("maintains caret position when tabbing in " + name) {
      for {
        (char, index) <- in.zipWithIndex if ! char.isWhitespace
      } {
        val code = new Scaffold(in)
        code.setCaretPosition(index)
        new SmartIndenter(code, compiler).handleTab()
        val startOffset = code.getCaretPosition.min(code.text.size - 1).max(0)
        val newChar = code.getText(startOffset, 1)
        assert(char == newChar.head, s"When caret started at: $index (${code.text.slice(index, 3)}), ended at: ${code.getCaretPosition}")
      }
    }

    test("maintains caret position when handling enter in " + name) {
      for {
        (char, index) <- in.zipWithIndex if ! char.isWhitespace
      } {
        val code = new Scaffold(in)
        code.setSelectionStart(index)
        code.setSelectionEnd(index)
        code.setCaretPosition(index)
        new SmartIndenter(code, compiler).handleEnter()
        val startOffset = code.getCaretPosition.min(code.text.size - 1).max(0)
        val newChar = code.getText(startOffset, 1)
        assert(char == newChar.head, s"When caret started at: $index (${code.text.slice(index, 3)}), ended at: ${code.getCaretPosition}")
      }
    }
  }
}
