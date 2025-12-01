// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

case class Import(
  packageName: Option[String],
  moduleName: String,
  filename: Option[String],
  alias: Option[String],
  token: Token
)

case class Export(
  filename: Option[String],
  exportedNames: Seq[String],
  token: Token
)
