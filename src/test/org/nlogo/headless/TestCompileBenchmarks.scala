// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.Version
import org.scalatest.FunSuite
import org.nlogo.util.SlowTest

class TestCompileBenchmarks extends FunSuite with SlowTest{

  private val names = Seq(
    "Ants", "Bureaucrats", "BZ", "CA1D", "Erosion", "Fire", "FireBig", "Flocking", "GasLabCirc",
    "GasLabNew", "GasLabOld", "GridWalk", "Heatbugs", "Ising", "Life", "PrefAttach",
    "Team", "Termites", "VirusNet", "Wealth", "Wolf", "ImportWorld")

  // this is here to help us remember that when the list of benchmarks changes, this file and the
  // contents of models/test/bench both need updating too - ST 2/11/09
  test("names") {
    assert(names.mkString("\n") === ChecksumsAndPreviews.allBenchmarks.mkString("\n"))
  }

  if(Version.useGenerator && !Version.is3D) {
    for(name <- names)
      test(name) {
        val dump = {
          val workspace = HeadlessWorkspace.newInstance
          workspace.open("models/test/benchmarks/" + name + " Benchmark.nlogo")
          val result = workspace.report("__dump")
          workspace.dispose()
          result
        }
        val source =
          io.Source.fromFile("models/test/bench/" + BranchName.branch + "/" + name + ".txt")
        assert(dump === source.getLines.mkString("","\n","\n"))
      }
  }
}

object BranchName {
  // the benchmarks dumps may be different on some branches, but since the benchmark models aren't
  // open source, they're in a separate repo.  so, in that separate repo, we have a "master"
  // directory with the dumps for the master branch here in the main repo, and for any branch here
  // which needs different dumps, there's a corresponding directory in the models repo.
  // A bit kludgy I guess, but it's OK. - ST 11/17/11
  val branch = "api-program-refactoring"
}
