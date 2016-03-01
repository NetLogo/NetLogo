// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{BreedIdentifierHandler, Instruction, Program, Token, TokenType}

// go thru our breed prim handlers, if one triggers, return the result
class BreedHandler(program: Program) extends NameHandler {
  override def apply(token: Token) = {
    BreedIdentifierHandler.process(token, program) flatMap {
      case (className, breedName, tokenType) =>
        program.dialect.tokenMapper
          .breedInstruction(className, breedName)
          .map(instruction => (tokenType, instruction))
    }
  }
}
