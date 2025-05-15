// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

case class Library(
  name: String,
  alias: Option[String],
  token: Token
)
