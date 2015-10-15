// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.{ CompilationEnvironment, DummyCompilationEnvironment }
import org.nlogo.api.{ CompilerServices}
import org.nlogo.core.Program

// We use this in contexts where we want to do compiler stuff (not full compilation) like
// colorization but it's OK to assume that we are 2D not 3D and no extensions are loaded.  The
// HubNet client is one such context; also various testing contexts; also when reading
// BehaviorSpace XML. - ST 2/23/09, 3/4/09

class DefaultCompilerServices(compiler: CompilerInterface) extends CompilerServices {
  def autoConvert(source: String, subprogram: Boolean, reporter: Boolean, modelVersion: String) = source
  def readNumberFromString(source: String) =
    compiler.readNumberFromString(source, null, null, false)
  def checkReporterSyntax(source: String) =
    compiler.checkReporterSyntax(source, Program.empty(),
                                 new java.util.HashMap[String,Procedure],
                                 null, false, new DummyCompilationEnvironment())
  def checkCommandSyntax(source: String) =
    compiler.checkCommandSyntax(source, Program.empty(),
                                new java.util.HashMap[String,Procedure],
                                null, true, new DummyCompilationEnvironment())
  def readFromString(source: String) =
    compiler.readFromString(source, false)
  def isConstant(s: String) =
    compiler.isValidIdentifier(s, false)
  def isValidIdentifier(s: String) =
    compiler.isValidIdentifier(s, false)
  def isReporter(s: String) =
    compiler.isReporter(s, Program.empty(), new java.util.HashMap[String, Procedure],
                        new org.nlogo.api.DummyExtensionManager, new DummyCompilationEnvironment())
  def tokenizeForColorization(source: String) =
    compiler.tokenizeForColorization(
      source, new org.nlogo.api.DummyExtensionManager, false)
  def getTokenAtPosition(source: String, pos: Int) =
    compiler.getTokenAtPosition(source, pos)
  def findProcedurePositions(source: String) =
    compiler.findProcedurePositions(source, false)
}
