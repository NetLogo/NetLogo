// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, EventQueue => AwtEventQueue, Frame, FileDialog => AwtFileDialog }
import java.awt.image.BufferedImage
import java.io.{InputStream, IOException, PrintWriter}
import javax.swing.{ JScrollPane, JTextArea }
import java.util.concurrent.atomic.AtomicReference
import javax.swing.Action

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.{ Duration, MILLISECONDS }
import scala.util.{ Failure, Success }

import org.nlogo.core.{ AgentKind, File, I18N, Model, UpdateMode, WorldDimensions }
import org.nlogo.agent.{ Agent, ImporterJ, OutputObject }
import org.nlogo.api.{ AgentFollowingPerspective, CommandRunnable, Exceptions,
  FileIO, LogoException, ModelSettings, RendererInterface, TrailDrawerInterface, WorldResizer }
import org.nlogo.awt.{ EventQueue, Hierarchy, UserCancelException }
import org.nlogo.log.Logger
import org.nlogo.nvm.{ DisplayAlways, DisplayStatus }
import org.nlogo.swing.{ FileDialog, ModalProgressTask, OptionDialog }
import org.nlogo.shape.ShapeConverter
import org.nlogo.workspace.{ AbstractWorkspace, ExportOutput, UserInteraction }
import org.nlogo.window.Events.{ Enable2DEvent, ExportPlotEvent, ExportWidgetEvent, LoadModelEvent, OutputEvent }

object GUIWorkspaceScala {
  /**
   * Displays a warning to the user, allowing her to continue or cancel.
   * This provides the nice graphical warning dialog for when we're GUI.
   * Returns true if the user OKs it.
   */
  class SwingUserInteraction(frame: Frame) extends UserInteraction {
    def warningMessage(text: String): Boolean = {
      val options = Array[String](I18N.gui.get("common.buttons.continue"), I18N.gui.get("common.buttons.cancel"))
      OptionDialog.showMessage(
        frame, I18N.gui.get("common.messages.warning"),
        I18N.gui.get("common.messages.warning") + ":" + text, options) == 0
    }
  }

  type DisplayStatusRef = AtomicReference[DisplayStatus]

  val initialDisplayStatus = new AtomicReference[DisplayStatus](DisplayAlways)
  def viewManager(displayStatus: AtomicReference[DisplayStatus]): ViewManager =
    new ViewManager(displayStatus)
}

abstract class GUIWorkspaceScala(config: WorkspaceConfig)
  extends {
    override val owner: GUIJobManagerOwner = config.owner
  } with AbstractWorkspace(config)
  with ExportPlotEvent.Handler
  with ExportWidgetEvent.Handler
  with LoadModelEvent.Handler
  with TrailDrawerInterface {

  val listenerManager = config.listenerManager
  val displayStatusRef = config.displayStatusRef
  val viewManager = config.viewManager
  val updateManager = config.updateManager
  protected val frame = config.frame
  protected val externalFileManager = config.externalFileManager
  private val controlSet = config.controlSet
  private val monitorManager = config.monitorManager

  def glView: GLViewManagerInterface

  def getFrame: Frame = frame

  val plotExportControls = new PlotExportControls(plotManager)

  // for grid snap
  private var _snapOn: Boolean = false

  val view = new View(this)
  val viewWidget: ViewWidget = new ViewWidget(this, view)

  def dualView: Boolean
  def dualView(on: Boolean): Unit
  def patchesCreatedNotify(): Unit
  def newRenderer(): RendererInterface
  def switchTo3DViewAction : Action

  // agent inspection

  def inspectAgent(agentKind: AgentKind): Unit = {
    inspectAgent(agentKind, null, (world.worldWidth - 1) / 2)
  }

  def inspectAgent(agent: org.nlogo.api.Agent, radius: Double) {
    val a = agent.asInstanceOf[org.nlogo.agent.Agent]
    monitorManager.inspect(a.kind, a, radius, this)
  }

  def inspectAgent(agentClass: AgentKind, agent: Agent, radius: Double) {
    monitorManager.inspect(agentClass, agent, radius, this)
  }

  def stopInspectingAgent(agent: Agent): Unit = {
    monitorManager.stopInspecting(agent)
  }

  def stopInspectingDeadAgents(): Unit = {
    monitorManager.stopInspectingDeadAgents()
  }

  def closeAgentMonitors() { monitorManager.closeAll() }


  def setSnapOn(snapOn: Boolean): Unit = {
    _snapOn = snapOn
  }

  def snapOn = _snapOn

  def frameRate: Double = updateManager.frameRate
  def frameRate(frameRate: Double): Unit = {
    updateManager.frameRate(frameRate)
  }

  def updateMode: UpdateMode = updateManager.updateMode
  def updateMode(updateMode: UpdateMode): Unit = {
    updateManager.updateMode(updateMode)
  }

  def setPeriodicUpdatesEnabled(periodicUpdatesEnabled: Boolean): Unit = {
    owner.setPeriodicUpdatesEnabled(periodicUpdatesEnabled)
  }

  // this is called on the job thread - ST 9/30/03
  def periodicUpdate(): Unit = {
    if (owner.periodicUpdatesEnabled) {
      ThreadUtils.waitFor(world, updateRunner)
    }
  }

  protected val updateRunner: Runnable =
    new Runnable() {
      def run(): Unit = {
        new Events.PeriodicUpdateEvent().raise(GUIWorkspaceScala.this)
      }
    }


  override def exportDrawingToCSV(writer: PrintWriter): Unit = {
    view.renderer.trailDrawer.exportDrawingToCSV(writer)
  }

  override def exportOutputAreaToCSV(writer: PrintWriter): Unit = {
    new Events.ExportWorldEvent(writer).raise(this)
  }

  override def importerErrorHandler: ImporterJ.ErrorHandler = new ImporterJ.ErrorHandler {
    def showError(title: String, errorDetails: String, fatalError: Boolean): Boolean = {
      EventQueue.mustBeEventDispatchThread()
      val options = {
        implicit val i18nPrefix = I18N.Prefix("common.buttons")
        val buttons = if (fatalError) Array("ok") else Array("continue", "cancel")
        buttons.map(I18N.gui.apply)
      }
      val textArea = new JTextArea
      textArea.setText(errorDetails)
      textArea.setEditable(false)
      0 == OptionDialog.showCustom(getFrame, title, new JScrollPane(textArea), options)
    }
  }

  @throws(classOf[IOException])
  override def importDrawing(is: InputStream): Unit = {
    view.renderer.trailDrawer.importDrawing(is)
  }

  @throws(classOf[IOException])
  override def importDrawing(file: File): Unit = {
    view.renderer.trailDrawer.importDrawing(file)
  }

  override protected def sendOutput(oo: OutputObject, toOutputArea: Boolean): Unit = {
    val event = new OutputEvent(false, oo, false, !toOutputArea)

    // This method can be called when we are ALREADY in the AWT
    // event thread, so check before we block on it. -- CLB 07/18/05
    if (! AwtEventQueue.isDispatchThread) {
      ThreadUtils.waitFor(world, new Runnable() {
        def run(): Unit = event.raise(GUIWorkspaceScala.this)
      })
    } else {
      event.raise(this)
    }
  }

  def handle(e: LoadModelEvent): Unit = {
    loadFromModel(e.model)
    world.turtleShapes.replaceShapes(e.model.turtleShapes.map(ShapeConverter.baseShapeToShape))
    world.linkShapes.replaceShapes(e.model.linkShapes.map(ShapeConverter.baseLinkShapeToLinkShape))
    e.model.optionalSectionValue[ModelSettings]("org.nlogo.modelsection.modelsettings") match {
      case Some(settings: ModelSettings) => setSnapOn(settings.snapToGrid)
      case _ =>
    }
  }

  def updateModel(model: Model): Model = {
    model.withOptionalSection("org.nlogo.modelsection.modelsettings", Some(ModelSettings(snapOn)), Some(ModelSettings(false)))
  }

  def handle(e: ExportWidgetEvent): Unit = {
    try {
      val guessedName = modelTracker.guessExportName(e.widget.getDefaultExportName)
      val exportPath = FileDialog.showFiles(e.widget, I18N.gui.get("menu.file.export"), AwtFileDialog.SAVE, guessedName)
      val exportToPath = Future.successful(exportPath)
      e.widget match {
        case pw: PlotWidget =>
          new ExportPlotEvent(PlotWidgetExport.ExportSinglePlot(pw.plot), exportPath, {() => }).raise(pw)
        case ow: OutputWidget =>
          exportToPath
            .map((filename: String) => (filename, ow.valueText))(SwingUnlockedExecutionContext)
            .onComplete({
              case Success((filename: String, text: String)) =>
                ExportOutput.silencingErrors(filename, text)
              case Failure(_) =>
            })(NetLogoExecutionContext.backgroundExecutionContext)
        case ib: InputBox =>
          exportToPath
            .map((filename: String) => (filename, ib.valueText))(SwingUnlockedExecutionContext)
            .map[Unit]({ // background
              case (filename: String, text: String) => FileIO.writeFile(filename, text, true); ()
            })(NetLogoExecutionContext.backgroundExecutionContext).failed.foreach({ // on UI Thread
              case ex: java.io.IOException =>
                ExportControls.displayExportError(Hierarchy.getFrame(ib),
                  I18N.gui.get("menu.file.export.failed"),
                  I18N.gui.getN("tabs.input.export.error", ex.getMessage))
            })(SwingUnlockedExecutionContext)
        case _ =>
      }
    } catch {
      case uce: UserCancelException => Exceptions.ignore(uce)
    }
  }

  protected def plotExportOperation(e: ExportPlotEvent): Future[Unit] = {
    def runAndComplete(f: () => Unit, onError: IOException => Unit): Unit = {
      try {
        f()
      } catch {
        case ex: IOException =>
          e.onCompletion.run()
          onError(ex)
      } finally {
        e.onCompletion.run()
      }
    }
    e.plotExport match {
      case PlotWidgetExport.ExportAllPlots =>
        if (plotManager.getPlotNames.isEmpty) {
          plotExportControls.sorryNoPlots(getFrame)
          e.onCompletion.run()
          Future.successful(())
        } else
          Future {
            runAndComplete(
              () => super.exportAllPlots(e.exportFilename),
              (ex) => plotExportControls.allPlotExportFailed(getFrame, e.exportFilename, ex))
          } (new LockedBackgroundExecutionContext(world))
      case PlotWidgetExport.ExportSinglePlot(plot) =>
        Future {
          runAndComplete(
            () => super.exportPlot(plot.name, e.exportFilename),
            (ex) => plotExportControls.singlePlotExportFailed(getFrame, e.exportFilename, plot, ex))
        } (new LockedBackgroundExecutionContext(world))
    }
  }

  def handle(e: ExportPlotEvent): Unit = {
    plotExportOperation(e)
  }

  def getExportWindowFrame: Component =
    viewManager.getPrimary.getExportWindowFrame

  def exportView: BufferedImage =
    viewManager.getPrimary.exportView()

  @throws(classOf[IOException])
  def exportView(filename: String, format: String): Unit = {
    if (jobManager.onJobThread) {
      val viewFuture =
        Future(viewManager.getPrimary.exportView())(new SwingLockedExecutionContext(world))
      val image = awaitFutureFromJobThread(viewFuture)
      FileIO.writeImageFile(image, filename, format)
    } else {
      exportViewFromUIThread(filename, format)
    }
  }

  @throws(classOf[java.io.IOException])
  def exportViewFromUIThread(filename: String, format: String, onCompletion: () => Unit = {() => }): Unit = {
    val f =
      Future[BufferedImage](viewManager.getPrimary.exportView())(new SwingLockedExecutionContext(world))
    f.foreach({ image =>
      try {
        FileIO.writeImageFile(image, filename, format)
      } catch {
        case ex: java.io.IOException =>
          onCompletion()
          ExportControls.displayExportError(getExportWindowFrame, ex.getMessage)
      } finally {
        onCompletion()
      }
    })(NetLogoExecutionContext.backgroundExecutionContext)
  }

  def doExportView(exportee: LocalViewInterface): Unit = {
    val exportPathOption =
      try {
        Some(FileDialog.showFiles(getExportWindowFrame, I18N.gui.get("menu.file.export.view"), AwtFileDialog.SAVE,
          modelTracker.guessExportName("view.png")))
      } catch {
        case ex: UserCancelException =>
          Exceptions.ignore(ex)
          None
      }
    exportPathOption.foreach { exportPath =>
      ModalProgressTask.onBackgroundThreadWithUIData(
        getFrame, I18N.gui.get("dialog.interface.export.task"), () => (exportee.exportView(), exportPath),
        { (data: (BufferedImage, String)) =>
          data match {
            case (exportedView, path) =>
              try {
                FileIO.writeImageFile(exportedView, path, "png")
              } catch {
                case ex: IOException =>
                  ExportControls.displayExportError(getExportWindowFrame, ex.getMessage)
              }
        } })
    }
  }

  private def awaitFutureFromJobThread[A](future: Future[A]): A = {
    var res = Option.empty[A]
    while (res.isEmpty) {
      world.synchronized { world.wait(50) }
      try {
        res = Some(awaitFuture(future, 1))
      } catch {
        case e: java.util.concurrent.TimeoutException => // ignore
      }
    }
    res.get
  }

  private def awaitFuture[A](future: Future[A], millis: Long): A = {
    Await.result(future, Duration(millis, MILLISECONDS))
  }

  @throws(classOf[IOException])
  def exportInterface(filename: String): Unit = {
    if (jobManager.onJobThread) {
      // we treat the job thread differently because it will be holding the world lock
      FileIO.writeImageFile(awaitFutureFromJobThread(controlSet.userInterface), filename, "png")
    } else {
      exportInterfaceFromUIThread(filename, { () => })
    }
  }

  def exportInterfaceFromUIThread(filename: String, onCompletion: () => Unit = { () => }): Unit = {
    controlSet.userInterface.foreach({ uiImage =>
      try {
        FileIO.writeImageFile(uiImage, filename, "png")
      } catch {
        case ex: IOException =>
          onCompletion()
          ExportControls.displayExportError(getExportWindowFrame, ex.getMessage)
      } finally {
        onCompletion()
      }
    })(NetLogoExecutionContext.backgroundExecutionContext)
  }

  @throws(classOf[IOException])
  def exportOutput(filename: String): Unit = {
    if (jobManager.onJobThread) {
      val text = awaitFutureFromJobThread(controlSet.userOutput)
      ExportOutput.throwingErrors(filename, text)
    } else
      ModalProgressTask.onBackgroundThreadWithUIData(frame, I18N.gui.get("dialog.interface.export.task"),
        () => controlSet.userOutput,
        (textFuture: Future[String]) => ExportOutput.silencingErrors(filename, awaitFuture(textFuture, 1000)))
  }

  @throws(classOf[IOException])
  override def getSource(filename: String): String = {
    if (filename == "") {
      throw new IllegalArgumentException("cannot provide source for empty filename")
    }
    externalFileManager.getSource(filename) getOrElse super.getSource(filename)
  }

  def logCustomMessage(msg: String) = Logger.logCustomMessage(msg)
  def logCustomGlobals(nameValuePairs: Seq[(String, String)]) = Logger.logCustomGlobals(nameValuePairs: _*)

  // view control

  def setDimensions(d: WorldDimensions, showProgress: Boolean, owner: WorldResizer.JobStop): Unit = {
    val runner =
      new Runnable() {
        def run(): Unit = {
          viewWidget.settings.setDimensions(d, showProgress, owner)
        }
      }
    // this may be called from _resizeworld in which case we're
    // already on the event thread - ST 7/21/09
    if (java.awt.EventQueue.isDispatchThread()) {
      runner.run()
    } else {
      try {
        org.nlogo.awt.EventQueue.invokeAndWait(runner)
      } catch {
        case ex: InterruptedException => org.nlogo.api.Exceptions.handle(ex)
      }
    }
  }
  // painting
  def enableDisplayUpdates(): DisplayStatus = {
    val status = displayStatusRef.updateAndGet(s => s.switchSet(true))
    viewManager.reloadSwitchStatus(true)
    status
  }

  def disableDisplayUpdates(): DisplayStatus = {
    val status = displayStatusRef.updateAndGet(s => s.switchSet(false))
    viewManager.reloadSwitchStatus(false)
    status
  }

  def disablePeriodicRendering(): Unit = {
    displayStatusRef.updateAndGet(s => s.codeSet(false))
  }

  def enablePeriodicRendering(): Unit = {
    displayStatusRef.updateAndGet(s => s.codeSet(true))
  }

  def withoutRendering(f: () => Unit): Unit = {
    displayStatusRef.updateAndGet(s => s.codeSet(false))
    try {
      f()
    } finally {
      displayStatusRef.updateAndGet(s => s.codeSet(true))
    }
  }

  def displayStatus: DisplayStatus =
    displayStatusRef.get

  def set2DViewEnabled(enabled: Boolean): Unit = {
    if (enabled) {
      viewManager.setPrimary(view)
      viewManager.remove(glView)

      view.dirty()

      if (displayStatusRef.get.shouldRender(false)) {
        view.thaw()
      }

      if (! (world.observer.perspective.isInstanceOf[AgentFollowingPerspective])) {
        world.observer.home()
      }
      viewWidget.setVisible(true)
    } else {
      viewManager.setPrimary(glView)

      if (!dualView) {
        viewManager.remove(view)
        view.freeze()
      }
      viewWidget.setVisible(dualView);
    }
    view.renderPerspective = enabled
    viewWidget.settings.refreshViewProperties(!enabled)
    new Enable2DEvent(enabled).raise(this)
  }

  // this is called *only* from job thread - ST 8/20/03, 1/15/04
  def updateDisplay(haveWorldLockAlready: Boolean, force: Boolean): Unit = {
    view.dirty()
    val displayStatus = displayStatusRef.get
    val displayIsRendering = displayStatus.shouldRender(force)
    val shouldUpdate = updateManager.shouldUpdateNow
    if (shouldUpdate && displayIsRendering) {
      if (haveWorldLockAlready) {
        try {
          waitFor(new CommandRunnable() {
            def run(): Unit = {
              viewManager.incrementalUpdateFromEventThread()
            }
          })
          // don't block the event thread during a smoothing pause
          // or the UI will go sluggish (issue #1263) - ST 9/21/11
          while(! updateManager.isDoneSmoothing()) {
            ThreadUtils.waitForQueuedEvents(world)
          }
        } catch {
          case ex: org.nlogo.nvm.HaltException => org.nlogo.api.Exceptions.ignore(ex)
          case ex: LogoException => throw new IllegalStateException(ex)
        }
      } else {
        viewManager.incrementalUpdateFromJobThread()
      }
      updateManager.pause()
    } else if (! shouldUpdate) {
      if (displayStatus.trackSkippedFrames) {
        viewManager.framesSkipped()
      }
    }
  }
}
