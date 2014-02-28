// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import scala.collection.mutable
import org.nlogo.api.{CompilerException, LogoThunkFactory, CommandLogoThunk}
import org.nlogo.api.{ PlotAction, ActionBroker }

// handles compilation and execution of plot code
// among a couple of other little tasks.
class PlotManager(factory: LogoThunkFactory)
  extends PlotManagerInterface
  with PlotActionRunner
  with ActionBroker[PlotAction] {

  override val runner = this // we act as both broker and runner

  // all the plots in the model
  private val _plots = mutable.Buffer[Plot]()
  def plots = _plots.toList

  // the currently selected plot.
  // needed for backwards comp with pre 5.0 plotting style.
  var currentPlot: Option[Plot] = None
  override def setCurrentPlot(name: String) {
    currentPlot = getPlot(name)
  }

  // plot creation
  def newPlot(name:String) = {
    val plot = new Plot(name)
    // we compile the plot before adding it to the _plots list
    // because if the runCode method sees it in there, it will expect
    // there to be compilation results available in the thunk maps.
    // newPlot will be called from the EventThread
    // (when the UI is coming up, or when a user adds a new plot)
    // and runCode will be called from the JobThread
    // (executing setup-plots, update-plots, tick, or reset-ticks)
    // JC - 3/22/11
    compilePlot(plot)
    _plots += plot
    currentPlot = Some(plot)
    plot
  }

  def hasPlot(name: String): Boolean = getPlot(name).isDefined
  def getPlot(name: String) = _plots.find(_.name.equalsIgnoreCase(name))

  def getPlotPen(plotName: String, penName: String) =
    getPlot(plotName).flatMap(_.getPen(penName))

  // used for letting the user choose which plot to export
  def getPlotNames = _plots.map(_.name)
  def nextName = Stream.from(1).map("plot " + _).find(getPlot(_).isEmpty).get

  def forgetPlot(goner: Plot) {
    if (currentPlot == Some(goner)) currentPlot = None
    _plots -= goner
  }
  def forgetAll() {
    _plots.clear()
    currentPlot = None
    // not strictly necessary to clear the thunk maps since the references
    // are weak, but might as well help the GC (and the help human porer-over of heap
    // dumps) - ST 1/18/13
    penThunks.clear()
    plotThunks.clear()
  }
  def clearAll() {
    _plots.foreach(_.clear())
  }

  //
  // code to compile code in plots
  //

  // the two maps below hold the compilation results for the plots and pens
  // a Results holds both the setup and update results.
  // the results could be a Thunk, or a CompilerException.
  type CompilationResult = Either[CompilerException,CommandLogoThunk]
  case class Results(setup: CompilationResult, update: CompilationResult)
  private val plotThunks = new mutable.WeakHashMap[Plot, Results]()
  private val penThunks = new mutable.WeakHashMap[PlotPen, Results]()

  // when plot code fails to compile in headless mode, we need to throw an exception
  // so that the model doesn't load. the user can then take action to fix their model.
  // in window mode, we hold the errors so that we can display them in the plot widget.
  // throwing them would be counter-productive. below, we return any exceptions that happen.
  // headless is free to then throw them as it pleases. -JC 5/5/10
  def compileAllPlots(): List[CompilerException] = { plots.flatMap(compilePlot) }
  def compilePlot(plot: Plot): List[CompilerException] = {

    // compile the given code, and return Right if ok, Left if bad.
    def compile(code: String, procName: String) =
      try Right(factory.makeCommandThunk(code, procName))
      catch { case ce: CompilerException => Left(ce) }

    def procName(setup:Boolean, pen:Option[PlotPen] = None) = {
      "plot '" + plot.name + "' " + (pen.map("pen '" + _.name + "' ").getOrElse("")) +
              (if(setup) "setup code" else "update code")
    }

    // compile the code in the plot
    val plotResults =
      Results(compile(plot.setupCode, procName(setup=true)),compile(plot.updateCode, procName(setup=false)))
    // store the results
    plotThunks(plot) = plotResults
    // compile all the pens in the plot (unless they are temporary)
    val plotPensResults = for(pen <- plot.pens; if(!pen.temporary)) yield
      (pen, Results(compile(pen.setupCode,procName(setup=true, pen=Some(pen))),
                    compile(pen.updateCode, procName(setup=false, pen=Some(pen)))))
    // store those results
    penThunks ++= plotPensResults

    // finally, gather up any compilation exceptions
    val results: List[Results] = plotResults :: plotPensResults.map(_._2).toList
    val errors: List[CompilerException] = results.flatMap(r => List(r.setup, r.update)).flatMap(_.left.toOption)
    errors
  }

  //
  // code to run code in plots
  //

  def setupPlots() { runCode(Setup) }
  def updatePlots() { runCode(Update) }

  private def runCode(codeType: CodeType) {
    // save the currently selected plot
    val oldCurrentPlot = currentPlot
    for (plot <- _plots) {
      currentPlot = Some(plot)
      // run the plot code (and pens), only if it was compiled successfully.
      // this line below runs the code if there is any to run, and it tells
      // us if stop was called from the code. if so, we dont run the pens code.
      val stopped = codeType.call(plotThunks(plot))
      if(! stopped){
        // save the currently selected pen
        val oldCurrentPen = plot.currentPen
        // run all the pen code for this plot
        // (again, only if it was compiled successfully.)

        // there is one important thing going on here that needs explaining.
        // a user can modify the plots pens in the UI while runCode is happening.
        // (for example, the go button is down, and a user adds another pen)
        // when that happens, the new pens won't yet be compiled
        // because that unfortunately happens a little bit later
        // therefore, the new pens won't yet be in the penThunks map.
        // to avoid a key not found exception, we call get on the map instead of apply.
        // if the pen isn't in the map, then the code in the for loop here wont run.
        // i think this is a bit awkward. it feels like we should always
        // have the most up to date penThunks map.
        // however, the only other option is to entirely synchronize access to the pens,
        // but that would have required modifications to several places and would have
        // been more error prone than this approach.
        // JC - 3/22/11
        for(pp <- plot.pens; if(!pp.temporary); results <- penThunks.get(pp)) {
          plot.currentPen=pp
          codeType.call(results)
        }
        // restore the currently selected pen
        plot.currentPen=oldCurrentPen
      }
    }
    // restore the currently selected plot
    currentPlot = oldCurrentPlot
  }

  sealed abstract trait CodeType {
    def selector(r:Results): CompilationResult
    def call(r:Results): Boolean = selector(r).right.toOption.map(_.call).getOrElse(false)
  }
  object Update extends CodeType {
    def selector(r:Results) = r.update
  }
  object Setup extends CodeType {
    def selector(r:Results) = r.setup
  }

  def hasErrors(plot:Plot): Boolean = {
    getPlotSetupError(plot).isDefined || getPlotUpdateError(plot).isDefined ||
    plot.pens.filterNot(_.temporary).exists(hasErrors)
  }
  def getPlotSetupError(plot:Plot) = plotThunks(plot).setup.left.toOption
  def getPlotUpdateError(plot:Plot) = plotThunks(plot).update.left.toOption
  def hasErrors(pen:PlotPen): Boolean = {
    getPenSetupError(pen).isDefined || getPenUpdateError(pen).isDefined
  }
  def getPenSetupError(pen: PlotPen) = penThunks(pen).setup.left.toOption
  def getPenUpdateError(pen: PlotPen) = penThunks(pen).update.left.toOption
}
