// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.Dialect
import org.nlogo.api.{ CompilerServices, EditorCompiler }
import org.nlogo.core.Program
import scala.collection.immutable.ListMap

// We use this in contexts where we want to do compiler stuff (not full compilation) like
// colorization but it's OK to assume that we are 2D not 3D and no extensions are loaded.  The
// HubNet client is one such context; also various testing contexts; also when reading
// BehaviorSpace XML. - ST 2/23/09, 3/4/09

class DefaultCompilerServices(compiler: EditorCompiler) extends CompilerServices {
  def dialect: Dialect = compiler.defaultDialect
  def emptyProgram = Program.fromDialect(compiler.defaultDialect)
  def autoConvert(modelVersion: String)(source: String) = source
  def readNumberFromString(source: String) =
    compiler.readNumberFromString(source, null, null)
  def checkReporterSyntax(source: String) =
    compiler.checkReporterSyntax(source, emptyProgram,
                                 Procedure.NoProcedures, null, false)
  def checkCommandSyntax(source: String) =
    compiler.checkCommandSyntax(source, emptyProgram,
                                Procedure.NoProcedures, null, true)
  def readFromString(source: String) =
    compiler.readFromString(source)
  def isConstant(s: String) =
    compiler.isValidIdentifier(s)
  def isValidIdentifier(s: String) =
    compiler.isValidIdentifier(s)
  def isReporter(s: String) =
    compiler.isReporter(s, emptyProgram, new ListMap[String, Procedure],
                        new org.nlogo.api.DummyExtensionManager)
  def tokenizeForColorization(source: String) =
    compiler.tokenizeForColorization(
      source, new org.nlogo.api.DummyExtensionManager)
  def tokenizeForColorizationIterator(source: String) =
    compiler.tokenizeForColorizationIterator(
      source, new org.nlogo.api.DummyExtensionManager)
  def getTokenAtPosition(source: String, pos: Int) =
    compiler.getTokenAtPosition(source, pos)
  def findProcedurePositions(source: String) =
    compiler.findProcedurePositions(source)
}
