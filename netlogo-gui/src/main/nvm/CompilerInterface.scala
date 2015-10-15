// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.CompilationEnvironment
import org.nlogo.api.{ World}
import org.nlogo.core.Program
import org.nlogo.core.CompilerException
import org.nlogo.core.Token
import org.nlogo.core.{ ExtensionManager => CoreExtensionManager }

// ought to be in the api package, except oops, it depends on nvm.Procedure - ST 2/23/09

trait CompilerInterface {

  @throws(classOf[CompilerException])
  def compileProgram(source: String, program: Program, extensionManager: CoreExtensionManager, compilationEnv: CompilationEnvironment): CompilerResults

  @throws(classOf[CompilerException])
  def compileMoreCode(source: String, displayName: Option[String], program: Program, oldProcedures: java.util.Map[String, Procedure],
                      extensionManager: CoreExtensionManager, compilationEnv: CompilationEnvironment): CompilerResults

  @throws(classOf[CompilerException])
  def checkCommandSyntax(source: String, program: Program, procedures: java.util.Map[String, Procedure],
                         extensionManager: CoreExtensionManager, parse: Boolean, compilationEnv: CompilationEnvironment)

  @throws(classOf[CompilerException])
  def checkReporterSyntax(source: String, program: Program, procedures: java.util.Map[String, Procedure],
                          extensionManager: CoreExtensionManager, parse: Boolean, compilationEnv: CompilationEnvironment)

  def autoConvert(source: String, subprogram: Boolean, reporter: Boolean, version: String,
                  workspace: AnyRef, ignoreErrors: Boolean, is3D: Boolean): String

  @throws(classOf[CompilerException])
  def readFromString(source: String, is3D: Boolean): AnyRef

  @throws(classOf[CompilerException])
  def readFromString(source: String, world: World, extensionManager: CoreExtensionManager, is3D: Boolean): AnyRef

  @throws(classOf[CompilerException])
  def readNumberFromString(source: String, world: World, extensionManager: CoreExtensionManager, is3D: Boolean): AnyRef

  @throws(classOf[CompilerException])
  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: org.nlogo.core.File, world: World, extensionManager: CoreExtensionManager): AnyRef

  def findProcedurePositions(source: String, is3D: Boolean): java.util.Map[String, java.util.List[AnyRef]]
  def findIncludes(sourceFileName: String, source: String, is3D: Boolean): Option[java.util.Map[String, String]]
  def isValidIdentifier(s: String, is3D: Boolean): Boolean
  def isReporter(s: String, program: Program, procedures: java.util.Map[String, Procedure], extensionManager: CoreExtensionManager, compilationEnv: CompilationEnvironment): Boolean
  def getTokenAtPosition(source: String, position: Int): Token
  def tokenizeForColorization(source: String, extensionManager: CoreExtensionManager, is3D: Boolean): Array[Token]
}
