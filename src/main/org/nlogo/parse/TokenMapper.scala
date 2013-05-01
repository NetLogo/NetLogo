// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.util.Utils
import org.nlogo.api

object TokenMapper extends api.TokenMapperInterface {
  val Path = "/system/tokens.txt"
  def getCommand(s: String): Option[api.TokenHolder] =
    commands.get(s.toUpperCase).map(instantiate[api.TokenHolder])
  def getReporter(s: String): Option[api.TokenHolder] =
    reporters.get(s.toUpperCase).map(instantiate[api.TokenHolder])
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
  private[parse] def checkInstructionMaps() {
    commands.keySet.foreach(getCommand)
    reporters.keySet.foreach(getReporter)
  }
  def allCommandClassNames = commands.values.toSet
  def allReporterClassNames = reporters.values.toSet
}
