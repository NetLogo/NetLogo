// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

enum ImportPathPrefix:
  case Up(count: Int)
  case PackageRoot

case class Import(
  filename: Option[String],
  pathComponents: Seq[String],
  pathPrefix: Option[ImportPathPrefix],
  pathAlias: Option[String],
  importedIdentifiers: Map[String, String],
  token: Token
)

case class Export(
  filename: Option[String],
  exportedNames: Seq[String],
  token: Token
)
