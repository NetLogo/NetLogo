// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.api,
  api.{ AgentSet, Breed, Program, Token, TokenType },
  TokenType.{ Command, Reporter }

object BreedIdentifierHandler {

  def process(token: Token, program: Program): Option[Token] =
    handlers.toStream.flatMap(_.process(token, program))
      .headOption

  case class Spec(
    patternString: String,
    tokenType: TokenType,
    singular: Boolean,
    primClass: String)

  def turtle(spec: Spec) =
    new Helper(spec, _.breeds, _ => true)

  def directedLink(spec: Spec) =
    new Helper(spec, _.linkBreeds, _.isDirected)

  def undirectedLink(spec: Spec) =
    new Helper(spec, _.linkBreeds, !_.isDirected)

  val handlers = List(
    // prims for turtle breeds
    turtle(Spec("CREATE-ORDERED-*" , Command , false, "_createorderedturtles" )),
    turtle(Spec("CREATE-*"         , Command , false, "_createturtles"        )),
    turtle(Spec("HATCH-*"          , Command , false, "_hatch"                )),
    turtle(Spec("SPROUT-*"         , Command , false, "_sprout"               )),
    turtle(Spec("*"                , Reporter, false, "_breed"                )),
    turtle(Spec("*-AT"             , Reporter, false, "_breedat"              )),
    turtle(Spec("*-HERE"           , Reporter, false, "_breedhere"            )),
    turtle(Spec("*-ON"             , Reporter, false, "_breedon"              )),
    turtle(Spec("*"                , Reporter, true , "_breedsingular"        )),
    turtle(Spec("IS-*?"            , Reporter, true , "_isbreed"              )),
    // prims for link breeds
    directedLink   ( Spec("CREATE-*-FROM"   , Command , true , "_createlinkfrom"    )),
    directedLink   ( Spec("CREATE-*-FROM"   , Command , false, "_createlinksfrom"   )),
    directedLink   ( Spec("CREATE-*-TO"     , Command , false, "_createlinksto"     )),
    undirectedLink ( Spec("CREATE-*-WITH"   , Command , false, "_createlinkswith"   )),
    directedLink   ( Spec("CREATE-*-TO"     , Command , true , "_createlinkto"      )),
    undirectedLink ( Spec("CREATE-*-WITH"   , Command , true , "_createlinkwith"    )),
    directedLink   ( Spec("IN-*-FROM"       , Reporter, true , "_inlinkfrom"        )),
    directedLink   ( Spec("IN-*-NEIGHBOR?"  , Reporter, true , "_inlinkneighbor"    )),
    directedLink   ( Spec("IN-*-NEIGHBORS"  , Reporter, true , "_inlinkneighbors"   )),
    directedLink   ( Spec("IS-*?"           , Reporter, true , "_isbreed"           )),
    undirectedLink ( Spec("IS-*?"           , Reporter, true , "_isbreed"           )),
    directedLink   ( Spec("*"               , Reporter, false, "_linkbreed"         )),
    undirectedLink ( Spec("*"               , Reporter, false, "_linkbreed"         )),
    directedLink   ( Spec("*"               , Reporter, true , "_linkbreedsingular" )),
    undirectedLink ( Spec("*"               , Reporter, true , "_linkbreedsingular" )),
    undirectedLink ( Spec("*-NEIGHBOR?"     , Reporter, true , "_linkneighbor"      )),
    undirectedLink ( Spec("*-NEIGHBORS"     , Reporter, true , "_linkneighbors"     )),
    undirectedLink ( Spec("*-WITH"          , Reporter, true , "_linkwith"          )),
    directedLink   ( Spec("MY-IN-*"         , Reporter, false, "_myinlinks"         )),
    undirectedLink ( Spec("MY-*"            , Reporter, false, "_mylinks"           )),
    directedLink   ( Spec("MY-OUT-*"        , Reporter, false, "_myoutlinks"        )),
    directedLink   ( Spec("OUT-*-NEIGHBOR?" , Reporter, true , "_outlinkneighbor"   )),
    directedLink   ( Spec("OUT-*-NEIGHBORS" , Reporter, true , "_outlinkneighbors"  )),
    directedLink   ( Spec("OUT-*-TO"        , Reporter, true , "_outlinkto"         ))
  )

  import java.util.regex.Pattern

  class Helper(spec: Spec, breeds: Program => collection.Map[String, Breed],
    isValidBreed: Breed => Boolean) {
    val pattern = Pattern.compile("\\A" +
      spec.patternString.replaceAll("\\?", "\\\\?")
      .replaceAll("\\*", "(.+)")+"\\Z")
    def process(tok: Token, program: Program): Option[Token] = {
      val matcher = pattern.matcher(tok.value.asInstanceOf[String])
      if(!matcher.matches())
        None
      else {
        val name = matcher.group(1)
        val breed =
          breeds(program).values
            .find{breed => name ==
              (if (spec.singular) breed.singular else breed.name)}
            .getOrElse(return None)
        if (!isValidBreed(breed)) return None
        val instr = Instantiator.newInstance[api.TokenHolder](
          Class.forName("org.nlogo.prim." + spec.primClass), breed.name)
        val tok2 = new Token(tok.name, spec.tokenType, instr)(
          tok.startPos, tok.endPos, tok.fileName)
        instr.token(tok2)
        Some(tok2)
      }
    }
  }

}
