// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite

import org.nlogo.api.{ CompilerException, DummyExtensionManager, Program }
import org.nlogo.nvm

class StructureParserTests extends FunSuite {

  def compile(source: String): StructureParser.Results = {
    new StructureParser(Compiler.Tokenizer2D.tokenize(source),
                        None, StructureParser.emptyResults())
      .parse(false)
  }

  test("empty") {
    val results = compile("")
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
      compile("to")
    }
    intercept[CompilerException] {
      compile("to-report")
    }
  }

  test("globals") {
    val results = compile("globals [foo bar]")
    expect("globals [FOO BAR]")(
      results.program.dump.split("\n").head)
  }

  test("turtles-own") {
    val results = compile("turtles-own [foo bar]")
    expect("turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE FOO BAR]")(
      results.program.dump.split("\n").drop(2).head)
  }

  test("breeds") {
    val results = compile("breed [mice mouse] breed [frogs]")
    expect("breeds MICE = Breed(MICE, MOUSE, , false)" +
           "FROGS = Breed(FROGS, TURTLE, , false)")(
      results.program.dump.split("\n").drop(5).take(2).mkString)
  }

  test("mice-own") {
    val results = compile("breed [mice mouse] mice-own [fur teeth]")
    expect("breeds MICE = Breed(MICE, MOUSE, FUR TEETH, false)")(
      results.program.dump.split("\n").drop(5).head)
  }

  test("command procedure") {
    val results = compile("to go fd 1 end")
    expect(1)(results.procedures.size)
    val proc = results.procedures("GO")
    expect(false)(proc.isReporter)
    expect("procedure GO:[]{OTPL}:\n")(proc.dump)
  }

  test("two command procedures") {
    val results = compile("globals [g] to foo print 5 end to bar print g end")
    expect("globals [G]")(
      results.program.dump.split("\n").head)
    expect(2)(results.procedures.size)
    expect("procedure FOO:[]{OTPL}:\n")(results.procedures("FOO").dump)
    expect("procedure BAR:[]{OTPL}:\n")(results.procedures("BAR").dump)
  }

  test("command procedure with empty args") {
    val results = compile("to go [] fd 1 end")
    expect(1)(results.procedures.size)
    val proc = results.procedures("GO")
    expect(false)(proc.isReporter)
    expect("procedure GO:[]{OTPL}:\n")(proc.dump)
  }

  test("command procedure with some args") {
    val results = compile("to go [a b c] fd 1 end")
    expect(1)(results.procedures.size)
    val proc = results.procedures("GO")
    expect(false)(proc.isReporter)
    expect("procedure GO:[A B C]{OTPL}:\n")(proc.dump)
  }

  test("reporter procedure") {
    val results = compile("to-report foo report 0 end")
    expect(1)(results.procedures.size)
    val proc = results.procedures("FOO")
    expect(true)(proc.isReporter)
    expect("reporter procedure FOO:[]{OTPL}:\n")(proc.dump)
  }

  test("includes") {
    val results = compile("__includes [\"foo.nls\"]")
    expect(0)(results.procedures.size)
    expect(1)(results.includes.size)
    expect("foo.nls")(results.includes.head.value)
  }

  test("declarations1") {
    val results = compile("extensions [foo] globals [g1 g2] turtles-own [t1 t2] patches-own [p1 p2]")
    assert(results.procedures.isEmpty)
    expect("globals [G1 G2]\n" +
      "interfaceGlobals []\n" +
      "turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE T1 T2]\n" +
      "patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR P1 P2]\n" +
      "links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]\n" +
      "breeds \n" +
      "link-breeds \n")(results.program.dump)
    expect("FOO")(results.extensions.map(_.value).mkString)
  }

  test("declarations2") {
    val results = compile("breed [b1s b1] b1s-own [b11 b12] breed [b2s b2] b2s-own [b21 b22]")
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

  test("missing open bracket after globals") {
    val e = intercept[CompilerException] {
      compile("globals schmobals")
    }
    expect("opening bracket expected")(e.getMessage.takeWhile(_ != ','))
  }

  test("bad top level keyword") {
    val e = intercept[CompilerException] {
      compile("schmobals")
    }
    expect("keyword expected")(e.getMessage.takeWhile(_ != ','))
  }

  test("missing close bracket after globals") {
    val e = intercept[CompilerException] {
      compile("globals [")
    }
    expect("closing bracket expected")(e.getMessage.takeWhile(_ != ','))
  }

  test("attempt primitive as variable") {
    val e = intercept[CompilerException] {
      compile("globals [turtle]")
    }
    expect("closing bracket expected")(e.getMessage)
  }

  test("redeclaration of globals") {
    val e = intercept[CompilerException] {
      compile("globals [] globals []")
    }
    expect("Redeclaration of GLOBALS")(e.getMessage.takeWhile(_ != ','))
  }

  test("redeclaration of turtles-own") {
    val e = intercept[CompilerException] {
      compile("turtles-own [] turtles-own []")
    }
    expect("Redeclaration of TURTLES-OWN")(e.getMessage.takeWhile(_ != ','))
  }

  test("redeclaration of extensions") {
    val e = intercept[CompilerException] {
      compile("extensions [sound] extensions [profiler]")
    }
    expect("Redeclaration of EXTENSIONS")(e.getMessage.takeWhile(_ != ','))
  }

}
