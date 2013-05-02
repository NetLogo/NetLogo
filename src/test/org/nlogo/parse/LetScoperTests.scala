// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite
import org.nlogo.{ api, nvm }

class LetScoperTests extends FunSuite {

  def compile(source: String): Iterable[api.Let] = {
    val wrappedSource = "to __test " + source + "\nend"
    val results = new StructureParser(
      Parser.tokenizer.tokenize(wrappedSource), None,
      StructureResults.empty)
      .parse(false)
    expectResult(1)(results.procedures.size)
    val procedure = results.procedures.values.iterator.next()
    new LetScoper(results.tokens(procedure))
      .scan(results.program.usedNames)
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
