// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object DefaultTokenMapper extends
  TokenMapper("/system/tokens-core.txt", "org.nlogo.core.prim.")

class TokenMapper(location: String, prefix: String) extends TokenMapperInterface {
  def getCommand(s: String): Option[Command] =
    commands.get(s.toUpperCase).map(_())
  def getReporter(s: String): Option[Reporter] =
    reporters.get(s.toUpperCase).map(_())

  val reporters:Map[String, () => Reporter] =
    TokenClasses.compiledReporters[Reporter]("org.nlogo.core.prim")
  val commands:Map[String, () => Command] =
    TokenClasses.compiledCommands[Command]("org.nlogo.core.prim")

  def allCommandNames = commands.keySet
  def allReporterNames = reporters.keySet

  def allCommandClassNames = commands.keys.toSet
  def allReporterClassNames = reporters.keys.toSet

  val reverseProcedureMap = TokenClasses.reverseProcedureMap

  def procedureNamesFromPrimName(primName: String): Seq[String] =
    reverseProcedureMap.getOrElse(primName.stripPrefix(prefix), Seq[String]())

  def checkInstructionMaps() = {
    commands.keySet.foreach(getCommand)
    reporters.keySet.foreach(getReporter)
  }
}
