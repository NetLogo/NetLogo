// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.util.Utils
import org.nlogo.api.{ Color, TokenMapperInterface, AgentVariables }
import org.nlogo.core.Nobody
import org.nlogo.core.Token
import org.nlogo.core.TokenHolder

// public for use from compiler.TestAllSyntaxes. yuck! - ST 1/21/09
object TokenMapper2D extends TokenMapper(false)
object TokenMapper3D extends TokenMapper(true)

class TokenMapper(is3D: Boolean) extends TokenMapperInterface {
  /// public stuff
  def wasRemoved(s: String) = removeds.contains(s.toUpperCase)
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
    AgentVariables.getImplicitTurtleVariables(is3D) ++
    AgentVariables.getImplicitPatchVariables(is3D) ++
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
  private def entries(entryType: String) =
    for {
      line <- Utils.getResourceLines("/system/tokens.txt")
      if !line.startsWith("#")
      Array(tpe, primName, className) = line.split(" ")
      if tpe == entryType
      // if a 3d version of the prim exists and we got to this point it
      // should override the 2d version. ev 12/11/06  note the overriding
      // 3d version must come after the 2d version in tokens.txt - ST 12/19/08
      if is3D || !className.startsWith("threed.")
    } yield primName.toUpperCase -> ("org.nlogo.prim." + className)
  private val commands = Map() ++ entries("C")
  private val reporters = Map() ++ entries("R")
  private val removeds =
    (for (
      (primName, className) <- commands ++ reporters if className.startsWith("org.nlogo.prim.dead.")
    ) yield primName).toSet
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
