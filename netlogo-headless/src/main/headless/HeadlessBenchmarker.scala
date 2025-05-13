// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.io.File

import org.nlogo.api.Version
import org.nlogo.workspace.Benchmarker

object HeadlessBenchmarker {
  def main(argv: Array[String]): Unit = {
    System.setProperty("java.awt.headless", "true")

    val minTime = 60
    val maxTime = 300

    println("@@@@@@ benchmarking " + Version.fullVersion)
    println("@@@@@@ warmup " + minTime + " seconds, min " + minTime + " seconds, max " + maxTime + " seconds")

    (new File("models/test/benchmarks")).listFiles.foreach { file =>
      if (file.isFile && file.getName.endsWith(".nlogox")) {
        println("@@@@@@ running " + file.toString)

        val workspace = HeadlessWorkspace.newInstance

        workspace.silent = true

        try {
          workspace.open(file.toString)

          Benchmarker.benchmark(workspace, minTime, maxTime)
        } finally { workspace.dispose() }
      }
    }
  }
}
