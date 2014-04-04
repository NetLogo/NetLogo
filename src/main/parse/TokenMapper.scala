// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.api.Resource
import org.nlogo.core.{ Syntax, Syntaxes }

object TokenMapper {
  def apply(s: String): Option[(String, Syntax)] =
    entries.get(s.toUpperCase)
      .map(s => (s, Syntaxes.syntaxes(s.dropWhile(_ != '_'))))
  val entries: Map[String, String] =
    (for {
      line <- Resource.getResourceLines("/system/tokens.txt")
      Array(primName, className) = line.split(" ")
    } yield primName.toUpperCase -> ("org.nlogo.prim." + className)).toMap
  def allNames = entries.keySet
  def allClassNames = entries.values.toSet
}
