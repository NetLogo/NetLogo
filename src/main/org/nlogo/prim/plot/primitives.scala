package org.nlogo.prim.plot

import org.nlogo.api.{ Color, PlotPenInterface, Syntax }
import org.nlogo.nvm.{ Context, Command, EngineException, Reporter }
import org.nlogo.plot.{ Plot, PlotManager }

//
// base classes
//

abstract class PlotManagerCommand(callsOtherCode:Boolean, args: Int*)
extends Command(callsOtherCode) {
  def perform(plotManager: PlotManager, c: Context)
  override def perform(context: Context) {
    perform(workspace.plotManager.asInstanceOf[PlotManager], context)
    context.ip = next
  }
  override def syntax = if(args.isEmpty) Syntax.commandSyntax else Syntax.commandSyntax(args.toArray)
}

abstract class CurrentPlotCommand(args: Int*)
extends Command {
  def perform(p: Plot, c: Context)
  def plotManager = workspace.plotManager.asInstanceOf[PlotManager]
  override def perform(context: Context) {
    perform(plotManager.currentPlotOrBust, context)
    context.ip = next
  }
  override def syntax = if(args.isEmpty) Syntax.commandSyntax else Syntax.commandSyntax(args.toArray)
}
abstract class ReallySimpleCurrentPlotCommand(f: Plot=>Unit)
extends CurrentPlotCommand {
  def perform(p: Plot, c: Context){ f(p) }
}

abstract class PlotReporter(returnType: Int, args: Int*)
extends Reporter {
  def report(p: Plot, c: Context): Object
  def plotManager = workspace.plotManager.asInstanceOf[PlotManager]
  override def report(context: Context): Object = report(plotManager.currentPlotOrBust, context)
  override def syntax = Syntax.reporterSyntax(args.toArray, returnType)
}
abstract class ReallySimplePlotReporter(returnType: Int, f: Plot=>Object)
extends PlotReporter(returnType){
  def report(p: Plot, c: Context) = f(p)
}

//
// commands requiring only the plot manager (its ok if there are no plots)
//

class _clearallplots extends PlotManagerCommand(callsOtherCode = false) {
  def perform(plotManager: PlotManager, c: Context){ plotManager.clearAll() }
}
class _setupplots extends PlotManagerCommand(callsOtherCode = true) {
  def perform(plotManager: PlotManager, c: Context) { workspace.setupPlots(c) }
}
class _updateplots extends PlotManagerCommand(callsOtherCode = true) {
  def perform(plotManager: PlotManager, c: Context) { workspace.updatePlots(c) }
}
class _setcurrentplot extends PlotManagerCommand(callsOtherCode = false, Syntax.StringType) {
  def perform(plotManager: PlotManager, context: Context){
    val name = argEvalString(context, 0)
    val plot = plotManager.getPlot(name)
    if (plot == null) { throw new EngineException(context, this, "no such plot: \"" + name + "\"") }
    plotManager.setCurrentPlot(plot)
    context.ip = next
  }
}

//
// commands requiring that there be a current plot.
//

class _clearplot extends ReallySimpleCurrentPlotCommand(_.clear())
class _autoplotoff extends ReallySimpleCurrentPlotCommand(_.autoPlotOn=false)
class _autoploton extends ReallySimpleCurrentPlotCommand(_.autoPlotOn=true)

class _plot extends CurrentPlotCommand(Syntax.NumberType) {
  override def perform(p: Plot, context: Context) {
    val y = argEvalDoubleValue(context, 0)
    p.currentPenOrBust.plot(y)
    p.makeDirty()
  }
}

class _plotxy extends CurrentPlotCommand(Syntax.NumberType, Syntax.NumberType) {
  override def perform(p: Plot, context: Context) {
    val x = argEvalDoubleValue(context, 0)
    val y = argEvalDoubleValue(context, 1)
    p.currentPenOrBust.plot(x, y)
    p.makeDirty()
  }
}

class _setplotxrange extends CurrentPlotCommand(Syntax.NumberType, Syntax.NumberType) {
  def perform(p: Plot, context: Context) {
    val min = argEvalDoubleValue(context, 0)
    val max = argEvalDoubleValue(context, 1)
    if (min >= max) {
      throw new EngineException(context, this,
        "the minimum must be less than the maximum, but " +  min + " is greater than or equal to " + max)
    }
    p.xMin=min
    p.xMax=max
    p.makeDirty()
  }
}

class _setplotyrange extends CurrentPlotCommand(Syntax.NumberType, Syntax.NumberType) {
  def perform(p: Plot, context: Context) {
    val min = argEvalDoubleValue(context, 0)
    val max = argEvalDoubleValue(context, 1)
    if (min >= max) {
      throw new EngineException(context, this,
        "the minimum must be less than the maximum, but " +  min + " is greater than or equal to " + max)
    }
    p.yMin=min
    p.yMax=max
    p.makeDirty()
  }
}

class _createtemporaryplotpen extends CurrentPlotCommand(Syntax.StringType) {
  def perform(plot: Plot, context: Context) {
    val name = argEvalString(context, 0)
    plot.currentPen=plot.getPen(name).getOrElse(plot.createPlotPen(name, true))
  }
}

class _histogram extends CurrentPlotCommand(Syntax.ListType) {
  def perform(plot: Plot, c: Context) {
    val list = argEvalList(c,0)
    val currentPen = plot.currentPenOrBust
    currentPen.plotListenerReset(false)
    plot.beginHistogram(currentPen)
    for(d <- list.scalaIterator.collect{case d: java.lang.Double => d.doubleValue})
      plot.nextHistogramValue(d)
    plot.endHistogram(currentPen)
    plot.makeDirty()
  }
}

class _sethistogramnumbars extends CurrentPlotCommand(Syntax.NumberType) {
  def perform(plot: Plot, context: Context) {
    val numBars = argEvalIntValue(context, 0)
    if (numBars < 1) {
      throw new EngineException(context, this, "You cannot make a histogram with " + numBars + " bars.")
    }
    plot.setHistogramNumBars(plot.currentPenOrBust, numBars)
  }
}

class _exportplot extends CurrentPlotCommand(Syntax.StringType, Syntax.StringType) {
  def perform(plot: Plot, context: Context) {
    val name = argEvalString(context, 0)
    val path = argEvalString(context, 1)
    if (plotManager.getPlot(name) == null) {
      throw new EngineException(context, this, "no such plot: \"" + name + "\"")
    }
    // Workspace.waitFor() switches to the event thread if we're
    // running with a GUI - ST 12/17/04
    workspace.waitFor(new org.nlogo.api.CommandRunnable() {
      def run() {
        try workspace.exportPlot(name, workspace.fileManager.attachPrefix(path))
        catch {
          case ex: java.io.IOException =>
            throw new EngineException(context, _exportplot.this, token.name + ": " + ex.getMessage)
        }
      }
    })
    context.ip = next
  }
}

// this also requires only the PlotManager, but it seems better to put it here next to exportplot.
class _exportplots extends PlotManagerCommand(callsOtherCode = false, Syntax.StringType) {
  def perform(plotManager: PlotManager, context: Context){
    val path = argEvalString(context, 0)
    if (plotManager.getPlotNames.length == 0) {
      throw new EngineException(context, this, "there are no plots to export")
    }
    // Workspace.waitFor() switches to the event thread if we're
    // running with a GUI - ST 12/17/04
    workspace.waitFor(new org.nlogo.api.CommandRunnable() {
      def run() {
        try workspace.exportAllPlots(workspace.fileManager().attachPrefix(path))
        catch {
          case ex: java.io.IOException =>
            throw new EngineException(context, _exportplots.this, token.name + ": " + ex.getMessage)
        }
      }
    })
    context.ip = next
  }
}

//
// reporters
//

class _autoplot extends ReallySimplePlotReporter(Syntax.BooleanType, p => Boolean.box(p.autoPlotOn))
class _plotname extends ReallySimplePlotReporter(Syntax.StringType, _.name)
class _plotxmin extends ReallySimplePlotReporter(Syntax.NumberType, p => Double.box(p.xMin))
class _plotxmax extends ReallySimplePlotReporter(Syntax.NumberType, p => Double.box(p.xMax))
class _plotymin extends ReallySimplePlotReporter(Syntax.NumberType, p => Double.box(p.yMin))
class _plotymax extends ReallySimplePlotReporter(Syntax.NumberType, p => Double.box(p.yMax))
class _plotpenexists extends PlotReporter(Syntax.BooleanType,Syntax.StringType){
  def report(p: Plot, c: Context) =
    Boolean.box(p.getPen(argEvalString(c,0)).isDefined)
}

//
// plot pen prims
//

final class _plotpendown extends ReallySimpleCurrentPlotCommand(_.currentPenOrBust.isDown = true)
final class _plotpenup extends ReallySimpleCurrentPlotCommand(_.currentPenOrBust.isDown = false)
final class _plotpenshow extends ReallySimpleCurrentPlotCommand(_.currentPenOrBust.hidden = false)
final class _plotpenhide extends ReallySimpleCurrentPlotCommand(_.currentPenOrBust.hidden = true)

final class _plotpenreset extends ReallySimpleCurrentPlotCommand( plot => {
  plot.currentPenOrBust.hardReset()
  plot.currentPenOrBust.plotListenerReset(true)
  plot.makeDirty()
})

final class _setplotpeninterval extends CurrentPlotCommand(Syntax.NumberType) {
  def perform(p: Plot, c: Context) { p.currentPenOrBust.interval = argEvalDoubleValue(c, 0) }
}

final class _setplotpenmode extends CurrentPlotCommand(Syntax.NumberType) {
  def perform(p: Plot, c: Context) {
    val mode = argEvalIntValue(c, 0)
    if (mode < PlotPenInterface.MinMode || mode > PlotPenInterface.MaxMode) {
      throw new EngineException(c, this, mode + " is not a valid plot pen mode (valid modes are 0, 1, and 2)")
    }
    p.currentPenOrBust.mode = mode
  }
}

final class _setplotpencolor extends CurrentPlotCommand(Syntax.NumberType) {
  def perform(p: Plot, c: Context) {
    p.currentPenOrBust.color = Color.getARGBbyPremodulatedColorNumber(Color.modulateDouble(argEvalDoubleValue(c, 0)))
  }
}

final class _setcurrentplotpen extends CurrentPlotCommand(Syntax.StringType) {
  def perform(p: Plot, c: Context) {
    val penName = argEvalString(c, 0)
    p.currentPen = p.getPen(penName).getOrElse(
      throw new EngineException(
        c, this, "There is no pen named \"" + penName + "\" in the current plot"))
  }
}
