// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

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
    val program = new Program(false)
    val results = compile("", program)
    assert(results.procedures.isEmpty)
    assert(results.tokens.isEmpty)
    expect("globals []\n" +
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
    expect(1)(results.procedures.size)
    expect("procedure GO:[]{OTPL}:\n")(results.procedures.get("GO").dump)
  }
  test("declarations1") {
    val program = new Program(false)
    val results = compile("globals [g1 g2] turtles-own [t1 t2] patches-own [p1 p2]", program)
    assert(results.procedures.isEmpty)
    expect("globals [G1 G2]\n" +
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
    expect("globals []\n" +
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
}
