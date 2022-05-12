// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import
  org.nlogo.core.{ prim, Expression, ReporterApp },
    prim.{ _commandlambda, _reporterlambda }

import org.scalatest.Inside
import org.scalatest.funsuite.AnyFunSuite

class AnonymousProcedureSourceTests extends AnyFunSuite with Inside with BaseParserTest {
  override val PREAMBLE = "to __test __ignore "

  def expression(source: String, expIndex: Int = 0, preamble: String = PREAMBLE, postamble: String = POSTAMBLE): Expression =
    compile(source, preamble, postamble).head.stmts.head.args(expIndex)

  def assertStringifies(exp: Expression, result: String): Unit = {
    inside(exp) {
      case ReporterApp(r: _reporterlambda, _, _) =>
        assertResult(Some(result))(r.source)
      case ReporterApp(c: _commandlambda, _, _) =>
        assertResult(Some(result))(c.source)
    }
  }

  test("simple anonymous reporter") {
    assertStringifies(expression("[ -> 1]"), "[ -> 1 ]")
  }
  test("anonymous reporter with one argument") {
    assertStringifies(expression("[a -> a]"), "[ a -> a ]")
  }
  test("anonymous reporter with newlines") {
    assertStringifies(expression("[a -> a\n ]"), "[ a -> a ]")
  }
  test("anonymous reporter with comments") {
    assertStringifies(expression("[a -> a ; foo \n ]"), "[ a -> a ]")
  }
  test("anonymous reporter with comments before the body starts") {
    assertStringifies(expression("[ a -> ;foo \n 1 ]"), "[ a -> 1 ]")
  }
  test("concise anonymous reporter") {
    assertStringifies(
      expression("", expIndex = 0, preamble = "to __test __ignore map + [] ")
        .asInstanceOf[ReporterApp].args(0),
      "+")
  }
  test("simple anonymous command") {
    assertStringifies(expression("[ -> fd 1]"), "[ -> fd 1 ]")
  }
  test("empty anonymous command") {
    assertStringifies(
      expression("[ -> fd 1]", expIndex = 1, preamble = "to __test foreach [] "),
      "[ -> fd 1 ]")
  }
  test("anonymous command with newlines") {
    assertStringifies(expression("[ -> \nfd 1]"), "[ -> fd 1 ]")
  }
  test("anonymous command with optional command block") {
    assertStringifies(expression("[ -> crt 1 ]"), "[ -> crt 1 ]")
  }
  test("concise anonymous command") {
    assertStringifies(
      expression("fd", expIndex = 1, preamble = "to __test foreach [] "),
      "fd")
  }
  test("anonymous command with arguments") {
    assertStringifies(expression("[ [a] -> fd a ]"), "[ [a] -> fd a ]")
  }
  test("infix operators") {
    assertStringifies(expression("[ [a b] -> a + b + sin b ]"), "[ [a b] -> a + b + sin b ]")
  }
  test("anonymous procedures and lists") {
    assertStringifies(expression("[ [a b] -> foreach [1 2] [ -> print a ] ]"),
      "[ [a b] -> foreach [1 2] [ -> print a ] ]")
  }
  test("nested anonymous procedures") {
    assertStringifies(expression("[ [a b] -> [ c -> a ] ]"), "[ [a b] -> [ c -> a ] ]")
  }
  test("blocks in anonymous procedures") {
    assertStringifies(expression("[ [a b] -> crt 10 [ fd 1 ] ]"), "[ [a b] -> crt 10 [ fd 1 ] ]")
  }
  test("reporter blocks in anonymous procedures") {
    assertStringifies(expression("[ -> count turtles with [ xcor = 10 ] ]"), "[ -> count turtles with [ xcor = 10 ] ]")
  }
  test("color literals in anonymous procedures") {
    assertStringifies(expression("[ -> blue ]"), "[ -> blue ]")
  }
}
