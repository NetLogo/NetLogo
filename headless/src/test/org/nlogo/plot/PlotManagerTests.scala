// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api.DummyLogoThunkFactory

class PlotManagerTests extends SimplePlotTest {

  def newPlotManager() =
    new PlotManager(new DummyLogoThunkFactory())

  test("Constructor") {
    expect(0)(newPlotManager().getPlotNames.length)
  }

  test("Current Plot") {
    val manager = newPlotManager()
    val plot = manager.newPlot("test")
    expect(1)(manager.getPlotNames.length)
    expect(plot)(manager.currentPlot.get)
    manager.currentPlot = Some(plot)
    expect(plot)(manager.currentPlot.get)
    manager.forgetPlot(plot)
    expect(None)(manager.currentPlot)
  }

  test("Get Plot") {
    val manager = newPlotManager()
    val plot = manager.newPlot("Test1")
    expect(plot)(manager.currentPlot.get)
    expect("Test1")(manager.currentPlot.get.name)
    expect(plot)(manager.getPlot("test1"))
    expect(plot)(manager.getPlot("TEST1"))
    expect(plot)(manager.getPlot("Test1"))
    expect(null)(manager.getPlot("test1 "))
    val plot2 = manager.newPlot("test2")
    expect(plot2)(manager.currentPlot.get)
    expect(2)(manager.getPlotNames.length)
    expect(List("Test1", "test2"))(manager.getPlotNames.toList)
    manager.forgetPlot(plot)
    expect(List("test2"))(manager.getPlotNames.toList)
  }

}
