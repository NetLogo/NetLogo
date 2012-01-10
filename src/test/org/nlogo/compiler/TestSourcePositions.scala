// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite
import org.nlogo.api.{ DummyExtensionManager, Program }
import org.nlogo.nvm.Procedure
import org.nlogo.api.Version.useGenerator

class TestSourcePositions extends FunSuite {
  val program = new Program(false)
  def compileReporter(source: String) =
    Compiler.compileMoreCode("to foo __ignore " + source + "\nend", None, program,
      java.util.Collections.emptyMap[String, Procedure],
      new DummyExtensionManager).head.code.head.args.head.source
  def compileCommand(source: String) =
    Compiler.compileMoreCode("to foo " + source + "\nend", None, program,
      java.util.Collections.emptyMap[String, Procedure],
      new DummyExtensionManager).head.code.head.source
  def reporter(s: String) { expect(s)(compileReporter(s)) }
  def command(s: String) { expect(s)(compileCommand(s)) }
  def command(expected: String, s: String) { expect(expected)(compileCommand(s)) }
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
