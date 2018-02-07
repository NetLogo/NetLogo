// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

import org.nlogo.util.SlowTestTag

/**
 * An example of using TestParallelModels
 * This runs gas lab gas in a box in parallel using 10 threads
 */
class TestGasLabInParallel extends ParallelSuite  {

  val path = "models/Sample Models/Chemistry & Physics/GasLab/GasLab Gas in a Box.nlogox"

  test("gas lab in parallel", SlowTestTag) {
    openParallel(path){implicit fixture =>
      fixture.testCommand("random-seed 571  setup  repeat 50 [ go ]")
      fixture.testReporter("avg-speed", "9.760082324073991")
    }
  }

}
