// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api.DummyLogoThunkFactory

class PlotManagerTests extends SimplePlotTest {

  def newPlotManager() =
    new PlotManager(new DummyLogoThunkFactory())

  test("Constructor") {
    expectResult(0)(newPlotManager().getPlotNames.length)
  }

  test("Current Plot") {
    val manager = newPlotManager()
    val plot = manager.newPlot("test")
    expectResult(1)(manager.getPlotNames.length)
    expectResult(plot)(manager.currentPlot.get)
    manager.currentPlot = Some(plot)
    expectResult(plot)(manager.currentPlot.get)
    manager.forgetPlot(plot)
    expectResult(None)(manager.currentPlot)
  }

  test("Get Plot") {
    val manager = newPlotManager()
    val plot = manager.newPlot("Test1")
    expectResult(plot)(manager.currentPlot.get)
    expectResult("Test1")(manager.currentPlot.get.name)
    expectResult(plot)(manager.getPlot("test1"))
    expectResult(plot)(manager.getPlot("TEST1"))
    expectResult(plot)(manager.getPlot("Test1"))
    expectResult(null)(manager.getPlot("test1 "))
    val plot2 = manager.newPlot("test2")
    expectResult(plot2)(manager.currentPlot.get)
    expectResult(2)(manager.getPlotNames.length)
    expectResult(List("Test1", "test2"))(manager.getPlotNames.toList)
    manager.forgetPlot(plot)
    expectResult(List("test2"))(manager.getPlotNames.toList)
  }

}
