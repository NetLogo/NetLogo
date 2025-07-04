// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import java.util.Locale

import org.nlogo.core.{ Command, Instruction, Reporter, TokenMapperInterface }

class TokenMapper extends TokenMapperInterface {
  private val commands: Map[String, String] = TokenMapping.commandClassNames
  private val reporters: Map[String, String] = TokenMapping.reporterClassNames

  def allCommandNames: Set[String] = commands.keySet
  def allReporterNames: Set[String] = reporters.keySet

  def allCommandClassNames: Set[String] = commands.values.toSet
  def allReporterClassNames: Set[String] = reporters.values.toSet

  def getCommand(s: String): Option[Command] =
    TokenMapping.commandPrimToInstance(s.toUpperCase(Locale.ENGLISH))
  def getReporter(s: String): Option[Reporter] =
    TokenMapping.reporterPrimToInstance(s.toUpperCase(Locale.ENGLISH))
  def breedInstruction(primName: String, breedName: String): Option[Instruction] =
    TokenMapping.breeded(breedName)(primName)

  def checkInstructionMaps(): Unit = {
    commands.keySet.foreach(getCommand)
    reporters.keySet.foreach(getReporter)
  }
}
