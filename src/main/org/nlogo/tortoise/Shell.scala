// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import java.io.{ BufferedReader, InputStreamReader }
import
  org.nlogo.{ api, headless },
  headless.Shell.{ input, isQuit },
  headless.Main.setHeadlessProperty

object Shell {

  val rhino = new Rhino

  def main(argv: Array[String]) {
    setHeadlessProperty()
    val (js, _, _) = Compiler.compileProcedures("", api.WorldDimensions.square(16))
    rhino.eval(js)
    System.err.println("Tortoise Shell 1.0")
    input.takeWhile(!isQuit(_))
      .foreach(run)
  }

  def run(s: String) {
    try {
      val (output, json) =
        rhino.run(
          Compiler.compileCommands(s))
      Seq(output) // , json)
        .filter(_.nonEmpty)
        .foreach(x => println(x.trim))
    }
    catch { case e: Exception => println(e) }
  }

}
