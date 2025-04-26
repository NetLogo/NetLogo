// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.scalatest.funsuite.AnyFunSuite

import org.nlogo.api.Version
import org.nlogo.util.SlowTest
import org.nlogo.headless.ChecksumsAndPreviewsSettings.DumpsPath

class TestCompileBenchmarks extends AnyFunSuite with SlowTest {

  private val names = Seq(
    "ANN", "Ants", "Bureaucrats", "BZ", "CA1D", "Erosion", "Fire", "FireBig", "Flocking", "GasLabCirc",
    "GasLabNew", "GasLabOld", "GridWalk", "Heatbugs", "Ising", "Life", "PrefAttach",
    "Team", "Termites", "VirusNet", "Wealth", "Wolf", "ImportWorld")

  // this is here to help us remember that when the list of benchmarks changes, this file and the
  // contents of test/benchdumps both need updating too - ST 2/11/09
  test("names", SlowTest.Tag) {
    assert(names.mkString("\n") === ChecksumsAndPreviews.allBenchmarks.mkString("\n"))
  }

  if(Version.useGenerator && !Version.is3D) {
    for(name <- names)
      test(name, SlowTest.Tag) {
        val dump = {
          val workspace = HeadlessWorkspace.newInstance
          workspace.open("models/test/benchmarks/" + name + " Benchmark.nlogox")
          val result = workspace.report("__dump")
          workspace.dispose()
          result
        }
        val source =
          io.Source.fromFile(DumpsPath + name + ".txt")
        assert(dump === source.getLines().mkString("","\n","\n"))
      }
  }
}
