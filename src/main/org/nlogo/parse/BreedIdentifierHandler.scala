// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.api.{ AgentSet, Breed, Program, Token, TokenType }
import org.nlogo.nvm.Instruction

// The Helper class and some of the methods aren't private because we want to get at them from
// TestBreedIdentifierHandler. - ST 12/22/08
private object BreedIdentifierHandler {
  import org.nlogo.prim._
  import TokenType.Command
  import TokenType.Reporter
  def process(token: Token, program: Program): Option[Token] =
    handlers.toStream.flatMap(_.process(token, program)).headOption
  def turtle(patternString: String, tokenType: TokenType, singular: Boolean, primClass: Class[_ <: Instruction]) =
    new Helper(patternString, tokenType, singular, primClass,
               _.breeds, _ => true)
  def directedLink(patternString: String, tokenType: TokenType, singular: Boolean, primClass: Class[_ <: Instruction]) =
    new Helper(patternString, tokenType, singular, primClass,
               _.linkBreeds, _.isDirected)
  def undirectedLink(patternString: String, tokenType: TokenType, singular: Boolean, primClass: Class[_ <: Instruction]) =
    new Helper(patternString, tokenType, singular, primClass,
               _.linkBreeds, !_.isDirected)
  private val handlers = List(
    // prims for turtle breeds
    turtle("CREATE-*"         , Command, false, classOf[_createturtles]),
    turtle("CREATE-ORDERED-*" , Command, false, classOf[_createorderedturtles]),
    turtle("HATCH-*"          , Command, false, classOf[_hatch]),
    turtle("SPROUT-*"         , Command, false, classOf[_sprout]),
    turtle("IS-*?"            , Reporter, true, classOf[_isbreed]),
    turtle("*-HERE"           , Reporter, false, classOf[_breedhere]),
    turtle("*-ON"             , Reporter, false, classOf[_breedon]),
    turtle("*"                , Reporter, false, classOf[_breed]),
    turtle("*"                , Reporter, true, classOf[_breedsingular]),
    turtle("*-AT"             , Reporter, false, classOf[_breedat]),
    // prims for link breeds
    directedLink("*"               , Reporter, true, classOf[_linkbreedsingular]),
    undirectedLink("*"             , Reporter, true, classOf[_linkbreedsingular]),
    directedLink("*"               , Reporter, false, classOf[_linkbreed]),
    undirectedLink("*"             , Reporter, false, classOf[_linkbreed]),
    directedLink("IS-*?"           , Reporter, true, classOf[_isbreed]),
    undirectedLink("IS-*?"         , Reporter, true, classOf[_isbreed]),
    directedLink("CREATE-*-FROM"   , Command, true, classOf[_createlinkfrom]),
    directedLink("CREATE-*-FROM"   , Command, false, classOf[_createlinksfrom]),
    directedLink("CREATE-*-TO"     , Command, true, classOf[_createlinkto]),
    directedLink("CREATE-*-TO"     , Command, false, classOf[_createlinksto]),
    undirectedLink("CREATE-*-WITH" , Command, true, classOf[_createlinkwith]),
    undirectedLink("CREATE-*-WITH" , Command, false, classOf[_createlinkswith]),
    directedLink("IN-*-NEIGHBOR?"  , Reporter, true, classOf[_inlinkneighbor]),
    directedLink("OUT-*-NEIGHBOR?" , Reporter, true, classOf[_outlinkneighbor]),
    directedLink("IN-*-FROM"       , Reporter, true, classOf[_inlinkfrom]),
    directedLink("OUT-*-TO"        , Reporter, true, classOf[_outlinkto]),
    directedLink("OUT-*-NEIGHBORS" , Reporter, true, classOf[_outlinkneighbors]),
    directedLink("IN-*-NEIGHBORS"  , Reporter, true, classOf[_inlinkneighbors]),
    directedLink("MY-IN-*"         , Reporter, false, classOf[_myinlinks]),
    directedLink("MY-OUT-*"        , Reporter, false, classOf[_myoutlinks]),
    undirectedLink("*-NEIGHBORS"   , Reporter, true, classOf[_linkneighbors]),
    undirectedLink("MY-*"          , Reporter, false, classOf[_mylinks]),
    undirectedLink("*-WITH"        , Reporter, true, classOf[_linkwith]),
    undirectedLink("*-NEIGHBOR?"   , Reporter, true, classOf[_linkneighbor])
  )
  class Helper
    (patternString: String, tokenType: TokenType, singular: Boolean, primClass: Class[_ <: Instruction],
     breeds: (Program) => collection.Map[String, Breed], isValidBreed: (Breed) => Boolean)
  {
    import java.util.regex.Pattern
    val pattern = Pattern.compile("\\A"+patternString.replaceAll("\\?", "\\\\?").replaceAll("\\*", "(.+)")+"\\Z")
    def process(tok: Token, program: Program): Option[Token] = {
      val matcher = pattern.matcher(tok.value.asInstanceOf[String])
      if(!matcher.matches()) return None
      val name = matcher.group(1)
      val breed =
        breeds(program).values
          .find{breed => name == (if (singular) breed.singular else breed.name)}
          .getOrElse(return None)
      if (!isValidBreed(breed)) return None
      val instr = Instantiator.newInstance[Instruction](primClass, breed.name)
      val tok2 = new Token(tok.value.asInstanceOf[String], tokenType, instr)(tok.startPos, tok.endPos, tok.fileName)
      instr.token(tok2)
      Some(tok2)
    }
  }
}
