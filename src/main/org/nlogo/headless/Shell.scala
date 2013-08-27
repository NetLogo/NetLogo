// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.{ CompilerException, ModelReader, Version }
import java.io.{ BufferedReader, InputStreamReader }
import org.nlogo.util.Utils.url2String

object Shell {

  val input: Iterator[String] = {
    val reader = new BufferedReader(new InputStreamReader(System.in))
    Iterator.continually(reader.readLine())
      .takeWhile(_ != null)
  }

  def isQuit(s: String) =
    List(":QUIT", ":EXIT").contains(s.trim.toUpperCase)

  def main(argv: Array[String]) {
    Main.setHeadlessProperty()
    System.err.println(Version.fullVersion)
    val workspace = HeadlessWorkspace.newInstance
    argv.headOption match {
      case None =>
        workspace.openString(url2String(ModelReader.emptyModelPath))
      case Some(path) =>
        workspace.open(path)
    }
    input.takeWhile(!isQuit(_))
      .foreach(run(workspace, _))
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
