// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestBenchmarks extends DockingSuite with SlowTest {

  test("ants") { implicit fixture => import fixture._
    open("models/test/benchmarks/Ants Benchmark.nlogo")
    testCommand("setup")
    testCommand("repeat 10 [ go ]")
    testCommand("ask turtle 0 [ move-to one-of patches with [shade-of? pcolor blue] ]")
    testCommand("repeat 10 [ go ]")
  }

  test("bureaucrats") { implicit fixture => import fixture._
    open("models/test/benchmarks/Bureaucrats Benchmark.nlogo")
    testCommand("setup")
    testCommand("repeat 50 [ go ]")
  }

  test("BZ") { implicit fixture => import fixture._
    open("models/test/benchmarks/BZ Benchmark.nlogo")
    testCommand("resize-world -20 20 -20 20")
    testCommand("setup")
    testCommand("repeat 5 [ go ]")
  }
}
