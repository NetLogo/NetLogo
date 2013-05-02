package org.nlogo.parse

import org.nlogo.api.{ Token, TokenType, Let }
import org.nlogo.prim._let
import Fail._

// Creates Let objects and stashes them in the `let` slot of the _let primitives.  The Let objects
// created have start and end slots that restrict the scope of the variable.  Some error checking is
// also performed along the way.  The Let objects created are also returned, so they can be stashed
// in the Procedure object.

class LetScoper(tokens: Iterable[Token], usedNames: Map[String, String]) {

  private val iter = new CountedIterator(tokens.iterator)
  private var result = Vector[Let]()

  def scan(): Vector[Let] = {
    recurse(List(Nil))
    result
  }

  def recurse(enclosingScopes: List[List[_let]]) {

    var currentScope: List[_let] = Nil

    def beginLet(prim: _let) {
      val nameToken = iter.next()
      cAssert(nameToken.tpe == TokenType.IDENT,
        "Expected variable name here", nameToken)
      val name = nameToken.value.asInstanceOf[String]
      for (displayName <- usedNames.get(name))
        exception("There is already a " + displayName + " called " + name, nameToken)
      val start = iter.count
      cAssert(!(enclosingScopes.flatten ++ currentScope).exists(_.let.name == name),
        "There is already a local variable called " + name + " here", nameToken)
      // we may change end later if we see a closing bracket
      prim.let = Let(name, start, tokens.size)
      currentScope +:= prim
    }

    def endLets(prims: List[_let]) =
      for (prim <- prims)
      yield {
        prim.let = prim.let.copy(end = iter.count - 1)
        prim.let
      }

    while(iter.hasNext) {
      val token = iter.next()
      token.tpe match {
        case TokenType.OPEN_BRACKET =>
          recurse(currentScope :: enclosingScopes)
        case TokenType.CLOSE_BRACKET =>
          result ++= endLets(currentScope)
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
    result ++= endLets(currentScope)

  }

}
