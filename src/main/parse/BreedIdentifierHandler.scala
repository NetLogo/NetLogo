// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.api,
  api.{ Breed, Program, Token, TokenType },
  TokenType.{ Command, Reporter }

object BreedIdentifierHandler {

  // example input:  Token("hatch-frogs", TokenType.Ident, "HATCH-FROGS")
  // example result: Some(("_hatch", "FROGS", TokenType.Command))
  def process(token: Token, program: Program): Option[(String, String, TokenType)] =
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
    // turtle creation
    turtle(Spec("CREATE-ORDERED-*" , Command , false, "_createorderedturtles" )),
    turtle(Spec("CREATE-*"         , Command , false, "_createturtles"        )),
    turtle(Spec("HATCH-*"          , Command , false, "_hatch"                )),
    turtle(Spec("SPROUT-*"         , Command , false, "_sprout"               )),
    // turtle breeds
    turtle(Spec("*"                , Reporter, false, "etc._breed"                )),
    turtle(Spec("*-AT"             , Reporter, false, "etc._breedat"              )),
    turtle(Spec("*-HERE"           , Reporter, false, "etc._breedhere"            )),
    turtle(Spec("*-ON"             , Reporter, false, "etc._breedon"              )),
    turtle(Spec("*"                , Reporter, true , "etc._breedsingular"        )),
    turtle(Spec("IS-*?"            , Reporter, true , "etc._isbreed"              )),
    // link breeds
    directedLink   ( Spec("CREATE-*-FROM"   , Command , true , "etc._createlinkfrom"    )),
    directedLink   ( Spec("CREATE-*-FROM"   , Command , false, "etc._createlinksfrom"   )),
    directedLink   ( Spec("CREATE-*-TO"     , Command , false, "etc._createlinksto"     )),
    undirectedLink ( Spec("CREATE-*-WITH"   , Command , false, "etc._createlinkswith"   )),
    directedLink   ( Spec("CREATE-*-TO"     , Command , true , "etc._createlinkto"      )),
    undirectedLink ( Spec("CREATE-*-WITH"   , Command , true , "etc._createlinkwith"    )),
    directedLink   ( Spec("IN-*-FROM"       , Reporter, true , "etc._inlinkfrom"        )),
    directedLink   ( Spec("IN-*-NEIGHBOR?"  , Reporter, true , "etc._inlinkneighbor"    )),
    directedLink   ( Spec("IN-*-NEIGHBORS"  , Reporter, true , "etc._inlinkneighbors"   )),
    directedLink   ( Spec("IS-*?"           , Reporter, true , "etc._isbreed"           )),
    undirectedLink ( Spec("IS-*?"           , Reporter, true , "etc._isbreed"           )),
    directedLink   ( Spec("*"               , Reporter, false, "etc._linkbreed"         )),
    undirectedLink ( Spec("*"               , Reporter, false, "etc._linkbreed"         )),
    directedLink   ( Spec("*"               , Reporter, true , "etc._linkbreedsingular" )),
    undirectedLink ( Spec("*"               , Reporter, true , "etc._linkbreedsingular" )),
    undirectedLink ( Spec("*-NEIGHBOR?"     , Reporter, true , "etc._linkneighbor"      )),
    undirectedLink ( Spec("*-NEIGHBORS"     , Reporter, true , "etc._linkneighbors"     )),
    undirectedLink ( Spec("*-WITH"          , Reporter, true , "etc._linkwith"          )),
    directedLink   ( Spec("MY-IN-*"         , Reporter, false, "etc._myinlinks"         )),
    undirectedLink ( Spec("MY-*"            , Reporter, false, "etc._mylinks"           )),
    directedLink   ( Spec("MY-OUT-*"        , Reporter, false, "etc._myoutlinks"        )),
    directedLink   ( Spec("OUT-*-NEIGHBOR?" , Reporter, true , "etc._outlinkneighbor"   )),
    directedLink   ( Spec("OUT-*-NEIGHBORS" , Reporter, true , "etc._outlinkneighbors"  )),
    directedLink   ( Spec("OUT-*-TO"        , Reporter, true , "etc._outlinkto"         ))
  )

  import java.util.regex.Pattern

  class Helper(spec: Spec, breeds: Program => collection.Map[String, Breed],
    isValidBreed: Breed => Boolean) {
    val pattern = Pattern.compile("\\A" +
      spec.patternString.replaceAll("\\?", "\\\\?")
      .replaceAll("\\*", "(.+)") + "\\Z")
    def process(tok: Token, program: Program): Option[(String, String, TokenType)] = {
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
        if (!isValidBreed(breed))
          None
        else
          Some((spec.primClass, breed.name, spec.tokenType))
      }
    }
  }

}
