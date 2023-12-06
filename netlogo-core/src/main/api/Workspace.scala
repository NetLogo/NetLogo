// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ CompilationEnvironment, CompilerException, Model, LiteralParser, LogoList }

import java.io.IOException

trait Workspace extends ImporterUser with LiteralParser with RandomServices
with ViewSettings with Controllable {
  def world: World
  def getExtensionManager: ExtensionManager
  def getLibraryManager:   LibraryManager
  def getCompilationEnvironment: CompilationEnvironment
  def waitFor(runnable: CommandRunnable)
  def waitForResult[T](runnable: ReporterRunnable[T]): T
  @throws(classOf[IOException])
  def importWorld(reader: java.io.Reader)
  @throws(classOf[IOException])
  def importWorld(path: String)
  @throws(classOf[IOException])
  def importDrawing(path: String)
  def clearDrawing()
  @throws(classOf[IOException])
  def exportDrawing(path: String, format: String)
  @throws(classOf[IOException])
  def exportView(path: String, format: String)
  def exportView: java.awt.image.BufferedImage
  @throws(classOf[IOException])
  def exportInterface(path: String)
  @throws(classOf[IOException])
  def exportWorld(path: String)
  @throws(classOf[IOException])
  def exportWorld(writer: java.io.PrintWriter)
  @throws(classOf[IOException])
  def exportOutput(path: String)
  @throws(classOf[IOException])
  def exportPlot(plotName: String, path: String)
  @throws(classOf[IOException])
  def exportAllPlots(path: String)
  def getAndCreateDrawing(): java.awt.image.BufferedImage
  def waitForQueuedEvents()
  def outputObject(obj: AnyRef, owner: AnyRef, addNewline: Boolean, readable: Boolean, destination: OutputDestination)
  def clearOutput()
  def clearAll()
  def getModelPath: String
  def setModelPath(path: String)
  def getModelDir: String
  def getModelFileName: String
  @deprecated("Use `realPlotManager`", "6.1.2")
  def plotManager: AnyRef
  // Annoyingly this will still likely have to be cast with `asInstanceOf` in GUI and headless because the
  // `api.PlotManagerInterface` doesn't have everything needed for the concrete implementations... yet.
  // -Jeremy Octover 2020
  def realPlotManager: PlotManagerInterface
  def previewCommands: PreviewCommands
  def clearTicks()
  @throws(classOf[InterruptedException])
  def dispose()
  def patchSize: Double
  def changeTopology(wrapX: Boolean, wrapY: Boolean)
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
  def beep() { }
  def updateUI() { }
  @throws(classOf[IOException])
  def addCustomShapes(filename: String) { }
  def userDirectory: Option[String] = None
  def userFile: Option[String] = None
  def userNewFile: Option[String] = None
  def userInput(msg: String): Option[String] = None
  def userOneOf(msg: String, xs: LogoList): Option[AnyRef] = None
  def userYesOrNo(msg: String): Option[Boolean] = None
  def userMessage(msg: String): Boolean = false
  def benchmark(minTime: Int, maxTime: Int)
  def behaviorSpaceRunNumber: Int
  def behaviorSpaceRunNumber(n: Int)
  def getBehaviorSpaceExperiments: List[LabProtocol]
  def setBehaviorSpaceExperiments(experiments: List[LabProtocol])
  // for now this only works in HeadlessWorkspace, returns null in GUIWorkspace.  error handling
  // stuff is a mess, should be redone - ST 3/10/09, 1/22/12
  def lastLogoException: LogoException
  def clearLastLogoException()
  def profilingEnabled: Boolean
  def worldChecksum: String
  def graphicsChecksum: String
  def renderer: RendererInterface
  def compilerTestingMode: Boolean
  def warningMessage(message: String): Boolean
}
