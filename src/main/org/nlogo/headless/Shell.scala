// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.{ CompilerException, Version }
import java.io.{ BufferedReader, InputStreamReader }

object Shell {

  val input: Iterator[String] = {
    val reader = new BufferedReader(new InputStreamReader(System.in))
    Iterator.continually(reader.readLine())
      .takeWhile(_ != null)
  }

  def main(argv: Array[String]) {
    Main.setHeadlessProperty()
    val workspace = HeadlessWorkspace.newInstance
    if (argv.isEmpty)
      workspace.initForTesting(-5, 5, -5, 5, HeadlessWorkspace.TestDeclarations)
    else
      workspace.open(argv(0))
    System.err.println(Version.fullVersion)
    input.foreach(run(workspace, _))
    workspace.dispose()
  }

  def run(workspace: HeadlessWorkspace, line: String) {
    val command =
      if (workspace.isReporter(line))
        "print " + line
      else
        line
    try workspace.command(command)
    catch {
      case ex: CompilerException =>
        println("COMPILER ERROR: " + ex.getMessage)
        ex.printStackTrace()
      case ex: org.nlogo.nvm.EngineException =>
        val msg = ex.context.buildRuntimeErrorMessage(ex.instruction, ex)
        println("RUNTIME ERROR: " + msg)
        ex.printStackTrace()
      case ex: org.nlogo.api.LogoException =>
        println("RUNTIME ERROR: " + ex.getMessage)
        ex.printStackTrace()
    }
  }

}
