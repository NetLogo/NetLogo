// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.agent.{ World, World2D, World3D }
import org.nlogo.api.{ AutoConvertable, ComponentSerialization, RendererInterface,
  LogoException, ModelType, NetLogoLegacyDialect, NetLogoThreeDDialect,
  PreviewCommands, Version, ViewSettings, WorldDimensions3D }
import org.nlogo.core.{ Femto, Program, WorldDimensions }
import org.nlogo.nvm.PresentationCompilerInterface
import org.nlogo.shape.{ LinkShape, ShapeConverter, VectorShape }
import org.nlogo.fileformat, fileformat.NLogoFormat
import org.nlogo.workspace.{ AbstractWorkspace, AbstractWorkspaceScala,
  Controllable, HubNetManagerFactory, WorldLoaderInterface }
import org.nlogo.util.Pico

import java.nio.file.Paths

object HeadlessSingleThreadWorkspace {
  def newInstance: HeadlessSingleThreadWorkspace =
    newInstance(classOf[HeadlessSingleThreadWorkspace])

  def newInstance(subclass: Class[_ <: HeadlessSingleThreadWorkspace]): HeadlessSingleThreadWorkspace = {
    val pico = new Pico
    pico.addComponent(if (Version.is3D) classOf[World3D] else classOf[World2D])
    pico.add("org.nlogo.compile.Compiler")
    if (Version.is3D)
      pico.addScalaObject("org.nlogo.api.NetLogoThreeDDialect")
    else
      pico.addScalaObject("org.nlogo.api.NetLogoLegacyDialect")
    pico.add("org.nlogo.render.Renderer")
    pico.addComponent(subclass)
    pico.addAdapter(new ModelLoaderComponent())
    pico.add(classOf[HubNetManagerFactory], "org.nlogo.hubnet.server.HeadlessHubNetManagerFactory")
    pico.getComponent(subclass)
  }
}

class HeadlessSingleThreadWorkspace(
  _world: World with org.nlogo.agent.CompilationManagement,
  val compiler: PresentationCompilerInterface,
  val renderer: RendererInterface,
  hubNetManagerFactory: HubNetManagerFactory)
  extends AbstractWorkspaceScala(_world, hubNetManagerFactory)
  with HeadlessWorkspaceBase {
  /*
  with ViewSettings {
    */

    override val dialect = if (Version.is3D) NetLogoThreeDDialect else NetLogoLegacyDialect

    // this is a blatant hack that makes it possible to test the new stack trace stuff.
    // lastErrorReport gives more information than the regular exception that gets
    // thrown from the command function.  -JC 11/16/10
    var lastErrorReport: ErrorReport = null

    private lazy val loader = {
      val allAutoConvertables = fileformat.defaultAutoConvertables :+ Femto.scalaSingleton[AutoConvertable]("org.nlogo.sdm.SDMAutoConvertable")
      val converter = fileformat.converter(getExtensionManager, getCompilationEnvironment, this, allAutoConvertables) _
      fileformat.standardLoader(compiler.utilities)
        .addSerializer[Array[String], NLogoFormat](
          Femto.get[ComponentSerialization[Array[String], NLogoFormat]]("org.nlogo.sdm.NLogoSDMFormat"))
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
        source, Program.fromDialect(dialect),
        getExtensionManager, getCompilationEnvironment)
      procedures = results.proceduresMap
      codeBits.clear()
      init()
      _world.program(results.program)
      world.createPatches(d)
      world.patchSize(d.patchSize)
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

    // Members declared in org.nlogo.workspace.AbstractWorkspace
    def aggregateManager(): org.nlogo.api.AggregateManagerInterface = ???
    def breathe(): Unit = ???
    def clearDrawing(): Unit = {
      world.clearDrawing()
      renderer.trailDrawer.clearDrawing()
    }
    def importDrawing(x$1: org.nlogo.core.File): Unit = ???
    var importerErrorHandler: org.nlogo.agent.ImporterJ.ErrorHandler =
      new org.nlogo.agent.ImporterJ.ErrorHandler {
        override def showError(title: String, errorDetails: String, fatalError: Boolean) = {
          System.err.println(
            "got a " + (if (fatalError) "" else "non") +
            "fatal error " + title + ": " + errorDetails)
          true
      }}

    override val evaluator = new SingleThreadEvaluator(this)

    def magicOpen(x$1: String): Unit = ???
    def openString(x$1: String): Unit = ???
    def requestDisplayUpdate(x$1: Boolean): Unit = ???
    def sendOutput(x$1: org.nlogo.agent.OutputObject,x$2: Boolean): Unit = ???

    // Members declared in org.nlogo.workspace.AbstractWorkspaceScala
    def isHeadless: Boolean = true

    // Members declared in org.nlogo.workspace.Controllable
    def open(path: String): Unit = {
      val m = loader.readModel(Paths.get(path).toUri).get
      setModelPath(path)
      setModelType(ModelType.Normal)
      fileManager.handleModelChange()
      openModel(m)
    }

    // Members declared in org.nlogo.workspace.Exporting
    def exportDrawingToCSV(writer: java.io.PrintWriter): Unit = ???
    def exportOutputAreaToCSV(writer: java.io.PrintWriter): Unit = ???

    // Members declared in org.nlogo.nvm.JobManagerOwner
    def ownerFinished(owner: org.nlogo.api.JobOwner): Unit = { }
    def periodicUpdate(): Unit = { }
    def runtimeError(owner: org.nlogo.api.JobOwner,context: org.nlogo.nvm.Context,instruction: org.nlogo.nvm.Instruction,ex: Exception): Unit = {
      ex match {
        case le: LogoException =>
          lastLogoException = le
          lastErrorReport = new ErrorReport(owner, context, instruction, le)
        case _ =>
          System.err.println("owner: " + owner.displayName)
          org.nlogo.api.Exceptions.handle(ex)
      }
    }
    def updateDisplay(haveWorldLockAlready: Boolean): Unit = { }

    // Members declared in org.nlogo.nvm.LoggingWorkspace
    def deleteLogFiles(): Unit = ???
    def startLogging(properties: String): Unit = ???
    def zipLogFiles(filename: String): Unit = ???

    // Members declared in org.nlogo.api.Workspace
    def changeTopology(wrapX: Boolean,wrapY: Boolean): Unit = {
      world.changeTopology(wrapX, wrapY)
      renderer.changeTopology(wrapX, wrapY)
    }
    def clearOutput(): Unit = ???
    def exportDrawing(path: String,format: String): Unit = ???
    def exportInterface(path: String): Unit = ???
    def exportOutput(path: String): Unit = ???
    def exportView: java.awt.image.BufferedImage = ???
    def exportView(path: String,format: String): Unit = ???
    def getAndCreateDrawing(): java.awt.image.BufferedImage = ???
    def openModel(model: org.nlogo.core.Model): Unit = {
      if (! Version.compatibleVersion(model.version))
        throw new IllegalStateException("Unknown NetLogo version: " + model.version)

      loadWorld(model.view, this)

      // plot loading
      // aggregate manager loading

      val results = {
        import collection.JavaConverters._
        val code = model.code
        val newProgram =
          Program.fromDialect(dialect)
            .copy(interfaceGlobals = model.interfaceGlobals)
        val compilerResults =
          compiler.compileProgram(code, Seq(), newProgram, getExtensionManager, getCompilationEnvironment)
        procedures = compilerResults.proceduresMap
        codeBits.clear()

        model.optionalSectionValue[PreviewCommands]("org.nlogo.modelsection.previewcommands")
          .foreach(previewCommands = _)

        world.turtleShapes.replaceShapes(model.turtleShapes
          .map(ShapeConverter.baseVectorShapeToVectorShape))
        if (model.turtleShapes.isEmpty) {
          world.turtleShapes.add(VectorShape.getDefaultShape)
        }

        world.linkShapes.replaceShapes(model.linkShapes
          .map(ShapeConverter.baseLinkShapeToLinkShape))
        if (model.linkShapes.isEmpty) {
          world.linkShapes.add(LinkShape.getDefaultLinkShape)
        }

        // load hubnet

        init()
        _world.program(compilerResults.program)

        // compiler testing mode
        world.realloc()

        // compile plots
        for ((vname, spec) <- model.constraints) {
        }
      }
    }

    def waitFor(runnable: org.nlogo.api.CommandRunnable): Unit = ???
    def waitForQueuedEvents(): Unit = ???
    def waitForResult[T](runnable: org.nlogo.api.ReporterRunnable[T]): T = ???

    // Members declared in org.nlogo.nvm.Workspace
    def inspectAgent(agentKind: org.nlogo.core.AgentKind,agent: org.nlogo.agent.Agent,radius: Double): Unit = ???
    def inspectAgent(agent: org.nlogo.api.Agent,radius: Double): Unit = ???
    def stopInspectingAgent(agent: org.nlogo.agent.Agent): Unit = ???
    def stopInspectingDeadAgents(): Unit = ???

    // Members declared in org.nlogo.api.WorldResizer
    def setDimensions(dim: org.nlogo.core.WorldDimensions,patchSize: Double): Unit = {
      world.patchSize(patchSize)
      if (! compilerTestingMode) {
        world.createPatches(dim)
      }
      renderer.resetCache(patchSize)
      clearDrawing()
    }
    def setDimensions(dim: org.nlogo.core.WorldDimensions): Unit = {
      world.createPatches(dim)
      clearDrawing()
    }
}
