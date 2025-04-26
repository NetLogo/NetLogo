// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless.test

import org.scalatest.funsuite.AnyFunSuite

class ParserTests extends AnyFunSuite {

  // simple regex tests
  test(" command regex") {
    val (agent, command) = "O> crt 1" match {
      case Parser.CommandRegex(a, c) => (a, c)
      case _ => throw new Exception(s"Unexpected regex: ${Parser.CommandRegex}")
    }
    assertResult("O")(agent)
    assertResult("crt 1")(command)
  }

  // tests for parsing line items.
  // (pending resolution of https://issues.scala-lang.org/browse/SI-6723
  // we avoid the `a -> b` syntax in favor of `(a, b)` - ST 1/3/13)
  val tests = List(
    ("O> crt 1",
      Command("crt 1")),
    ("O> crt 1 => ERROR some message",
      Command("crt 1", result = RuntimeError("some message"))),
    ("O> crt 1 => COMPILER ERROR some message",
      Command("crt 1", result = CompileError("some message"))),
    ("[turtle-set self] of turtle 0 = turtles => true",
      Reporter("[turtle-set self] of turtle 0 = turtles", Success("true"))),
    ("[link-set self] of link 0 2 => ERROR some message",
      Reporter("[link-set self] of link 0 2", RuntimeError("some message"))),
    ("to p1 repeat 5 [ crt 1 __ignore p2 ] end",
      Declaration("to p1 repeat 5 [ crt 1 __ignore p2 ] end")),
    ("to-report p2 foreach [1 2 3] [ report 0 ] end",
      Declaration("to-report p2 foreach [1 2 3] [ report 0 ] end")),
    ("extensions [ array ]",
      Declaration("extensions [ array ]"))
  )

  for((input, output) <- tests)
    test(s"parse: $input") {
      assertResult(output)(Parser.parse(input))
    }

  // test entire path
  test("parse a simple test") {
    val code = """
               |TurtleSet
               |  to foo fd 1 end
               |  O> crt 1
               |  [turtle-set self] of turtle 0 = turtles => true""".stripMargin
    val tests = Parser.parse("test", code)
    val expectedOutputs =
      List(LanguageTest("test", "TurtleSet",
        List(
          Declaration("to foo fd 1 end"),
          Command("crt 1"),
          Reporter("[turtle-set self] of turtle 0 = turtles", Success("true")))))
    assertResult(expectedOutputs.toString)(tests.toString)
  }

}
