// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.Version
import org.nlogo.core.{ CompilerException, Model }
import java.io.{ BufferedReader, InputStreamReader }

object Shell {

  val input: Iterator[String] = {
    val reader = new BufferedReader(new InputStreamReader(System.in))
    Iterator.continually(reader.readLine())
      .takeWhile(_ != null)
  }

  def main(argv: Array[String]) {
    Main.setHeadlessProperty()
    System.err.println(Version.fullVersion)
    // NOTE: While generally we shouldn't rely on a system property to tell
    // us whether or not we're in 3D, it's fine to do it here because:
    // * We're in the process of constructing the App / World / Workspace
    // * We do not change the 2D / 3D arity for the duration of the run
    val workspace = HeadlessWorkspace.newInstance(Version.is3DInternal)
    argv.headOption match {
      case None =>
        workspace.openModel(Model())
      case Some(path) =>
        workspace.open(path)
    }
    input.foreach(run(workspace, _))
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
