// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.core.{ CompilerException, ProcedureSyntax, Token }
import org.nlogo.api.CompilerServices
import org.nlogo.agent.World
import org.nlogo.nvm.{ PresentationCompilerInterface, Procedure }

import scala.collection.immutable.ListMap

class LiveCompilerServices(
  val compiler: PresentationCompilerInterface,
  val extensionManager: ExtensionManager,
  val world: World,
  val evaluator: Evaluator) extends CompilerServices {

  private var _procedures: Procedure.ProceduresMap = Procedure.NoProcedures

  def procedures: ListMap[String, Procedure] = _procedures

  def procedures_=(procs: Procedure.ProceduresMap): Unit = {
    _procedures = procs
  }

  def setProcedures(procs: Procedure.ProceduresMap): Unit = {
    _procedures = procs
  }

  def dialect = world.program.dialect

  def isConstant(s: String): Boolean = {
    try {
      compiler.readFromString(s)
      true
    } catch {
      case e: CompilerException => false
    }
  }

  @throws(classOf[CompilerException])
  def readNumberFromString(source: String): java.lang.Double =
    compiler.readNumberFromString(source, world, extensionManager)

  @throws(classOf[CompilerException])
  def checkReporterSyntax(source: String): Unit = {
    compiler.checkReporterSyntax(source, world.program, procedures, extensionManager, false)
  }

  @throws(classOf[CompilerException])
  def checkCommandSyntax(source: String): Unit = {
    compiler.checkCommandSyntax(source, world.program, procedures, extensionManager, false)
  }

  def isReporter(s: String): Boolean =
    compiler.isReporter(s, world.program, procedures, extensionManager)

  def isValidIdentifier(s: String): Boolean =
    compiler.isValidIdentifier(s)

  def tokenizeForColorization(s: String): Array[Token] =
    compiler.tokenizeForColorization(s, extensionManager)

  def tokenizeForColorizationIterator(s: String): Iterator[Token] =
    compiler.tokenizeForColorizationIterator(s, extensionManager)

  def getTokenAtPosition(s: String, pos: Int): Token =
    compiler.getTokenAtPosition(s, pos)

  def findProcedurePositions(source: String): Map[String, ProcedureSyntax] =
    compiler.findProcedurePositions(source)

  @throws(classOf[CompilerException])
  def readFromString(string: String): AnyRef =
    evaluator.readFromString(string, extensionManager)
}
