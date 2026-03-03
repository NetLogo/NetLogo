// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

case class Import(
  filename: Option[String],
  pathComponents: Seq[String],
  pathAlias: Option[String],
  importedIdentifiers: Map[String, String],
  token: Token
)

case class Export(
  filename: Option[String],
  exportedNames: Seq[String],
  token: Token
)
