// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.awt.image.BufferedImage
import java.io.{ IOException, PrintWriter, Reader }

import org.nlogo.core.{ CompilationEnvironment, CompilerException, Model, LiteralParser, LogoList }

trait Workspace extends ImporterUser with LiteralParser with RandomServices
with ViewSettings with Controllable {
  def workspaceContext: WorkspaceContext
  def world: World
  def getExtensionManager: ExtensionManager
  def getLibraryManager:   LibraryManager
  def getResourceManager: ExternalResourceManager
  def getCompilationEnvironment: CompilationEnvironment
  def waitFor(runnable: CommandRunnable): Unit
  def waitForResult[T](runnable: ReporterRunnable[T]): T
  @throws(classOf[IOException])
  def importWorld(reader: Reader): Unit
  @throws(classOf[IOException])
  def importWorld(path: String): Unit
  @throws(classOf[IOException])
  def importDrawing(path: String): Unit
  def clearDrawing(): Unit
  @throws(classOf[IOException])
  def exportDrawing(path: String, format: String): Unit
  @throws(classOf[IOException])
  def exportView(path: String, format: String): Unit
  def exportView: BufferedImage
  @throws(classOf[IOException])
  def exportInterface(path: String): Unit
  @throws(classOf[IOException])
  def exportWorld(path: String): Unit
  @throws(classOf[IOException])
  def exportWorld(writer: PrintWriter): Unit
  @throws(classOf[IOException])
  def exportOutput(path: String): Unit
  @throws(classOf[IOException])
  def exportPlot(plotName: String, path: String): Unit
  @throws(classOf[IOException])
  def exportAllPlots(path: String): Unit
  def getAndCreateDrawing(): BufferedImage
  // this is used in headless to register arbitrary drawing actions with DrawingActionBroker. ideally,
  // all drawing actions would go through NetLogo APIs, but that would require a bunch of additional
  // infrastructure to support complex drawing actions in extensions like bitmap and gis. (Isaac B 12/24/25)
  def syncDrawing(image: BufferedImage): Unit = {}
  def waitForQueuedEvents(): Unit
  def outputObject(obj: AnyRef, owner: AnyRef, addNewline: Boolean, readable: Boolean,
                   destination: OutputDestination): Unit
  def clearOutput(): Unit
  def clearAll(): Unit
  def getModelPath: String
  def setModelPath(path: String): Unit
  def getModelDir: String
  def getModelFileName: String
  // Annoyingly this will still likely have to be cast with `asInstanceOf` in GUI and headless because the
  // `api.PlotManagerInterface` doesn't have everything needed for the concrete implementations... yet.
  // -Jeremy Octover 2020
  def realPlotManager: PlotManagerInterface
  def previewCommands: PreviewCommands
  def clearTicks(): Unit
  @throws(classOf[InterruptedException])
  def dispose(): Unit
  def patchSize: Double
  def changeTopology(wrapX: Boolean, wrapY: Boolean): Unit
  @throws(classOf[IOException])
  @throws(classOf[CompilerException])
  @throws(classOf[LogoException])
  def open(modelPath: String, shouldAutoInstallLibs: Boolean): Unit
  def openModel(model: Model): Unit = openModel(model, false)
  def openModel(model: Model, shouldAutoInstallLibs: Boolean): Unit
  def mouseDown: Boolean = false
  def mouseInside: Boolean = false
  def mouseXCor: Double = 0
  def mouseYCor: Double = 0
  def beep(): Unit = { }
  def updateUI(): Unit = { }
  @throws(classOf[IOException])
  def addCustomShapes(filename: String): Unit = { }
  def userDirectory: Option[String] = None
  def userFile: Option[String] = None
  def userNewFile: Option[String] = None
  def userInput(msg: String): Option[String] = None
  def userOneOf(msg: String, xs: LogoList): Option[AnyRef] = None
  def userYesOrNo(msg: String): Option[Boolean] = None
  def userMessage(msg: String): Boolean = false
  def benchmark(minTime: Int, maxTime: Int): Unit
  def behaviorSpaceRunNumber: Int
  def behaviorSpaceRunNumber(n: Int): Unit
  // for now this only works in HeadlessWorkspace, returns null in GUIWorkspace.  error handling
  // stuff is a mess, should be redone - ST 3/10/09, 1/22/12
  def lastLogoException: LogoException
  def clearLastLogoException(): Unit
  def profilingEnabled: Boolean
  def worldChecksum: String
  def graphicsChecksum: String
  def renderer: RendererInterface
  def compilerTestingMode: Boolean
  def warningMessage(message: String): Boolean
  def getHubNetManager: Option[HubNetInterface]
}
