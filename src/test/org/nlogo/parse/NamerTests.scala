// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite
import org.nlogo.api.{ DummyExtensionManager, Program, Token, TokenType }
import org.nlogo.parse0

class NamerTests extends FunSuite {

  def compile(source: String): Iterator[Token] = {
    val wrappedSource = "to __test " + source + "\nend"
    val program = Program.empty().copy(interfaceGlobals = Seq("X"))
    val results = new StructureParser(
        Parser.tokenizer.tokenize(wrappedSource).map(parse0.Namer0),
        None, StructureResults(program))
      .parse(false)
    expectResult(1)(results.procedures.size)
    val procedure = results.procedures.values.iterator.next()
    val lets =
      new parse0.LetScoper(results.tokens(procedure))
        .scan(results.program.usedNames)
    new Namer(results.program, results.procedures,
        new DummyExtensionManager, lets)
      .process(results.tokens(procedure).iterator, procedure)
      .takeWhile(_.tpe != TokenType.Eof)
  }

  test("empty") {
    expectResult("")(compile("").mkString)
  }
  test("interface global") {
    expectResult("Token(x,Reporter,_observervariable:0)")(
      compile("print x").drop(1).mkString)
  }
  test("let") {
    val expected =
      "Token(let,Command,_let)" +
      "Token(y,Reporter,_letvariable(Y))" +
      "Token(5,Literal,5.0)"
    expectResult(expected)(
      compile("let y 5").mkString)
  }

}
