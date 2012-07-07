// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.plot.{PlotPoint, PlotPen}

class TestPlotModels extends AbstractTestModels {

  implicit def pen2ContainsPoint(pen:PlotPen) = new {
    // couldnt call contains here because it seemed to call the java method instead.
    def containsPoint(x:Double, y:Double) = {
      pen.points.find((p:PlotPoint) => p.x == x && p.y == y).isDefined
    }
  }

  def onlyPlot = workspace.plotManager.plots(0)
  def onlyPen = workspace.plotManager.currentPlot.get.pens.head

  val modelCode = "breed [dogs dog] to setup reset-ticks clear-all-plots end  to go create-dogs 1 tick end"
  val theModel = Model(modelCode, Plot(pens = Pens(Pen(updateCode = "plot count dogs * 2"))))

  testModel("plot on tick", theModel) {
    observer>>"setup"
    observer>>"go"
    reporter("count dogs") -> 1.0
    reporter("ticks") -> 1.0
    assert(onlyPen.containsPoint(0.0, 2.0))

    observer>>"go"
    reporter("count dogs") -> 2.0
    reporter("ticks") -> 2.0
    assert(onlyPen.containsPoint(1.0, 4.0))

    observer>>"tick"
    assert(onlyPen.containsPoint(2.0, 4.0))
  }

  testModel("several ticks", theModel) {
    observer>>"setup"
    observer>>"tick"
    assert(onlyPen.containsPoint(0.0, 0.0))

    observer>>"tick"
    assert(onlyPen.containsPoint(1.0, 0.0))

    observer>>"create-dogs 1"
    observer>>"tick"
    assert(onlyPen.containsPoint(2.0, 2.0))
  }

  testModel("update-plots", theModel) {
    observer>>"setup"
    observer>>"update-plots"
    assert(onlyPen.containsPoint(0.0, 0.0))

    observer>>"update-plots"
    assert(onlyPen.containsPoint(1.0, 0.0))

    observer>>"create-dogs 1"
    observer>>"update-plots"
    assert(onlyPen.containsPoint(2.0, 2.0))

    observer>>"update-plots"
    assert(onlyPen.containsPoint(3.0, 2.0))
  }

  testModel("setup-plots",
    Model(modelCode,
      Plot(setupCode = "create-dogs 5", pens = Pens(Pen(updateCode = "plot count dogs * 2"))))){
    observer>>"setup-plots"
    reporter("count dogs") -> 5.0
  }

  testModel("plot with setup code and pen with setup code",
    Model(modelCode,
      Plot(setupCode = "create-dogs 5", pens = Pens(Pen(setupCode = "create-dogs 3"))))){
    reporter("count dogs") -> 0.0
    observer>>"setup-plots"
    reporter("count dogs") -> 8.0
  }

  testModel("pen with no update code should not get plotted on tick",
    Model(modelCode,Plot(pens = Pens(Pen(updateCode = ""))))){

    observer>>"reset-ticks"
    assert(onlyPen.points.size === 0)
    observer>>"tick"
    assert(onlyPen.points.size === 0)
  }

  testModel("plot update code should run on tick",
    Model(modelCode,Plot(updateCode = "plot count turtles", pens=Pens(Pen())))){
    observer>>"reset-ticks clear-all-plots"
    assert(onlyPen.points.size === 0)

    observer>>"tick"
    assert(onlyPen.containsPoint(0.0, 0.0))
    assert(onlyPen.points.size === 1)
  }

  testModel("two plots with setup code",
    Model(modelCode,
      Plot(setupCode = "create-dogs 5", pens = Pens(Pen(updateCode = "plot count dogs * 2"))),
      Plot(setupCode = "create-dogs 2", pens = Pens(Pen(updateCode = "plot count dogs * 2")))
      )){

    reporter("count dogs") -> 0.0
    observer>>"setup-plots"
    reporter("count dogs") -> 7.0
  }

  testModel("stop in plot update code",
    Model(modelCode,
      Plot(updateCode = "create-dogs 7 stop", pens = Pens(Pen(updateCode = "create-dogs 8"))))){

    reporter("count dogs") -> 0.0
    observer>>"update-plots"
    reporter("count dogs") -> 7.0
  }

  val modelCode2 = "breed [dogs dog] to go tick create-dogs 4 end"
  testModel("stop in plot update code doesnt kill outer procedure",
    Model(modelCode2, Plot(updateCode = "create-dogs 1 stop", pens = Pens(Pen(updateCode = "create-dogs 42"))))){
    observer>>"ca"
    reporter("count dogs") -> 0.0
    observer>>"reset-ticks"
    // reset ticks calls the plot update code, which creates 1 dog.
    // it then uses stop, so the pen code doesnt create 42 dogs.
    // so only one dog gets created
    reporter("count dogs") -> 1.0
    observer>>"go"
    // go runs the plot code again, creating 1 dog. the pen code doesnt run.
    // the outer procedure "go" is not stopped, and it creates 4 more dogs.
    reporter("count dogs") -> 6.0
  }

  // same exact test as the previous test, just call update-plots directly instead of tick.
  val modelCode3 = "breed [dogs dog] to go update-plots create-dogs 4 end"
  testModel("stop in plot update code doesnt kill outer procedure (2)",
    Model(modelCode3, Plot(updateCode = "create-dogs 1 stop", pens = Pens(Pen(updateCode = "create-dogs 42"))))){
    observer>>"ca"
    reporter("count dogs") -> 0.0
    observer>>"reset-ticks"
    // reset ticks calls the plot update code, which creates 1 dog.
    // it then uses stop, so the pen code doesnt create 42 dogs.
    // so only one dog gets created
    reporter("count dogs") -> 1.0
    observer>>"go"
    // go runs the plot code again, creating 1 dog. the pen code doesnt run.
    // the outer procedure "go" is not stopped, and it creates 4 more dogs.
    reporter("count dogs") -> 6.0
  }

  testModel("inner stop doesnt prevent pens from running",
    Model(modelCode,
      Plot(updateCode = "ask turtles [stop]", pens = Pens(Pen(updateCode = "create-dogs 8"))))){
    reporter("count dogs") -> 0.0
    observer>>"update-plots"
    reporter("count dogs") -> 8.0
  }

  testModel("stop in pen doesnt prevent other pens from running",
    Model(modelCode,
      Plot(pens = Pens(Pen(updateCode = "create-dogs 8 stop"), Pen(updateCode = "create-dogs 8 stop"))))){
    reporter("count dogs") -> 0.0
    observer>>"update-plots"
    reporter("count dogs") -> 16.0
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
  testModel("plot code uses aux rng",
    Model(modelCode4, Plot(updateCode = "set x n-values 10 [random 10]",
      pens = Pens(Pen(updateCode = "set x n-values 10 [random 10]"))))){
    observer>>"reset-ticks"
    observer>>"random-seed 10"
    assert(reporter("n-values 10 [random 10]").a.toString === "[8.0, 9.0, 8.0, 4.0, 2.0, 4.0, 5.0, 4.0, 7.0, 9.0]")
    observer>>"random-seed 10"
    observer>>"tick" // runs plot code, which uses rng in this test case
    assert(reporter("n-values 10 [random 10]").a.toString === "[8.0, 9.0, 8.0, 4.0, 2.0, 4.0, 5.0, 4.0, 7.0, 9.0]")
  }

  testModelCompileError("Plot With Bad Update Code Should Throw Exception on Load (headless only)",
    Model(modelCode, Plot(updateCode="weijefwef"))){ ex =>
    assert("Nothing named WEIJEFWEF has been defined" === ex.getMessage)
  }

  testModelCompileError("Plot With Bad Setup Code Should Throw Exception on Load (headless only)",
    Model(modelCode, Plot(setupCode="weijefwef"))){ ex =>
    assert("Nothing named WEIJEFWEF has been defined" === ex.getMessage)
  }

  testModelCompileError("Plot With Bad Pen Setup Code Should Throw Exception on Load (headless only)",
    Model(modelCode, Plot(pens = Pens(Pen(setupCode = "create-fails 8"))))){ ex =>
    assert("Nothing named CREATE-FAILS has been defined" === ex.getMessage)
  }

  testModelCompileError("Plot With Bad Pen Update Code Should Throw Exception on Load (headless only)",
    Model(modelCode, Plot(pens = Pens(Pen(updateCode = "create-fails 8"))))){ ex =>
    assert("Nothing named CREATE-FAILS has been defined" === ex.getMessage)
  }
}
