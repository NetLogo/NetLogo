// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api.{ PlotAction, PlotInterface, PlotListener, PlotManagerInterface => ApiPlotManagerInterface }
import org.nlogo.core.CompilerException

trait PlotManagerInterface extends ApiPlotManagerInterface {
  def nextName:String
  def addPlot(plot: Plot) : Unit
  def newPlot(name:String): Plot
  def compilePlot(plot:Plot): List[CompilerException]
  def forgetPlot(plot:Plot): Unit
  def hasErrors(plot:Plot): Boolean
  def getPlotSetupError(plot:Plot): Option[CompilerException]
  def getPlotUpdateError(plot:Plot): Option[CompilerException]
  def getPenSetupError(pen:PlotPen): Option[CompilerException]
  def getPenUpdateError(pen:PlotPen): Option[CompilerException]
}

class DummyPlotManager extends PlotManagerInterface{
  private val names = Iterator.from(1).map("plot " + _)
  def nextName: String = names.next()
  def addPlot(plot: Plot): Unit = {}
  def newPlot(name:String): Plot = new Plot(name)
  def compilePlot(plot:Plot): List[CompilerException] = Nil
  def forgetPlot(plot:Plot): Unit = {}
  def hasErrors(plot:Plot): Boolean = false
  def getPlotSetupError(plot:Plot): Option[CompilerException] = None
  def getPlotUpdateError(plot:Plot): Option[CompilerException] = None
  def getPenSetupError(pen:PlotPen): Option[CompilerException] = None
  def getPenUpdateError(pen:PlotPen): Option[CompilerException] = None

  // Added for api compatibility -Jeremy B Octover 2020
  def currentPlot: Option[PlotInterface] = ???
  def getPlotNames: Seq[String] = ???
  def hasPlot(name: String): Boolean = ???
  def maybeGetPlot(name: String): Option[PlotInterface] = ???
  def publish(action: PlotAction): Unit = ???
  def setCurrentPlot(name: String): Unit = ???
  def plots: Seq[PlotInterface] = ???
  def setPlotListener(listener: PlotListener): Unit = ???
}
