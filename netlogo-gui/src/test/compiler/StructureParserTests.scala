// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite

import org.nlogo.api.{ CompilerException, DummyExtensionManager, Program }
import org.nlogo.nvm.{ DummyCompilationEnvironment, Procedure }

class StructureParserTests extends FunSuite {
  // private so StructureParser.Results doesn't escape compiler package
  private def compile(source: String, program: Program = new Program(false)): StructureParser.Results =
    TestHelper.structureParse(source, program)
  test("empty") {
    val program = new Program(false)
    val results = compile("", program)
    assert(results.procedures.isEmpty)
    assert(results.tokens.isEmpty)
    assertResult("globals []\n" +
      "interfaceGlobals []\n" +
      "turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE]\n" +
      "patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR]\n" +
      "links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]\n" +
      "breeds \n" +
      "breeds-own \n" +
      "link-breeds \n" +
      "link-breeds-own \n")(program.dump)
  }
  test("missing procedure name") {  // ticket #1183
    intercept[CompilerException] {
      compile("to", new Program(false))
    }
    intercept[CompilerException] {
      compile("to-report", new Program(false))
    }
  }
  test("commandProcedure") {
    val results = compile("to go fd 1 end", new Program(false))
    assertResult(1)(results.procedures.size)
    assertResult("procedure GO:[]{OTPL}:\n")(results.procedures.get("GO").dump)
  }
  test("declarations1") {
    val program = new Program(false)
    val results = compile("globals [g1 g2] turtles-own [t1 t2] patches-own [p1 p2]", program)
    assert(results.procedures.isEmpty)
    assertResult("globals [G1 G2]\n" +
      "interfaceGlobals []\n" +
      "turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE T1 T2]\n" +
      "patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR P1 P2]\n" +
      "links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]\n" +
      "breeds \n" +
      "breeds-own \n" +
      "link-breeds \n" +
      "link-breeds-own \n")(program.dump)
  }
  test("declarations2") {
    val program = new Program(false)
    val results = compile("breed [b1s b1] b1s-own [b11 b12] breed [b2s b2] b2s-own [b21 b22]", program)
    assert(results.procedures.isEmpty)
    assertResult("globals []\n" +
      "interfaceGlobals []\n" +
      "turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE]\n" +
      "patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR]\n" +
      "links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]\n" +
      "breeds B1S = B1S\n" +
      "B2S = B2S\n" +
      "breeds-own B1S = [B11, B12]\n" +
      "B2S = [B21, B22]\n" +
      "link-breeds \n" +
      "link-breeds-own \n")(program.dump)
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
