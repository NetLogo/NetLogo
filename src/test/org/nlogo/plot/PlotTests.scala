// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.scalatest.FunSuite

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
    expect("test")(plot.name)
    expect(0)(plot.pens.size) // no pens
    assert(plot.defaultAutoPlotOn)
    assert(plot.autoPlotOn)
  }

  testPlot("ClearRemovesTemporaryPens") { plot =>
    plot.createPlotPen("permanent", false)
    plot.createPlotPen("temporary", true)
    expect(2)(plot.pens.size)
    plot.clear()
    expect(1)(plot.pens.size)
  }
  testPlot("ClearMisc") { plot =>
    plot.pens = Nil
    val pen1 = plot.createPlotPen("pen1", false)
    val pen2 = plot.createPlotPen("pen2", false)
    plot.currentPen=pen2
    pen1.plot(50)
    pen1.plot(100)
    pen2.plot(25)
    expect(2)(pen1.points.size)
    expect(1)(pen2.points.size)
    plot.clear()
    expect(0)(pen1.points.size)
    expect(0)(pen2.points.size)
    expect(pen1)(plot.currentPen.get)
  }
  testPlot("AutoPlotGrowMin") { plot =>
    val pen = plot.createPlotPen("test", false)
    pen.plot(-0.0001, -0.0001)
    expect(-2.5)(plot.xMin)
    expect(-1.0)(plot.yMin)
    expect(10.0)(plot.xMax)
    expect(10.0)(plot.yMax)
  }
  testPlot("AutoPlotGrowMax") { plot =>
    val pen = plot.createPlotPen("test", false)
    expect(0.0)(plot.xMin)
    expect(0.0)(plot.yMin)
    expect(10.0)(plot.xMax)
    expect(10.0)(plot.yMax)
    pen.plot(10.0001, 10.0001)
    expect(12.5)(plot.xMax)
    expect(11.0)(plot.yMax)
  }
  testPlot("AutoPlotGrowExtraRoomForBar") { plot =>
    val pen = plot.createPlotPen("test", false)
    pen.mode = PlotPen.BAR_MODE
    pen.interval = 5.0
    pen.plot(10.0001, 10.0001)
    expect(18.8)(plot.xMax)
    expect(11.0)(plot.yMax)
  }
  testPlot("HistogramNumBars") { plot =>
    val pen = plot.createPlotPen("test", false)
    plot.setHistogramNumBars(pen, 5)
    expect(2.0)(pen.interval)
  }
  /// histogram tests
  // we already have TestHistogram for basic histogram testing,
  // so these just need to test the extra histogram code in
  // Plot itself - ST 2/23/06
  testPlot("Histogram") { plot =>
    val pen = plot.createPlotPen("test", false)
    plot.beginHistogram(pen)
    // 0 1 4 9 16 25 36 49 64 81 100 121
    (0 to 11).map(i => (i * i) % 10).foreach(plot.nextHistogramValue(_))
    plot.endHistogram(pen)
    expect(6)(pen.points.size)
    expect(0.0)(pen.points(0).x)
    expect(1.0)(pen.points(1).x)
    expect(4.0)(pen.points(2).x)
    expect(5.0)(pen.points(3).x)
    expect(6.0)(pen.points(4).x)
    expect(9.0)(pen.points(5).x)
    expect(2.0)(pen.points(0).y)
    expect(3.0)(pen.points(1).y)
    expect(2.0)(pen.points(2).y)
    expect(1.0)(pen.points(3).y)
    expect(2.0)(pen.points(4).y)
    expect(2.0)(pen.points(5).y)
  }
  testPlot("HistogramGrowHeight") { plot =>
    plot.yMax=5
    val pen = plot.createPlotPen("test", false)
    plot.beginHistogram(pen)
    (0 until 5).foreach(_ => plot.nextHistogramValue(0))
    plot.endHistogram(pen)
    expect(5.0)(plot.yMax)
    plot.beginHistogram(pen)
    (0 until 10).foreach(_ => plot.nextHistogramValue(0))
    plot.endHistogram(pen)
    expect(10.0)(plot.yMax)
    plot.clear()
    plot.yMax=5
    pen.plot(0, 10)
    expect(11.0)(plot.yMax)
  }
  testPlot("Iterator") { plot =>
    plot.pens = Nil
    val pen1 = plot.createPlotPen("pen1", false)
    plot.createPlotPen("pen2", false)
    plot.createPlotPen("pen3", false)
    expect(3)(plot.pens.size)
    expect(pen1)(plot.pens.head)
    // pens should come back in same order inserted
    expect(List("pen1", "pen2", "pen3"))(plot.pens.map(_.name))
  }
  testPlot("Get") { plot =>
    plot.pens = Nil
    val pen1 = plot.createPlotPen("PEN1", false)
    val pen2 = plot.createPlotPen("pen2", false)
    expect(2)(plot.pens.size)
    expect(pen1)(plot.getPen("pen1").get)
    expect(pen2)(plot.getPen("PEN2").get)
    expect(null)(plot.getPen("pen3").orNull)
    expect(pen1)(plot.pens.head)
  }
}
