// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.io.File

import org.nlogo.api.{ APIVersion, ExportPlotWarningAction, LabDefaultValues, LabProtocol, PlotCompilationErrorAction,
                       Version }
import org.nlogo.core.WorldDimensions
import org.nlogo.nvm.DummyPrimaryWorkspace
import org.nlogo.nvm.LabInterface.{ Settings, Worker }

object Main {
  // *TODO*: Get this I18N'd and also a way to keep it in sync with the BehaviorSpace docs.
  private val HelpString = """
Run NetLogo using the NetLogo_Console app with the --headless command line argument.  The NetLogo_Console script supports the following arguments:

* --headless: Enable headless mode to run a BehaviorSpace experiment (required, will open the graphical interface otherwise).
* --model <path>: pathname of model to open (required)
* --setup-file <path>: read experiment setups from this file instead of the model file
* --experiment <name>: name of experiment to run
* --table <path>: pathname to send table output to (or - for standard output)
* --spreadsheet <path>: pathname to send spreadsheet output to (or - for standard output)
* --lists <path>: pathname to send lists output to (or - for standard output), cannot be used without --table or --spreadsheet
* --stats <path>: pathname to send statistics output to (or - for standard output)
* --threads <number>: use this many threads to do model runs in parallel, or 1 to disable parallel runs. defaults to floor( .75 * number of processors).
* --update-plots: enable plot updates. Include this if you want to export plot data, or exclude it for better performance.
* --skip <number>: skip the specified number of runs instead of starting at the beginning of the experiment
* --min-pxcor <number>: override world size setting in model file
* --max-pxcor <number>: override world size setting in model file
* --min-pycor <number>: override world size setting in model file
* --max-pycor <number>: override world size setting in model file

The --model flag is required. If you don't specify --experiment, you must specify --setup-file. By default no results are generated, so you'll usually want to specify either --table or --spreadsheet, or both. If you specify any of the world dimensions, you must specify all four.

Here is an example of running an experiment already defined and saved within a model (path separators and line-continue slashes are for macOS or Linux, Windows would be different):

./NetLogo_Console --headless \
  --model "models/IABM Textbook/chapter 4/Wolf Sheep Simple 5.nlogo" \
  --experiment "Wolf Sheep Simple model analysis" \
  --table -

See the Advanced Usage section of the BehaviorSpace documentation in the NetLogo User Manual for more information and examples.
"""

  def main(args: Array[String]): Unit = {
    setHeadlessProperty()
    runExperiment(parseArgs(args))
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
        runExperimentWithProtocol(settings, protocol, _ => {})
        finish()

      case None =>
        throw new IllegalArgumentException("Invalid run, specify experiment name or setup file")
    }
  }

  // used in bspace extension
  def runExperimentWithProtocol(settings: Settings, protocol: LabProtocol, assignWorker: Worker => Unit): Unit = {
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

    val worker = lab.newWorker(protocol.copy(runsCompleted = protocol.runsCompleted.max(settings.runsCompleted)))

    assignWorker(worker)

    lab.run(settings, worker, new DummyPrimaryWorkspace, () => newWorkspace)

    settings.ipcHandler.foreach(_.close())
  }

  def setHeadlessProperty(): Unit = {
    // force headless mode if it is not set.  This is necessary for the headless workspace to run
    // on most platforms when a display is not available. --CLB
    // note that since our check is for null, so the user can still force the property to false and
    // not be overridden by this - ST 4/21/05
    val p = "java.awt.headless"
    if (System.getProperty(p) == null) {
      System.setProperty(p, "true")
    }
  }

  private def parseArgs(args: Array[String]): Settings = {
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
    var threads =  LabDefaultValues.getDefaultThreads
    var suppressErrors = false
    var updatePlots = false
    var runsCompleted = 0

    def die(msg: String): Unit = {
      System.err.println(msg)
      System.exit(1)
    }

    def printAndExit(message: String): Unit = {
      println(message)

      System.exit(0)
    }

    val it = args.iterator

    while (it.hasNext) {
      val arg = it.next().toLowerCase

      def nextOrDie(): String = {
        if (!it.hasNext) {
          die("missing argument after " + arg)

          ""
        } else {
          it.next()
        }
      }

      arg match {
        case "--headless" => // noop to avoid tripping unknown argument error
        case "--help" => printAndExit(HelpString)
        case "--version" => printAndExit(Version.version)
        case "--extension-api-version" => printAndExit(APIVersion.version)
        case "--builddate" => printAndExit(Version.buildDate)
        case "--fullversion" => printAndExit(Version.fullVersion)
        case "--3d" => Version.set3D(true)
        case "--model" => model = Some(nextOrDie())
        case "--min-pxcor" => minPxcor = Some(nextOrDie())
        case "--max-pxcor" => maxPxcor = Some(nextOrDie())
        case "--min-pycor" => minPycor = Some(nextOrDie())
        case "--max-pycor" => maxPycor = Some(nextOrDie())
        case "--setup-file" => setupFile = Some(new File(nextOrDie()))
        case "--experiment" => experiment = Some(nextOrDie())
        case "--table" => table = Option(nextOrDie())
        case "--spreadsheet" => spreadsheet = Option(nextOrDie())
        case "--lists" => lists = Option(nextOrDie())
        case "--stats" => stats = Option(nextOrDie())
        case "--threads" => threads = nextOrDie().toInt
        case "--suppress-errors" => suppressErrors = true
        case "--update-plots" => updatePlots = true
        case "--skip" => runsCompleted = nextOrDie().toInt
        case _ => die("unknown argument: " + arg)
      }
    }

    if (model.isEmpty)
      die("You must specify --model.  Try --help for more information.")

    if (setupFile.isEmpty && experiment.isEmpty)
      die("You must specify either --setup-file or --experiment (or both).  Try --help for more information.")

    if (lists.isDefined && table.isEmpty && spreadsheet.isEmpty)
      die("You cannot specify --lists without also specifying --table or --spreadsheet. Try --help for more information.")

    val dimStrings = Seq(minPxcor, maxPxcor, minPycor, maxPycor)

    if (dimStrings.exists(_.isDefined) && dimStrings.exists(!_.isDefined))
      die("If any of min/max-px/ycor are specified, all four must be specified.  Try --help for more information.")

    if (stats.isDefined && (table.isEmpty && spreadsheet.isEmpty))
      die("You cannot specify --stats without also specifying --table or --spreadsheet. Try --help for more information.")

    val dims = if (dimStrings.exists(!_.isDefined)) {
      None
    } else {
      Some(new WorldDimensions(minPxcor.get.toInt, maxPxcor.get.toInt,
                               minPycor.get.toInt, maxPycor.get.toInt))
    }

    Settings(model.get, experiment, setupFile, table, spreadsheet, stats, lists, dims, threads, suppressErrors,
             updatePlots, false, runsCompleted, None)
  }
}
