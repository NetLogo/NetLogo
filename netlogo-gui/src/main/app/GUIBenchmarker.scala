// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.io.File

import org.nlogo.workspace.Benchmarker

object GUIBenchmarker {
  def main(args: Array[String]): Unit = {
    App.main(Array())

    val workspace = App.app.workspace

    (new File("models/test/benchmarks")).listFiles.foreach { file =>
      if (file.isFile && file.getName.endsWith(".nlogox")) {
        println("@@@@@@ running " + file.toString)

        workspace.open(file.toString)

        Benchmarker.benchmark(workspace, 60, 300)
      }
    }
  }
}
