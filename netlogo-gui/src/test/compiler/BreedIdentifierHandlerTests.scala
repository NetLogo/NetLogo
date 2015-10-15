// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite
import org.nlogo.core.Breed
import org.nlogo.core.Program
import org.nlogo.core.Token
import org.nlogo.core.TokenType
import org.nlogo.prim._

class BreedIdentifierHandlerTests extends FunSuite {
  def tester(handler: BreedIdentifierHandler.Helper, code: String, tokenString: String): Token = {
    val p= Program.empty()
    val program = p.copy(
      breeds = scala.collection.immutable.ListMap(
        "FROGS" -> Breed("FROGS", "FROG")),
      linkBreeds = scala.collection.immutable.ListMap(
        "AS" -> Breed("AS", "A", isLinkBreed = true, isDirected = true),
        "BS" -> Breed("BS", "B", isLinkBreed = true, isDirected = false)))
    handler.process(
      Compiler.Tokenizer2D.tokenize(code).find(_.text.equalsIgnoreCase(tokenString)).orNull,
      program)
      .get
  }
  test("turtleBreedIdentifier") {
    val token = tester(BreedIdentifierHandler.turtle("CREATE-*", TokenType.Command, false,
      classOf[_createturtles]),
      "breed[frogs frog] to foo create-frogs 1 end", "CREATE-FROGS")
    assert(token.value.isInstanceOf[_createturtles])
    assertResult("_createturtles:FROGS,+0")(token.value.toString)
  }
  test("directedLinkBreedIdentifier1") {
    val token = tester(BreedIdentifierHandler.directedLink
      ("CREATE-*-TO", TokenType.Command, true,
        classOf[_createlinkto]),
      "directed-link-breed[as a] to foo ask turtle 0 [ create-a-to turtle 1 ] end",
      "CREATE-A-TO")
    assert(token.value.isInstanceOf[_createlinkto])
    assertResult("_createlinkto:AS,+0")(token.value.toString)
  }
  test("directedLinkBreedIdentifier2") {
    val token = tester(BreedIdentifierHandler.directedLink
      ("OUT-*-NEIGHBOR?", TokenType.Reporter, true,
        classOf[_outlinkneighbor]),
      "directed-link-breed[as a] to foo ask turtle 0 [ print out-a-neighbor? turtle 1 ] end",
      "OUT-A-NEIGHBOR?")
    assert(token.value.isInstanceOf[_outlinkneighbor])
    assertResult("_outlinkneighbor:AS")(token.value.toString)
  }
  test("undirectedLinkBreedIdentifier") {
    val token = tester(BreedIdentifierHandler.undirectedLink
      ("CREATE-*-WITH", TokenType.Command, true,
        classOf[_createlinkwith]),
      "undirected-link-breed[bs b] to foo ask turtle 0 [ create-b-with turtle 1 ] end",
      "CREATE-B-WITH")
    assert(token.value.isInstanceOf[_createlinkwith])
    assertResult("_createlinkwith:BS,+0")(token.value.toString)
  }
}
