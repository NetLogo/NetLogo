// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import org.nlogo.core.StructureDeclarations.Identifier
import org.scalatest.FunSuite

import scala.collection.immutable.ListMap

class BreedIdentifierHandlerTests extends FunSuite {

  val tokenizer: TokenizerInterface =
    Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer")

  def tester(handler: BreedIdentifierHandler.BreedPrimSpec, code: String, tokenString: String): (String, String, TokenType) = {
    val program =
      Program.empty.copy(
        breeds = ListMap("FROGS" -> Breed("FROGS", "FROG")),
        linkBreeds = ListMap(
          "AS" -> Breed("AS", "A", isDirected = true),
          "BS" -> Breed("BS", "B", isDirected = false)))
    handler.process(
      tokenizer.tokenizeString(code).find(_.text.equalsIgnoreCase(tokenString)).orNull,
      program)
      .get
  }

  test("turtleBreedIdentifier") {
    assertResult(("_createturtles", "FROGS", TokenType.Command))(
      tester(BreedIdentifierHandler.TurtlePrimitive(
        "CREATE-<BREEDS>", TokenType.Command, "_createturtles"),
        "breed[frogs frog] to foo create-frogs 1 end", "CREATE-FROGS"))
  }

  test("directedLinkBreedIdentifier1") {
    assertResult(("_createlinkto", "AS", TokenType.Command))(
      tester(BreedIdentifierHandler.DirectedLinkPrimitive(
        "CREATE-<BREED>-TO", TokenType.Command, "_createlinkto"),
        "directed-link-breed[as a] to foo ask turtle 0 [ create-a-to turtle 1 ] end",
        "CREATE-A-TO"))
  }

  test("directedLinkBreedIdentifier2") {
    assertResult(("_outlinkneighbor", "AS", TokenType.Reporter))(
      tester(BreedIdentifierHandler.DirectedLinkPrimitive(
        "OUT-<BREED>-NEIGHBOR?", TokenType.Reporter, "_outlinkneighbor"),
      "directed-link-breed[as a] to foo ask turtle 0 [ print out-a-neighbor? turtle 1 ] end",
      "OUT-A-NEIGHBOR?"))
  }

  test("undirectedLinkBreedIdentifier") {
    assertResult(("_createlinkwith", "BS", TokenType.Command))(
    tester(BreedIdentifierHandler.UndirectedLinkPrimitive(
      "CREATE-<BREED>-WITH", TokenType.Command, "_createlinkwith"),
      "undirected-link-breed[bs b] to foo ask turtle 0 [ create-b-with turtle 1 ] end",
      "CREATE-B-WITH"))
  }

  test("breedCommands returns commands for a turtle breed") {
    assertResult(
      List("CREATE-ORDERED-KITTENS", "CREATE-KITTENS", "HATCH-KITTENS", "SPROUT-KITTENS")
    )(BreedIdentifierHandler.breedCommands(breed("KITTENS", "KITTEN")))
  }

  test("breedHomonymProcedures returns singular and plural procedures") {
    assertResult(
      List("KITTENS", "KITTEN")
    )(BreedIdentifierHandler.breedHomonymProcedures(breed("KITTENS", "KITTEN")))
  }

  test("breedHomonymProcedures returns only one procedure name when singular and plural are the same") {
    assertResult(
      List("DEER")
    )(BreedIdentifierHandler.breedHomonymProcedures(breed("DEER", "DEER")))
  }

  test("breedReporters returns reporters for a turtle breed") {
    assertResult(
      List("KITTENS-AT", "KITTENS-HERE", "KITTENS-ON","IS-KITTEN?")
    )(BreedIdentifierHandler.breedReporters(breed("KITTENS", "KITTEN")))
  }

  test("breedCommands returns commands for directed links") {
    assertResult(
      List("CREATE-CHAINS-FROM", "CREATE-CHAIN-FROM", "CREATE-CHAINS-TO", "CREATE-CHAIN-TO")
    )(BreedIdentifierHandler.breedCommands(breed("CHAINS", "CHAIN", isLink = true, isDirected = true)))
  }

  test("breedReporters returns reporters for directed links") {
    assertResult(
      List("IN-CHAIN-FROM", "IN-CHAIN-NEIGHBOR?", "IN-CHAIN-NEIGHBORS", "IS-CHAIN?", "MY-IN-CHAINS", "MY-OUT-CHAINS",
           "OUT-CHAIN-NEIGHBOR?", "OUT-CHAIN-NEIGHBORS", "OUT-CHAIN-TO")
    )(BreedIdentifierHandler.breedReporters(breed("CHAINS", "CHAIN", isLink = true, isDirected = true)))
  }
  private def breed(plural: String, singular: String, isLink: Boolean = false, isDirected: Boolean = false): StructureDeclarations.Breed = {
    def dummyToken(s: String): Token = Token(s, TokenType.Ident, "")(1, 2, "")
    def dummyIdent(s: String): Identifier = Identifier(s, dummyToken(s))

    StructureDeclarations.Breed(dummyIdent(plural), dummyIdent(singular), isLink, isDirected)
  }
}
