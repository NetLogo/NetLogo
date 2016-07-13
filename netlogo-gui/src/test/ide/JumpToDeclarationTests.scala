// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import org.nlogo.core.{SourceLocation, Token, TokenType}
import org.scalatest.FunSuite

class JumpToDeclarationTests extends FunSuite {

  val source1 =
    """
    |me-own[
    |  variable
    |]
    |
    |you-own [
    |  variable
    |]
    |
    |globals [scared]
    |to foo
    |  scared 1
    |  let scared
    |  set scared 0
    |end
    |
    |to new [scared]
    |  set variable 7
    |  set scared 5
    |end
    """.stripMargin

  val source2 =
    """
      |to foo
      |ask turtles [
      |  let scared
      |  set scared 0
      |]
      |set scared 5
      |end
    """.stripMargin

  val source3 =
    """
      |globals [
      |
      |me-own [scared
      |
      |to foo
      |  set scared 0
    """

  def createToken(text: String, tokenType: TokenType, startPos: Int) = new Token(text, tokenType, null)(SourceLocation(startPos, 0, null))

  def testTokenPosition(tokenToFindDeclaration: Token, startIndexOfDeclaration: Int): Unit = {
      val tokenOption = JumpToDeclaration.getDeclaration(tokenToFindDeclaration, source1)
      assert(tokenOption.isDefined)
      tokenOption.foreach(t => {
        assertResult(startIndexOfDeclaration)(t.start)
      })
  }

  def testTokenNotPresent(token: Token): Unit = {
    assertResult(None)(JumpToDeclaration.getDeclaration(token, source2))
  }

  test("global"){
    val token = createToken("scared", TokenType.Ident, source1.indexOf("scared 1") + 1)
    val startIndex = source1.indexOf("globals [scared]") + 9
    testTokenPosition(token, startIndex)
  }

  test("local"){
    val token = createToken("scared", TokenType.Ident, source1.indexOf("set scared 0") + 4)
    val startIndex = source1.indexOf("let scared") + 4
    testTokenPosition(token, startIndex)
  }

  test("functionArguments") {
    val token = createToken("scared", TokenType.Ident, source1.indexOf("set scared 5") + 4)
    val startIndex = source1.indexOf("new [scared]") + 5
    testTokenPosition(token, startIndex)
  }

  test("own-test") {
    val token = createToken("variable", TokenType.Ident, source1.indexOf("set variable 7") + 4)
    val startIndex = source1.indexOf("variable")
    testTokenPosition(token, startIndex)
  }

  test("scope-test") {
    val token = createToken("scared", TokenType.Ident, source1.indexOf("set scared") + 4)
    val startIndex = source1.indexOf("let " + token.text) + 4
    testTokenPosition(token, startIndex)
  }

  test("non-identifier") {
    val token = createToken("set", TokenType.Ident, source1.indexOf("set") + 1)
    testTokenNotPresent(token)
  }

  test("ouside-scope") {
    val token = createToken("scared", TokenType.Ident, source2.indexOf("set scared 5") + 5)
    testTokenNotPresent(token)
  }

  test("incomplete-global") {
    val token = createToken("[", TokenType.Ident, source3.indexOf("["))
    testTokenNotPresent(token)
  }

  test("incomplete-own") {
    val token = createToken("scared", TokenType.Ident, source3.indexOf("set scared") + 4)
    testTokenNotPresent(token)
  }
}
