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

  case class Identifier(name: String, token: Token)

  case class Export(name: String, exportSpecs: Seq[ExportSpec], token: Token)
      extends Declaration {

      override val start: Token = token
      override val end: Token = token
  }
  sealed trait ExportSpec
  case class SimpleExport(name: String)
      extends ExportSpec

  case class Import(packageName: Option[String], moduleName: String, options: Seq[ImportOption], token: Token)
      extends Declaration {

      override val start: Token = token
      override val end: Token = token
  }
  sealed trait ImportOption
  case class ImportAlias(name: String, token: Token)
      extends ImportOption
}
