// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

/**
 * maybe we could use some more tests that test sliders.
 * they would be easy to add here.
 */
class TestSliderModels extends AbstractTestModels {

  val modelCode = "globals [glob1] to-report square [x] report x * x end"
  val theModel = Model(modelCode,Slider(name="density", max="99", current="57.0"))

  testModel("simple slider test", theModel){
    reporter("glob1") -> 0.0
    observer>>"set glob1 12345"
    reporter("glob1") -> 12345.0
    reporter("density") -> 57.0
    observer>>"set density 63"
    reporter("density") -> 63.0
    reporter("square 5") -> 25.0
  }
}
