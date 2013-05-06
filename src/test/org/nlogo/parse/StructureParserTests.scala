// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite

import org.nlogo.api, api.CompilerException
import org.nlogo.util.Femto

class StructureParserTests extends FunSuite {

  val tokenizer: api.TokenizerInterface =
    Femto.scalaSingleton("org.nlogo.lex.Tokenizer")

  def compile(source: String): StructureResults =
    new StructureParser(tokenizer.tokenize(source),
                        None, StructureResults.empty)
      .parse(false)

  test("empty") {
    val results = compile("")
    assert(results.procedures.isEmpty)
    assert(results.tokens.isEmpty)
    expectResult("globals []\n" +
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
    expectResult("globals [FOO BAR]")(
      results.program.dump.split("\n").head)
  }

  test("turtles-own") {
    val results = compile("turtles-own [foo bar]")
    expectResult("turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE FOO BAR]")(
      results.program.dump.split("\n").drop(2).head)
  }

  test("breeds") {
    val results = compile("breed [mice mouse] breed [frogs]")
    expectResult("breeds MICE = Breed(MICE, MOUSE, , false)" +
           "FROGS = Breed(FROGS, TURTLE, , false)")(
      results.program.dump.split("\n").drop(5).take(2).mkString)
  }

  test("mice-own") {
    val results = compile("breed [mice mouse] mice-own [fur teeth]")
    expectResult("breeds MICE = Breed(MICE, MOUSE, FUR TEETH, false)")(
      results.program.dump.split("\n").drop(5).head)
  }

  test("command procedure") {
    val results = compile("to go fd 1 end")
    expectResult(1)(results.procedures.size)
    val proc = results.procedures("GO")
    expectResult(false)(proc.isReporter)
    expectResult("procedure GO:[]{OTPL}:\n")(proc.dump)
  }

  test("two command procedures") {
    val results = compile("globals [g] to foo print 5 end to bar print g end")
    expectResult("globals [G]")(
      results.program.dump.split("\n").head)
    expectResult(2)(results.procedures.size)
    expectResult("procedure FOO:[]{OTPL}:\n")(results.procedures("FOO").dump)
    expectResult("procedure BAR:[]{OTPL}:\n")(results.procedures("BAR").dump)
  }

  test("command procedure with empty args") {
    val results = compile("to go [] fd 1 end")
    expectResult(1)(results.procedures.size)
    val proc = results.procedures("GO")
    expectResult(false)(proc.isReporter)
    expectResult("procedure GO:[]{OTPL}:\n")(proc.dump)
  }

  test("command procedure with some args") {
    val results = compile("to go [a b c] fd 1 end")
    expectResult(1)(results.procedures.size)
    val proc = results.procedures("GO")
    expectResult(false)(proc.isReporter)
    expectResult("procedure GO:[A B C]{OTPL}:\n")(proc.dump)
  }

  test("reporter procedure") {
    val results = compile("to-report foo report 0 end")
    expectResult(1)(results.procedures.size)
    val proc = results.procedures("FOO")
    expectResult(true)(proc.isReporter)
    expectResult("reporter procedure FOO:[]{OTPL}:\n")(proc.dump)
  }

  test("includes") {
    val results = compile("__includes [\"foo.nls\"]")
    expectResult(0)(results.procedures.size)
    expectResult(1)(results.includes.size)
    expectResult("foo.nls")(results.includes.head.value)
  }

  test("declarations1") {
    val results = compile("extensions [foo] globals [g1 g2] turtles-own [t1 t2] patches-own [p1 p2]")
    assert(results.procedures.isEmpty)
    expectResult("globals [G1 G2]\n" +
      "interfaceGlobals []\n" +
      "turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE T1 T2]\n" +
      "patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR P1 P2]\n" +
      "links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]\n" +
      "breeds \n" +
      "link-breeds \n")(results.program.dump)
    expectResult("FOO")(results.extensions.map(_.value).mkString)
  }

  test("declarations2") {
    val results = compile("breed [b1s b1] b1s-own [b11 b12] breed [b2s b2] b2s-own [b21 b22]")
    assert(results.procedures.isEmpty)
    expectResult("globals []\n" +
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
    expectResult("opening bracket expected")(e.getMessage.takeWhile(_ != ','))
  }

  test("bad top level keyword") {
    val e = intercept[CompilerException] {
      compile("schmobals")
    }
    expectResult("keyword expected")(e.getMessage.takeWhile(_ != ','))
  }

  test("missing close bracket after globals") {
    val e = intercept[CompilerException] {
      compile("globals [")
    }
    expectResult("closing bracket expected")(e.getMessage.takeWhile(_ != ','))
  }

  test("attempt primitive as variable") {
    val e = intercept[CompilerException] {
      compile("globals [turtle]")
    }
    expectResult("There is already a primitive reporter called TURTLE")(e.getMessage)
  }

  test("redeclaration of globals") {
    val e = intercept[CompilerException] {
      compile("globals [] globals []")
    }
    expectResult("Redeclaration of GLOBALS")(e.getMessage.takeWhile(_ != ','))
  }

  test("redeclaration of turtles-own") {
    val e = intercept[CompilerException] {
      compile("turtles-own [] turtles-own []")
    }
    expectResult("Redeclaration of TURTLES-OWN")(e.getMessage.takeWhile(_ != ','))
  }

  test("redeclaration of extensions") {
    val e = intercept[CompilerException] {
      compile("extensions [foo] extensions [bar]")
    }
    expectResult("Redeclaration of EXTENSIONS")(e.getMessage.takeWhile(_ != ','))
  }

}
