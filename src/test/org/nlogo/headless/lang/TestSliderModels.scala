// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import ModelCreator._

/**
 * maybe we could use some more tests that test sliders.
 * they would be easy to add here.
 */
class TestSliderModels extends FixtureSuite {

  val model =
    Model(widgets = List(Slider(name="density", max="99", current="57.0")))

  test("simple slider test") { implicit fixture =>
    import fixture._
    open(model)
    testReporter("density", "57")
    testCommand("set density 63")
    testReporter("density", "63")
  }

}
