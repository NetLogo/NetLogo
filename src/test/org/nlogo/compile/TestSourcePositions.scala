// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.scalatest.FunSuite
import org.nlogo.api.{ DummyExtensionManager, Program }
import org.nlogo.api.Version.useGenerator
import org.nlogo.nvm

class TestSourcePositions extends FunSuite {
  val program = Program.empty()
  def compileReporter(source: String) =
    Compiler.compileMoreCode("to foo __ignore " + source + "\nend", None, program,
      nvm.ParserInterface.NoProcedures,
      new DummyExtensionManager).head.code.head.args.head.source
  def compileCommand(source: String) =
    Compiler.compileMoreCode("to foo " + source + "\nend", None, program,
      nvm.ParserInterface.NoProcedures,
      new DummyExtensionManager).head.code.head.source
  def reporter(s: String) { assertResult(s)(compileReporter(s)) }
  def command(s: String) { assertResult(s)(compileCommand(s)) }
  def command(expected: String, s: String) { assertResult(expected)(compileCommand(s)) }
  if (useGenerator) {
    /// reporters
    test("one") { reporter("timer") }
    test("many") { reporter("timer + timer + timer + timer + timer") }
    test("less") { reporter("timer < timer") }
    test("int") { reporter("3") }
    test("string") { reporter("\"foo\"") }
    test("constantFolding") { reporter("2 + 2") }
    /// commands
    test("iffy") { command("if timer < 10", "if timer < 10 [ print timer ]") }
    // TODO fails, probably because of custom assembly. would be nice to fix - ST 2/12/09
    // @Test def repeat { command("repeat 3","repeat 3 [ ca ]") }
    // TODO fails, gives "fd (3" instead. fixing seems hard - ST 2/12/09
    // @Test def parens { command("fd (3)") }
  }
}
