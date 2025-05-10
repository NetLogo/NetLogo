// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object DefaultTokenMapper extends
  TokenMapper("/system/tokens-core.txt", "org.nlogo.core.prim.")

class TokenMapper(location: String, prefix: String) extends TokenMapperInterface {
  def getCommand(s: String): Option[Command] =
    commands.get(s.toUpperCase).map(c => TokenMapping2.command(c))
  def getReporter(s: String): Option[Reporter] =
    reporters.get(s.toUpperCase).map(r => TokenMapping2.reporter(r))
  def breedInstruction(primName: String, breedName: String): Option[Instruction] =
    TokenMapping2.breeded(primName, breedName)

  val reporters:Map[String, String] = TokenMapping1.reporters
  val commands:Map[String, String] = TokenMapping1.commands

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
