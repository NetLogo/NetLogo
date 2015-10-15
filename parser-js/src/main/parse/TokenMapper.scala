// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.TokenHolder

class TokenMapper(location: String, prefix: String) {
  def getCommand(s: String): Option[TokenHolder] =
    commands.get(s.toUpperCase).map(_())
  def getReporter(s: String): Option[TokenHolder] =
    reporters.get(s.toUpperCase).map(_())

  val reporters:Map[String, () => TokenHolder] =
    TokenClasses.compiledReporters[TokenHolder]("org.nlogo.core.prim")
  val commands:Map[String, () => TokenHolder] =
    TokenClasses.compiledCommands[TokenHolder]("org.nlogo.core.prim")

  def allCommandNames = commands.keySet
  def allReporterNames = reporters.keySet

  def allCommandClassNames = commands.values.toSet
  def allReporterClassNames = reporters.values.toSet

  val reverseProcedureMap = TokenClasses.reverseProcedureMap

  def procedureNamesFromPrimName(primName: String): Seq[String] =
    reverseProcedureMap.getOrElse(primName.stripPrefix(prefix), Seq[String]())

  def checkInstructionMaps() = {
    commands.keySet.foreach(getCommand)
    reporters.keySet.foreach(getReporter)
  }
}
