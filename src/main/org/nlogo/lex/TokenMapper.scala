// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.util.Utils
import org.nlogo.api._

object TokenMapper extends TokenMapperInterface {
  val Path = "/system/tokens.txt"
  def getCommand(s: String): Option[TokenHolder] =
    commands.get(s.toUpperCase).map(instantiate[TokenHolder])
  def getReporter(s: String): Option[TokenHolder] =
    reporters.get(s.toUpperCase).map(instantiate[TokenHolder])
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
