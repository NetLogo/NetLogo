// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// for unit testing of compiler front end stuff, we don't want to have a runtime dependency on the
// prim classes, so we use a mock mapper that doesn't instantiate any prims, but just puts the prim
// names in the Token.value slot - ST 4/30/13

object DummyTokenMapper extends TokenMapperInterface {
  case class Holder(name: String) extends TokenHolder {
    override def token(t: Token) { }
    override def toString = name
  }
  // these are just grab bags of primitives that happen to have been
  // mentioned in tests written over the years - ST 5/1/13
  override def getCommand(s: String) =
    PartialFunction.condOpt(s){
      case "__IGNORE" => "_ignore"
      case "ASK" => "_ask"
      case "CRT" => "_createturtles"
      case "SET" => "_set"
      case "LET" => "org.nlogo.prim._let"
    }.map(Holder)
  override def getReporter(s: String) =
    PartialFunction.condOpt(s){
      case "ROUND" => "_round"
      case "TURTLE" => "_turtle"
    }.map(Holder)
}
