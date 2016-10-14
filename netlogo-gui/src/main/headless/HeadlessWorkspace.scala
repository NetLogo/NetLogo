// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.nio.file.Paths

// Note that in the Scaladoc we distribute, this class is included, but Workspace and
// AbstractWorkspace are not, so if you want to document a method for everyone, override that method
// here and document it here.  The overriding method can simply call super(). - ST 6/1/05, 7/28/11

import org.nlogo.agent.{ Agent, Observer }
import org.nlogo.api.{ AutoConvertable, ComponentSerialization, Version, ModelLoader, RendererInterface,
  WorldDimensions3D, AggregateManagerInterface, FileIO, LogoException, ModelReader, ModelType, NetLogoLegacyDialect,
  NetLogoThreeDDialect, SimpleJobOwner, HubNetInterface, CommandRunnable, ReporterRunnable }, ModelReader.modelSuffix
import org.nlogo.core.{ AgentKind, CompilerException, Femto, Model, UpdateMode, WorldDimensions }
import org.nlogo.agent.{ World, World3D }
import org.nlogo.nvm.{ LabInterface,
                       Workspace, DefaultCompilerServices, CompilerInterface }
import org.nlogo.workspace.{ AbstractWorkspace, AbstractWorkspaceScala, HubNetManagerFactory }
import org.nlogo.fileformat, fileformat.NLogoFormat
import org.nlogo.util.Pico
import org.picocontainer.Parameter
import org.picocontainer.parameters.ComponentParameter

/**
 * Companion object, and factory object, for the HeadlessWorkspace class.
 */
object HeadlessWorkspace {

  /**
   * Makes a new instance of NetLogo capable of running a model "headless", with no GUI.
   */
  def newInstance: HeadlessWorkspace =
    newInstance(classOf[HeadlessWorkspace])

  /**
   * If you derive your own subclass of HeadlessWorkspace, use this method to instantiate it.
   */
  def newInstance(subclass: Class[_ <: HeadlessWorkspace]): HeadlessWorkspace = {
    val pico = new Pico
    pico.addComponent(if (Version.is3D) classOf[World3D] else classOf[World])
    pico.add("org.nlogo.compile.Compiler")
    if (Version.is3D)
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

  def newLab: LabInterface = {
    val pico = new Pico
    pico.add("org.nlogo.compile.Compiler")
    if (Version.is3D)
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
  _world: World,
  val compiler: CompilerInterface,
  val renderer: RendererInterface,
  val aggregateManager: AggregateManagerInterface,
  hubNetManagerFactory: HubNetManagerFactory)
extends AbstractWorkspaceScala(_world, hubNetManagerFactory)
with org.nlogo.workspace.Controllable
with org.nlogo.workspace.WorldLoaderInterface
with org.nlogo.api.ViewSettings {

  AbstractWorkspace.isApplet(false)
  world.trailDrawer(renderer.trailDrawer)

  /**
   * Has a model been opened in this workspace?
   */
  var modelOpened = false

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
  var compilerTestingMode = false

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
  def initForTesting(worldSize: Int) {
    initForTesting(worldSize, "")
  }

  /**
   * Internal use only.
   */
  def initForTesting(worldSize: Int, modelString: String) {
    if (Version.is3D)
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

  /**
   * Internal use only.
   */
  def initForTesting(d: WorldDimensions, source: String) {
    world.turtleShapes.add(org.nlogo.shape.VectorShape.getDefaultShape)
    world.linkShapes.add(org.nlogo.shape.LinkShape.getDefaultLinkShape)
    world.createPatches(d)
    import collection.JavaConverters._
    val results = compiler.compileProgram(
      source, world.newProgram(Seq[String]()),
      getExtensionManager, getCompilationEnvironment)
    procedures = results.proceduresMap
    codeBits.clear()
    init()
    world.program(results.program)
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
  def setDimensions(d: WorldDimensions) {
    world.createPatches(d)
    clearDrawing()
  }

  def setDimensions(d: WorldDimensions, patchSize: Double) {
    world.patchSize(patchSize)
    if (!compilerTestingMode) {
      world.createPatches(d)
    }
    renderer.resetCache(patchSize)
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
  override def computePatchSize(width: Int, numPatches: Int): Double =
    width / numPatches
  override def calculateHeight(worldHeight: Int, patchSize: Double) =
    (worldHeight * patchSize).toInt
  def calculateWidth(worldWidth: Int, patchSize: Double): Int =
    (worldWidth * patchSize).toInt
  override def resizeView() { }
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
  override def inspectAgent(agent: org.nlogo.api.Agent, radius: Double) {
    if (!silent)
      println(agent)
  }
  def inspectAgent(agentClass: AgentKind, agent: org.nlogo.agent.Agent, radius: Double) {
    if (!silent) {
      println(agent)
    }
  }
  override def stopInspectingAgent(agent: org.nlogo.agent.Agent): Unit = { }
  override def stopInspectingDeadAgents(): Unit = { }
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
    writer.println(org.nlogo.api.Dump.csv.encode("OUTPUT"))
    org.nlogo.api.Dump.csv.stringToCSV(writer, outputAreaBuffer.toString)
  }

  /**
   * Internal use only.
   */
  // called from job thread - ST 10/1/03
  override def clearOutput() {
    outputAreaBuffer.setLength(0)
  }

  /// world importing error handling

  var importerErrorHandler: org.nlogo.agent.Importer.ErrorHandler =
    new org.nlogo.agent.Importer.ErrorHandler {
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

  /**
   * Internal use only.
   */
  def updateDisplay(haveWorldLockAlready: Boolean) { }

  /**
   * Internal use only.
   */
  override def requestDisplayUpdate(force: Boolean) {
    hubNetManager.foreach(_.incrementalUpdateFromEventThread())
  }

  /**
   * Internal use only.
   */
  override def breathe() { }

  /**
   * Internal use only.
   */
  def periodicUpdate() { }

  /**
   * Internal use only.
   */
  override def magicOpen(name: String) = unsupported

  /**
   * Internal use only.
   */
  def startLogging(properties: String) = unsupported

  /**
   * Internal use only.
   */
  def zipLogFiles(filename: String) = unsupported

  /**
   * Internal use only.
   */
  def deleteLogFiles() = unsupported

  def logCustomMessage(msg: String): Unit = unsupported

  def logCustomGlobals(nameValuePairs: Seq[(String, String)]): Unit = unsupported

  /**
   * Internal use only.
   */

  // this is a blatant hack that makes it possible to test the new stack trace stuff.
  // lastErrorReport gives more information than the regular exception that gets thrown from the
  // command function.  -JC 11/16/10
  var lastErrorReport: ErrorReport = null

  /**
   * Internal use only.
   */
  def runtimeError(owner: org.nlogo.api.JobOwner, context: org.nlogo.nvm.Context,
                   instruction: org.nlogo.nvm.Instruction, ex: Exception) {
    ex match {
      case le: LogoException =>
        lastLogoException = le
        lastErrorReport = new ErrorReport(owner, context, instruction, le)
      case _ =>
        System.err.println("owner: " + owner.displayName)
        org.nlogo.api.Exceptions.handle(ex)
    }
  }

  private lazy val loader = {
    val allAutoConvertables = fileformat.defaultAutoConvertables :+ Femto.scalaSingleton[AutoConvertable]("org.nlogo.sdm.SDMAutoConvertable")
    val converter = fileformat.converter(getExtensionManager, getCompilationEnvironment, this, allAutoConvertables) _
    fileformat.standardLoader(compiler.utilities)
      .addSerializer[Array[String], NLogoFormat](
        Femto.get[ComponentSerialization[Array[String], NLogoFormat]]("org.nlogo.sdm.NLogoSDMFormat"))
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
      setModelPath(path)
      setModelType(ModelType.Normal)
      fileManager.handleModelChange()
      openModel(m)
    }
    catch {
      case ex: CompilerException =>
        // models with special comment are allowed not to compile
        if (compilerTestingMode &&
            FileIO.file2String(path).startsWith(";; DOESN'T COMPILE IN CURRENT BUILD"))
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
    openFromSource(modelContents, modelSuffix)
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
