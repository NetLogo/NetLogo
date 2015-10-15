// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite

import org.nlogo.api.{ DummyExtensionManager}
import org.nlogo.core.Program
import org.nlogo.core.CompilerException
import org.nlogo.core.DummyCompilationEnvironment
import org.nlogo.nvm.Procedure

class StructureParserTests extends FunSuite {
  // private so StructureParser.Results doesn't escape compiler package
  private def compile(source: String, program: Program = Program.empty()): StructureParser.Results =
    TestHelper.structureParse(source, program)
  test("empty") {
    val program = Program.empty()
    val results = compile("", program)
    assert(results.procedures.isEmpty)
    assert(results.tokens.isEmpty)
    assertResult("globals []\n" +
      "interfaceGlobals []\n" +
      "turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE]\n" +
      "patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR]\n" +
      "links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]\n" +
      "breeds \n" +
      "link-breeds \n")(results.program.dump)
  }
  test("missing procedure name") {  // ticket #1183
    intercept[CompilerException] {
      compile("to", Program.empty())
    }
    intercept[CompilerException] {
      compile("to-report", Program.empty())
    }
  }
  test("commandProcedure") {
    val results = compile("to go fd 1 end", Program.empty())
    assertResult(1)(results.procedures.size)
    assertResult("procedure GO:[]{OTPL}:\n")(results.procedures.get("GO").dump)
  }
  test("declarations1") {
    val program = Program.empty()
    val results = compile("globals [g1 g2] turtles-own [t1 t2] patches-own [p1 p2]", program)
    assert(results.procedures.isEmpty)
    assertResult("globals [G1 G2]\n" +
      "interfaceGlobals []\n" +
      "turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE T1 T2]\n" +
      "patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR P1 P2]\n" +
      "links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]\n" +
      "breeds \n" +
      "link-breeds \n")(results.program.dump)
  }
  test("declarations2") {
    val program = Program.empty()
    val results = compile("breed [b1s b1] b1s-own [b11 b12] breed [b2s b2] b2s-own [b21 b22]", program)
    assert(results.procedures.isEmpty)
    assertResult(
      """|globals []
         |interfaceGlobals []
         |turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE]
         |patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR]
         |links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]
         |breeds B1S = Breed(B1S, B1, B11 B12, false)
         |B2S = Breed(B2S, B2, B21 B22, false)
         |link-breeds """.stripMargin + "\n")(results.program.dump)
  }
  test("missing end 1") {
    val e = intercept[CompilerException] {
      compile("to foo to bar")
    }
    assertResult("This doesn't make sense here")(e.getMessage.takeWhile(_ != ','))
  }
  test("missing end 2") {
    val e = intercept[CompilerException] {
      compile("to foo fd 1")
    }
    assertResult("Last procedure doesn't end with END")(e.getMessage.takeWhile(_ != ','))
  }
  test("missing end 3") {
    val e = intercept[CompilerException] {
      compile("to foo")
    }
    assertResult("Last procedure doesn't end with END")(e.getMessage.takeWhile(_ != ','))
  }
}
