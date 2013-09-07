// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite
import org.nlogo.{ api, nvm }

class LetScoperTests extends FunSuite {

  def compile(source: String): Iterable[api.Let] = {
    val wrappedSource = "to __test " + source + "\nend"
    val results = new StructureParser(
      Parser.Tokenizer2D.tokenize(wrappedSource), None,
      StructureParser.emptyResults())
      .parse(false)
    assertResult(1)(results.procedures.size)
    val procedure = results.procedures.values.iterator.next()
    new LetScoper(procedure, results.tokens(procedure), results.program.usedNames).scan()
    procedure.lets
  }

  test("empty") {
    assertResult("")(compile("").mkString)
  }

  test("let") {
    assertResult("Let(Y,2,5)")(
      compile("let y 5 print y").mkString)
  }

  test("local let") {
    assertResult("Let(X,5,6)")(
      compile("ask turtles [ let x 5 ] print 0").mkString)
  }

  // https://github.com/NetLogo/NetLogo/issues/348
  test("let of task variable") {
    val e = intercept[api.CompilerException] {
      compile("foreach [1] [ let ? 0 ]") }
    val message =
      "Names beginning with ? are reserved for use as task inputs"
    assertResult(message)(e.getMessage)
  }

}
