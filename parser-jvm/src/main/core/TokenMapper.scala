// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object DefaultTokenMapper extends
  TokenMapper("/system/tokens-core.txt", "org.nlogo.core.prim.")

class TokenMapper(path: String, prefix: String) extends TokenMapperInterface {
  def getCommand(s: String): Option[Command] =
    commands.get(s.toUpperCase).map(instantiate[Command])
  def getReporter(s: String): Option[Reporter] =
    reporters.get(s.toUpperCase).map(instantiate[Reporter])
  private def entries(entryType: String): Iterator[(String, String)] =
    for {
      line <- Resource.lines(path)
      if !line.startsWith("#")
      Array(tpe, primName, className) = line.split(" ")
      if tpe == entryType
    } yield primName.toUpperCase -> (prefix + className)
  private val commands = entries("C").toMap
  private val reporters = entries("R").toMap
  def allCommandNames = commands.keySet
  def allReporterNames = reporters.keySet
  /// private helper
  private def instantiate[T](name: String) =
    Class.forName(name).newInstance.asInstanceOf[T]
  // for integration testing
  def allCommandClassNames = commands.values.toSet
  def allReporterClassNames = reporters.values.toSet
  def checkInstructionMaps() {
    commands.keySet.foreach(getCommand)
    reporters.keySet.foreach(getReporter)
  }
}
