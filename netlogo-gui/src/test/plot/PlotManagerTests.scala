// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api.{ CommandLogoThunk, DummyLogoThunkFactory, LogoException }

import scala.util.{ Failure, Try }

class PlotManagerTests extends SimplePlotTest {

  def newPlotManager() =
    new PlotManager(new DummyLogoThunkFactory())

  def errorThunkFactory = new DummyLogoThunkFactory() {
    override def makeCommandThunk(code: String, jobOwnerName: String): CommandLogoThunk = {
      return new CommandLogoThunk {
        def call: Try[Boolean] = {
          Failure(new LogoException("runtime error!") {})
        }
      }
    }
  }

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
    assertResult(plot)(manager.getPlot("test1"))
    assertResult(plot)(manager.getPlot("TEST1"))
    assertResult(plot)(manager.getPlot("Test1"))
    assertResult(null)(manager.getPlot("test1 "))
    val plot2 = manager.newPlot("test2")
    assertResult(plot2)(manager.currentPlot.get)
    assertResult(2)(manager.getPlotNames.length)
    assertResult(List("Test1", "test2"))(manager.getPlotNames.toList)
    manager.forgetPlot(plot)
    assertResult(List("test2"))(manager.getPlotNames.toList)
  }

  test("plot runtime errors do not propagate out") {
    val manager = new PlotManager(errorThunkFactory)
    val plot = manager.newPlot("test")
    plot.setupCode = "histogram runresult [false]"
    manager.compileAllPlots()
    manager.setupPlots()
    assert(plot.runtimeError.isDefined)
    assert(manager.hasErrors(plot))
  }

  test("plot pen errors are saved for later display") {
    val manager = new PlotManager(errorThunkFactory)
    val plot = manager.newPlot("test")
    val penErroringAtRuntime =
      new PlotPen(plot, "error", false, "", "plot runresult [ false ]")
    plot.addPen(penErroringAtRuntime)
    manager.compileAllPlots()
    manager.updatePlots()
    assert(plot.runtimeError.isDefined)
    assert(manager.hasErrors(plot))
    assert(manager.hasErrors(penErroringAtRuntime))
  }
}
