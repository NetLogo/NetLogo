// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.util.Utils
import org.nlogo.api._

object TokenMapper extends TokenMapperInterface {

  val paths = Seq("/system/tokens.txt")

  /// public stuff
  def isCommand(s: String) = commands.contains(s.toUpperCase)
  def isReporter(s: String) = reporters.contains(s.toUpperCase)
  // caller's responsibility to validate input for these two
  def getCommand(s: String) = instantiate[TokenHolder](commands(s.toUpperCase))
  def getReporter(s: String) = instantiate[TokenHolder](reporters(s.toUpperCase))
  private def entries(entryType: String, path: String) =
    for {
      line <- Utils.getResourceLines(path)
      if !line.startsWith("#")
      Array(tpe, primName, className) = line.split(" ")
      if tpe == entryType
    } yield primName.toUpperCase -> ("org.nlogo.prim." + className)
  private val commands =
    paths.foldLeft(Map[String, String]())(_ ++ entries("C", _))
  private val reporters =
    paths.foldLeft(Map[String, String]())(_ ++ entries("R", _))
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
