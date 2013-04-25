// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api

// We use this in contexts where we want to do compiler stuff (not full compilation) like
// colorization but it's OK to assume that we are 2D not 3D and no extensions are loaded.  The
// HubNet client is one such context; also various testing contexts; also when reading
// BehaviorSpace XML. - ST 2/23/09, 3/4/09

class DefaultParserServices(parser: ParserInterface) extends api.ParserServices {
  def autoConvert(source: String, subprogram: Boolean, reporter: Boolean, modelVersion: String) =
    source
  def readNumberFromString(source: String) =
    parser.readNumberFromString(source, null, null)
  def checkReporterSyntax(source: String) =
    parser.checkReporterSyntax(source, api.Program.empty(),
      CompilerInterface.NoProcedures, null, false)
  def checkCommandSyntax(source: String) =
    parser.checkCommandSyntax(source, api.Program.empty(),
      CompilerInterface.NoProcedures, null, true)
  def readFromString(source: String) =
    parser.readFromString(source)
  def isConstant(s: String) =
    parser.isValidIdentifier(s)
  def isValidIdentifier(s: String) =
    parser.isValidIdentifier(s)
  def isReporter(s: String) =
    parser.isReporter(s, api.Program.empty(), CompilerInterface.NoProcedures,
      new org.nlogo.api.DummyExtensionManager)
  def tokenizeForColorization(source: String) =
    parser.tokenizeForColorization(
      source, new org.nlogo.api.DummyExtensionManager)
  def getTokenAtPosition(source: String, pos: Int) =
    parser.getTokenAtPosition(source, pos)
  def findProcedurePositions(source: String) =
    parser.findProcedurePositions(source)
}
