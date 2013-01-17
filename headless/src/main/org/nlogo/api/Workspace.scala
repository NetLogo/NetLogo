// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.IOException

trait Workspace extends ImporterUser with CompilerServices with RandomServices {
  def world: World
  def getExtensionManager: ExtensionManager
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
  @throws(classOf[IOException])
  def convertToNormal(): String
  def getModelPath: String
  def setModelPath(path: String)
  def getModelDir: String
  def getModelFileName: String
  // kludgy this is AnyRef, but we don't want to have a compile-time dependency on the plot
  // package. should be cleaned up sometime by introducing PlotManager? ST 2/12/08
  def plotManager: AnyRef
  def previewCommands: String
  def clearTicks()
  @throws(classOf[java.net.MalformedURLException])
  def attachModelDir(filePath: String): String
  @throws(classOf[InterruptedException])
  def dispose()
  def patchSize: Double
  def changeTopology(wrapX: Boolean, wrapY: Boolean)
  @throws(classOf[IOException])
  def open(modelPath: String)
  def openString(modelContents: String)
  def mouseDown: Boolean = false
  def mouseInside: Boolean = false
  def mouseXCor: Double = 0
  def mouseYCor: Double = 0
  def beep() { }
  def updateUI() { }
  def updateMonitor(owner: JobOwner, value: AnyRef) { }
  @throws(classOf[IOException])
  def addCustomShapes(filename: String) { }
  def movieIsOpen: Boolean = false
  def movieAnyFramesCaptured: Boolean = false
  def movieCancel() { }
  def movieClose() { }
  def movieGrabInterface() { }
  def movieGrabView() { }
  def movieSetRate(rate: Float) { }
  def movieStart(path: String) { }
  def movieStatus: String = "No movie."
  def userDirectory: Option[String] = None
  def userFile: Option[String] = None
  def userNewFile: Option[String] = None
  def userInput(msg: String): Option[String] = None
  def userOneOf(msg: String, xs: LogoList): Option[AnyRef] = None
  def userYesOrNo(msg: String): Option[Boolean] = None
  def userMessage(msg: String): Boolean = false
  def benchmark(minTime: Int, maxTime: Int)
  def isHeadless: Boolean
  def behaviorSpaceRunNumber: Int
  def behaviorSpaceRunNumber(n: Int)
  // for now this only works in HeadlessWorkspace, returns null in GUIWorkspace.  error handling
  // stuff is a mess, should be redone - ST 3/10/09, 1/22/12
  def lastLogoException: LogoException
  def clearLastLogoException()
  def profilingEnabled: Boolean
}
