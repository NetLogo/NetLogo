// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.api.Resource
import org.nlogo.core.{ Syntax, Syntaxes }

class TokenMapper(path: String, prefix: String) {
  def apply(s: String): Option[(String, Syntax)] =
    commands.get(s.toUpperCase)
      .orElse(reporters.get(s.toUpperCase))
      .map(s => (s, Syntaxes.syntaxes(s.dropWhile(_ != '_'))))
  private def entries(entryType: String): Iterator[(String, String)] =
    for {
      line <- Resource.getResourceLines(path)
      if !line.startsWith("#")
      Array(tpe, primName, className) = line.split(" ")
      if tpe == entryType
    } yield primName.toUpperCase -> (prefix + className)
  private val commands = entries("C").toMap
  private val reporters = entries("R").toMap
  def allCommandNames = commands.keySet
  def allReporterNames = reporters.keySet
  // for integration testing
  def allCommandClassNames = commands.values.toSet
  def allReporterClassNames = reporters.values.toSet
}
