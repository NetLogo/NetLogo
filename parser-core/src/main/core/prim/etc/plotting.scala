// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core
package prim.etc

//
// commands
//

abstract class PlotCommand(args: Int*) extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = args.toList)
}

case class _clearallplots() extends PlotCommand()
case class _setupplots() extends PlotCommand()
case class _updateplots() extends PlotCommand()
case class _setcurrentplot() extends PlotCommand(Syntax.StringType)
case class _clearplot() extends PlotCommand()
case class _autoplotoff() extends PlotCommand()
case class _autoploton() extends PlotCommand()

class SetPlotRangeCommand extends PlotCommand(Syntax.NumberType, Syntax.NumberType)
case class _setplotxrange() extends SetPlotRangeCommand
case class _setplotyrange() extends SetPlotRangeCommand

case class _createtemporaryplotpen() extends PlotCommand(Syntax.StringType)
case class _exportplot() extends PlotCommand(Syntax.StringType, Syntax.StringType)
case class _exportplots() extends PlotCommand(Syntax.StringType)

case class _plot() extends PlotCommand(Syntax.NumberType)
case class _plotxy() extends PlotCommand(Syntax.NumberType, Syntax.NumberType)
case class _histogram() extends PlotCommand(Syntax.ListType)
case class _sethistogramnumbars() extends PlotCommand(Syntax.NumberType)
case class _setplotpeninterval() extends PlotCommand(Syntax.NumberType)
case class _plotpendown() extends PlotCommand()
case class _plotpenup() extends PlotCommand()
case class _plotpenshow() extends PlotCommand()
case class _plotpenhide() extends PlotCommand()
case class _plotpenreset() extends PlotCommand()
case class _setplotpenmode() extends PlotCommand(Syntax.NumberType)
case class _setplotpencolor() extends PlotCommand(Syntax.NumberType)
case class _setcurrentplotpen() extends PlotCommand(Syntax.StringType)

//
// reporters
//

abstract class PlotReporter(returnType: Int, args: Int*) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = args.toList,
      ret = returnType)
}

case class _autoplot() extends PlotReporter(Syntax.BooleanType)
case class _plotname() extends PlotReporter(Syntax.StringType)
case class _plotxmin() extends PlotReporter(Syntax.NumberType)
case class _plotxmax() extends PlotReporter(Syntax.NumberType)
case class _plotymin() extends PlotReporter(Syntax.NumberType)
case class _plotymax() extends PlotReporter(Syntax.NumberType)
case class _plotpenexists() extends PlotReporter(Syntax.BooleanType, Syntax.StringType)
