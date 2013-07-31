// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package model

import org.nlogo.util.SlowTest

/**
 * An example of using TestMultiThreadedModels
 * This runs gas lab gas in a box in parallel using 10 threads
 */
class GasLabInParallelTests extends TestMultiThreadedModels with SlowTest {

  testModelFile("gas lab in parallel",
    "models/Sample Models/Chemistry & Physics/GasLab/GasLab Gas in a Box.nlogo"){
    observer >> "random-seed 571  setup  repeat 50 [ go ]"
    reporter("avg-speed") -> 9.760082324073991
  }
}
