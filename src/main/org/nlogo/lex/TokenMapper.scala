// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.util.Utils
import org.nlogo.api._

object TokenMapper extends TokenMapperInterface {
  val Path = "/system/tokens.txt"
  def isCommand(s: String) = commands.contains(s.toUpperCase)
  def isReporter(s: String) = reporters.contains(s.toUpperCase)
  // caller's responsibility to validate input for these two
  def getCommand(s: String) = instantiate[TokenHolder](commands(s.toUpperCase))
  def getReporter(s: String) = instantiate[TokenHolder](reporters(s.toUpperCase))
  private def entries(entryType: String): Iterator[(String, String)] =
    for {
      line <- Utils.getResourceLines(Path)
      if !line.startsWith("#")
      Array(tpe, primName, className) = line.split(" ")
      if tpe == entryType
    } yield primName.toUpperCase -> ("org.nlogo.prim." + className)
  private val commands = entries("C").toMap
  private val reporters = entries("R").toMap
  /// private helper
  private def instantiate[T](name: String) =
    Class.forName(name).newInstance.asInstanceOf[T]
  // for integration testing
  def checkInstructionMaps() {
    commands.keySet.foreach(getCommand)
    reporters.keySet.foreach(getReporter)
  }
  def allCommandClassNames = commands.values.toSet
  def allReporterClassNames = reporters.values.toSet
}
