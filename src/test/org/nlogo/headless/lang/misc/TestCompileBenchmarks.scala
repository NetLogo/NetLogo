// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

import org.nlogo.api.Version
import org.scalatest.FunSuite
import org.nlogo.util.SlowTest

class TestCompileBenchmarks extends FixtureSuite with SlowTest {

  val names = Seq(
    "Ants", "Bureaucrats", "BZ", "CA1D", "Erosion", "Fire", "FireBig", "Flocking", "GasLabCirc",
    "GasLabNew", "GasLabOld", "GridWalk", "Heatbugs", "Ising", "Life", "PrefAttach",
    "Team", "Termites", "VirusNet", "Wealth", "Wolf", "ImportWorld")

  // this is here to help us remember that when the list of benchmarks changes, this file and the
  // contents of test/benchdumps both need updating too - ST 2/11/09
  test("names") { _ =>
    assert(names.mkString("\n") === ChecksumsAndPreviews.allBenchmarks.mkString("\n"))
  }

  if(Version.useGenerator)
    for(name <- names)
      test(name) { fixture =>
        import fixture.workspace.{ open, report }
        open("models/test/benchmarks/" + name + " Benchmark.nlogo")
        val expected =
          io.Source.fromFile("test/benchdumps/" + name + ".txt")
            .getLines.mkString("","\n","\n")
        assertResult(expected)(report("__dump"))
      }

}
