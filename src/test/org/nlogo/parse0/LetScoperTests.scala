// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse0

import org.scalatest.FunSuite
import org.nlogo.api, org.nlogo.util.Femto

class LetScoperTests extends FunSuite {

  val tokenizer =
    Femto.get(classOf[api.TokenizerInterface],
      "org.nlogo.lex.Tokenizer", Array(api.DummyTokenMapper))

  def compile(source: String) =
    new LetScoper(tokenizer.tokenize(source))
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

}
