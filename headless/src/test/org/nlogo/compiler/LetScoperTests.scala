// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite
import org.nlogo.{ api, nvm }

class LetScoperTests extends FunSuite {

  def compile(source: String): Iterable[api.Let] = {
    val wrappedSource = "to __test " + source + "\nend"
    val results = new StructureParser(
      Compiler.Tokenizer2D.tokenize(wrappedSource), None,
      StructureParser.emptyResults)
      .parse(false)
    expectResult(1)(results.procedures.size)
    val procedure = results.procedures.values.iterator.next()
    new LetScoper(procedure, results.tokens(procedure), results.program.usedNames).scan()
    procedure.lets
  }

  test("empty") {
    expectResult("")(compile("").mkString)
  }

  test("let") {
    expectResult("Let(Y,2,5)")(
      compile("let y 5 print y").mkString)
  }

  test("local let") {
    expectResult("Let(X,5,6)")(
      compile("ask turtles [ let x 5 ] print 0").mkString)
  }

}
