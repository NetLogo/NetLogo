package org.nlogo.plot

import org.nlogo.api.{ DummyLogoThunkFactory, LogoThunkFactory }

class PlotManagerTests extends SimplePlotTest {

  def newPlotManager() =
    new PlotManager(new DummyLogoThunkFactory())

  def noCurrentPlot(manager: PlotManager) {
    intercept[PlotException] {
      manager.currentPlotOrBust
    }
  }

  test("Constructor") {
    expect(0)(newPlotManager().getPlotNames.length)
  }

  test("Current Plot") {
    val manager = newPlotManager()
    val plot = manager.newPlot("test")
    expect(1)(manager.getPlotNames.length)
    expect(plot)(manager.currentPlotOrBust)
    manager.setCurrentPlot(plot)
    expect(plot)(manager.currentPlotOrBust)
    manager.forgetPlot(plot)
    noCurrentPlot(manager)
  }

  test("Get Plot") {
    val manager = newPlotManager()
    val plot = manager.newPlot("Test1")
    expect(plot)(manager.currentPlotOrBust)
    expect("Test1")(manager.currentPlotOrBust.name)
    expect(plot)(manager.getPlot("test1"))
    expect(plot)(manager.getPlot("TEST1"))
    expect(plot)(manager.getPlot("Test1"))
    expect(null)(manager.getPlot("test1 "))
    val plot2 = manager.newPlot("test2")
    expect(plot2)(manager.currentPlotOrBust)
    expect(2)(manager.getPlotNames.length)
    expect(List("Test1", "test2"))(manager.getPlotNames.toList)
    manager.forgetPlot(plot)
    expect(List("test2"))(manager.getPlotNames.toList)
  }

}
