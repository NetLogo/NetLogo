package org.nlogo.parse0

import org.nlogo.api.{ Token, TokenType, Let }
import Fail._

// Finds uses of "let" and creates Let objects with start and end slots that restrict the scope of
// the variable.  Some error checking is also performed along the way.  The Let objects created are
// also returned, so they can be used by IdentifierParser to connect _letvariable references to the
// right Lets.

class LetScoper(tokens: Iterable[Token]) {

  private val iter = new CountedIterator(tokens.iterator)
  private var result = Vector[Let]()

  def scan(usedNames: Map[String, String]): Vector[Let] = {
    recurse(usedNames)
    result
  }

  def recurse(usedNames: Map[String, String]) {

    var currentScope: List[Let] = Nil
    def namesInCurrentScope: Map[String, String] =
      currentScope.map(_.name -> "local variable here").toMap

    def beginLet() {
      val nameToken = iter.next()
      cAssert(nameToken.tpe == TokenType.IDENT,
        "Expected variable name here", nameToken)
      val name = nameToken.value.asInstanceOf[String]
      for (displayName <- (usedNames ++ namesInCurrentScope).get(name))
        exception("There is already a " + displayName + " called " + name, nameToken)
      // we may change end later if we see a closing bracket
      currentScope +:= Let(name, iter.count, tokens.size)
    }

    def endLets(): List[Let] =
      for (let <- currentScope)
      yield let.copy(end = iter.count - 1)

    while(iter.hasNext) {
      val token = iter.next()
      token.tpe match {
        case TokenType.OPEN_BRACKET =>
          recurse(namesInCurrentScope ++ usedNames)
        case TokenType.CLOSE_BRACKET =>
          result ++= endLets()
          return
        case TokenType.COMMAND =>
          if (List("LET", "__LET").contains(token.name.toUpperCase))
            beginLet()
        case _ =>
      }
    }

    // reached end of procedure body
    result ++= endLets()

  }

}
