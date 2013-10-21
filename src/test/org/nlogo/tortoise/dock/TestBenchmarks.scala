// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestBenchmarks extends DockingSuite with SlowTest {

  test("ants") { implicit fixture => import fixture._
    open("models/test/benchmarks/Ants Benchmark.nlogo")
    testCommand("setup")
    testCommand("repeat 10 [ go ]")
  }
}
