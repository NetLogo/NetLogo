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
  case class ExtensionDeclaration(
    token: Token,    // This token is the "EXTENSION" keyword
    name: Identifier,
    url: Option[Identifier],
    // May be extended in the future to include more information
    ) extends Declaration {
        override def toString: String = {
            val urlPart = url match {
                case Some(u) => s"[url ${u.name}]"
                case None => ""
            }
            // e.g. extension [phys[url <url>]]
            s"extension [${name.name}$urlPart]"
        }
    } 
  case class Identifier(name: String, token: Token)
}
