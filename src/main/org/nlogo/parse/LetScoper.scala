// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.api.{ Token, TokenType, Let }
import Fail._

// Finds uses of "let" and creates Let objects with start and end slots that restrict the scope of
// the variable.  Some error checking is also performed along the way.  The Let objects created are
// also returned, so they can be used by Namer to connect _letvariable references to the
// right Lets.

// (It's rather weird that this happens before ExpressionParser, so we have to resort to
// bracket-counting to determine the scopes.  Perhaps this should be moved to happen after
// ExpressionParser -- then we would have the actual tree structure to work with. - ST 5/3/13)

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
      cAssert(nameToken.tpe == TokenType.Ident,
        "Expected variable name here", nameToken)
      val name = nameToken.value.asInstanceOf[String]
      cAssert(!name.startsWith("?"),
        "Names beginning with ? are reserved for use as task inputs", nameToken)
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
        case TokenType.OpenBracket =>
          recurse(namesInCurrentScope ++ usedNames)
        case TokenType.CloseBracket =>
          result ++= endLets()
          return
        case TokenType.Ident =>
          if (List("LET", "__LET").contains(token.value))
            beginLet()
        case _ =>
      }
    }

    // reached end of procedure body
    result ++= endLets()

  }

}
