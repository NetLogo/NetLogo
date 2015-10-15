// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{BreedIdentifierHandler, Instantiator, Program, Token, Instruction}

// go thru our breed prim handlers, if one triggers, return the result
class BreedHandler(program: Program) extends NameHandler {
  override def apply(token: Token) =
    BreedIdentifierHandler.process(token, program) map {
      case (className, breedName, tokenType) =>
        (tokenType, Instantiator.newInstance[Instruction](
          Class.forName(s"org.nlogo.core.prim.$className"), breedName))
    }
}

