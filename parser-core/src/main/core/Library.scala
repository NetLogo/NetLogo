// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object Library {
  sealed trait LibraryOption
  case class LibraryAlias(name: String) extends LibraryOption
}

case class Library(name: String, options: Seq[Library.LibraryOption])
