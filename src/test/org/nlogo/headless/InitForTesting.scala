// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.{ api, shape }

object InitForTesting {

  def apply(ws: HeadlessWorkspace, worldSize: Int) {
    apply(ws, worldSize, "")
  }

  def apply(ws: HeadlessWorkspace, worldSize: Int, modelString: String) {
    apply(ws, -worldSize, worldSize, -worldSize, worldSize, modelString)
  }

  def apply(ws: HeadlessWorkspace, minPxcor: Int, maxPxcor: Int, minPycor: Int, maxPycor: Int, source: String) {
    apply(ws, new api.WorldDimensions(minPxcor, maxPxcor, minPycor, maxPycor), source)
  }

  def apply(ws: HeadlessWorkspace, minPxcor: Int, maxPxcor: Int, minPycor: Int, maxPycor: Int) {
    apply(ws, new api.WorldDimensions(minPxcor, maxPxcor, minPycor, maxPycor))
  }

  def apply(ws: HeadlessWorkspace, d: api.WorldDimensions) {
    apply(ws, d, "")
  }

  def apply(ws: HeadlessWorkspace, d: api.WorldDimensions, source: String) {
    import ws._
    world.turtleShapeList.add(shape.VectorShape.getDefaultShape)
    world.linkShapeList.add(shape.LinkShape.getDefaultLinkShape)
    world.createPatches(d)
    val results = compiler.compileProgram(
      source, api.Program.empty, getExtensionManager)
    procedures = results.proceduresMap
    clearRunCache()
    init()
    world.program(results.program)
    world.realloc()

    plotManager.forgetAll()
    val plot1 = plotManager.newPlot("plot1")
    plot1.createPlotPen(name = "pen1")
    plot1.createPlotPen(name = "pen2")
    val plot2 = plotManager.newPlot("plot2")
    plot2.createPlotPen(name = "pen1")
    plot2.createPlotPen(name = "pen2")
    plotManager.compileAllPlots()

    clearDrawing()
  }

}
