// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite

import org.nlogo.api.{ CompilerException, DummyExtensionManager, Program }
import org.nlogo.nvm.Procedure

class StructureParserTests extends FunSuite {
  // private so StructureParser.Results doesn't escape compiler package
  private def compile(source: String, program: Program): StructureParser.Results = {
    implicit val tokenizer = Compiler.Tokenizer2D
    new StructureParser(tokenizer.tokenize(source), None, program,
      java.util.Collections.emptyMap[String, Procedure], new DummyExtensionManager)
      .parse(false)
  }
  test("empty") {
    val results = compile("", Program.empty())
    assert(results.procedures.isEmpty)
    assert(results.tokens.isEmpty)
    expect("globals []\n" +
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
    expect(1)(results.procedures.size)
    expect("procedure GO:[]{OTPL}:\n")(results.procedures.get("GO").dump)
  }
  test("declarations1") {
    val results = compile("globals [g1 g2] turtles-own [t1 t2] patches-own [p1 p2]", Program.empty())
    assert(results.procedures.isEmpty)
    expect("globals [G1 G2]\n" +
      "interfaceGlobals []\n" +
      "turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE T1 T2]\n" +
      "patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR P1 P2]\n" +
      "links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]\n" +
      "breeds \n" +
      "link-breeds \n")(results.program.dump)
  }
  test("declarations2") {
    val results = compile("breed [b1s b1] b1s-own [b11 b12] breed [b2s b2] b2s-own [b21 b22]", Program.empty())
    assert(results.procedures.isEmpty)
    expect("globals []\n" +
      "interfaceGlobals []\n" +
      "turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE]\n" +
      "patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR]\n" +
      "links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]\n" +
      "breeds B1S = Breed(B1S, B1, B11 B12, false)\n" +
      "B2S = Breed(B2S, B2, B21 B22, false)\n" +
      "link-breeds \n")(results.program.dump)
  }
}
