// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import org.nlogo.api.ProgramGenerator

import org.scalatest.FunSuite

class NetLogoFoldParserTest extends FunSuite with ProgramGenerator {
  def sections(text: String) = NetLogoFoldParser.sections(text)

  test("an empty document has empty root elements") {
    assert(sections("").length == 0)
  }

  test("a document with a globals declaration has a single globals element") {
    val code = "globals [ foo ]"
    assert(sections(code).length === 1)
  }

  test("a document with a procedure has a single procedure element") {
    val code = "to foo bar end"
    assert(sections(code).length === 1)
  }

  test("a document with globals and a procedure has both as elements") {
    val code = "globals [ baz ] to foo bar end"
    assert(sections(code).length === 2)
    assert(sections(code)(0).length === 4) // this counts tokens, not characters
    assert(sections(code)(0).map(_.text).mkString(" ") === "globals [ baz ]")
    assert(sections(code)(1).length === 4)
    assert(sections(code)(1).map(_.text).mkString(" ") === "to foo bar end")
  }

  test("handles bad tokens without error") {
    val code = """to foo fput " list end"""
    assert(sections(code).length == 1)
  }

  test("delimits the appropriate number of syntax elements") {
    forAll(wellFormedPrograms) { p =>
      assert(sections(p.programText).length === p.statementCount, p.programText)
    }
  }

  test("delimits the appropriate number of syntax elements, even when the program isn't well-formed") {
    forAll(onceMangledProgram) { p =>
      assert(sections(p.invalidProgramText).length <= p.statementCount)
      val commentCount = p.originalElements.count(_.isInstanceOf[TopLevelComment])
      assert(sections(p.invalidProgramText).length >= p.statementCount - commentCount)
    }
  }
}
