// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import StructureDeclarations.{Breed => DeclBreed}
import TokenType.{Command, Reporter}

import scala.util.matching.Regex

object BreedIdentifierHandler {

  private type SpecMatcher = PartialFunction[BreedPrimSpec, BreedPrimSpec]

  private val BreedPatternString = "<BREEDS?>"

  private val handlers = List(

    // turtle creation
    TurtlePrimitive("CREATE-ORDERED-<BREEDS>" , Command , "_createorderedturtles" ),
    TurtlePrimitive("CREATE-<BREEDS>"         , Command ,  "_createturtles"       ),
    TurtlePrimitive("HATCH-<BREEDS>"          , Command ,  "_hatch"               ),
    TurtlePrimitive("SPROUT-<BREEDS>"         , Command ,  "_sprout"              ),

    // turtle breeds
    TurtlePrimitive("<BREEDS>"      , Reporter,  "_breed"             ),
    TurtlePrimitive("<BREEDS>-AT"   , Reporter,  "etc._breedat"       ),
    TurtlePrimitive("<BREEDS>-HERE" , Reporter,  "etc._breedhere"     ),
    TurtlePrimitive("<BREEDS>-ON"   , Reporter,  "etc._breedon"       ),
    TurtlePrimitive("<BREED>"       , Reporter,  "etc._breedsingular" ),
    TurtlePrimitive("IS-<BREED>?"   , Reporter,  "etc._isbreed"       ),

    // link breeds
    DirectedLinkPrimitive   ( "CREATE-<BREEDS>-FROM"  , Command ,  "etc._createlinksfrom"   ),
    DirectedLinkPrimitive   ( "CREATE-<BREED>-FROM"   , Command ,  "etc._createlinkfrom"    ),
    DirectedLinkPrimitive   ( "CREATE-<BREEDS>-TO"    , Command ,  "etc._createlinksto"     ),
    UndirectedLinkPrimitive ( "CREATE-<BREEDS>-WITH"  , Command ,  "etc._createlinkswith"   ),
    DirectedLinkPrimitive   ( "CREATE-<BREED>-TO"     , Command ,  "etc._createlinkto"      ),
    UndirectedLinkPrimitive ( "CREATE-<BREED>-WITH"   , Command ,  "etc._createlinkwith"    ),
    DirectedLinkPrimitive   ( "IN-<BREED>-FROM"       , Reporter,  "etc._inlinkfrom"        ),
    DirectedLinkPrimitive   ( "IN-<BREED>-NEIGHBOR?"  , Reporter,  "etc._inlinkneighbor"    ),
    DirectedLinkPrimitive   ( "IN-<BREED>-NEIGHBORS"  , Reporter,  "etc._inlinkneighbors"   ),
    UndirectedLinkPrimitive ( "IS-<BREED>?"           , Reporter,  "etc._isbreed"           ),
    DirectedLinkPrimitive   ( "IS-<BREED>?"           , Reporter,  "etc._isbreed"           ),
    DirectedLinkPrimitive   ( "<BREEDS>"              , Reporter,  "etc._linkbreed"         ),
    UndirectedLinkPrimitive ( "<BREEDS>"              , Reporter,  "etc._linkbreed"         ),
    DirectedLinkPrimitive   ( "<BREED>"               , Reporter,  "etc._linkbreedsingular" ),
    UndirectedLinkPrimitive ( "<BREED>"               , Reporter,  "etc._linkbreedsingular" ),
    UndirectedLinkPrimitive ( "<BREED>-NEIGHBOR?"     , Reporter,  "etc._linkneighbor"      ),
    UndirectedLinkPrimitive ( "<BREED>-NEIGHBORS"     , Reporter,  "etc._linkneighbors"     ),
    UndirectedLinkPrimitive ( "<BREED>-WITH"          , Reporter,  "etc._linkwith"          ),
    DirectedLinkPrimitive   ( "MY-IN-<BREEDS>"        , Reporter,  "etc._myinlinks"         ),
    UndirectedLinkPrimitive ( "MY-<BREEDS>"           , Reporter,  "etc._mylinks"           ),
    DirectedLinkPrimitive   ( "MY-OUT-<BREEDS>"       , Reporter,  "etc._myoutlinks"        ),
    DirectedLinkPrimitive   ( "OUT-<BREED>-NEIGHBOR?" , Reporter,  "etc._outlinkneighbor"   ),
    DirectedLinkPrimitive   ( "OUT-<BREED>-NEIGHBORS" , Reporter,  "etc._outlinkneighbors"  ),
    DirectedLinkPrimitive   ( "OUT-<BREED>-TO"        , Reporter,  "etc._outlinkto"         )

  )

  // example input:  Token("hatch-frogs", TokenType.Ident, "HATCH-FROGS")
  // example result: Some(("_hatch", "FROGS", TokenType.Command))
  def process(token: Token, program: Program): Option[(String, String, TokenType)] =
    handlers.toStream.flatMap(_.process(token, program)).headOption

  def breedCommands(breed: DeclBreed): Seq[String] =
    handlers.collect(breedPrimitivesMatching(breed, Command) andThen refineName(breed))

  def breedReporters(breed: DeclBreed): Seq[String] =
    handlers.collect(breedPrimitivesMatching(breed, Reporter) andThen refineName(breed))

  def breedHomonymProcedures(breed: DeclBreed): Seq[String] =
    handlers.collect(primitivesNamedLike(breed) andThen refineName(breed)).distinct

  private def breedPrimitivesMatching(breed: DeclBreed, tokenType: TokenType): SpecMatcher =
    breedPrimsMatching(tokenType, breed, !matchesBreedName(_))

  private def primitivesNamedLike(breed: DeclBreed): SpecMatcher =
    breedPrimsMatching(Reporter, breed, matchesBreedName)

  private def matchesBreedName(patternString: String): Boolean =
    patternString.matches(s"^$BreedPatternString$$")

  private def breedPrimsMatching(tokenType: TokenType, breed: DeclBreed, matches: (String) => Boolean): SpecMatcher =
    if (breed.isLinkBreed && breed.isDirected)
      { case directedLink@DirectedLinkPrimitive(pattern, `tokenType`, _) if matches(pattern) => directedLink }
    else if (breed.isLinkBreed)
      { case undirectedLink@UndirectedLinkPrimitive(pattern, `tokenType`, _) if matches(pattern) => undirectedLink }
    else
      { case turtle@TurtlePrimitive(pattern, `tokenType`, _) if matches(pattern) => turtle }

  private def refineName(breed: DeclBreed)(helper: BreedPrimSpec): String =
    helper.patternString.
      replaceAll("<BREEDS>", breed.plural.name).
      replaceAll("<BREED>", breed.singular.name)

  trait BreedPrimSpec {

    private val PatternMatcher: Regex = {
      val refinedPattern = patternString.replaceAll("\\?", "\\\\?").replaceAll(BreedPatternString, "(.+)")
      s"^$refinedPattern$$".r
    }

    def patternString:              String
    def tokenType:                  TokenType
    def primClass:                  String
    def breeds(program: Program):   Map[String, Breed]
    def isValidBreed(breed: Breed): Boolean

    def process(tok: Token, program: Program): Option[(String, String, TokenType)] =
      tok.value.asInstanceOf[String] match {
        case PatternMatcher(name) =>
          breeds(program).values.collectFirst {
            case breed if name == matchingName(breed) && isValidBreed(breed) => (primClass, breed.name, tokenType)
          }
        case _ =>
          None
      }

    private def matchingName(breed: Breed): String =
      if (patternString.contains("<BREED>")) breed.singular else breed.name

  }

  // sigh, these are private[core] rather than just private so we can reference
  // them directly in the tests - ST 11/17/14

  private[core] case class TurtlePrimitive(patternString: String, tokenType: TokenType, primClass: String) extends BreedPrimSpec {
    override def breeds(program: Program):   Map[String, Breed] = program.breeds
    override def isValidBreed(breed: Breed): Boolean            = true
  }

  private[core] case class DirectedLinkPrimitive(patternString: String, tokenType: TokenType, primClass: String) extends BreedPrimSpec {
    override def breeds(program: Program):   Map[String, Breed] = program.linkBreeds
    override def isValidBreed(breed: Breed): Boolean            = breed.isDirected
  }

  private[core] case class UndirectedLinkPrimitive(patternString: String, tokenType: TokenType, primClass: String) extends BreedPrimSpec {
    override def breeds(program: Program):   Map[String, Breed] = program.linkBreeds
    override def isValidBreed(breed: Breed): Boolean            = !breed.isDirected
  }
}
