// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

import org.nlogo.core._

/**
 * maybe we could use some more tests that test sliders.
 * they would be easy to add here.
 */
class TestSliderModels extends FixtureSuite {

  val model =
    Model(widgets = List(Slider(display="density", varName="density", max="99", default=57), View()))

  test("simple slider test") { implicit fixture =>
    import fixture._
    open(model)
    testReporter("density", "57")
    testCommand("set density 63")
    testReporter("density", "63")
  }

}
