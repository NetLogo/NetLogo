// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// no "BREED" keyword because it conflicts with BREED turtle variable -- CLB

object Keywords {
  val keywords = Set(
    "TO", "TO-REPORT", "END",
    "GLOBALS", "TURTLES-OWN", "LINKS-OWN", "PATCHES-OWN",
    "DIRECTED-LINK-BREED", "UNDIRECTED-LINK-BREED",
    "EXTENSIONS", "__INCLUDES")
  def isKeyword(s: String) =
    keywords.contains(s.toUpperCase) || s.toUpperCase.endsWith("-OWN")
}
