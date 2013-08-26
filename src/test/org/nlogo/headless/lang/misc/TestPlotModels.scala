// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

import org.nlogo.plot.{ PlotPoint, PlotPen }
import ModelCreator._

class TestPlotModels extends FixtureSuite {

  // convenience
  def containsPoint(pen: PlotPen, x: Double, y: Double) =
    pen.points.exists(p => p.x == x && p.y == y)

  def onlyPlot(implicit fixture: Fixture) =
    fixture.workspace.plotManager.plots match {
      case Seq(plot) => plot }
  def onlyPen(implicit fixture: Fixture) =
    onlyPlot.pens match {
      case Seq(pen) => pen }

  val modelCode =
    """|breed [dogs dog]
       |to setup
       |  clear-all
       |  reset-ticks
       |end
       |to go
       |  create-dogs 1
       |  tick
       |end""".stripMargin
  val theModel =
    Model(modelCode, widgets = List(
      Plot(pens = Pens(Pen(updateCode = "plot count dogs * 2")))))

  test("plot on tick") { implicit fixture =>
    import fixture._
    open(theModel)
    assertResult(1)(workspace.plotManager.plots.size)
    assertResult(1)(onlyPlot.pens.size)
    testCommand("setup")
    assertResult(1)(onlyPen.points.size)
    assert(containsPoint(onlyPen, 0.0, 0.0))
    testCommand("go")
    testReporter("count dogs", "1")
    testReporter("ticks", "1")
    assert(containsPoint(onlyPen, 1.0, 2.0))

    testCommand("go")
    testReporter("count dogs", "2")
    testReporter("ticks", "2")
    assert(containsPoint(onlyPen, 2.0, 4.0))

    testCommand("tick")
    assert(containsPoint(onlyPen, 3.0, 4.0))
  }

  test("several ticks") { implicit fixture =>
    import fixture._
    open(theModel)
    testCommand("setup")
    testCommand("tick")
    assert(containsPoint(onlyPen, 0.0, 0.0))
    assert(containsPoint(onlyPen, 1.0, 0.0))

    testCommand("tick")
    assert(containsPoint(onlyPen, 2.0, 0.0))

    testCommand("create-dogs 1")
    testCommand("tick")
    assert(containsPoint(onlyPen, 3.0, 2.0))
  }

  test("update-plots") { implicit fixture =>
    import fixture._
    open(theModel)
    testCommand("setup")
    assertResult(1)(onlyPen.points.size)
    assert(containsPoint(onlyPen, 0.0, 0.0))
    testCommand("update-plots")
    assertResult(2)(onlyPen.points.size)
    assert(containsPoint(onlyPen, 1.0, 0.0))

    testCommand("update-plots")
    assertResult(3)(onlyPen.points.size)
    assert(containsPoint(onlyPen, 2.0, 0.0))

    testCommand("create-dogs 1")
    testReporter("count dogs", "1")
    testCommand("update-plots")
    assertResult(4)(onlyPen.points.size)
    assert(containsPoint(onlyPen, 3.0, 2.0))

    testCommand("update-plots")
    assertResult(5)(onlyPen.points.size)
    assert(containsPoint(onlyPen, 4.0, 2.0))
  }

  test("setup-plots") { implicit fixture =>
    import fixture._
    open(
      Model(modelCode, widgets = List(
        Plot(setupCode = "create-dogs 5",
          pens = Pens(Pen(updateCode = "plot count dogs * 2"))))))
    testCommand("setup-plots")
    testReporter("count dogs", "5")
  }

  test("plot with setup code and pen with setup code") { implicit fixture =>
    import fixture._
    open(Model(modelCode, widgets = List(
      Plot(setupCode = "create-dogs 5",
           pens = Pens(Pen(setupCode = "create-dogs 3"))))))
    testReporter("count dogs", "0")
    testCommand("setup-plots")
    testReporter("count dogs", "8")
  }

  test("pen with no update code should not get plotted on tick") { implicit fixture =>
    import fixture._
    open(Model(modelCode, widgets = List(Plot(pens = Pens(Pen(updateCode = ""))))))
    testCommand("reset-ticks")
    assert(onlyPen.points.size === 0)
    testCommand("tick")
    assert(onlyPen.points.size === 0)
  }

  test("plot update code should run on tick") { implicit fixture =>
    import fixture._
    open(
      Model(modelCode, widgets = List(Plot(updateCode = "plot count turtles",
                                 pens = Pens(Pen())))))
    testCommand("reset-ticks clear-all-plots")
    assert(onlyPen.points.size === 0)

    testCommand("tick")
    assert(containsPoint(onlyPen, 0.0, 0.0))
    assert(onlyPen.points.size === 1)
  }

  test("two plots with setup code") { implicit fixture =>
    import fixture._
    open(
      Model(modelCode, widgets = List(
        Plot(setupCode = "create-dogs 5", pens = Pens(Pen(updateCode = "plot count dogs * 2"))),
        Plot(setupCode = "create-dogs 2", pens = Pens(Pen(updateCode = "plot count dogs * 2")))
        )))
    testReporter("count dogs", "0")
    testCommand("setup-plots")
    testReporter("count dogs", "7")
  }

  test("stop in plot update code") { implicit fixture =>
    import fixture._
    open(
      Model(modelCode, widgets = List(
        Plot(updateCode = "create-dogs 7 stop", pens = Pens(Pen(updateCode = "create-dogs 8"))))))
    testReporter("count dogs", "0")
    testCommand("update-plots")
    testReporter("count dogs", "7")
  }

  val modelCode2 = "breed [dogs dog] to go tick create-dogs 4 end"
  test("stop in plot update code doesnt kill outer procedure") { implicit fixture =>
    import fixture._
    open(
      Model(modelCode2,
        widgets = List(
          Plot(updateCode = "create-dogs 1 stop",
            pens = Pens(Pen(updateCode = "create-dogs 42"))))))
    testCommand("ca")
    testReporter("count dogs", "0")
    testCommand("reset-ticks")
    // reset ticks calls the plot update code, which creates 1 dog.
    // it then uses stop, so the pen code doesnt create 42 dogs.
    // so only one dog gets created
    testReporter("count dogs", "1")
    testCommand("go")
    // go runs the plot code again, creating 1 dog. the pen code doesnt run.
    // the outer procedure "go" is not stopped, and it creates 4 more dogs.
    testReporter("count dogs", "6")
  }

  // same exact test as the previous test, just call update-plots directly instead of tick.
  val modelCode3 = "breed [dogs dog] to go update-plots create-dogs 4 end"
  test("stop in plot update code doesnt kill outer procedure (2)") { implicit fixture =>
    import fixture._
    open(
      Model(modelCode3, widgets = List(Plot(updateCode = "create-dogs 1 stop",
                                  pens = Pens(Pen(updateCode = "create-dogs 42"))))))
    testCommand("ca")
    testReporter("count dogs", "0")
    testCommand("reset-ticks")
    // reset ticks calls the plot update code, which creates 1 dog.
    // it then uses stop, so the pen code doesnt create 42 dogs.
    // so only one dog gets created
    testReporter("count dogs", "1")
    testCommand("go")
    // go runs the plot code again, creating 1 dog. the pen code doesnt run.
    // the outer procedure "go" is not stopped, and it creates 4 more dogs.
    testReporter("count dogs", "6")
  }

  test("inner stop doesnt prevent pens from running") { implicit fixture =>
    import fixture._
    open(
      Model(modelCode, widgets = List(
        Plot(updateCode = "ask turtles [stop]",
             pens = Pens(Pen(updateCode = "create-dogs 8"))))))
    testReporter("count dogs", "0")
    testCommand("update-plots")
    testReporter("count dogs", "8")
  }

  test("stop in pen doesnt prevent other pens from running") { implicit fixture =>
    import fixture._
    open(
      Model(modelCode, widgets = List(
        Plot(pens = Pens(Pen(updateCode = "create-dogs 8 stop"),
                         Pen(updateCode = "create-dogs 8 stop"))))))
    testReporter("count dogs", "0")
    testCommand("update-plots")
    testReporter("count dogs", "16")
  }

  /**
   * from the user manual:
   *   random-seed 10
   *   with-local-randomness [ print n-values 10 [random 10] ]
   *   ;; prints [8 9 8 4 2 4 5 4 7 9]
   *   print n-values 10 [random 10]
   *   ;; prints [8 9 8 4 2 4 5 4 7 9]
   */
  val modelCode4 = "globals [x]"
  test("plot code uses aux rng") { implicit fixture =>
    import fixture._
    open(
      Model(modelCode4, widgets = List(
        Plot(updateCode = "set x n-values 10 [random 10]",
             pens = Pens(Pen(updateCode = "set x n-values 10 [random 10]"))))))
    testCommand("reset-ticks")
    testCommand("random-seed 10")
    testReporter("n-values 10 [random 10]", "[8 9 8 4 2 4 5 4 7 9]")
    testCommand("random-seed 10")
    testCommand("tick") // runs plot code, which uses rng in this test case
    testReporter("n-values 10 [random 10]", "[8 9 8 4 2 4 5 4 7 9]")
  }

  def testCompileError(model: Model)(f: Throwable => Unit)(implicit fixture: Fixture) = {
    val ex = intercept[Throwable] {
      fixture.workspace.openFromSource(model.toString)
    }
    f(ex)
  }

  test("Plot With Bad Update Code Should Throw Exception on Load (headless only)") { implicit fixture =>
    testCompileError(Model(modelCode, widgets = List(Plot(updateCode="weijefwef")))) { ex =>
      assert("Nothing named WEIJEFWEF has been defined" === ex.getMessage)
    }}

  test("Plot With Bad Setup Code Should Throw Exception on Load (headless only)") { implicit fixture =>
    testCompileError(Model(modelCode, widgets = List(Plot(setupCode="weijefwef")))){ ex =>
      assert("Nothing named WEIJEFWEF has been defined" === ex.getMessage)
    }}

  test("Plot With Bad Pen Setup Code Should Throw Exception on Load (headless only)") { implicit fixture =>
    testCompileError(Model(modelCode, widgets = List(Plot(pens = Pens(Pen(setupCode = "create-fails 8")))))) { ex =>
      assert("Nothing named CREATE-FAILS has been defined" === ex.getMessage)
    }}

  test("Plot With Bad Pen Update Code Should Throw Exception on Load (headless only)") { implicit fixture =>
    testCompileError(Model(modelCode, widgets = List(Plot(pens = Pens(Pen(updateCode = "create-fails 8")))))) { ex =>
      assert("Nothing named CREATE-FAILS has been defined" === ex.getMessage)
    }}
}
