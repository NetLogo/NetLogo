// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api.{ DummyLogoThunkFactory, MersenneTwisterFast }

class PlotManagerTests extends SimplePlotTest {

  def newPlotManager() =
    new PlotManager(new DummyLogoThunkFactory(), new MersenneTwisterFast())

  test("Constructor") {
    assertResult(0)(newPlotManager().getPlotNames.length)
  }

  test("Current Plot") {
    val manager = newPlotManager()
    val plot = manager.newPlot("test")
    assertResult(1)(manager.getPlotNames.length)
    assertResult(plot)(manager.currentPlot.get)
    manager.currentPlot = Some(plot)
    assertResult(plot)(manager.currentPlot.get)
    manager.forgetPlot(plot)
    assertResult(None)(manager.currentPlot)
  }

  test("Get Plot") {
    val manager = newPlotManager()
    val plot = manager.newPlot("Test1")
    assertResult(plot)(manager.currentPlot.get)
    assertResult("Test1")(manager.currentPlot.get.name)
    assertResult(Some(plot))(manager.getPlot("test1"))
    assertResult(Some(plot))(manager.getPlot("TEST1"))
    assertResult(Some(plot))(manager.getPlot("Test1"))
    assertResult(None)(manager.getPlot("test1 "))
    val plot2 = manager.newPlot("test2")
    assertResult(plot2)(manager.currentPlot.get)
    assertResult(2)(manager.getPlotNames.length)
    assertResult(List("Test1", "test2"))(manager.getPlotNames.toList)
    manager.forgetPlot(plot)
    assertResult(List("test2"))(manager.getPlotNames.toList)
  }

}
