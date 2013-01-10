package org.nlogo.compiler

import Fail.{ cAssert, exception }
import org.nlogo.{ api, nvm }
import api.{ Token, TokenType, Let }
import nvm.Procedure
import org.nlogo.prim._let

// Creates Let objects and stores them in the `lets` slot of the Procedure object as well as the
// `let` slot of the _let primitives.  The Let objects created have start and end slots that
// restrict the scope of the variable.  Some error checking is also performed along the way.

class LetScoper(procedure: Procedure, tokens: Iterable[Token], usedNames: Map[String, String]) {

  private val iter = new CountedIterator(tokens.iterator)

  def scan(level: Int = 0, ancestors: List[List[_let]] = List(Nil)) {

    var newLets: List[_let] = Nil

    def beginLet(prim: _let) {
      val nameToken = iter.next()
      cAssert(nameToken.tpe == TokenType.IDENT,
        "Expected variable name here", nameToken)
      val name = nameToken.value.asInstanceOf[String]
      for (displayName <- usedNames.get(name))
        exception("There is already a " + displayName + " called " + name, nameToken)
      cAssert(!procedure.args.contains(name),
        "There is already a local variable called " + name + " here", nameToken)
      val start = iter.count
      cAssert(!(ancestors.flatten ++ newLets).exists(_.let.name == name),
        "There is already a local variable called " + name + " here", nameToken)
      // we may change end later if we see a closing bracket
      prim.let = Let(name, start, tokens.size)
      newLets +:= prim
    }

    def endLets(prims: List[_let]) {
      for(prim <- prims) {
        prim.let = prim.let.copy(end = iter.count - 1)
        procedure.lets :+= prim.let
      }
    }

    while(iter.hasNext) {
      val token = iter.next()
      token.tpe match {
        case TokenType.OPEN_BRACKET =>
          scan(level + 1, newLets :: ancestors)
        case TokenType.CLOSE_BRACKET =>
          endLets(newLets)
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

    endLets(newLets)

  }

}
