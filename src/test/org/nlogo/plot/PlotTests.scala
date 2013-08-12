// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.scalatest.FunSuite
import org.nlogo.api.PlotPenInterface

trait SimplePlotTest extends FunSuite {

  def testPlot(name: String)(f: Plot => Unit) {
    test(name) {
      f(new Plot(name))
    }
  }
}

class PlotTests extends SimplePlotTest {

  test("Constructor") {
    val plot = new Plot("test")
    assertResult("test")(plot.name)
    assertResult(0)(plot.pens.size) // no pens
    assert(plot.defaultState.autoPlotOn)
    assert(plot.state.autoPlotOn)
  }

  testPlot("ClearRemovesTemporaryPens") { plot =>
    plot.createPlotPen("permanent", false)
    plot.createPlotPen("temporary", true)
    assertResult(2)(plot.pens.size)
    plot.clear()
    assertResult(1)(plot.pens.size)
  }
  testPlot("ClearMisc") { plot =>
    plot.pens = Nil
    val pen1 = plot.createPlotPen("pen1", false)
    val pen2 = plot.createPlotPen("pen2", false)
    plot.currentPen=pen2
    pen1.plot(50)
    pen1.plot(100)
    pen2.plot(25)
    assertResult(2)(pen1.points.size)
    assertResult(1)(pen2.points.size)
    plot.clear()
    assertResult(0)(pen1.points.size)
    assertResult(0)(pen2.points.size)
    assertResult(pen1)(plot.currentPen.get)
  }
  testPlot("AutoPlotGrowMin") { plot =>
    val pen = plot.createPlotPen("test", false)
    plot.plot(pen, -0.0001, -0.0001)
    assertResult(-2.5)(plot.state.xMin)
    assertResult(-1.0)(plot.state.yMin)
    assertResult(10.0)(plot.state.xMax)
    assertResult(10.0)(plot.state.yMax)
  }
  testPlot("AutoPlotGrowMax") { plot =>
    val pen = plot.createPlotPen("test", false)
    assertResult(0.0)(plot.state.xMin)
    assertResult(0.0)(plot.state.yMin)
    assertResult(10.0)(plot.state.xMax)
    assertResult(10.0)(plot.state.yMax)
    plot.plot(pen, 10.0001, 10.0001)
    assertResult(12.5)(plot.state.xMax)
    assertResult(11.0)(plot.state.yMax)
  }
  testPlot("AutoPlotGrowExtraRoomForBar") { plot =>
    val pen = plot.createPlotPen("test", false)
    pen.state = pen.state.copy(
      mode = PlotPenInterface.BarMode,
      interval = 5.0)
    plot.plot(pen, 10.0001, 10.0001)
    assertResult(18.8)(plot.state.xMax)
    assertResult(11.0)(plot.state.yMax)
  }

  /// histogram tests
  // we already have TestHistogram for basic histogram testing,
  // so these just need to test the extra histogram code in
  // Plot itself - ST 2/23/06
  // Modified to use histogramActions - NP 2013-02-12
  testPlot("HistogramActions") { plot =>
    val pen = plot.createPlotPen("test", false)
    // 0 1 4 9 16 25 36 49 64 81 100 121
    val values = (0 to 11).map(i => ((i * i) % 10).toDouble)
    val actions = plot.histogramActions(pen, values)
    val runner = new BasicPlotActionRunner(Seq(plot))
    actions.foreach(runner.run)
    assertResult(6)(pen.points.size)
    assertResult(0.0)(pen.points(0).x)
    assertResult(1.0)(pen.points(1).x)
    assertResult(4.0)(pen.points(2).x)
    assertResult(5.0)(pen.points(3).x)
    assertResult(6.0)(pen.points(4).x)
    assertResult(9.0)(pen.points(5).x)
    assertResult(2.0)(pen.points(0).y)
    assertResult(3.0)(pen.points(1).y)
    assertResult(2.0)(pen.points(2).y)
    assertResult(1.0)(pen.points(3).y)
    assertResult(2.0)(pen.points(4).y)
    assertResult(2.0)(pen.points(5).y)
  }

  testPlot("HistogramActionsGrowHeight") { plot =>
    plot.state = plot.state.copy(yMax = 5)
    val pen = plot.createPlotPen("test", false)
    val runner = new BasicPlotActionRunner(Seq(plot))
    def zeros = Stream.continually(0.0)
    plot
      .histogramActions(pen, zeros.take(5))
      .foreach(runner.run)
    assertResult(5.0)(plot.state.yMax)
    plot
      .histogramActions(pen, zeros.take(10))
      .foreach(runner.run)
    assertResult(10.0)(plot.state.yMax)
    plot.clear()
    plot.state = plot.state.copy(yMax = 5)
    plot.plot(pen, 0, 10)
    assertResult(11.0)(plot.state.yMax)
  }

  testPlot("Iterator") { plot =>
    plot.pens = Nil
    val pen1 = plot.createPlotPen("pen1", false)
    plot.createPlotPen("pen2", false)
    plot.createPlotPen("pen3", false)
    assertResult(3)(plot.pens.size)
    assertResult(pen1)(plot.pens.head)
    // pens should come back in same order inserted
    assertResult(List("pen1", "pen2", "pen3"))(plot.pens.map(_.name))
  }
  testPlot("Get") { plot =>
    plot.pens = Nil
    val pen1 = plot.createPlotPen("PEN1", false)
    val pen2 = plot.createPlotPen("pen2", false)
    assertResult(2)(plot.pens.size)
    assertResult(pen1)(plot.getPen("pen1").get)
    assertResult(pen2)(plot.getPen("PEN2").get)
    assertResult(null)(plot.getPen("pen3").orNull)
    assertResult(pen1)(plot.pens.head)
  }
}
