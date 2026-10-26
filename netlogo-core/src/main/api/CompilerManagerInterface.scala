// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait CompilerManagerInterface {
  def procedures: ProceduresInterface
  def compileAll(shouldAutoInstallLibs: Boolean = false): Unit
  def compileMoreSource(owner: JobOwner): Unit
  def loadEnd(shouldAutoInstallLibs: Boolean): Unit
}

class DummyCompilerManager extends CompilerManagerInterface {
  override def procedures: ProceduresInterface = null
  override def compileAll(shouldAutoInstallLibs: Boolean): Unit = {}
  override def compileMoreSource(owner: JobOwner): Unit = {}
  override def loadEnd(shouldAutoInstallLibs: Boolean): Unit = {}
}
