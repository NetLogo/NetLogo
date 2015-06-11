// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite
import org.nlogo.api.{ Program, Token, TokenType }
import org.nlogo.prim._

class BreedIdentifierHandlerTests extends FunSuite {
  def tester(handler: BreedIdentifierHandler.Helper, code: String, tokenString: String): Token = {
    val program = new Program(false)
    program.breeds.put("FROGS", "FROGS")
    program.breedsSingular.put("FROG", "FROGS")
    program.linkBreeds.put("AS", "DIRECTED-LINK-BREED")
    program.linkBreedsSingular.put("A", "AS")
    program.linkBreeds.put("BS", "UNDIRECTED-LINK-BREED")
    program.linkBreedsSingular.put("B", "BS")
    handler.process(
      Compiler.Tokenizer2D.tokenize(code).find(_.name.equalsIgnoreCase(tokenString)).orNull,
      program)
      .get
  }
  test("turtleBreedIdentifier") {
    val token = tester(BreedIdentifierHandler.turtle("CREATE-*", TokenType.COMMAND, false,
      classOf[_createturtles]),
      "breed[frogs frog] to foo create-frogs 1 end", "CREATE-FROGS")
    assert(token.value.isInstanceOf[_createturtles])
    assertResult("_createturtles:FROGS,+0")(token.value.toString)
  }
  test("directedLinkBreedIdentifier1") {
    val token = tester(BreedIdentifierHandler.directedLink
      ("CREATE-*-TO", TokenType.COMMAND, true,
        classOf[_createlinkto]),
      "directed-link-breed[as a] to foo ask turtle 0 [ create-a-to turtle 1 ] end",
      "CREATE-A-TO")
    assert(token.value.isInstanceOf[_createlinkto])
    assertResult("_createlinkto:AS,+0")(token.value.toString)
  }
  test("directedLinkBreedIdentifier2") {
    val token = tester(BreedIdentifierHandler.directedLink
      ("OUT-*-NEIGHBOR?", TokenType.REPORTER, true,
        classOf[_outlinkneighbor]),
      "directed-link-breed[as a] to foo ask turtle 0 [ print out-a-neighbor? turtle 1 ] end",
      "OUT-A-NEIGHBOR?")
    assert(token.value.isInstanceOf[_outlinkneighbor])
    assertResult("_outlinkneighbor:AS")(token.value.toString)
  }
  test("undirectedLinkBreedIdentifier") {
    val token = tester(BreedIdentifierHandler.undirectedLink
      ("CREATE-*-WITH", TokenType.COMMAND, true,
        classOf[_createlinkwith]),
      "undirected-link-breed[bs b] to foo ask turtle 0 [ create-b-with turtle 1 ] end",
      "CREATE-B-WITH")
    assert(token.value.isInstanceOf[_createlinkwith])
    assertResult("_createlinkwith:BS,+0")(token.value.toString)
  }
}
