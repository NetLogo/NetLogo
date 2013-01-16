// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.scalatest.FunSuite
import org.nlogo.{ api, nvm, headless, prim }
import org.nlogo.util.{ Femto, MersenneTwisterFast }

class TestTortoise extends FunSuite {

  val ws = headless.HeadlessWorkspace.newInstance
  ws.silent = true
  val owner = new api.SimpleJobOwner("Tortoise", new MersenneTwisterFast)

  def compare(logo: String) {
    val expected = ws.report(logo)
    val actual = Rhino.eval(Compiler.compileReporter(logo))
    expectResult(expected)(actual)
  }

  def compareCommands(logo: String) {
    ws.clearOutput()
    ws.command(logo)
    val expected = ws.outputAreaBuffer.toString
    val actual = Rhino.run(Compiler.compileCommands(logo))
    expectResult(expected)(actual)
  }

  test("comments") {
    compare("3 ; comment")
    compare("[1 ; comment\n2]")
  }

  test("simple literals") {
    compare("false")
    compare("true")
    compare("2")
    compare("2.0")
    compare("\"foo\"")
  }

  test("literal lists") {
    compare("[]")
    compare("[1]")
    compare("[1 2]")
    compare("[\"foo\"]")
    compare("[1 \"foo\" 2]")
    compare("[1 [2] [3 4 [5]] 6 [] 7]")
    compare("[false true]")
  }

  test("arithmetic") {
    compare("2 + 2")
    compare("1 + 2 + 3")
    compare("1 - 2 - 3")
    compare("1 - (2 - 3)")
    compare("(1 - 2) - 3")
    compare("2 * 3 + 4 * 5")
    compare("6 / 2 + 12 / 6")
  }

  test("empty commands") {
    compareCommands("")
  }

  test("printing") {
    compareCommands("output-print 1")
    compareCommands("output-print \"foo\"")
    compareCommands("output-print 2 + 2")
    compareCommands("output-print 1 output-print 2 output-print 3")
  }

}
