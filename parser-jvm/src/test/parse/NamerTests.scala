// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite
import org.nlogo.{ core, parse },
  core._

class NamerTests extends FunSuite {

  def compile(source: String): Iterator[Token] = {
    val wrappedSource = "to __test " + source + "\nend"
    val program = Program.empty().copy(interfaceGlobals = Seq("X"))
    val results = new StructureParser(
        FrontEnd.tokenizer.tokenizeString(wrappedSource).map(parse.Namer0),
        None, StructureResults(program))
      .parse(false)
    assertResult(1)(results.procedures.size)
    val procedure = results.procedures.values.iterator.next()
    new Namer(results.program, results.procedures,
        new DummyExtensionManager)
      .process(results.procedureTokens(procedure.name).iterator, procedure)
      .takeWhile(_.tpe != TokenType.Eof)
  }

  test("empty") {
    assertResult("")(compile("").mkString)
  }
  test("interface global") {
    assertResult("Token(x,Reporter,_observervariable(0))")(
      compile("print x").drop(1).mkString)
  }

}
