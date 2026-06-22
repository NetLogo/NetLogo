// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

// retains tokens so we can report error locations

object StructureDeclarations {
  sealed trait Declaration {
    val start: Token
    val end: Token
  }

  case class Includes(start: Token, names: Seq[Token], end: Token)
    extends Declaration

  case class Extensions(start: Token, names: Seq[Identifier], end: Token)
    extends Declaration

  case class Breed(plural: Identifier, singular: Identifier, isLinkBreed: Boolean, isDirected: Boolean, start: Token,
                   end: Token) extends Declaration

  case class Variables(kind: Identifier, names: Seq[Identifier], start: Token, end: Token)
    extends Declaration

  case class Procedure(name: Identifier, isReporter: Boolean, inputs: Seq[Identifier], tokens: Seq[Token])
    extends Declaration {

    override val start: Token = tokens.head
    override val end: Token = tokens.last
  }

  case class ExtensionDeclaration(
    start: Token,    // This token is the "EXTENSION" keyword
    name: Identifier,
    url: Option[Identifier],
    end: Token
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
