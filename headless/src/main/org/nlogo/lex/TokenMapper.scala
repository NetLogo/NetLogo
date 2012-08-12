// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.util.Utils
import org.nlogo.api._

// public for use from compiler.TestAllSyntaxes. yuck! - ST 1/21/09
object TokenMapper2D extends TokenMapper

class TokenMapper extends TokenMapperInterface {

  val paths = Seq("/system/tokens.txt")

  /// public stuff
  def isCommand(s: String) = commands.contains(s.toUpperCase)
  def isKeyword(s: String) = keywords.contains(s.toUpperCase) || s.toUpperCase.endsWith("-OWN")
  def isVariable(s: String) = variables.contains(s.toUpperCase)
  def isReporter(s: String) = reporters.contains(s.toUpperCase)
  def isConstant(s: String) = constants.contains(s.toUpperCase)
  // caller's responsibility to validate input for these three
  def getConstant(s: String) = constants.get(s.toUpperCase).get
  def getCommand(s: String) = instantiate[TokenHolder](commands(s.toUpperCase))
  def getReporter(s: String) = instantiate[TokenHolder](reporters(s.toUpperCase))
  private val variables = Set() ++
    AgentVariables.getImplicitObserverVariables ++
    AgentVariables.getImplicitTurtleVariables ++
    AgentVariables.getImplicitPatchVariables ++
    AgentVariables.getImplicitLinkVariables
  private val constants = Map(
    "FALSE" -> false, "TRUE" -> true, "NOBODY" -> Nobody,
    "E" -> StrictMath.E, "PI" -> StrictMath.PI) ++
    (for ((name, index) <- Color.getColorNamesArray.zipWithIndex) yield (name.toUpperCase -> Color.getColorNumberByIndex(index))) +
    ("GREY" -> Color.getColorNumberByIndex(Color.getColorNamesArray.indexOf("gray")))
  private val keywords = Set(
    "TO", "TO-REPORT", "END", "GLOBALS", "TURTLES-OWN", "LINKS-OWN",
    "PATCHES-OWN", "EXTENSIONS", "__INCLUDES", "DIRECTED-LINK-BREED",
    "UNDIRECTED-LINK-BREED") // no "BREED" here because it conflicts with BREED turtle variable -- CLB
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
