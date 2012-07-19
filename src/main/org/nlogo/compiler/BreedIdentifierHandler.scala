// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.api.{AgentSet,Breed,Program,Token,TokenType}
import org.nlogo.nvm.Instruction

// The Helper class and some of the methods aren't private because we want to get at them from
// TestBreedIdentifierHandler. - ST 12/22/08
private object BreedIdentifierHandler {
  import org.nlogo.prim._
  import TokenType.COMMAND
  import TokenType.REPORTER
  def process(token:Token,program:Program):Option[Token] =
    handlers.toStream.flatMap(_.process(token,program)).headOption
  def turtle(patternString:String,tokenType:TokenType,singular:Boolean,primClass:Class[_ <: Instruction]) =
    new Helper(patternString,tokenType,singular,primClass,
               _.breeds, _ => true)
  def directedLink(patternString:String,tokenType:TokenType,singular:Boolean,primClass:Class[_ <: Instruction]) =
    new Helper(patternString,tokenType,singular,primClass,
               _.linkBreeds, _.isDirected)
  def undirectedLink(patternString:String,tokenType:TokenType,singular:Boolean,primClass:Class[_ <: Instruction]) =
    new Helper(patternString,tokenType,singular,primClass,
               _.linkBreeds, !_.isDirected)
  private val handlers = List(
    // prims for turtle breeds
    turtle("CREATE-*", COMMAND, false, classOf[_createturtles]),
    turtle("CREATE-ORDERED-*", COMMAND, false, classOf[_createorderedturtles]),
    turtle("HATCH-*", COMMAND, false, classOf[_hatch]),
    turtle("SPROUT-*", COMMAND, false,classOf[_sprout]),
    turtle("IS-*?", REPORTER, true,classOf[_isbreed]),
    turtle("*-HERE", REPORTER, false,classOf[_breedhere]),
    turtle("*-ON", REPORTER, false,classOf[_breedon]),
    turtle("*", REPORTER, false, classOf[_breed]),
    turtle("*", REPORTER, true, classOf[_breedsingular]),
    turtle("*-AT", REPORTER, false, classOf[_breedat]),
    // prims for link breeds
    directedLink("*", REPORTER, true, classOf[_linkbreedsingular]),
    undirectedLink("*", REPORTER, true, classOf[_linkbreedsingular]),
    directedLink("*", REPORTER, false, classOf[_linkbreed]),
    undirectedLink("*", REPORTER, false, classOf[_linkbreed]),
    directedLink("IS-*?", REPORTER, true,classOf[_isbreed]),
    undirectedLink("IS-*?", REPORTER, true,classOf[_isbreed]),
    directedLink("CREATE-*-FROM", COMMAND, true,classOf[_createlinkfrom]),
    directedLink("CREATE-*-FROM", COMMAND, false,classOf[_createlinksfrom]),
    directedLink("CREATE-*-TO", COMMAND, true,classOf[_createlinkto]),
    directedLink("CREATE-*-TO", COMMAND, false,classOf[_createlinksto]),
    undirectedLink("CREATE-*-WITH", COMMAND, true,classOf[_createlinkwith]),
    undirectedLink("CREATE-*-WITH", COMMAND, false,classOf[_createlinkswith]),
    directedLink("IN-*-NEIGHBOR?", REPORTER, true,classOf[_inlinkneighbor]),
    directedLink("OUT-*-NEIGHBOR?", REPORTER, true,classOf[_outlinkneighbor]),
    directedLink("IN-*-FROM", REPORTER, true,classOf[_inlinkfrom]),
    directedLink("OUT-*-TO", REPORTER, true,classOf[_outlinkto]),
    directedLink("OUT-*-NEIGHBORS", REPORTER, true,classOf[_outlinkneighbors]),
    directedLink("IN-*-NEIGHBORS", REPORTER, true,classOf[_inlinkneighbors]),
    directedLink("MY-IN-*", REPORTER, false,classOf[_myinlinks]),
    directedLink("MY-OUT-*", REPORTER, false,classOf[_myoutlinks]),
    undirectedLink("*-NEIGHBORS", REPORTER, true,classOf[_linkneighbors]),
    undirectedLink("MY-*", REPORTER, false,classOf[_mylinks]),
    undirectedLink("*-WITH", REPORTER, true,classOf[_linkwith]),
    undirectedLink("*-NEIGHBOR?", REPORTER, true,classOf[_linkneighbor])
  )
  class Helper
    (patternString:String,tokenType:TokenType,singular:Boolean,primClass:Class[_ <: Instruction],
     breeds:(Program)=>collection.Map[String,Breed],isValidBreed:(Breed)=>Boolean)
  {
    import java.util.regex.Pattern
    val pattern = Pattern.compile("\\A"+patternString.replaceAll("\\?","\\\\?").replaceAll("\\*","(.+)")+"\\Z")
    def process(tok:Token,program:Program):Option[Token] = {
      val matcher = pattern.matcher(tok.value.asInstanceOf[String])
      if(!matcher.matches()) return None
      val name = matcher.group(1)
      val breed =
        breeds(program).values
          .find{breed => name == (if (singular) breed.singular else breed.name)}
          .getOrElse(return None)
      if (!isValidBreed(breed)) return None
      val instr = Instantiator.newInstance[Instruction](primClass,breed.name)
      val tok2 = new Token(tok.value.asInstanceOf[String],tokenType,instr)(tok.startPos,tok.endPos,tok.fileName)
      instr.token(tok2)
      Some(tok2)
    }
  }
}
