// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.plot

import org.nlogo.api
import api.Syntax
import org.nlogo.nvm.{ Command, Context, EngineException, Instruction, Reporter }
import org.nlogo.plot
import plot.{ PlotManager, PlotAction }

//
// base classes
//

trait Helpers extends Instruction {
  def plotManager =
    workspace.plotManager.asInstanceOf[PlotManager]
  def currentPlot(context: Context) =
    plotManager.currentPlot.getOrElse(
      throw new EngineException(
        context, this,
        api.I18N.errors.get("org.nlogo.plot.noPlotSelected")))
  def currentPen(context: Context) = {
    val plot = currentPlot(context)
    plot.currentPen.getOrElse(
      throw new EngineException(
        context, this, "Plot '" + plot.name + "' has no pens!"))
  }
}

abstract class PlotCommand(args: Int*)
extends Command with Helpers {
  override def syntax =
    Syntax.commandSyntax(args.toArray)
}

abstract class PlotActionCommand(args: Int*)
extends PlotCommand(args: _*) {
  override def syntax =
    Syntax.commandSyntax(args.toArray)
  override def perform(context: Context) {
    plotManager.publish(action(context))
    context.ip = next
  }
  def action(context: Context): PlotAction
}

abstract class PlotReporter(returnType: Int, args: Int*)
extends Reporter with Helpers {
  override def syntax =
    Syntax.reporterSyntax(args.toArray, returnType)
}

//
// commands requiring only the plot manager (it's ok if there are no plots)
//

class _clearallplots extends PlotCommand() {
  override def perform(context: Context) {
    for (plot <- plotManager.plots)
      plotManager.publish(PlotAction.ClearPlot(plot.name))
    context.ip = next
  }
}

class _setupplots extends PlotCommand() {
  override def callsOtherCode = true
  override def perform(context: Context) {
    workspace.setupPlots(context)
    context.ip = next
  }
}
class _updateplots extends PlotCommand() {
  override def callsOtherCode = true
  override def perform(context: Context) {
    workspace.updatePlots(context)
    context.ip = next
  }
}
class _setcurrentplot extends PlotCommand(Syntax.StringType) {
  override def perform(context: Context) {
    val name = argEvalString(context, 0)
    val plot = plotManager.getPlot(name)
    if (plot.isEmpty)
      throw new EngineException(context, this,
        "no such plot: \"" + name + "\"")
    plotManager.currentPlot = plot
    context.ip = next
  }
}

//
// commands requiring that there be a current plot.
//

class _clearplot extends PlotActionCommand() {
  override def action(context: Context) =
    PlotAction.ClearPlot(currentPlot(context).name)
}

class _autoplotoff extends PlotActionCommand() {
  override def action(context: Context) =
    PlotAction.AutoPlot(currentPlot(context).name, on = false)
}
class _autoploton extends PlotActionCommand() {
  override def action(context: Context) =
    PlotAction.AutoPlot(currentPlot(context).name, on = true)
}

class SetPlotRangeCommand(isX: Boolean) extends PlotActionCommand(Syntax.NumberType, Syntax.NumberType) {
  override def action(context: Context) = {
    val min = argEvalDoubleValue(context, 0)
    val max = argEvalDoubleValue(context, 1)
    if (min >= max)
      throw new EngineException(
        context, this,
        "the minimum must be less than the maximum, but " + min +
        " is greater than or equal to " + max)
    PlotAction.SetRange(
      plotName = currentPlot(context).name,
      isX = isX, min = min, max = max)
  }
}
class _setplotxrange extends SetPlotRangeCommand(isX = true)
class _setplotyrange extends SetPlotRangeCommand(isX = false)

class _createtemporaryplotpen extends PlotActionCommand(Syntax.StringType) {
  override def action(context: Context) =
    PlotAction.CreateTemporaryPen(
      currentPlot(context).name, argEvalString(context, 0))
}

class _exportplot extends PlotCommand(Syntax.StringType, Syntax.StringType) {
  override def perform(context: Context) {
    val name = argEvalString(context, 0)
    val path = argEvalString(context, 1)
    if (plotManager.getPlot(name).isEmpty) {
      throw new EngineException(context, this, "no such plot: \"" + name + "\"")
    }
    // Workspace.waitFor() switches to the event thread if we're running with a GUI - ST 12/17/04
    workspace.waitFor(new api.CommandRunnable {
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
class _exportplots extends PlotCommand(Syntax.StringType) {
  override def perform(context: Context) {
    val path = argEvalString(context, 0)
    if (plotManager.getPlotNames.length == 0)
      throw new EngineException(context, this, "there are no plots to export")
    // Workspace.waitFor() switches to the event thread if we're running with a GUI - ST 12/17/04
    workspace.waitFor(new api.CommandRunnable {
      def run() {
        try workspace.exportAllPlots(workspace.fileManager.attachPrefix(path))
        catch {
          case ex: java.io.IOException =>
            throw new EngineException(context, _exportplots.this,
              token.name + ": " + ex.getMessage)
        }
      }
    })
    context.ip = next
  }
}

//
// reporters
//

class _autoplot extends PlotReporter(Syntax.BooleanType) {
  override def report(context: Context) =
    Boolean.box(currentPlot(context).state.autoPlotOn)
}
class _plotname extends PlotReporter(Syntax.StringType) {
  override def report(context: Context) =
    currentPlot(context).name
}
class _plotxmin extends PlotReporter(Syntax.NumberType) {
  override def report(context: Context) =
    Double.box(currentPlot(context).state.xMin)
}
class _plotxmax extends PlotReporter(Syntax.NumberType) {
  override def report(context: Context) =
    Double.box(currentPlot(context).state.xMax)
}
class _plotymin extends PlotReporter(Syntax.NumberType) {
  override def report(context: Context) =
    Double.box(currentPlot(context).state.yMin)
}
class _plotymax extends PlotReporter(Syntax.NumberType) {
  override def report(context: Context) =
    Double.box(currentPlot(context).state.yMax)
}
class _plotpenexists extends PlotReporter(Syntax.BooleanType, Syntax.StringType) {
  override def report(context: Context) =
    Boolean.box(currentPlot(context).getPen(argEvalString(context, 0)).isDefined)
}

//
// plot pen prims
//

class _plot extends PlotActionCommand(Syntax.NumberType) {
  override def action(context: Context) =
    PlotAction.PlotY(
      currentPlot(context).name,
      currentPen(context).name,
      argEvalDoubleValue(context, 0))
}

class _plotxy extends PlotActionCommand(Syntax.NumberType, Syntax.NumberType) {
  override def action(context: Context) =
    PlotAction.PlotXY(
      currentPlot(context).name,
      currentPen(context).name,
      argEvalDoubleValue(context, 0),
      argEvalDoubleValue(context, 1))
}

class _histogram extends PlotActionCommand(Syntax.ListType) {
  override def action(context: Context) = {
    val list = argEvalList(context, 0)
    val pen = currentPen(context)
    if(pen.state.interval <= 0)
      throw new EngineException(context, this,
        "You cannot histogram with a plot-pen-interval of " + api.Dump.number(pen.state.interval) + ".")
    val values = list.scalaIterator.collect{
        case d: java.lang.Double =>
          d.doubleValue
      }.toSeq
    PlotAction.Histogram(
      currentPlot(context).name, pen.name, values)
  }
}

class _sethistogramnumbars extends PlotActionCommand(Syntax.NumberType) {
  override def action(context: Context) = {
    val numBars = argEvalIntValue(context, 0)
    if (numBars < 1)
      throw new EngineException(context, this,
        "You cannot make a histogram with " + numBars + " bars.")
    val plot = currentPlot(context)
    val pen = currentPen(context)
    val newInterval = (plot.state.xMax - plot.state.xMin) / numBars
    PlotAction.SetPenInterval(
      plot.name, pen.name, newInterval)
  }
}
class _setplotpeninterval extends PlotActionCommand(Syntax.NumberType) {
  override def action(context: Context) =
    PlotAction.SetPenInterval(
      currentPlot(context).name,
      currentPen(context).name,
      argEvalDoubleValue(context, 0))
}

class _plotpendown extends PlotActionCommand() {
  override def action(context: Context) =
    PlotAction.PenDown(
      currentPlot(context).name,
      currentPen(context).name,
      down = true)
}
class _plotpenup extends PlotActionCommand() {
  override def action(context: Context) =
    PlotAction.PenDown(
      currentPlot(context).name,
      currentPen(context).name,
      down = false)
}
class _plotpenshow extends PlotActionCommand() {
  override def action(context: Context) =
    PlotAction.HidePen(
      currentPlot(context).name,
      currentPen(context).name,
      hidden = false)
}
class _plotpenhide extends PlotActionCommand() {
  override def action(context: Context) =
    PlotAction.HidePen(
      currentPlot(context).name,
      currentPen(context).name,
      hidden = true)
}
class _plotpenreset extends PlotActionCommand() {
  override def action(context: Context) =
    PlotAction.ResetPen(
      currentPlot(context).name,
      currentPen(context).name)
}

class _setplotpenmode extends PlotActionCommand(Syntax.NumberType) {
  override def action(context: Context) = {
    val mode = argEvalIntValue(context, 0)
    if (mode < api.PlotPenInterface.MinMode || mode > api.PlotPenInterface.MaxMode) {
      throw new EngineException(context, this,
        mode + " is not a valid plot pen mode (valid modes are 0, 1, and 2)")
    }
    val plotName = currentPlot(context).name
    val penName = currentPen(context).name
    PlotAction.SetPenMode(plotName, penName, mode)
  }
}

class _setplotpencolor extends PlotActionCommand(Syntax.NumberType) {
  override def action(context: Context) = {
    val color =
      api.Color.getARGBbyPremodulatedColorNumber(
        api.Color.modulateDouble(argEvalDoubleValue(context, 0)))
    val plotName = currentPlot(context).name
    val penName = currentPen(context).name
    PlotAction.SetPenColor(plotName, penName, color)
  }
}

class _setcurrentplotpen extends PlotCommand(Syntax.StringType) {
  override def perform(context: Context) {
    val penName = argEvalString(context, 0)
    val plot = currentPlot(context)
    plot.currentPen = plot.getPen(penName).getOrElse(
      throw new EngineException(
        context, this, "There is no pen named \"" + penName + "\" in the current plot"))
    context.ip = next
  }
}
