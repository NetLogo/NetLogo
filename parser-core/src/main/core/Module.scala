// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

case class Import(
  name: String,
  filename: Option[String],
  alias: Option[String],
  token: Token
)

case class DefineLibrary(
  name: String,
  filename: Option[String],
  version: String,
  exportedNames: Seq[String],
  token: Token
)
