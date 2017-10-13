// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.nio.file.Paths

// Note that in the Scaladoc we distribute, this class is included, but Workspace and
// AbstractWorkspace are not, so if you want to document a method for everyone, override that method
// here and document it here.  The overriding method can simply call super(). - ST 6/1/05, 7/28/11

import org.nlogo.api.{ ComponentSerialization, Version, RendererInterface, WorldDimensions3D,
  AggregateManagerInterface, FileIO, LogoException, ModelType, NetLogoLegacyDialect,
  NetLogoThreeDDialect, CommandRunnable, Pico, ReporterRunnable, WorldResizer }
import org.nlogo.core.{ CompilerException, Femto, Model, Output, Program, UpdateMode, WorldDimensions }
import org.nlogo.agent.{ CompilationManagement, World, World2D, World3D }
import org.nlogo.nvm.{ CompilerFlags, DefaultCompilerServices, LabInterface, Optimizations, PresentationCompilerInterface }
import org.nlogo.workspace.{ DefaultAbstractWorkspace, HeadlessCatchAll, HubNetManagerFactory, RuntimeError, WorkspaceEvent }
import org.nlogo.fileformat, fileformat.{ NLogoFormat, NLogoXFormat, ScalaXmlElementFactory }

import scala.io.Codec

/**
 * Companion object, and factory object, for the HeadlessWorkspace class.
 */
object HeadlessWorkspace {

  /**
   * Makes a new instance of NetLogo capable of running a model "headless", with no GUI.
   */
  @deprecated("Use HeadlessWorkspace.newInstance(Boolean) instead, specifying whether workspace is3D", "6.1.0")
  def newInstance: HeadlessWorkspace = {
    System.err.println("""|HeadlessWorkspace.newInstance(Class[_ <: HeadlessWorkspace]) is deprecated and may not reflect the 2D/3D state of the current model.
                          |Query the model, world type, or dialect to determine whether NetLogo is 3D""".stripMargin)
    newInstance(classOf[HeadlessWorkspace], Version.is3DInternal)

  }

  def newInstance(is3D: Boolean): HeadlessWorkspace =
    newInstance(classOf[HeadlessWorkspace], is3D)

  @deprecated("Use HeadlessWorkspace.newInstance(Class[_ <: HeadlessWorkspace], Boolean) instead, specifying whether workspace is3D", "6.1.0")
  def newInstance(subclass: Class[_ <: HeadlessWorkspace]): HeadlessWorkspace = {
    System.err.println("""|HeadlessWorkspace.newInstance(Class[_ <: HeadlessWorkspace]) is deprecated and may not reflect the 2D/3D state of the current model.
                          |Query the model, world type, or dialect to determine whether NetLogo is 3D""".stripMargin)
    newInstance(subclass, Version.is3DInternal)
  }

  /**
   * If you derive your own subclass of HeadlessWorkspace, use this method to instantiate it.
   */
  def newInstance(subclass: Class[_ <: HeadlessWorkspace], is3D: Boolean): HeadlessWorkspace = {
    val pico = new Pico
    pico.addComponent(if (is3D) classOf[World3D] else classOf[World2D])
    pico.add("org.nlogo.compile.Compiler")
    if (is3D)
      pico.addScalaObject("org.nlogo.api.NetLogoThreeDDialect")
    else
      pico.addScalaObject("org.nlogo.api.NetLogoLegacyDialect")
    pico.add("org.nlogo.sdm.AggregateManagerLite")
    pico.add("org.nlogo.render.Renderer")
    pico.addComponent(subclass)
    pico.addAdapter(new ModelLoaderComponent())
    pico.add(classOf[HubNetManagerFactory], "org.nlogo.hubnet.server.HeadlessHubNetManagerFactory")
    pico.getComponent(subclass)
  }

  def fromPath(path: String): HeadlessWorkspace = {
    fromPath(classOf[HeadlessWorkspace], path)
  }

  def fromPath(subclass: Class[_ <: HeadlessWorkspace], path: String): HeadlessWorkspace = {
    val version = fileformat.modelVersionAtPath(path)
    version.map { v =>
      val i = newInstance(subclass, v.is3D)
      try {
        i.open(path)
        i
      } catch {
        case e: Exception =>
          i.dispose()
          throw e
      }
    }.getOrElse(throw new Exception(s"The path $path contains no model file or an invalid model file"))
  }

  def newLab(is3D: Boolean): LabInterface = {
    val pico = new Pico
    pico.add("org.nlogo.compile.Compiler")
    if (is3D)
      pico.addScalaObject("org.nlogo.api.NetLogoThreeDDialect")
    else
      pico.addScalaObject("org.nlogo.api.NetLogoLegacyDialect")
    pico.add("org.nlogo.lab.Lab")
    pico.addComponent(classOf[DefaultCompilerServices])
    pico.getComponent(classOf[LabInterface])
  }

  /**
   * Internal use only.
   */
  // batrachomyomachia!
  val TestDeclarations =
    "globals [glob1 glob2 glob3 ]\n" +
    "breed [mice mouse]\n " +
    "breed [frogs frog]\n " +
    "breed [nodes node]\n " +
    "directed-link-breed [directed-edges directed-edge]\n" +
    "undirected-link-breed [undirected-edges undirected-edge]\n" +
    "turtles-own [tvar]\n" +
    "patches-own [pvar]\n" +
    "mice-own [age fur]\n" +
    "frogs-own [age spots]\n" +
    "directed-edges-own [lvar]\n" +
    "undirected-edges-own [weight]\n"

}

/**
 * The primary class for headless (no GUI) operation of NetLogo.
 *
 * You may create more than one HeadlessWorkspace object.  Multiple
 * instances can operate separately and independently.  (Behind the
 * scenes, this is supported by creating a separate thread for each
 * instance.)
 *
 * When you are done using a HeadlessWorkspace, you should call its
 * dispose() method.  This will shut down the thread associated with
 * the workspace and allow resources to be freed.
 *
 * See the "Controlling" section of the NetLogo User Manual
 * for example code.
 *
 * Don't try to use the constructor yourself; use
 * HeadlessWorkspace.newInstance instead.
 */
class HeadlessWorkspace(
  _world: World with CompilationManagement,
  compiler: PresentationCompilerInterface,
  val renderer: RendererInterface,
  val aggregateManager: AggregateManagerInterface,
  hubNetManagerFactory: HubNetManagerFactory)
extends DefaultAbstractWorkspace(_world, compiler, hubNetManagerFactory, Seq(aggregateManager),
  CompilerFlags(optimizations = Optimizations.standardOptimizations))
with org.nlogo.workspace.Controllable
with org.nlogo.api.ViewSettings
with HeadlessCatchAll {
  world.trailDrawer(renderer.trailDrawer)

  /**
   * Has a model been opened in this workspace?
   */
  def modelOpened = _openModel.nonEmpty

  private[this] var _openModel = Option.empty[Model]
  def setOpenModel(model: Model): Unit = { _openModel = Some(model) }

  val outputAreaBuffer = new StringBuilder

  /**
   * If true, don't send anything to standard output.
   */
  var silent = false

  /**
   * Internal use only.
   */
  override def isHeadless = true

  /**
   * Internal use only.
   */
  def waitFor(runnable: CommandRunnable) {
    runnable.run()
  }

  /**
   * Internal use only.
   */
  def waitForResult[T](runnable: ReporterRunnable[T]): T =
    runnable.run()

  /**
   * Internal use only.
   */
  def waitForQueuedEvents() { }

  /**
   * Internal use only.
   */
  def initForTesting(is3D: Boolean, worldSize: Int) {
    initForTesting(is3D, worldSize, "")
  }

  /**
   * Internal use only.
   */
  def initForTesting(is3D: Boolean, worldSize: Int, modelString: String) {
    if (is3D)
      initForTesting(new WorldDimensions3D(
          -worldSize, worldSize, -worldSize, worldSize, -worldSize, worldSize),
          modelString)
    else
      initForTesting(-worldSize, worldSize, -worldSize, worldSize, modelString)
  }

  /**
   * Internal use only.
   */
  def initForTesting(minPxcor: Int, maxPxcor: Int, minPycor: Int, maxPycor: Int, source: String) {
    initForTesting(new WorldDimensions(minPxcor, maxPxcor, minPycor, maxPycor), source)
  }

  private val compilerFlags =
    if (compiler.dialect.is3D) CompilerFlags(optimizations = Optimizations.gui3DOptimizations)
    else                       CompilerFlags(optimizations = Optimizations.guiOptimizations)

  /**
   * Internal use only.
   */
  def initForTesting(d: WorldDimensions, source: String) {
    world.turtleShapes.add(org.nlogo.shape.VectorShape.getDefaultShape)
    world.linkShapes.add(org.nlogo.shape.LinkShape.getDefaultLinkShape)
    world.createPatches(d)
    // See comment in initForTesting(Int, String) above.
    val dialect = d match {
      case _: WorldDimensions3D => NetLogoThreeDDialect
      case _ => NetLogoLegacyDialect
    }
    val newProgram = Program.fromDialect(dialect)
    val results = compiler.compileProgram(source, newProgram,
      getExtensionManager, getCompilationEnvironment, compilerFlags)
    procedures = results.proceduresMap
    codeBits.clear()
    init()
    _world.program = results.program
    world.realloc()

    // setup some test plots.
    plotManager.forgetAll()
    val plot1 = plotManager.newPlot("plot1")
    plot1.createPlotPen("pen1", false)
    plot1.createPlotPen("pen2", false)
    val plot2 = plotManager.newPlot("plot2")
    plot2.createPlotPen("pen1", false)
    plot2.createPlotPen("pen2", false)
    plotManager.compileAllPlots()

    clearDrawing()
  }

  /**
   * Internal use only.
   */
  def initForTesting(d: org.nlogo.core.WorldDimensions) {
    world.createPatches(d)
    world.realloc()
    clearDrawing()
  }

  /**
   * Kills all turtles, clears all patch variables, and makes a new patch grid.
   */
  def setDimensions(d: WorldDimensions, showProgress: Boolean, toStop: WorldResizer.JobStop): Unit = {
    if (d.patchSize != world.patchSize)
      world.patchSize(d.patchSize)
    if (d.wrappingAllowedInX != world.wrappingAllowedInX ||
      d.wrappingAllowedInY != world.wrappingAllowedInY) {
      world.changeTopology(d.wrappingAllowedInX, d.wrappingAllowedInY)
      renderer.changeTopology(d.wrappingAllowedInX, d.wrappingAllowedInY)
    }
    if (! compilerTestingMode) {
      world.createPatches(d)
    }
    renderer.resetCache(d.patchSize)
    clearDrawing()
  }

  private var _frameRate = 0.0
  override def frameRate = _frameRate
  override def frameRate(frameRate: Double) { _frameRate = frameRate }

  private var _tickCounterLabel = "ticks"
  override def tickCounterLabel = _tickCounterLabel
  override def tickCounterLabel(s: String) { _tickCounterLabel = tickCounterLabel }

  private var _showTickCounter = true
  override def showTickCounter = _showTickCounter
  override def showTickCounter(showTickCounter: Boolean) { _showTickCounter = showTickCounter }

  override def getMinimumWidth = 0
  override def insetWidth = 0
  override def viewWidth = world.worldWidth
  override def viewHeight = world.worldHeight
  override def patchSize(patchSize: Double) {
    world.patchSize(patchSize)
    renderer.resetCache(patchSize)
    renderer.trailDrawer.rescaleDrawing()
  }
  override def patchSize = world.patchSize
  override def changeTopology(wrapX: Boolean, wrapY: Boolean) {
    world.changeTopology(wrapX, wrapY)
    renderer.changeTopology(wrapX, wrapY)
  }
  override def perspective = world.observer.perspective
  override def drawSpotlight = true
  override def renderPerspective = true
  override def viewOffsetX = world.observer.followOffsetX
  override def viewOffsetY = world.observer.followOffsetY
  override def updateMode(updateMode: UpdateMode) { }
  override def setSize(x: Int, y: Int) { }
  override def clearTurtles() {
    if (!compilerTestingMode)
      world.clearTurtles()
  }
  override def getAndCreateDrawing =
    renderer.trailDrawer.getAndCreateDrawing(true)
  override def importDrawing(file: org.nlogo.core.File) {
    renderer.trailDrawer.importDrawing(file)
  }
  override def clearDrawing() {
    world.clearDrawing()
    renderer.trailDrawer.clearDrawing()
  }
  override def exportDrawing(filename: String, format: String) {
    FileIO.writeImageFile(
      renderer.trailDrawer.getAndCreateDrawing(true), filename, format)
  }
  override def exportDrawingToCSV(writer: java.io.PrintWriter) {
    renderer.trailDrawer.exportDrawingToCSV(writer)
  }

  def exportOutput(filename: String) {
    val file: org.nlogo.core.File = new org.nlogo.api.LocalFile(filename)
    try {
      file.open(org.nlogo.core.FileMode.Write)
      val lines =
          new java.util.StringTokenizer(outputAreaBuffer.toString, "\n")
      while (lines.hasMoreTokens) {
        // note that since we always use println, we always output a final carriage return
        // even if the TextArea doesn't have one; hmm, bug or feature? let's call it a feature
        file.println(lines.nextToken())
      }
      file.close(true)
    } catch {
      case ex: java.io.IOException =>
        try file.close(false)
        catch {
          case ex2: java.io.IOException =>
            org.nlogo.api.Exceptions.ignore(ex2)
        }
      case ex: RuntimeException =>
        org.nlogo.api.Exceptions.handle(ex)
    }
  }

  override def exportOutputAreaToCSV(writer: java.io.PrintWriter) {
    if (_openModel.exists(_.widgets.exists(_.isInstanceOf[Output]))) {
      writer.println(org.nlogo.api.Dump.csv.encode("OUTPUT"))
      org.nlogo.api.Dump.csv.stringToCSV(writer, outputAreaBuffer.toString)
    }
  }

  /**
   * Internal use only.
   */
  // called from job thread - ST 10/1/03
  override def clearOutput() {
    outputAreaBuffer.setLength(0)
  }

  /// world importing error handling

  var importerErrorHandler: org.nlogo.agent.ImporterJ.ErrorHandler =
    new org.nlogo.agent.ImporterJ.ErrorHandler {
      override def showError(title: String, errorDetails: String, fatalError: Boolean) = {
        System.err.println(
          "got a " + (if (fatalError) "" else "non") +
          "fatal error " + title + ": " + errorDetails)
        true
      }}

  /**
   * Get a snapshot of the 2D view.
   */
  override def exportView = renderer.exportView(this)

  /**
   * Get a snapshot of the 2D view, using an existing BufferedImage
   * object.
   */
  def getGraphics(image: java.awt.image.BufferedImage) = {
    val graphics = image.getGraphics.asInstanceOf[java.awt.Graphics2D]
    val font = graphics.getFont
    val newFont = new java.awt.Font(font.getName, font.getStyle, fontSize)
    graphics.setFont(newFont)
    renderer.exportView(graphics, this)
  }

  override def exportView(filename: String, format: String) {
    FileIO.writeImageFile(renderer.exportView(this), filename, format)
  }

  /**
   * Not implemented.
   */
  override def exportInterface(filename: String) = unsupported

  /**
   * Internal use only. Called from job thread.
   */
  override def sendOutput(oo: org.nlogo.agent.OutputObject, toOutputArea: Boolean) {
    // output always goes to stdout in headless mode
    if (!silent)
      print(oo.get)
    // we also need to record it if it headed for the Output Area widget
    if (toOutputArea)
      outputAreaBuffer.append(oo.get)
  }

  /**
   * Internal use only.
   */
  def ownerFinished(owner: org.nlogo.api.JobOwner) { }

  def disablePeriodicRendering(): Unit = { }
  def enablePeriodicRendering(): Unit = { }

  /**
   * Internal use only.
   */
  override def requestDisplayUpdate(force: Boolean) {
    hubNetManager.foreach(_.incrementalUpdateFromEventThread())
  }

  /**
   * Internal use only.
   */
  override def breathe(context: org.nlogo.nvm.Context) { }

  /**
   * Internal use only.
   */
  def periodicUpdate() { }

  // this is a blatant hack that makes it possible to test the new stack trace stuff.
  // lastErrorReport gives more information than the regular exception that gets thrown from the
  // command function.  -JC 11/16/10
  var lastErrorReport: ErrorReport = null

  // The headless collaborator JMO sends a RuntimeError WorkspaceEvent, dealt with here.
  // In the long run, we'd like to remove all of the JobManagerOwner methods on workspace.
  // - RG 2/11/17
  override def processWorkspaceEvent(evt: WorkspaceEvent): Unit = {
    super.processWorkspaceEvent(evt)
    evt match {
      case e: RuntimeError =>
        if (e.exception.isInstanceOf[LogoException]) {
          lastErrorReport = new ErrorReport(e.owner, e.context, e.instruction, e.exception)
          lastLogoException = e.exception.asInstanceOf[LogoException]
        }
      case _ =>
    }
  }

  private lazy val loader = {
    fileformat.standardLoader(compiler.utilities)
      .addSerializer[Array[String], NLogoFormat](
        Femto.get[ComponentSerialization[Array[String], NLogoFormat]]("org.nlogo.sdm.NLogoSDMFormat"))
      .addSerializer[NLogoXFormat.Section, NLogoXFormat](
        Femto.get[ComponentSerialization[NLogoXFormat.Section, NLogoXFormat]]("org.nlogo.sdm.NLogoXSDMFormat", ScalaXmlElementFactory))
  }
  /// Controlling API methods

  /**
   * Opens a model stored in a file.
   *
   * @param path the path (absolute or relative) of the NetLogo model to open.
   */
  @throws(classOf[java.io.IOException])
  @throws(classOf[CompilerException])
  @throws(classOf[LogoException])
  override def open(path: String) {
    try {
      val m = loader.readModel(Paths.get(path).toUri).get
      modelTracker.setModelPath(path)
      modelTracker.setModelType(ModelType.Normal)
      fileManager.handleModelChange()
      openModel(m)
    }
    catch {
      case ex: CompilerException =>
        // models with special comment are allowed not to compile
        if (compilerTestingMode &&
            FileIO.fileToString(path)(Codec.UTF8).startsWith(";; DOESN'T COMPILE IN CURRENT BUILD"))
          System.out.println("ignored compile error: " + path)
        else throw ex
    }
  }

  /**
   * Opens a model stored in a string
   *
   * @param modelContents
   */
  override def openString(modelContents: String) {
    val suffix = fileformat.modelSuffix(modelContents)
    openFromSource(modelContents, suffix.getOrElse(throw new Exception(s"Invalid model: $modelContents")))
  }

  /**
   * Opens a model stored in a string.
   * Can only be called once per instance of HeadlessWorkspace
   *
   * @param source The complete model, including widgets and so forth,
   *               in the same format as it would be stored in a file.
   */
  def openFromSource(source: String, extension: String) {
    loader.readModel(source, extension).foreach(openModel)
  }

  def openModel(model: Model): Unit = {
    new HeadlessModelOpener(this).openFromModel(model)
  }

  /**
   * Halts all running NetLogo code in this workspace.
   */
  override def halt() {
    // we just invoke the method in our superclass, but explicitly writing that lets us doc the
    // method - ST 6/1/05
    super.halt()
  }

  def unsupported = throw new UnsupportedOperationException
}
