// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.api.CompilerServices
import org.nlogo.core.{ CompilerException, ProcedureSyntax, Token }

// This trait implements all the api.CompilerServices methods *except* readFromString, which
// is in the Evaluating trait - RG 5/23/2017
trait Compiling extends CompilerServices { this: AbstractWorkspace =>
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
    compiler.readNumberFromString(source, _world, getExtensionManager)

  @throws(classOf[CompilerException])
  def checkReporterSyntax(source: String): Unit = {
    compiler.checkReporterSyntax(source, _world.program, procedures, getExtensionManager, false, getCompilationEnvironment)
  }

  @throws(classOf[CompilerException])
  def checkCommandSyntax(source: String): Unit = {
    compiler.checkCommandSyntax(source, _world.program, procedures, getExtensionManager, false, getCompilationEnvironment)
  }

  def isReporter(s: String): Boolean =
    compiler.isReporter(s, _world.program, procedures, getExtensionManager, getCompilationEnvironment);

  def isValidIdentifier(s: String): Boolean =
    compiler.isValidIdentifier(s)

  def tokenizeForColorization(s: String): Array[Token] =
    compiler.tokenizeForColorization(s, getExtensionManager)

  def tokenizeForColorizationIterator(s: String): Iterator[Token] =
    compiler.tokenizeForColorizationIterator(s, getExtensionManager)

  def getTokenAtPosition(s: String, pos: Int): Token =
    compiler.getTokenAtPosition(s, pos)

  def findProcedurePositions(source: String): Map[String, ProcedureSyntax] =
    compiler.findProcedurePositions(source)
}
