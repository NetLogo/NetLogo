// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api, api.CompilerException

trait PlotManagerInterface extends api.PlotManagerInterface {
  def newPlot(name: String): Plot
  def compilePlot(plot: Plot): List[CompilerException]
  def forgetPlot(plot: Plot): Unit
  def hasErrors(plot: Plot): Boolean
  def getPlotSetupError(plot: Plot): Option[CompilerException]
  def getPlotUpdateError(plot: Plot): Option[CompilerException]
  def getPenSetupError(pen: PlotPen): Option[CompilerException]
  def getPenUpdateError(pen: PlotPen): Option[CompilerException]
  def getPlot(name: String): Option[Plot]
  def currentPlot: Option[Plot]
}

class DummyPlotManager extends PlotManagerInterface {
  private val names = Iterator.from(1).map("plot " + _)
  override def nextName: String = names.next()
  override def newPlot(name: String) = new Plot(name)
  override def compilePlot(plot: Plot) = Nil
  override def forgetPlot(plot: Plot) {}
  override def hasErrors(plot: Plot) = false
  override def getPlotSetupError(plot: Plot)= None
  override def getPlotUpdateError(plot: Plot) = None
  override def getPenSetupError(pen: PlotPen) = None
  override def getPenUpdateError(pen: PlotPen) = None
  override def currentPlot = None
  override def publish(action: api.PlotAction) { }
  override def setCurrentPlot(name: String) { }
  override def hasPlot(name: String) = false
  override def getPlot(name: String) = None
  override def getPlotNames = Seq()
}
