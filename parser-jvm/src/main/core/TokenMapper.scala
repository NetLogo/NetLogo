// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object DefaultTokenMapper extends
  TokenMapper("/system/tokens-core.txt", "org.nlogo.core.prim.")

class TokenMapper(path: String, prefix: String) extends TokenMapperInterface {
  def getCommand(s: String): Option[Command] =
    commands.get(s.toUpperCase).map(c => TokenMapping2.command(c))
  def getReporter(s: String): Option[Reporter] =
    reporters.get(s.toUpperCase).map(r => TokenMapping2.reporter(r))
  def breedInstruction(primName: String, breedName: String): Option[Instruction] =
    TokenMapping2.breeded(primName, breedName)

  private def entries(entryType: String): Iterator[(String, String)] =
    for {
      line <- Resource.lines(path)
      if !line.startsWith("#")
      split = line.split(" ")
      tpe = split(0)
      primName = split(1)
      className = split(2)
      if tpe == entryType
    } yield primName.toUpperCase -> (prefix + className)
  private val commands = entries("C").toMap
  private val reporters = entries("R").toMap
  def allCommandNames = commands.keySet
  def allReporterNames = reporters.keySet
  // for integration testing
  def allCommandClassNames = commands.values.toSet
  def allReporterClassNames = reporters.values.toSet
  def checkInstructionMaps(): Unit = {
    commands.keySet.foreach(getCommand)
    reporters.keySet.foreach(getReporter)
  }
}
