// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import org.nlogo.api.{ EditorAreaInterface, NetLogoLegacyDialect, NetLogoThreeDDialect, Version }
import org.nlogo.api.FileIO.fileToString
import org.nlogo.nvm.{ PresentationCompilerInterface, DefaultCompilerServices }
import org.nlogo.core.Femto
import org.nlogo.editor.DummyEditorArea
import org.scalatest.FunSuite

import scala.util.matching.Regex

class SmartIndenterTests extends FunSuite {
  class DummyEditor(text: String) extends DummyEditorArea(text) with EditorAreaInterface

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
    Femto.get[PresentationCompilerInterface]("org.nlogo.compile.Compiler", if (Version.is3D) NetLogoLegacyDialect else NetLogoThreeDDialect))

  test("handleCloseBracket adds closing bracket") {
    val code = new DummyEditor("")
    new SmartIndenter(code, compiler).handleCloseBracket()
    assert("]" == code.presentationText)
  }

  test("handleCloseBracket correctly indents line") {
    val code = new DummyEditor("[\n  abc\n  ")
    code.setSelectionStart(10)
    code.setSelectionEnd(10)
    code.setCaretPosition(10)
    new SmartIndenter(code, compiler).handleCloseBracket()
    assert("[\n  abc\n]" == code.presentationText)
  }

  // call FunSuite's test method for each test read
  for(Array(name, in, out) <- data) {
    test("indent all lines in " + name) {
      val code = new DummyEditor(in)
      new SmartIndenter(code, compiler).handleTab()
      assert(out === code.presentationText)
    }

    test("indent a single line in " + name) {
      val lineCount = out.lines.length
      for {
        lineNumber <- (0 until lineCount)
      } {
        val code = new DummyEditor(in)
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

    test("indents lines when handleEnter replaces a selection including their start " + name) {
      val newline = new Regex("\\n")
      for {
        (newLine, i) <- (newline.findAllMatchIn(in).toSeq.zipWithIndex)
      } {
        if (i < out.lines.length) {
          val code = new DummyEditor(in)
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
        val code = new DummyEditor(in)
        code.setCaretPosition(index)
        new SmartIndenter(code, compiler).handleTab()
        val startOffset = code.getCaretPosition.min(code.presentationText.size - 1).max(0)
        val newChar = code.getText(startOffset, 1)
        assert(char == newChar.head, s"When caret started at: $index (${code.presentationText.slice(index, 3)}), ended at: ${code.getCaretPosition}")
      }
    }

    test("maintains caret position when handling enter in " + name) {
      for {
        (char, index) <- in.zipWithIndex if ! char.isWhitespace
      } {
        val code = new DummyEditor(in)
        code.setSelectionStart(index)
        code.setSelectionEnd(index)
        code.setCaretPosition(index)
        new SmartIndenter(code, compiler).handleEnter()
        val startOffset = code.getCaretPosition.min(code.presentationText.size - 1).max(0)
        val newChar = code.getText(startOffset, 1)
        assert(char == newChar.head, s"When caret started at: $index (${code.presentationText.slice(index, 3)}), ended at: ${code.getCaretPosition}")
      }
    }
  }
}
