// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

// retains tokens so we can report error locations

object StructureDeclarations {
  sealed abstract trait Declaration {
    val range: Seq[Token]
  }

  case class Includes(token: Token, names: Seq[Token], range: Seq[Token])
    extends Declaration
  case class Extensions(token: Token, names: Seq[Identifier], range: Seq[Token])
    extends Declaration
  case class Breed(token: Token, plural: Identifier, singular: Identifier, isLinkBreed: Boolean = false,
                   isDirected: Boolean = false, range: Seq[Token]) extends Declaration
  case class Variables(kind: Identifier, names: Seq[Identifier], range: Seq[Token])
    extends Declaration
  case class Procedure(name: Identifier, isReporter: Boolean, inputs: Seq[Identifier], tokens: Seq[Token],
                       range: Seq[Token]) extends Declaration
  case class Identifier(name: String, token: Token)
}
