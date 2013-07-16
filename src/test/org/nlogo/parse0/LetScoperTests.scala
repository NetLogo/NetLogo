// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse0

import org.scalatest.FunSuite
import org.nlogo.api, org.nlogo.util.Femto

class LetScoperTests extends FunSuite {

  val tokenizer: api.TokenizerInterface =
    Femto.scalaSingleton("org.nlogo.lex.Tokenizer")

  def compile(source: String) =
    new LetScoper(tokenizer.tokenize(source).toSeq)
      .scan(Map())

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

  // https://github.com/NetLogo/NetLogo/issues/348
  test("let of task variable") {
    val e = intercept[api.CompilerException] {
      compile("foreach [1] [ let ? 0 ]") }
    val message =
      "Names beginning with ? are reserved for use as task inputs"
    expectResult(message)(e.getMessage)
  }

}
