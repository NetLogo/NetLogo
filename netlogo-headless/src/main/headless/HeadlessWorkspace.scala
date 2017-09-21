// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

// Note that in the Scaladoc we distribute, this class is included, but Workspace and
// AbstractWorkspace are not, so if you want to document a method for everyone, override that method
// here and document it here.  The overriding method can simply call super(). - ST 6/1/05, 7/28/11

import
  org.nlogo.{ agent, api, core, drawing, fileformat, nvm, workspace },
    agent.{ Agent, World, World2D },
    api.{ CommandRunnable, FileIO, LogoException, RendererInterface, ReporterRunnable, SimpleJobOwner, WorldResizer },
    core.{ AgentKind, CompilerException, Femto, File, FileMode, Model, Output, UpdateMode, WorldDimensions },
    drawing.DrawingActionBroker,
    fileformat.{ NLogoFormat, NLogoPreviewCommandsFormat },
    nvm.{ CompilerInterface, Context, LabInterface },
    workspace.AbstractWorkspace

import java.nio.file.Paths

import scala.io.Codec

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
    val world = new World2D
    Femto.get(subclass, world,
      Femto.scalaSingleton[CompilerInterface](
        "org.nlogo.compile.Compiler"),
      Femto.get[RendererInterface](
        "org.nlogo.render.Renderer", world))
  }

  def newLab: LabInterface = Femto.get("org.nlogo.lab.Lab")
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
  val renderer: RendererInterface)
extends AbstractWorkspace(_world)
with org.nlogo.workspace.WorldLoaderInterface {

  def parser = compiler.utilities

  def isHeadless = true

  val drawingActionBroker = new DrawingActionBroker(renderer.trailDrawer)
  world.trailDrawer(drawingActionBroker)

  val defaultOwner =
    new SimpleJobOwner("HeadlessWorkspace", world.mainRNG)

  /**
   * Has a model been opened in this workspace?
   */
  def modelOpened = _openModel.nonEmpty

  private[this] var _openModel = Option.empty[Model]
  def setOpenModel(model: Model) { _openModel = Some(model) }

  val outputAreaBuffer = new StringBuilder

  /**
   * If true, don't send anything to standard output.
   */
  var silent = false

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
   * Kills all turtles, clears all patch variables, and makes a new patch grid.
   */
  def setDimensions(d: WorldDimensions,showProgress: Boolean,stop: WorldResizer.JobStop): Unit = {
    world.patchSize(d.patchSize)
    if (!compilerTestingMode) {
      world.createPatches(d)
    }
    renderer.resetCache(d.patchSize)
    clearDrawing()
  }

  private var _fontSize = 13
  override def fontSize = _fontSize
  override def fontSize(i: Int) { _fontSize = i }

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
    drawingActionBroker.rescaleDrawing()
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
  def inspectAgent(agent: Agent, radius: Double) {
    if (!silent)
      println(agent)
  }
  override def inspectAgent(kind: AgentKind, agent: Agent, radius: Double) {
    if (!silent) {
      println(agent)
    }
  }
  override def getAndCreateDrawing =
    drawingActionBroker.getAndCreateDrawing(true)
  override def importDrawing(file: File) {
    drawingActionBroker.importDrawing(file)
  }
  override def clearDrawing() {
    world.clearDrawing()
    drawingActionBroker.clearDrawing()
  }
  override def exportDrawing(filename: String, format: String) {
    val stream = new java.io.FileOutputStream(new java.io.File(filename))
    javax.imageio.ImageIO.write(
      drawingActionBroker.getAndCreateDrawing(true), format, stream)
    stream.close()
  }
  override def exportDrawingToCSV(writer: java.io.PrintWriter) {
    drawingActionBroker.exportDrawingToCSV(writer)
  }

  def exportOutput(filename: String) {
    val file: File = new org.nlogo.api.LocalFile(filename)
    try {
      file.open(FileMode.Write)
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
  def getGraphics(image: java.awt.image.BufferedImage) {
    val graphics = image.getGraphics.asInstanceOf[java.awt.Graphics2D]
    val font = graphics.getFont
    val newFont = new java.awt.Font(font.getName, font.getStyle, fontSize)
    graphics.setFont(newFont)
    renderer.exportView(graphics, this)
  }

  override def exportView(filename: String, format: String) {
    // there's a form of ImageIO.write that just takes a filename, but if we use that when the
    // filename is invalid (e.g. refers to a directory that doesn't exist), we get an
    // IllegalArgumentException instead of an IOException, so we make our own OutputStream so we get
    // the proper exceptions. - ST 8/19/03
    val image = renderer.exportView(this)
    val stream = new java.io.FileOutputStream(new java.io.File(filename))
    javax.imageio.ImageIO.write(image, format, stream)
    stream.close()
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
  def updateDisplay(haveWorldLockAlready: Boolean, forced: Boolean) { }

  def disablePeriodicRendering(): Unit = { }
  def enablePeriodicRendering(): Unit = { }

  /**
   * Internal use only.
   */
  override def requestDisplayUpdate(force: Boolean) { }

  /**
   * Internal use only.
   */
  override def breathe(context: Context) { }

  /**
   * Internal use only.
   */
  def periodicUpdate() { }

  // This lastLogoException stuff is gross.  We should write methods that are declared to throw
  // LogoException, rather than requiring that this variable be checked. - ST 2/28/05
  private var _lastLogoException: LogoException = null
  override def lastLogoException: LogoException = _lastLogoException
  override def clearLastLogoException() { _lastLogoException = null }

  // this is a blatant hack that makes it possible to test the new stack trace stuff.
  // lastErrorReport gives more information than the regular exception that gets thrown from the
  // command function.  -JC 11/16/10
  var lastErrorReport: ErrorReport = null

  /**
   * Internal use only.
   */
  def runtimeError(owner: org.nlogo.api.JobOwner, context: Context,
                   instruction: org.nlogo.nvm.Instruction, ex: Exception) {
    ex match {
      case le: LogoException =>
        _lastLogoException = le
        lastErrorReport = new ErrorReport(owner, context, instruction, le)
      case _ =>
        System.err.println("owner: " + owner.displayName)
        org.nlogo.api.Exceptions.handle(ex)
    }
  }

  private lazy val loader = {
    fileformat.basicLoader.addSerializer[Array[String], NLogoFormat](new NLogoPreviewCommandsFormat())
  }

  /// Controlling API methods

  /**
   * Opens a model stored in a file.
   *
   * @param path the path (absolute or relative) of the NetLogo model to open.
   */
  @throws(classOf[java.io.IOException])
  override def open(path: String) {
    setModelPath(path)
    val modelContents = FileIO.fileToString(path)(Codec.UTF8)
    try loader.readModel(Paths.get(path).toUri).foreach(openModel)
    catch {
      case ex: CompilerException =>
        // models with special comment are allowed not to compile
        if (compilerTestingMode &&
            modelContents.startsWith(";; DOESN'T COMPILE IN CURRENT BUILD"))
          System.out.println("ignored compile error: " + path)
        else throw ex
    }
  }

  /**
   * Opens a model stored in memory.
   * Can only be called once per instance of HeadlessWorkspace
   *
   * @param source The complete model, including widgets and so forth, as created from core.Model()
   */
  def openModel(model: Model = Model()) {
    new HeadlessModelOpener(this).openFromModel(model)
  }

  /**
   * Runs NetLogo commands and waits for them to complete.
   *
   * @param source The command or commands to run
   * @throws core.CompilerException if the code fails to compile
   * @throws api.LogoException if the code fails to run
   */
  @throws(classOf[core.CompilerException])
  @throws(classOf[api.LogoException])
  def command(source: String) {
    evaluator.evaluateCommands(defaultOwner, source, world.observers, true, flags)
    if (lastLogoException != null) {
      val ex = lastLogoException
      _lastLogoException = null
      throw ex
    }
  }

  /**
   * Runs a NetLogo reporter.
   *
   * @param source The reporter to run
   * @return the result reported; may be of type java.lang.Integer, java.lang.Double,
   *         java.lang.Boolean, java.lang.String, {@link org.nlogo.core.LogoList},
   *         {@link org.nlogo.api.Agent}, AgentSet, or Nobody
   * @throws core.CompilerException if the code fails to compile
   * @throws api.LogoException if the code fails to run
   */
  @throws(classOf[core.CompilerException])
  @throws(classOf[api.LogoException])
  def report(source: String): AnyRef = {
    val result = evaluator.evaluateReporter(defaultOwner, source, world.observers, flags)
    if (lastLogoException != null) {
      val ex = lastLogoException
      _lastLogoException = null
      throw ex
    }
    result
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
