// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.core.CompilerException
import org.nlogo.api.Version
import org.nlogo.workspace
import Main.setHeadlessProperty

object Shell extends workspace.Shell {

  def main(argv: Array[String]) {
    setHeadlessProperty()
    System.err.println(Version.fullVersion)
    val workspace = HeadlessWorkspace.newInstance
    argv.headOption match {
      case None =>
        workspace.openModel()
      case Some(path) =>
        workspace.open(path)
    }
    input.takeWhile(!isQuit(_))
      .foreach(run(workspace, _))
    workspace.dispose()
  }

  def run(workspace: HeadlessWorkspace, line: String) {
    val command =
      if (workspace.compilerServices.isReporter(line))
        "print " + line
      else
        line
    try workspace.command(command)
    catch {
      case ex: CompilerException =>
        println("COMPILER ERROR: " + ex.getMessage)
        ex.printStackTrace()
      case ex: org.nlogo.nvm.EngineException =>
        val msg = ex.runtimeErrorMessage
        println("RUNTIME ERROR: " + msg)
        ex.printStackTrace()
      case ex: org.nlogo.api.LogoException =>
        println("RUNTIME ERROR: " + ex.getMessage)
        ex.printStackTrace()
    }
  }

}
