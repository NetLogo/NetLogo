package org.nlogo.extensions.vid

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import org.nlogo.api.{ Activation, Agent, CommandRunnable, Context, ExtensionManager, ExternalResourceManager,
                       ImportErrorHandler, JobOwner, LabProtocol, LibraryManager, LogoException, MersenneTwisterFast,
                       OutputDestination, Perspective, PlotInterface, PlotManagerInterface, PreviewCommands,
                       RendererInterface, ReporterRunnable, Workspace, WorkspaceContext, World }

class FakeContext extends Context {
  def attachCurrentDirectory(path: String): String =
    s"/currentdir/$path"
  def attachModelDir(filePath: String): String = ???
  def getAgent: Agent = ???
  def getDrawing: java.awt.image.BufferedImage = ???
  def getRNG: MersenneTwisterFast = ???
  def importPcolors(image: java.awt.image.BufferedImage,asNetLogoColors: Boolean): Unit = ???
  def logCustomGlobals(nameValuePairs: Seq[(String, String)]): Unit = ???
  def logCustomMessage(msg: String): Unit = ???
  def activation: Activation = ???
  def world: World = ???
  val workspace = new FakeWorkspace()
}

class FakeWorkspace extends Workspace {
  override def exportView: BufferedImage = {
    new BufferedImage(480, 480, BufferedImage.TYPE_INT_ARGB)
  }

  def getResourceManager: ExternalResourceManager = ???

  // Members declared in Controllable
  def command(source: String): Unit = ???
  def evaluateCommands(owner: JobOwner,source: String,waitForCompletion: Boolean): Unit = ???
  def evaluateCommands(owner: JobOwner,source: String): Unit = ???
  def evaluateReporter(owner: JobOwner,source: String): AnyRef = ???
  def report(source: String): AnyRef = ???

  // Members declared in ImporterUser
  def currentPlot(plot: String): Unit = ???
  def maybeGetPlot(plot: String): Option[PlotInterface] = ???
  def importExtensionData(name: String,data: java.util.List[Array[String]],handler: ImportErrorHandler): Unit = ???
  def isExtensionName(name: String): Boolean = ???
  def setOutputAreaContents(text: String): Unit = ???

  // Members declared in org.nlogo.core.LiteralParser
  def readFromString(s: String): AnyRef = ???
  def readNumberFromString(source: String): AnyRef = ???

  // Members declared in RandomServices
  def auxRNG: MersenneTwisterFast = ???
  def mainRNG: MersenneTwisterFast = ???
  def seedRNGs(seed: Int): Unit = ???

  // Members declared in ViewSettings
  def drawSpotlight: Boolean = ???
  def fontSize: Int = ???
  def workspaceContext: WorkspaceContext = ???
  def perspective: Perspective = ???
  def renderPerspective: Boolean = ???
  def viewHeight: Double = ???
  def viewOffsetX: Double = ???
  def viewOffsetY: Double = ???
  def viewWidth: Double = ???

  // Members declared in Workspace
  def behaviorSpaceRunNumber(n: Int): Unit = ???
  def behaviorSpaceRunNumber: Int = ???
  def getBehaviorSpaceExperiments: List[LabProtocol] = ???
  def setBehaviorSpaceExperiments(experiments: List[LabProtocol]): Unit = ???
  def benchmark(minTime: Int,maxTime: Int): Unit = ???
  def changeTopology(wrapX: Boolean,wrapY: Boolean): Unit = ???
  def clearAll(): Unit = ???
  def clearDrawing(): Unit = ???
  def clearLastLogoException(): Unit = ???
  def clearOutput(): Unit = ???
  def clearTicks(): Unit = ???
  def compilerTestingMode: Boolean = ???
  def dispose(): Unit = ???
  def getCompilationEnvironment: org.nlogo.core.CompilationEnvironment = ???
  def exportAllPlots(path: String): Unit = ???
  def exportDrawing(path: String,format: String): Unit = ???
  def exportInterface(path: String): Unit = {
    ImageIO.write(new BufferedImage(640, 640, BufferedImage.TYPE_INT_ARGB), "png", new File(path))
  }
  def exportOutput(path: String): Unit = ???
  def exportPlot(plotName: String,path: String): Unit = ???
  def exportView(path: String,format: String): Unit = ???
  def exportWorld(writer: java.io.PrintWriter): Unit = ???
  def exportWorld(path: String): Unit = ???
  def getAndCreateDrawing(): java.awt.image.BufferedImage = ???
  def getExtensionManager: ExtensionManager = ???
  def getLibraryManager: LibraryManager = ???
  def getModelDir: String = ???
  def getModelFileName: String = ???
  def getModelPath: String = ???
  def graphicsChecksum: String = ???
  def importDrawing(path: String): Unit = ???
  def importWorld(path: String): Unit = ???
  def importWorld(reader: java.io.Reader): Unit = ???
  def lastLogoException: LogoException = ???
  def open(modelPath: String, shouldAutoInstallLibs: Boolean = false): Unit = ???
  def openModel(model: org.nlogo.core.Model, shouldAutoInstallLibs: Boolean = false): Unit = ???
  def outputObject(obj: AnyRef,owner: AnyRef,addNewline: Boolean,readable: Boolean,destination: OutputDestination): Unit = ???
  def patchSize: Double = ???
  def plotManager: AnyRef = ???
  def realPlotManager: PlotManagerInterface = ???
  def previewCommands: PreviewCommands = ???
  def profilingEnabled: Boolean = ???
  def renderer: RendererInterface = ???
  def setModelPath(path: String): Unit = ???
  def waitFor(runnable: CommandRunnable): Unit = ???
  def waitForQueuedEvents(): Unit = ???
  def waitForResult[T](runnable: ReporterRunnable[T]): T = ???
  def warningMessage(message: String): Boolean = ???
  def world: World = ???
  def worldChecksum: String = ???
  // Members declared in WorldResizer
  def patchSize(patchSize: Double): Unit = ???
  def resizeView(): Unit = ???
  def setDimensions(dim: org.nlogo.core.WorldDimensions,patchSize: Double): Unit = ???
  def setDimensions(dim: org.nlogo.core.WorldDimensions): Unit = ???
}
