// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

// retains tokens so we can report error locations

object StructureDeclarations {
  sealed trait Declaration
  case class Includes(token: Token, names: Seq[Token])
      extends Declaration
  case class Extensions(token: Token, names: Seq[Identifier])
      extends Declaration
  case class Breed(plural: Identifier, singular: Identifier, isLinkBreed: Boolean = false, isDirected: Boolean = false)
      extends Declaration
  case class Variables(kind: Identifier, names: Seq[Identifier])
      extends Declaration
  case class Procedure(name: Identifier, isReporter: Boolean, inputs: Seq[Identifier], tokens: Seq[Token])
      extends Declaration
  case class Identifier(name: String, token: Token)
}
