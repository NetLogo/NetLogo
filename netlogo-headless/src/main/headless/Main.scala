// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.io.File

import org.nlogo.core.WorldDimensions
import org.nlogo.api.{ APIVersion, ExportPlotWarningAction, LabDefaultValues, LabProtocol, Version }
import org.nlogo.nvm.DummyPrimaryWorkspace
import org.nlogo.nvm.LabInterface.{ Settings, Worker }
import org.nlogo.api.PlotCompilationErrorAction

object Main {
  class CancelException extends RuntimeException

  def main(args: Array[String]): Unit = {
    try {
      setHeadlessProperty()
      parseArgs(args).foreach(runExperiment(_))
    } catch {
      case e: CancelException => // ignore
    }
  }

  def runExperiment(settings: Settings, finish: () => Unit = () => {}): Unit = {
    var plotCompilationErrorAction: PlotCompilationErrorAction = PlotCompilationErrorAction.Output
    var exportPlotWarningAction: ExportPlotWarningAction = ExportPlotWarningAction.Output
    def newWorkspace = {
      val w = HeadlessWorkspace.newInstance
      w.setPlotCompilationErrorAction(plotCompilationErrorAction)
      w.setExportPlotWarningAction(exportPlotWarningAction)
      w.open(settings.modelPath)
      plotCompilationErrorAction = PlotCompilationErrorAction.Ignore
      exportPlotWarningAction = ExportPlotWarningAction.Ignore
      w.setShouldUpdatePlots(settings.updatePlots)
      w
    }
    val openWs = newWorkspace
    val proto = try {
      BehaviorSpaceCoordinator.selectProtocol(settings)
    } finally {
      openWs.dispose()
    }
    proto match {
      case Some(protocol) =>
        runExperimentWithProtocol(settings, protocol, _ => {}, finish)

      case None =>
        throw new IllegalArgumentException("Invalid run, specify experiment name or setup file")
    }
  }

  // used in bspace extension
  def runExperimentWithProtocol(settings: Settings, protocol: LabProtocol, assignWorker: Worker => Unit,
                                finish: () => Unit = () => {}): Unit = {
    var plotCompilationErrorAction: PlotCompilationErrorAction = PlotCompilationErrorAction.Output
    var exportPlotWarningAction: ExportPlotWarningAction = ExportPlotWarningAction.Output
    def newWorkspace = {
      val w = HeadlessWorkspace.newInstance
      w.setPlotCompilationErrorAction(plotCompilationErrorAction)
      w.setExportPlotWarningAction(exportPlotWarningAction)
      w.open(settings.modelPath)
      plotCompilationErrorAction = PlotCompilationErrorAction.Ignore
      exportPlotWarningAction = ExportPlotWarningAction.Ignore
      w.setShouldUpdatePlots(settings.updatePlots)
      w
    }
    val lab = HeadlessWorkspace.newLab
    val worker = lab.newWorker(protocol)
    assignWorker(worker)
    lab.run(settings, worker, new DummyPrimaryWorkspace, () => newWorkspace)
  }

  def setHeadlessProperty(): Unit = {
    // force headless mode if it is not set.  This is necessary for the headless workspace to run
    // on most platforms when a display is not available. --CLB
    // note that since our check is for null, so the user can still force the property to false and
    // not be overridden by this - ST 4/21/05
    if (System.getProperty("java.awt.headless") == null)
      System.setProperty("java.awt.headless", "true")
  }

  private def parseArgs(args: Array[String]): Option[Settings] = {
    var model: Option[String] = None
    var minPxcor: Option[String] = None
    var maxPxcor: Option[String] = None
    var minPycor: Option[String] = None
    var maxPycor: Option[String] = None
    var setupFile: Option[File] = None
    var experiment: Option[String] = None
    var table: Option[String] = None
    var spreadsheet: Option[String] = None
    var stats: Option[String] = None
    var lists: Option[String] = None
    var threads = LabDefaultValues.getDefaultThreads
    var suppressErrors = false
    var updatePlots = false
    val it = args.iterator
    def die(msg: String): Unit = { Console.err.println(msg); throw new CancelException }
    while(it.hasNext) {
      val arg = it.next()
      def requireHasNext(): Unit = {
        if (!it.hasNext)
          die("missing argument after " + arg)
      }
      if(arg == "--version")
        { println(Version.version); return None }
      else if(arg == "--extension-api-version")
        { println(APIVersion.version); return None }
      else if(arg == "--builddate")
        { println(Version.buildDate); return None }
      else if(arg == "--fullversion")
        { println(Version.fullVersion); return None }
      else if(arg == "--model")
        { requireHasNext(); model = Some(it.next()) }
      else if(arg == "--min-pxcor")
        { requireHasNext(); minPxcor = Some(it.next()) }
      else if(arg == "--max-pxcor")
        { requireHasNext(); maxPxcor = Some(it.next()) }
      else if(arg == "--min-pycor")
        { requireHasNext(); minPycor = Some(it.next()) }
      else if(arg == "--max-pycor")
        { requireHasNext(); maxPycor = Some(it.next()) }
      else if(arg == "--setup-file")
        { requireHasNext(); setupFile = Some(new File(it.next())) }
      else if(arg == "--experiment")
        { requireHasNext(); experiment = Some(it.next()) }
      else if(arg == "--table") {
        requireHasNext()
        table = Some(it.next().trim)
      }
      else if(arg == "--spreadsheet") {
        requireHasNext()
        spreadsheet = Some(it.next().trim)
      }
      else if(arg == "--stats")
        { requireHasNext(); stats = Some(it.next().trim) }
      else if(arg == "--lists")
        { requireHasNext(); lists = Some(it.next().trim) }
      else if(arg == "--threads")
        { requireHasNext(); threads = it.next().toInt }
      else if(arg == "--suppress-errors")
        { suppressErrors = true }
      else if (arg == "--update-plots")
        { updatePlots = true }
      else
        die("unknown argument: " + arg)
    }
    if(model == None)
      die("you must specify --model")
    if(setupFile == None && experiment == None)
      die("you must specify either --setup-file or --experiment (or both)")
    if(lists != None && table == None && spreadsheet == None)
      die("you cannot specify --lists without also specifying --table or --spreadsheet")
    val dimStrings = List(minPxcor, maxPxcor, minPycor, maxPycor)
    if(dimStrings.exists(_.isDefined) && dimStrings.exists(!_.isDefined))
      die("if any of min/max-px/ycor are specified, all four must be specified")
    if (stats != None && (table == None && spreadsheet == None)) {
      die("You cannot specify --stats without also specifying --table or --spreadsheet. Try --help for more information.")
    }
    val dims =
      if(dimStrings.forall(!_.isDefined))
        None
      else
        Some(new WorldDimensions(minPxcor.get.toInt, maxPxcor.get.toInt,
                                 minPycor.get.toInt, maxPycor.get.toInt))
    Some(new Settings(model.get, experiment, setupFile, table, spreadsheet, stats, lists, dims, threads,
                      suppressErrors, updatePlots, false, 0))
  }
}
