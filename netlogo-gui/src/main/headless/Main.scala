// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.io.{ File, FileWriter, PrintWriter }

import org.nlogo.core.WorldDimensions
import org.nlogo.api.{ APIVersion, ExportPlotWarningAction, LabDefaultValues, LabProtocol, PlotCompilationErrorAction,
                       Version }
import org.nlogo.nvm.LabInterface.{ Settings, Worker }

object Main {

  // *TODO*: Get this I18N'd and also a way to keep it in sync with the BehaviorSpace docs.
  private val HELP_STRING = """
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
    parseArgs(args).foreach(runExperiment(_))
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
      BehaviorSpaceCoordinator.selectProtocol(settings, openWs)
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
    lab.run(settings, worker, () => newWorkspace, finish)
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
    System.setProperty("org.nlogo.preferHeadless", "true")
  }

  private def parseArgs(args: Array[String]): Option[Settings] = {
    var model: Option[String] = None
    var minPxcor: Option[String] = None
    var maxPxcor: Option[String] = None
    var minPycor: Option[String] = None
    var maxPycor: Option[String] = None
    var setupFile: Option[File] = None
    var experiment: Option[String] = None
    var tableWriter: Option[PrintWriter] = None
    var spreadsheetWriter: Option[PrintWriter] = None
    var statsWriter: Option[(PrintWriter, String)] = None
    var listsWriter: Option[(PrintWriter, String)] = None
    var threads =  LabDefaultValues.getDefaultThreads
    var suppressErrors = false
    var updatePlots = false
    val it = args.iterator

    def die(msg: String): Unit = {
      System.err.println(msg)
      System.exit(1)
    }

    def path2writer(path: String) =
      if (path == "-") {
        new PrintWriter(System.out) {
          // don't close System.out - ST 6/9/09
          override def close(): Unit = { }
        }
      } else {
        new PrintWriter(new FileWriter(path.trim))
      }

    var outputPath = ""

    while (it.hasNext) {
      val arg = it.next().toLowerCase

      def requireHasNext(): Unit = {
        if (!it.hasNext)
          die("missing argument after " + arg)
      }

      if (arg == "--headless") {
        // noop to avoid tripping unknown argument error

      } else if (arg == "--help") {
        println(Main.HELP_STRING)
        return None

      } else if (arg == "--version") {
        println(Version.version)
        return None

      } else if (arg == "--extension-api-version") {
        println(APIVersion.version)
        return None

      } else if (arg == "--builddate") {
        println(Version.buildDate)
        return None

      } else if (arg == "--fullversion") {
        println(Version.fullVersion)
        return None

      } else if (arg == "--3d") {
        System.setProperty("org.nlogo.is3d", "true")

      } else if (arg == "--model") {
        requireHasNext()
        model = Some(it.next())

      } else if (arg == "--min-pxcor") {
        requireHasNext()
        minPxcor = Some(it.next())

      } else if (arg == "--max-pxcor") {
        requireHasNext()
        maxPxcor = Some(it.next())

      } else if (arg == "--min-pycor") {
        requireHasNext()
        minPycor = Some(it.next())

      } else if (arg == "--max-pycor") {
        requireHasNext()
        maxPycor = Some(it.next())

      } else if (arg == "--setup-file") {
        requireHasNext()
        setupFile = Some(new File(it.next()))

      } else if (arg == "--experiment") {
        requireHasNext()
        experiment = Some(it.next())

      } else if (arg == "--table") {
        requireHasNext()
        outputPath = it.next()
        tableWriter = Some(path2writer(outputPath))

      } else if (arg == "--spreadsheet") {
        requireHasNext()
        val localOut = it.next()
        if (outputPath.isEmpty)
          outputPath = localOut
        spreadsheetWriter = Some(path2writer(localOut))

      } else if (arg == "--lists") {
        requireHasNext()
        listsWriter = Some((path2writer(it.next()), outputPath))

      } else if (arg == "--stats") {
        requireHasNext()
        statsWriter = Some((path2writer(it.next()), outputPath))
      }

      else if (arg == "--threads") {
        requireHasNext()
        threads = it.next().toInt

      } else if (arg == "--suppress-errors") {
        suppressErrors = true

      } else if (arg == "--update-plots") {
        updatePlots = true
      } else {
        die("unknown argument: " + arg)
      }
    }

    if (model == None) {
      die("You must specify --model.  Try --help for more information.")
    }

    if (setupFile == None && experiment == None) {
      die("You must specify either --setup-file or --experiment (or both).  Try --help for more information.")
    }

    if (listsWriter != None && tableWriter == None && spreadsheetWriter == None) {
      die("You cannot specify --lists without also specifying --table or --spreadsheet. Try --help for more information.")
    }

    val dimStrings = List(minPxcor, maxPxcor, minPycor, maxPycor)
    if (dimStrings.exists(_.isDefined) && dimStrings.exists(!_.isDefined)) {
      die("If any of min/max-px/ycor are specified, all four must be specified.  Try --help for more information.")
    }

    if (statsWriter != None && (tableWriter == None && spreadsheetWriter == None)) {
      die("You cannot specify --stats without also specifying --table or --spreadsheet. Try --help for more information.")
    }

    val dims = if (dimStrings.forall(!_.isDefined)) {
      None
    } else {
      Some(new WorldDimensions(
        minPxcor.get.toInt
      , maxPxcor.get.toInt
      , minPycor.get.toInt
      , maxPycor.get.toInt
      ))
    }

    Some(new Settings(
      model.get
    , experiment
    , setupFile
    , tableWriter
    , spreadsheetWriter
    , statsWriter
    , listsWriter
    , dims
    , threads
    , suppressErrors
    , updatePlots
    , None // mainWorkspace
    ))
  }
}
