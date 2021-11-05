// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core
import org.scalatest.funsuite.AnyFunSuite

class TokenColorizeTests extends AnyFunSuite {
  import FrontEndTests.extensionManager

  def colorTokenize(source: String): Seq[core.Token] = {
    FrontEnd.tokenizeForColorization(source, core.NetLogoCore, extensionManager)
  }

  def assertColorTokenize(source: String, expectedTypes: Seq[core.TokenType]): Unit = {
    val toks = colorTokenize(source)
    (toks zip expectedTypes).foreach {
      case (t, expectedType) => assertResult(expectedType)(t.tpe)
    }
  }

  test("tokenizeForColorization tokenizes unknown values as Ident") {
    assertColorTokenize("foobarbaz", Seq(core.TokenType.Ident))
  }
  test("tokenizeForColorization tags commands with the correct token type") {
    assertColorTokenize("fd 1", Seq(core.TokenType.Command, core.TokenType.Literal))
  }
  test("tokenizeForColorization tags reporters with correct token type") {
    assertColorTokenize("list 1 2 3", Seq(core.TokenType.Reporter))
  }
  test("tokenizeForColorization tags keywords with correct token type") {
    assertColorTokenize("to foo", Seq(core.TokenType.Keyword, core.TokenType.Ident))
  }
  test("tokenizeForColorization tags extension prims with correct token type") {
    assertColorTokenize("foo:bar foo:baz",
      Seq(core.TokenType.Command, core.TokenType.Reporter))
  }
  test("tokenizeForColorization tags agent variables with correct token type") {
    assertColorTokenize("set pcolor color",
      Seq(core.TokenType.Command, core.TokenType.Reporter, core.TokenType.Reporter))
  }
}
