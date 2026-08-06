// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import java.util.Locale

// no "BREED" keyword because it conflicts with BREED turtle variable -- CLB

object Keywords {
  val keywords = Set(
    "TO", "TO-REPORT", "END",
    "GLOBALS", "TURTLES-OWN", "LINKS-OWN", "PATCHES-OWN",
    "DIRECTED-LINK-BREED", "UNDIRECTED-LINK-BREED",
    "EXTENSIONS", "EXTENSION", "__INCLUDES")
  def isKeyword(s: String) =
    keywords.contains(s.toUpperCase(Locale.ENGLISH)) || s.toUpperCase(Locale.ENGLISH).endsWith("-OWN")
}
