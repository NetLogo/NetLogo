// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import org.nlogo.api.ModelCreator._

/**
 * maybe we could use some more tests that test sliders.
 * they would be easy to add here.
 */
class TestSliderModels extends FixtureSuite {

  val code =
    """|globals [glob1]
       |to-report square [x]
       |  report x * x
       |end""".stripMargin
  val model =
    Model(code,
      widgets = List(Slider(name="density", max="99", current="57.0")))

  test("simple slider test") { implicit fixture =>
    import fixture._
    open(model)
    testReporter("glob1", "0")
    testCommand("set glob1 12345")
    testReporter("glob1", "12345")
    testReporter("density", "57")
    testCommand("set density 63")
    testReporter("density", "63")
    testReporter("square 5", "25")
  }

}
