// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.net.URI
import java.nio.file.{ Files, Paths }

import org.nlogo.core.WorldDimensions
import org.nlogo.api.{ APIVersion, TwoDVersion, Version }
import org.nlogo.nvm.LabInterface.Settings
import org.nlogo.fileformat

object Main {
  def main(args: Array[String]) {
    setHeadlessProperty()
    parseArgs(args).foreach(runExperiment)
  }

  def runExperiment(settings: Settings) {
    def newWorkspace = {
      val w = HeadlessWorkspace.newInstance(settings.version.is3D)
      w.open(settings.modelPath)
      w
    }
    val openWs = newWorkspace
    val proto = try {
      BehaviorSpaceCoordinator.selectProtocol(settings, openWs)
    } finally {
      openWs.dispose()
    }
    proto match {
      case Some((protocol, model, conversionPath)) =>
        conversionPath.foreach { p =>
          System.err.println(
            s"""|The Behaviorspace setup-file format has changed.
                |NetLogo has converted the provided setup file and saved a copy at: ${p}
                |Please update your existing file with this copy.
                |Continuing Behaviorspace run....""".stripMargin)
        }
        val lab = HeadlessWorkspace.newLab(Version.is3D(model.version))
        lab.run(settings, protocol, newWorkspace _)
      case None =>
        throw new IllegalArgumentException("Invalid run, specify experiment name or setup file")
    }
  }
  def setHeadlessProperty() {
    // force headless mode if it is not set.  This is necessary for the headless workspace to run
    // on most platforms when a display is not available. --CLB
    // note that since our check is for null, so the user can still force the property to false and
    // not be overridden by this - ST 4/21/05
    val p = "java.awt.headless"
    if(System.getProperty(p) == null)
      System.setProperty(p, "true")
  }
  private def parseArgs(args: Array[String]): Option[Settings] = {
    var model: Option[URI] = None
    var modelPathString: Option[String] = None
    var minPxcor: Option[String] = None
    var maxPxcor: Option[String] = None
    var minPycor: Option[String] = None
    var maxPycor: Option[String] = None
    var setupFile: Option[URI] = None
    var experiment: Option[String] = None
    var tableWriter: Option[java.io.PrintWriter] = None
    var spreadsheetWriter: Option[java.io.PrintWriter] = None
    var threads = Runtime.getRuntime.availableProcessors
    var suppressErrors = false
    val it = args.iterator
    def die(msg: String) { System.err.println(msg); System.exit(1) }
    def path2writer(path: String) =
      if(path == "-")
        new java.io.PrintWriter(System.out) {
          // don't close System.out - ST 6/9/09
          override def close() { } }
      else
        new java.io.PrintWriter(new java.io.FileWriter(path.trim))
    while(it.hasNext) {
      val arg = it.next()
      def requireHasNext() {
        if (!it.hasNext)
          die("missing argument after " + arg)
      }
      if(arg == "--version")
        { println(TwoDVersion.version); return None }
      else if(arg == "--extension-api-version")
        { println(APIVersion.version); return None }
      else if(arg == "--builddate")
        { println(Version.buildDate); return None }
      else if(arg == "--fullversion")
        { println(Version.fullVersion); return None }
      else if(arg == "--model")
        { requireHasNext()
          val modelString = it.next()
          model = Some(Paths.get(modelString).toUri)
          modelPathString = Some(modelString)
        }
      else if(arg == "--min-pxcor")
        { requireHasNext(); minPxcor = Some(it.next()) }
      else if(arg == "--max-pxcor")
        { requireHasNext(); maxPxcor = Some(it.next()) }
      else if(arg == "--min-pycor")
        { requireHasNext(); minPycor = Some(it.next()) }
      else if(arg == "--max-pycor")
        { requireHasNext(); maxPycor = Some(it.next()) }
      else if(arg == "--setup-file")
        { requireHasNext(); setupFile = Some(new java.io.File(it.next()).toPath.toUri) }
      else if(arg == "--experiment")
        { requireHasNext(); experiment = Some(it.next()) }
      else if(arg == "--table")
        { requireHasNext(); tableWriter = Some(path2writer(it.next())) }
      else if(arg == "--spreadsheet")
        { requireHasNext(); spreadsheetWriter = Some(path2writer(it.next())) }
      else if(arg == "--threads")
        { requireHasNext(); threads = it.next().toInt }
      else if(arg == "--suppress-errors")
        { suppressErrors = true }
      else
        die("unknown argument: " + arg)
    }
    if(model == None)
      die("you must specify --model")
    if(setupFile == None && experiment == None)
      die("you must specify either --setup-file or --experiment (or both)")
    val dimStrings = List(minPxcor, maxPxcor, minPycor, maxPycor)
    if(dimStrings.exists(_.isDefined) && dimStrings.exists(!_.isDefined))
      die("if any of min/max-px/ycor are specified, all four must be specified")
    val dims =
      if(dimStrings.forall(!_.isDefined))
        None
      else
        Some(new WorldDimensions(minPxcor.get.toInt, maxPxcor.get.toInt,
                                 minPycor.get.toInt, maxPycor.get.toInt))
    if (! Files.exists(Paths.get(model.get)))
      die(s"Could not find model file at ${Paths.get(model.get)}, please check the path and try again")
    val version = fileformat.modelVersionAtPath(modelPathString.get)
    if (version.isEmpty)
      die(s"Unable to detect model version at path ${model.get}")
    Some(new Settings(modelPathString.get, model.get, experiment, setupFile, tableWriter,
      spreadsheetWriter, dims, threads, suppressErrors, version.get))
  }
}
