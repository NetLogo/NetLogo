// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.scalatest.FunSuite
import org.nlogo.core.TokenizerInterface
import org.nlogo.util.Femto

class TestAllTokens extends FunSuite {
  val tokenizer = Femto.scalaSingleton(classOf[TokenizerInterface],
    "org.nlogo.lex.Tokenizer2D").asInstanceOf[org.nlogo.lex.Tokenizer2D.type]
  test("all listed primitives exist") {
    tokenizer.checkInstructionMaps()
  }
}
