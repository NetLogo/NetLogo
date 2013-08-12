// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse0

import org.scalatest.FunSuite
import collection.immutable.ListMap
import org.nlogo.api, api.{ Breed, Program, Token, TokenType }
import org.nlogo.util.Femto
import BreedIdentifierHandler.Spec

class BreedIdentifierHandlerTests extends FunSuite {

  val tokenizer: api.TokenizerInterface =
    Femto.scalaSingleton("org.nlogo.lex.Tokenizer")

  def tester(handler: BreedIdentifierHandler.Helper, code: String, tokenString: String): (String, String, TokenType) = {
    val program =
      Program.empty.copy(
        breeds = ListMap("FROGS" -> Breed("FROGS", "FROG")),
        linkBreeds = ListMap(
          "AS" -> Breed("AS", "A", isDirected = true),
          "BS" -> Breed("BS", "B", isDirected = false)))
    handler.process(
      tokenizer.tokenize(code).find(_.text.equalsIgnoreCase(tokenString)).orNull,
      program)
      .get
  }

  test("turtleBreedIdentifier") {
    assertResult(("_createturtles", "FROGS", TokenType.Command))(
      tester(BreedIdentifierHandler.turtle(
        Spec("CREATE-*", TokenType.Command, false, "_createturtles")),
        "breed[frogs frog] to foo create-frogs 1 end", "CREATE-FROGS"))
  }

  test("directedLinkBreedIdentifier1") {
    assertResult(("_createlinkto", "AS", TokenType.Command))(
      tester(BreedIdentifierHandler.directedLink(Spec(
        "CREATE-*-TO", TokenType.Command, true, "_createlinkto")),
        "directed-link-breed[as a] to foo ask turtle 0 [ create-a-to turtle 1 ] end",
        "CREATE-A-TO"))
  }

  test("directedLinkBreedIdentifier2") {
    assertResult(("_outlinkneighbor", "AS", TokenType.Reporter))(
      tester(BreedIdentifierHandler.directedLink(Spec(
        "OUT-*-NEIGHBOR?", TokenType.Reporter, true,
        "_outlinkneighbor")),
      "directed-link-breed[as a] to foo ask turtle 0 [ print out-a-neighbor? turtle 1 ] end",
      "OUT-A-NEIGHBOR?"))
  }

  test("undirectedLinkBreedIdentifier") {
    assertResult(("_createlinkwith", "BS", TokenType.Command))(
    tester(BreedIdentifierHandler.undirectedLink(Spec(
      "CREATE-*-WITH", TokenType.Command, true,
      "_createlinkwith")),
      "undirected-link-breed[bs b] to foo ask turtle 0 [ create-b-with turtle 1 ] end",
      "CREATE-B-WITH"))
  }

}
