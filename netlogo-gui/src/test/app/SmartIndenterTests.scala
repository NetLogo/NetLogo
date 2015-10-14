// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.api.EditorAreaInterface
import org.nlogo.nvm.{CompilerInterface, DefaultCompilerServices}
import org.nlogo.util.Femto
import org.nlogo.api.FileIO.file2String
import org.scalatest.FunSuite

class SmartIndenterTests extends FunSuite {
  val path = "test/indent.txt" // where the actual test cases live

  // read tests from file
  val data: Seq[Array[String]] = {

    def replaceLeading(s: String, c: Char, replacement: String) =
      s.toList.span(_ == c) match {
        case (leading, rest) => leading.map(_ => replacement).mkString + rest.mkString
      }

    def spec2in(spec: String) = replaceLeading(replaceLeading(spec, '+', ""), '-', " ")

    def spec2out(spec: String) = replaceLeading(replaceLeading(spec, '-', ""), '+', " ")

    file2String(path)
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
    Femto.scalaSingleton(classOf[CompilerInterface],
                         "org.nlogo.compiler.Compiler"))

  // call FunSuite's test method for each test read
  for(Array(name, in, out) <- data) {
    test(name) {
      val code = new Scaffold(in)
      new SmartIndenter(code, compiler).handleTab()
      assert(out === code.text)
    }
  }

  class Scaffold(var text: String) extends EditorAreaInterface {
    import scala.beans.BeanProperty
    @BeanProperty var selectionStart = 0
    @BeanProperty var selectionEnd = 0
    selectAll()
    def lines: Array[String] = text.split("\\n")

    def selectAll() { selectionStart = 0; selectionEnd = text.size }

    def offsetToLine(offset: Int): Int = lines.indices.find(lineToEndOffset(_) > offset)
            .getOrElse(lines.size - 1)

    def lineToStartOffset(line: Int) = lines.take(line).map(_.length + 1).sum

    def lineToEndOffset(line: Int) = lineToStartOffset(line) + lines(line).length

    def getLineOfText(lineNum: Int) = {
      val start = lineToStartOffset(lineNum)
      getText(start, lineToEndOffset(lineNum) - start)
    }

    def getText(start: Int, len: Int) = text.drop(start).take(len)

    def insertString(pos: Int, str: String) { text = text.take(pos) + str + text.drop(pos) }

    def replaceSelection(str: String) { text = text.take(selectionStart) + str + text.drop(selectionEnd) }

    def remove(start: Int, len: Int) { text = text.take(start) + text.drop(start + len) }
  }
}
