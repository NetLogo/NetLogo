// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.io.File

import org.nlogo.window.GUIWorkspace
import org.nlogo.workspace.Benchmarker

object GUIBenchmarker {
  def main(args: Array[String]): Unit = {
    val app: App = App.createInstance(Array())
    val workspace: GUIWorkspace = app.workspace

    (new File("models/test/benchmarks")).listFiles.foreach { file =>
      if (file.isFile && file.getName.endsWith(".nlogox")) {
        println("@@@@@@ running " + file.toString)

        workspace.open(file.toString)

        Benchmarker.benchmark(workspace, 60, 300)
      }
    }
  }
}
