// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.scalatest.FunSuite
import org.nlogo.api.DummyLogoThunkFactory
import org.nlogo.api.ModelCreator.{Plot => MCPlot, Pen, Pens}

class PlotLoaderTests extends TestPlotLoaderHelper{

  def twice(s: String) = s + s

  test("converter converts plot setup code") {
    val plot = loadPlot(MCPlot(setupCode = "crt 1"), twice)
    assertResult("crt 1crt 1")(plot.setupCode)
  }

  test("converter converts plot update code") {
    val plot = loadPlot(MCPlot(updateCode = "crt 2"), twice)
    assertResult("crt 2crt 2")(plot.updateCode)
  }

  test("converter converts plot pen setup code") {
    val plot = loadPlot(MCPlot(pens = Pens(Pen(setupCode = "crt 3"))), twice)
    assertResult("crt 3crt 3")(plot.pens(0).setupCode)
  }

  test("converter converts plot pen update code") {
    val plot = loadPlot(MCPlot(pens = Pens(Pen(updateCode = "crt 4"))), twice)
    assertResult("crt 4crt 4")(plot.pens(0).updateCode)
  }

  test("convert everything") {
    val plot = loadPlot(
      MCPlot(setupCode = "crt 1", updateCode = "crt 2",
        pens = Pens(
          Pen(setupCode = "crt 3", updateCode = "crt 4"),
          Pen(setupCode = "crt 5", updateCode = "crt 6"))), twice)
    assertResult("crt 1crt 1")(plot.setupCode)
    assertResult("crt 2crt 2")(plot.updateCode)
    assertResult("crt 3crt 3")(plot.pens(0).setupCode)
    assertResult("crt 4crt 4")(plot.pens(0).updateCode)
    assertResult("crt 5crt 5")(plot.pens(1).setupCode)
    assertResult("crt 6crt 6")(plot.pens(1).updateCode)
  }
}

trait TestPlotLoaderHelper extends FunSuite {
  def loadPlot(corePlot: org.nlogo.core.Plot, converter: String => String = identity): Plot = {
    val plot = new PlotManager(new DummyLogoThunkFactory).newPlot("test")
    PlotLoader.loadPlot(corePlot, plot, converter)
    plot
  }
}
