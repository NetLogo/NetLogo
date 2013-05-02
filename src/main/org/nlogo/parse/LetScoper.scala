package org.nlogo.parse

import org.nlogo.api.{ Token, TokenType, Let }
import org.nlogo.prim._let
import Fail._

// Creates Let objects and stashes them in the `let` slot of the _let primitives.  The Let objects
// created have start and end slots that restrict the scope of the variable.  Some error checking is
// also performed along the way.  The Let objects created are also returned, so they can be used by
// IdentifierParser to connect _letvariable references to the right Lets.

class LetScoper(tokens: Iterable[Token]) {

  private val iter = new CountedIterator(tokens.iterator)
  private var result = Vector[Let]()

  def scan(usedNames: Map[String, String]): Vector[Let] = {
    recurse(usedNames)
    result
  }

  def recurse(usedNames: Map[String, String]) {

    var currentScope: List[_let] = Nil
    def namesInCurrentScope: Map[String, String] =
      currentScope.map(_.let.name -> "local variable here").toMap

    def beginLet(prim: _let) {
      val nameToken = iter.next()
      cAssert(nameToken.tpe == TokenType.IDENT,
        "Expected variable name here", nameToken)
      val name = nameToken.value.asInstanceOf[String]
      for (displayName <- (usedNames ++ namesInCurrentScope).get(name))
        exception("There is already a " + displayName + " called " + name, nameToken)
      // we may change end later if we see a closing bracket
      prim.let = Let(name, iter.count, tokens.size)
      currentScope +:= prim
    }

    def endLets(): List[Let] =
      for (prim <- currentScope)
      yield {
        prim.let = prim.let.copy(end = iter.count - 1)
        prim.let
      }

    while(iter.hasNext) {
      val token = iter.next()
      token.tpe match {
        case TokenType.OPEN_BRACKET =>
          recurse(namesInCurrentScope ++ usedNames)
        case TokenType.CLOSE_BRACKET =>
          result ++= endLets()
          return
        case TokenType.COMMAND =>
          token.value match {
            case prim: _let =>
              beginLet(prim)
            case _ =>
          }
        case _ =>
      }
    }

    // reached end of procedure body
    result ++= endLets()

  }

}
