// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, EventQueue => AWTEventQueue, FileDialog => AwtFileDialog, Frame }
import java.awt.event.ActionEvent
import java.awt.image.BufferedImage
import java.io.{ IOException, InputStream, PrintWriter, Reader }
import java.lang.Thread
import java.net.MalformedURLException
import java.nio.file.Paths
import java.util.Locale
import java.util.concurrent.TimeoutException
import javax.swing.{ AbstractAction, Action, Timer }
import javax.swing.border.LineBorder

import org.nlogo.agent.{ Agent, BooleanConstraint, ImporterJ, OutputObject, SliderConstraint, World }
import org.nlogo.api.{ Agent => ApiAgent, AgentFollowingPerspective, CommandRunnable, ControlSet, DrawingInterface,
                       Exceptions, FileIO, JobOwner, LogoException, ModelReader, ModelSections,
                       ModelType, PreviewCommands, RendererInterface, ReporterRunnable, SimpleJobOwner,
                       TrailDrawerInterface, WorldPropertiesInterface }
import org.nlogo.awt.{ EventQueue, Hierarchy, UserCancelException }
import org.nlogo.core.{ AgentKind, CompilerException, File, I18N, Model, ModelSettings,
                        Shape, UpdateMode, WorldDimensions }
import org.nlogo.nvm.{ Context, HaltException, Instruction, Procedure }
import org.nlogo.shape.ShapeConverter
import org.nlogo.swing.{ CustomOptionPane, FileDialog, ModalProgressTask, OptionPane, ScrollPane, TextArea, Utils }
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.Event.LinkChild
import org.nlogo.window.Events.{ AboutToQuitEvent, AddBooleanConstraintEvent, AddChooserConstraintEvent,
                                 AddInputBoxConstraintEvent, AddJobEvent, AddSliderConstraintEvent, AfterLoadEvent,
                                 AppEvent, BeforeLoadEvent, CompiledEvent, Enable2DEvent, ExportPlotEvent,
                                 ExportWidgetEvent, JobRemovedEvent, JobStoppingEvent, LoadModelEvent, OpenModelEvent,
                                 OutputEvent,  PatchesCreatedEvent, PeriodicUpdateEvent, RemoveConstraintEvent,
                                 RemoveAllJobsEvent, RemoveJobEvent, RuntimeErrorEvent, TickStateChangeEvent }
import org.nlogo.workspace.{ AbstractWorkspaceScala, ExportOutput, HubNetManagerFactory }

import scala.collection.immutable.ListMap
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.{ Duration, MILLISECONDS }
import scala.util.{ Failure, Success }

object GUIWorkspace {
  sealed trait KioskLevel

  object KioskLevel {
    object None extends KioskLevel
    object Moderate extends KioskLevel
  }
}

abstract class GUIWorkspace(world: World, kioskLevel: GUIWorkspace.KioskLevel, frame: Frame, linkParent: Component,
                            hubNetManagerFactory: HubNetManagerFactory, externalFileManager: ExternalFileManager,
                            listenerManager: NetLogoListenerManager, errorDialogManager: ErrorDialogManager,
                            controlSet: ControlSet)
  extends AbstractWorkspaceScala(world, hubNetManagerFactory)
  with LinkChild with AboutToQuitEvent.Handler with AddJobEvent.Handler with AfterLoadEvent.Handler
  with BeforeLoadEvent.Handler with JobStoppingEvent.Handler with RemoveAllJobsEvent.Handler
  with RemoveJobEvent.Handler with AddSliderConstraintEvent.Handler with RemoveConstraintEvent.Handler
  with AddBooleanConstraintEvent.Handler with AddChooserConstraintEvent.Handler with AddInputBoxConstraintEvent.Handler
  with CompiledEvent.Handler with ExportPlotEvent.Handler with ExportWidgetEvent.Handler with LoadModelEvent.Handler
  with TrailDrawerInterface with DrawingInterface with ModelSections.ModelSaveable {

  val viewManager = new ViewManager

  val plotExportControls = new PlotExportControls(plotManager)

  private var _snapOn: Boolean = true

  val viewWidget = new ViewWidget(this)

  val view = viewWidget.view

  var glView: GLViewManagerInterface = null

  private val periodicUpdater = new PeriodicUpdater(jobManager)

  // ensure that any skipped frames get painted eventually
  // 10 checks a second seems like plenty
  private val repaintTimer = new Timer(100, new AbstractAction {
    override def actionPerformed(e: ActionEvent): Unit = {
      if (world.displayOn && displaySwitchOn && !jobManager.anyPrimaryJobs())
        viewManager.paintImmediately(world.observer.updatePosition())
    }
  })

  // Lifeguard ensures the engine comes up for air every so often.
  // we use a separate thread and not a Swing timer because Swing
  // timers run on the event thread, but we need to make sure the
  // engine comes up for air even when the event thread is blocked,
  // since often the reason the event thread is blocked is exactly
  // because it needs the engine to come up for air! - ST 9/4/07
  // forcing the engine to come up for air serves several purposes;
  // it makes sure Tools -> Halt as opportunities to take effect,
  // and it also gives us opportunities for view updates when we're
  // using continuous updates - ST 3/1/11
  private val lifeguard = new Thread("Lifeguard") {
    override def run(): Unit = {
      try {
        while (true) {
          if (jobManager.anyPrimaryJobs())
            world.comeUpForAir = true

          // 100 times a second seems like plenty
          Thread.sleep(10)
        }
      } catch {
        case e: InterruptedException =>
          // ignore because we may be interrupted during
          // applet shutdown, e.g. in Camino - ST 11/29/07
          Exceptions.ignore(e)
      }
    }
  }

  private var _frameRate = 30.0
  private var _dualView = false

  private val updateRunner = new Runnable {
    override def run(): Unit = {
      new PeriodicUpdateEvent().raise(GUIWorkspace.this)
    }
  }

  private var periodicUpdatesEnabled = false
  private var lastTicksListenersHeard = -1.0

  val hubNetControlCenterAction = new HubNetControlCenterAction(this)
  val switchTo3DViewAction = new AbstractAction(I18N.gui.get("menu.tools.3DView.switch")) {
    override def actionPerformed(e: ActionEvent): Unit =
      open3DView()
  }

  def getSwitchTo3DViewAction: Action =
    switchTo3DViewAction

  hubNetControlCenterAction.setEnabled(false)

  viewManager.setPrimary(view)

  periodicUpdater.start()

  world.trailDrawer(this)

  repaintTimer.start()
  lifeguard.start()

  def getFrame: Frame =
    frame

  def setSnapOn(snapOn: Boolean): Unit =
    _snapOn = snapOn

  def snapOn: Boolean =
    _snapOn

  def init(glView: GLViewManagerInterface): Unit =
    this.glView = glView

  def getGlView: GLViewManagerInterface =
    glView

  def frameRate: Double =
    _frameRate

  def frameRate(frameRate: Double): Unit = {
    _frameRate = frameRate

    updateManager.recompute()
  }

  def updateManager: UpdateManagerInterface
  def newRenderer: RendererInterface

  def stamp(agent: ApiAgent, erase: Boolean): Unit = {
    view.renderer.prepareToPaint(view, view.renderer.trailDrawer.getWidth, view.renderer.trailDrawer.getHeight)
    view.renderer.trailDrawer.stamp(agent, erase)

    hubNetManager.foreach(_.sendStamp(agent, erase))
  }

  override def importWorld(filename: String): Unit = {
    super.importWorld(filename)

    new TickStateChangeEvent(world.tickCounter.ticks > -1).raiseLater(this)
  }

  override def importWorld(reader: Reader): Unit = {
    super.importWorld(reader)

    new TickStateChangeEvent(world.tickCounter.ticks > -1).raiseLater(this)
  }

  override def importDrawing(is: InputStream, mimeType: Option[String]): Unit =
    view.renderer.trailDrawer.importDrawing(is, mimeType)

  override def importDrawing(is: InputStream): Unit =
    importDrawing(is, None)

  override def importDrawing(file: File): Unit =
    view.renderer.trailDrawer.importDrawing(file)

  override def importDrawingBase64(base64: String): Unit =
    view.renderer.trailDrawer.importDrawingBase64(base64)

  override def importerErrorHandler: ImporterJ.ErrorHandler = new ImporterJ.ErrorHandler {
    def showError(title: String, errorDetails: String, fatalError: Boolean): Boolean = {
      val options = {
        if (fatalError) {
          OptionPane.Options.Ok
        } else {
          OptionPane.Options.OkCancel
        }
      }

      val textArea = new TextArea(0, 0, errorDetails) {
        setEditable(false)
      }

      val scrollPane = new ScrollPane(textArea) {
        setBorder(new LineBorder(InterfaceColors.textAreaBorderNoneditable()))
        setBackground(InterfaceColors.textAreaBackground())
      }

      if (AWTEventQueue.isDispatchThread) {
        new CustomOptionPane(getFrame, title, scrollPane, options).getSelectedIndex == 0
      } else {
        var result = false

        AWTEventQueue.invokeAndWait(() => {
          result = new CustomOptionPane(getFrame, title, scrollPane, options).getSelectedIndex == 0
        })

        result
      }
    }
  }

  override def exportDrawing(filename: String, format: String): Unit =
    FileIO.writeImageFile(view.renderer.trailDrawer.getAndCreateDrawing(true), filename, format)

  override def exportDrawingToCSV(writer: PrintWriter): Unit =
    view.renderer.trailDrawer.exportDrawingToCSV(writer)

  override def exportOutputAreaToCSV(writer: PrintWriter): Unit =
    new Events.ExportWorldEvent(writer).raise(this)

  def getAndCreateDrawing(): BufferedImage =
    getAndCreateDrawing(true)

  def getAndCreateDrawing(dirty: Boolean): BufferedImage =
    view.renderer.trailDrawer.getAndCreateDrawing(dirty)

  override def clearDrawing(): Unit = {
    world.clearDrawing()
    view.renderer.trailDrawer.clearDrawing()

    hubNetManager.foreach(_.sendClear())
  }

  override def resetTicks(context: Context): Unit = {
    super.resetTicks(context)

    new TickStateChangeEvent(true).raiseLater(this)
  }

  override def clearTicks(): Unit = {
    super.clearTicks()

    new Events.TickStateChangeEvent(false).raiseLater(this)
  }

  override def clearAll(): Unit = {
    super.clearAll()

    new Events.TickStateChangeEvent(false).raiseLater(this)
  }

  def sendPixels: Boolean =
    view.renderer.trailDrawer.sendPixels

  def sendPixels(dirty: Boolean): Unit =
    view.renderer.trailDrawer.sendPixels(dirty)

  override def dispose(): Unit = {
    periodicUpdater.stop()
    repaintTimer.stop()
    lifeguard.interrupt()
    lifeguard.join()

    super.dispose()
  }

  override def isHeadless: Boolean =
    false

  def waitFor(runnable: Runnable): Unit =
    ThreadUtils.waitFor(this, runnable)

  def waitFor(runnable: CommandRunnable): Unit =
    ThreadUtils.waitFor(this, runnable)

  def waitForResult[T](runnable: ReporterRunnable[T]): T =
    ThreadUtils.waitForResult(this, runnable)

  def waitForQueuedEvents(): Unit =
    ThreadUtils.waitForQueuedEvents(this)

  /// Event.LinkChild stuff

  def getLinkParent: AnyRef =
    linkParent

  /**
   * Displays a warning to the user, allowing her to continue or cancel.
   * This provides the nice graphical warning dialog for when we're GUI.
   * Returns true if the user OKs it.
   */
  override def warningMessage(message: String): Boolean = {
    new OptionPane(getFrame, I18N.gui.get("common.messages.warning"), message,
                   OptionPane.Options.OkCancel, OptionPane.Icons.Warning).getSelectedIndex == 0
  }

  def resizeView(): Unit = {
    EventQueue.mustBeEventDispatchThread()

    viewWidget.settings.resizeWithProgress(true)
  }

  def patchSize(patchSize: Double): Unit =
    viewWidget.settings.patchSize(patchSize)

  def patchSize: Double =
    world.patchSize

  def setDimensions(d: WorldDimensions): Unit = {
    // this may be called from _resizeworld in which case we're
    // already on the event thread - ST 7/21/09
    if (AWTEventQueue.isDispatchThread) {
      viewWidget.settings.setDimensions(d)
    } else {
      try {
        EventQueue.invokeAndWait(() => {
          viewWidget.settings.setDimensions(d)
        })
      } catch {
        case e: InterruptedException =>
          Exceptions.handle(e)
      }
    }
  }

  def setDimensions(d: WorldDimensions, patchSize: Double): Unit = {
    // this may be called from _setpatchsize in which case we're
    // already on the event thread - ST 7/21/09
    if (AWTEventQueue.isDispatchThread) {
      viewWidget.settings.setDimensions(d, patchSize)
    } else {
      try {
        EventQueue.invokeAndWait(() => {
          viewWidget.settings.setDimensions(d, patchSize)
        })
      } catch {
        case e: InterruptedException =>
          Exceptions.handle(e)
      }
    }
  }

  def patchesCreatedNotify(): Unit =
    new PatchesCreatedEvent().raise(this)

  def compilerTestingMode: Boolean =
    false

  override def getPropertiesInterface: WorldPropertiesInterface =
    viewWidget.settings

  def changeTopology(wrapX: Boolean, wrapY: Boolean): Unit = {
    world.changeTopology(wrapX, wrapY)
    viewWidget.view.renderer.changeTopology(wrapX, wrapY)
  }

  /// very kludgy stuff for communicating with stuff in app
  /// package without having any compile-time dependencies on it

  // called from an "other" thread (neither event thread nor job thread)
  override def open(path: String, shouldAutoInstallLibs: Boolean): Unit = {
    try {
      EventQueue.invokeAndWait(() => {
        new OpenModelEvent(path, shouldAutoInstallLibs).raise(this)
      })
    } catch {
      case e: InterruptedException =>
        throw new IllegalStateException(e)
    }
  }

  // Right now I only need this for HeadlessWorkspace, for parallel BehaviorSpace - ST 3/12/09
  override def openString(modelContents: String): Unit =
    throw new UnsupportedOperationException

  override def openModel(model: Model, shouldAutoInstallLibs: Boolean): Unit =
    throw new UnsupportedOperationException

  override def renderer: RendererInterface =
    view.renderer

  // called from the job thread
  def reload(): Unit =
    new AppEvent(AppEventType.RELOAD, Array()).raiseLater(this)

  // called from the job thread
  override def magicOpen(name: String): Unit =
    new AppEvent(AppEventType.MAGIC_OPEN, Array(name)).raiseLater(this)

  /// painting

  def displaySwitchOn: Boolean =
    viewManager.getPrimary.displaySwitch

  def displaySwitchOn(on: Boolean): Unit =
    viewManager.getPrimary.displaySwitch(on)

  def set2DViewEnabled(enabled: Boolean): Unit = {
    if (enabled) {
      displaySwitchOn(glView.displayOn)

      viewManager.setPrimary(view)
      viewManager.remove(glView)

      view.dirty()

      if (glView.displayOn)
        view.thaw()

      if (!world.observer.perspective.isInstanceOf[AgentFollowingPerspective])
        world.observer.home()

      viewWidget.setVisible(true)

      try {
        viewWidget.displaySwitch.setOn(glView.displaySwitch)
      } catch {
        case e: IllegalStateException =>
          Exceptions.ignore(e)
      }
    } else {
      viewManager.setPrimary(glView)

      if (!_dualView) {
        viewManager.remove(view)
        view.freeze()
      }

      glView.displaySwitch(viewWidget.displaySwitch.isSelected)

      viewWidget.setVisible(_dualView)
    }

    view.renderPerspective = enabled

    new Enable2DEvent(enabled).raise(this)
  }

  def dualView: Boolean =
    return dualView

  def dualView(on: Boolean): Unit = {
    if (on != _dualView) {
      _dualView = on

      if (on) {
        view.thaw()
        viewManager.setSecondary(view)
      } else {
        view.freeze()
        viewManager.remove(view)
      }

      viewWidget.setVisible(on)
    }
  }

  // when we've got two views going the mouse reporters should
  // be smart about which view we might be in and return something that makes
  // sense ev 12/20/07
  override def mouseDown: Boolean =
    viewManager.mouseDown

  override def mouseInside: Boolean =
    viewManager.mouseInside

  override def mouseXCor: Double =
    viewManager.mouseXCor

  override def mouseYCor: Double =
    viewManager.mouseYCor

  // shouldn't have to fully qualify UpdateMode here, but we were having
  // intermittent compile failures on this line since upgrading to
  // Scala 2.8.0.RC1 - ST 4/16/10
  override def updateMode(updateMode: UpdateMode): Unit = {
    super.updateMode(updateMode)

    updateManager.recompute()
  }

  // Translate between the physical position of the speed slider and
  // the abstract speed value.  The slider has an area in the center
  // where the speed is 0 regardless of the precise position, and the
  // scale is different. - ST 3/3/11
  def speedSliderPosition(): Double = {
    val s = updateManager.speed * 2

    if (s > 0) {
      s + 10
    } else if (s < 0) {
      s - 10
    } else {
      s
    }
  }

  def speedSliderPosition(speed: Double): Unit =
    updateManager.speed = speed

  // this is called *only* from job thread - ST 8/20/03, 1/15/04
  def updateDisplay(haveWorldLockAlready: Boolean): Unit = {
    view.dirty()

    if (!world.displayOn)
      return

    if (!updateManager.shouldUpdateNow) {
      viewManager.framesSkipped()

      return
    }

    if (!displaySwitchOn)
      return

    if (haveWorldLockAlready) {
      try {
        waitFor(new CommandRunnable {
          override def run(): Unit = {
            viewManager.incrementalUpdateFromEventThread()
          }
        })

        // don't block the event thread during a smoothing pause
        // or the UI will go sluggish (issue #1263) - ST 9/21/11
        while (!updateManager.isDoneSmoothing())
          ThreadUtils.waitForQueuedEvents(this)
      } catch {
        case e: HaltException =>
          Exceptions.ignore(e)
        case e: LogoException =>
          throw new IllegalStateException(e)
      }
    } else {
      viewManager.incrementalUpdateFromJobThread()
    }

    updateManager.pause()
  }

  /// Job manager stuff

  def setPeriodicUpdatesEnabled(periodicUpdatesEnabled: Boolean): Unit =
    this.periodicUpdatesEnabled = periodicUpdatesEnabled

  // this is called on the job thread - ST 9/30/03
  def periodicUpdate(): Unit = {
    if (periodicUpdatesEnabled)
      ThreadUtils.waitFor(this, updateRunner)
  }

  // this is called on the job thread when the engine comes up for air - ST 1/10/07
  override def breathe(): Unit = {
    jobManager.maybeRunSecondaryJobs()

    if (updateMode == UpdateMode.Continuous) {
      updateManager.pseudoTick()
      updateDisplay(true)
    }

    world.comeUpForAir = updateManager.shouldComeUpForAirAgain

    notifyListeners()
  }

  // called only from job thread, by such primitives as
  // _exportinterface and _usermessage, which need to make sure the
  // whole UI is up-to-date before proceeding - ST 8/30/07, 3/3/11
  override def updateUI(): Unit = {
    // this makes the tick counter et al update
    ThreadUtils.waitFor(this, updateRunner)

    // resetting first ensures that if we are allowed to update the view, we will
    updateManager.reset()
    requestDisplayUpdate(true)
  }

  // on the job thread,
  // - updateUI() calls requestDisplayUpdate(true)
  // - _display, _tick, _reset-ticks call requestDisplayUpdate(true)
  // - _tickadvance calls requestDisplayUpdate(false)
  // - ST 1/4/07, 3/3/11
  override def requestDisplayUpdate(force: Boolean): Unit = {
    if (force)
      updateManager.pseudoTick()

    updateDisplay(true) // haveWorldLockAlready = true
    notifyListeners()
  }

  private def notifyListeners(): Unit = {
    val ticks = world.tickCounter.ticks

    if (ticks != lastTicksListenersHeard) {
      lastTicksListenersHeard = ticks
      listenerManager.tickCounterChanged(ticks)
    }

    listenerManager.possibleViewUpdate()
  }

  override def halt(): Unit = {
    jobManager.interrupt()

    ModalProgressTask.onUIThread(getFrame, "Halting...", new Runnable {
      override def run(): Unit = {
        GUIWorkspace.super.halt()

        view.dirty()
        view.repaint()
      }
    })
  }

  // for notification of a changed shape
  def shapeChanged(shape: Shape): Unit =
    viewManager.shapeChanged(shape)

  def handle(e: AfterLoadEvent): Unit = {
    setPeriodicUpdatesEnabled(true)

    world.observer.resetPerspective()

    updateManager.reset()
    updateManager.speed = 0

    // even when we're in 3D close the window first
    // then reopen it as the shapes won't get loaded
    // properly otherwise ev 2/24/06
    if (glView != null)
      glView.close()

    if (world.program.dialect.is3D)
      open3DView()

    try {
      evaluateCommands(new SimpleJobOwner("startup", world.mainRNG, AgentKind.Observer),
                       "without-interruption [ startup ]", false)
    } catch {
      case e: CompilerException =>
        Exceptions.ignore(e)
    }
  }

  private def open3DView(): Unit = {
    try {
      glView.open()
      set2DViewEnabled(false)
    } catch {
      case e: JOGLLoadingException =>
        Utils.alert("3D View", e.getMessage, "" + e.getCause, I18N.gui.get("common.buttons.continue"))

        switchTo3DViewAction.setEnabled(false)
    }
  }

  override def addCustomShapes(filename: String): Unit = {
    try {
      glView.addCustomShapes(fileManager.attachPrefix(filename))
    } catch {
      case e: MalformedURLException =>
        throw new IllegalStateException(e)
    }
  }

  // DrawingInterface for 3D renderer
  def colors: Array[Int] =
    view.renderer.trailDrawer.colors

  def isDirty: Boolean =
    view.renderer.trailDrawer.isDirty

  def isBlank: Boolean =
    view.renderer.trailDrawer.isBlank

  def markClean(): Unit =
    view.renderer.trailDrawer.markClean()

  def markDirty(): Unit =
    view.renderer.trailDrawer.markDirty()

  def getWidth: Int =
    view.renderer.trailDrawer.getWidth

  def getHeight: Int =
    view.renderer.trailDrawer.getHeight

  def readImage(is: InputStream): Unit =
    view.renderer.trailDrawer.readImage(is)

  def readImage(image: BufferedImage): Unit =
    view.renderer.trailDrawer.readImage(image)

  def rescaleDrawing(): Unit =
    view.renderer.trailDrawer.rescaleDrawing()

  def drawLine(x0: Double, y0: Double, x1: Double, y1: Double, color: Object, size: Double, mode: String): Unit = {
    view.renderer.trailDrawer.drawLine(x0, y0, x1, y1, color, size, mode)

    hubNetManager.foreach(_.sendLine(x0, y0, x1, y1, color, size, mode))
  }

  def setColors(colors: Array[Int], width: Int, height: Int): Unit =
    view.renderer.trailDrawer.setColors(colors, width, height)

  def getDrawing: AnyRef =
    view.renderer.trailDrawer.getDrawing

  // called on job thread, but without world lock - ST 9/12/07
  def ownerFinished(owner: JobOwner): Unit = {
    new JobRemovedEvent(owner).raiseLater(this)

    if (owner.ownsPrimaryJobs) {
      updateManager.reset()
      updateDisplay(false)
    }
  }

  def handle(e: AddJobEvent): Unit = {
    val owner = e.owner

    val agents = owner match {
      case w: JobWidget if e.agents == null && w.useAgentClass =>
        world.agentSetOfKind(w.kind)
      case _ =>
        e.agents
    }

    if (owner.ownsPrimaryJobs) {
      if (e.procedure != null) {
        jobManager.addJob(owner, agents, this, e.procedure)
      } else {
        new JobRemovedEvent(owner).raiseLater(this)
      }
    } else {
      jobManager.addSecondaryJob(owner, agents, this, e.procedure)
    }
  }

  def handle(e: RemoveJobEvent): Unit = {
    if (e.owner.ownsPrimaryJobs) {
      jobManager.finishJobs(e.owner)
    } else {
      jobManager.finishSecondaryJobs(e.owner)
    }
  }

  def handle(e: JobStoppingEvent): Unit =
    jobManager.stoppingJobs(e.owner)

  def handle(e: RemoveAllJobsEvent): Unit = {
    jobManager.haltSecondary()
    jobManager.haltPrimary()
  }

  def handle(e: AddBooleanConstraintEvent): Unit = {
    // now we set the constraint in the observer, so that it is enforced.
    val index = world.observerOwnsIndexOf(e.varname.toUpperCase(Locale.ENGLISH))

    if (index != -1)
      world.observer.setConstraint(index, new BooleanConstraint(e.defaultValue))
  }

  def handle(e: AddInputBoxConstraintEvent): Unit = {
    // now we set the constraint in the observer, so that it is enforced.
    val index = world.observerOwnsIndexOf(e.varname.toUpperCase(Locale.ENGLISH))

    if (index != -1)
      world.observer.setConstraint(index, e.constraint)
  }

  def handle(e: AddChooserConstraintEvent): Unit = {
    // now we set the constraint in the observer, so that it is enforced.
    val index = world.observerOwnsIndexOf(e.varname.toUpperCase(Locale.ENGLISH))

    if (index != -1)
      world.observer.setConstraint(index, e.constraint)
  }

  def handle(e: AddSliderConstraintEvent): Unit = {
    try {
      val con = SliderConstraint.makeSliderConstraint(world.observer, e.minSpec, e.maxSpec, e.incSpec, e.value,
                                                      e.slider.name, this, this)

      e.slider.removeAllErrors()
      e.slider.setSliderConstraint(con)

      // now we set the constraint in the observer, so that it is enforced.
      val index = world.observerOwnsIndexOf(e.varname.toUpperCase(Locale.ENGLISH))

      if (index != -1)
        world.observer.setConstraint(index, con)
    } catch {
      case err: SliderConstraint.ConstraintExceptionHolder =>
        for (cce <- err.getErrors)
          e.slider.error(cce.spec.fieldName, cce)
    }
  }

  def handle(e: RemoveConstraintEvent): Unit = {
    val index = world.observerOwnsIndexOf(e.varname.toUpperCase(Locale.ENGLISH))

    if (index != -1)
      world.observer.setConstraint(index, null)
  }

  def handle(e: CompiledEvent): Unit =
    codeBits.clear()

  def handle(e: LoadModelEvent): Unit = {
    loadFromModel(e.model)

    world.turtleShapes.replaceShapes(e.model.turtleShapes.map(ShapeConverter.baseShapeToShape))
    world.linkShapes.replaceShapes(e.model.linkShapes.map(ShapeConverter.baseLinkShapeToLinkShape))

    e.model.optionalSectionValue[ModelSettings]("org.nlogo.modelsection.modelsettings") match {
      case Some(settings: ModelSettings) => setSnapOn(settings.snapToGrid)
      case _ =>
    }
  }

  def handle(e: ExportWidgetEvent): Unit = {
    try {
      val guessedName = guessExportName(e.widget.getDefaultExportName)
      val exportPath = FileDialog.showFiles(e.widget, I18N.gui.get("menu.file.export"), AwtFileDialog.SAVE, guessedName)
      val exportToPath = Future.successful(exportPath)

      e.widget match {
        case pw: PlotWidget =>
          new ExportPlotEvent(PlotWidgetExport.ExportSinglePlot(pw.plot), exportPath, {() => }).raise(pw)

        case ow: OutputWidget =>
          exportToPath
            .map((filename: String) => (filename, ow.valueText))(using SwingUnlockedExecutionContext)
            .onComplete({
              case Success((filename: String, text: String)) =>
                ExportOutput.silencingErrors(filename, text)
              case Failure(_) =>
            })(using NetLogoExecutionContext.backgroundExecutionContext)

        case ib: InputBox =>
          exportToPath
            .map((filename: String) => (filename, ib.valueText))(using SwingUnlockedExecutionContext)
            .map[Unit]({ // background
              case (filename: String, text: String) => FileIO.writeFile(filename, text, true); ()
            })(using NetLogoExecutionContext.backgroundExecutionContext).failed.foreach({ // on UI Thread
              case ex: java.io.IOException =>
                ExportControls.displayExportError(Hierarchy.getFrame(ib),
                  I18N.gui.get("menu.file.export.failed"),
                  I18N.gui.getN("tabs.input.export.error", ex.getMessage))
              case _ =>
            })(using SwingUnlockedExecutionContext)

        case _ =>
      }
    } catch {
      case uce: UserCancelException => Exceptions.ignore(uce)
    }
  }

  private def plotExportOperation(e: ExportPlotEvent): Future[Unit] = {
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
          } (using new LockedBackgroundExecutionContext(world))

      case PlotWidgetExport.ExportSinglePlot(plot) =>
        Future {
          runAndComplete(
            () => super.exportPlot(plot.name, e.exportFilename),
            (ex) => plotExportControls.singlePlotExportFailed(getFrame, e.exportFilename, plot, ex))
        } (using new LockedBackgroundExecutionContext(world))
    }
  }

  def handle(e: ExportPlotEvent): Unit =
    plotExportOperation(e)

  def getExportWindowFrame: Component =
    viewManager.getPrimary.getExportWindowFrame

  def exportView: BufferedImage =
    viewManager.getPrimary.exportView

  def exportView(filename: String, format: String): Unit = {
    if (jobManager.onJobThread) {
      val viewFuture = Future(viewManager.getPrimary.exportView)(using new SwingLockedExecutionContext(world))
      val image = awaitFutureFromJobThread(viewFuture)

      FileIO.writeImageFile(image, filename, format)
    } else {
      exportViewFromUIThread(filename, format)
    }
  }

  def exportViewFromUIThread(filename: String, format: String, onCompletion: () => Unit = {() => }): Unit = {
    val f = Future[BufferedImage](viewManager.getPrimary.exportView)(using new SwingLockedExecutionContext(world))

    f.foreach { image =>
      try {
        FileIO.writeImageFile(image, filename, format)
      } catch {
        case ex: java.io.IOException =>
          onCompletion()
          ExportControls.displayExportError(getExportWindowFrame, ex.getMessage)
      } finally {
        onCompletion()
      }
    }(using NetLogoExecutionContext.backgroundExecutionContext)
  }

  def doExportView(exportee: LocalViewInterface): Unit = {
    val exportPathOption =
      try {
        val userPath      = FileDialog.showFiles(getExportWindowFrame, I18N.gui.get("menu.file.export.view"),
                                                 AwtFileDialog.SAVE, guessExportName("view.png"))
        val extensionPath = FileIO.ensureExtension(userPath, "png")
        val path          = Paths.get(extensionPath)

        if (!path.toFile.exists || userPath == extensionPath) {
          Some(extensionPath)
        } else {
          FileDialog.confirmFileOverwrite(frame, extensionPath)
        }

      } catch {
        case ex: UserCancelException =>
          Exceptions.ignore(ex)
          None
      }

    exportPathOption.foreach { exportPath =>
      ModalProgressTask.runForResultOnBackgroundThread(
        getFrame, I18N.gui.get("dialog.interface.export.task"), () => (exportee.exportView, exportPath),
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
    var res: Option[A] = None

    while (res.isEmpty) {
      world.synchronized { world.wait(50) }

      try {
        res = Some(awaitFuture(future, 1))
      } catch {
        case e: TimeoutException => // ignore
      }
    }

    res.get
  }

  private def awaitFuture[A](future: Future[A], millis: Long): A =
    Await.result(future, Duration(millis, MILLISECONDS))

  def exportInterface(filename: String): Unit = {
    if (jobManager.onJobThread) {
      // we treat the job thread differently because it will be holding the world lock
      FileIO.writeImageFile(awaitFutureFromJobThread(controlSet.userInterface), filename, "png")
    } else {
      exportInterfaceFromUIThread(filename)
    }
  }

  def exportInterfaceFromUIThread(filename: String, onCompletion: () => Unit = { () => }): Unit = {
    controlSet.userInterface.foreach { uiImage =>
      try {
        FileIO.writeImageFile(uiImage, filename, "png")
      } catch {
        case ex: IOException =>
          onCompletion()
          ExportControls.displayExportError(getExportWindowFrame, ex.getMessage)
      } finally {
        onCompletion()
      }
    }(using NetLogoExecutionContext.backgroundExecutionContext)
  }

  def exportOutput(filename: String): Unit = {
    if (jobManager.onJobThread) {
      val text = awaitFutureFromJobThread(controlSet.userOutput)
      ExportOutput.throwingErrors(filename, text)
    } else {
      ModalProgressTask.runForResultOnBackgroundThread(frame, I18N.gui.get("dialog.interface.export.task"),
        () => controlSet.userOutput,
        (textFuture: Future[String]) => ExportOutput.silencingErrors(filename, awaitFuture(textFuture, 1000)))
    }
  }

  override def getSource(filename: String): String = {
    if (filename == "")
      throw new IllegalArgumentException("cannot provide source for empty filename")

    externalFileManager.getSource(filename).getOrElse(super.getSource(filename))
  }

  override def guessExportName(defaultName: String): String = {
    if (Option(getModelFileName).contains("empty." + ModelReader.modelSuffix)) {
      defaultName
    } else {
      super.guessExportName(defaultName)
    }
  }

  /// agents

  def closeAgentMonitors(): Unit
  def inspectAgent(agentClass: AgentKind, agent: Agent, radius: Double): Unit

  def inspectAgent(agentClass: AgentKind): Unit =
    inspectAgent(agentClass, null, (world.worldWidth - 1) / 2)

  /// output

  def clearOutput(): Unit = {
    val event = new OutputEvent(true, null, false, false, System.currentTimeMillis)

    // This method can be called when we are ALREADY in the AWT
    // event thread, so check before we block on it. -- CLB 07/18/05
    if (!AWTEventQueue.isDispatchThread) {
      ThreadUtils.waitFor(this, new Runnable {
        override def run(): Unit =
          event.raise(GUIWorkspace.this)
      })
    } else {
      event.raise(this)
    }
  }

  override def sendOutput(oo: OutputObject, toOutputArea: Boolean): Unit = {
    val event = new OutputEvent(false, oo, false, !toOutputArea, System.currentTimeMillis)

    // This method can be called when we are ALREADY in the AWT
    // event thread, so check before we block on it. -- CLB 07/18/05
    if (!AWTEventQueue.isDispatchThread) {
      ThreadUtils.waitFor(this, new Runnable {
        override def run(): Unit =
          event.raise(GUIWorkspace.this)
      })
    } else {
      event.raise(this)
    }
  }

  /// runtime error handling

  def runtimeError(owner: JobOwner, context: Context, instruction: Instruction, e: Exception): Unit = {
    // this method is called from the job thread, so we need to switch over
    // to the event thread.  but in the error dialog we want to be able to
    // show the original thread in which it happened, so we hang on to the
    // current thread before switching - ST 7/30/04
    val thread = Thread.currentThread

    EventQueue.invokeLater(() => {
      runtimeErrorPrivate(owner, context, instruction, thread, e)
    })
  }

  private def runtimeErrorPrivate(owner: JobOwner, context: Context, instruction: Instruction, thread: Thread,
                                  e: Exception): Unit = {
    fileManager.closeAllFiles()

    // halt, or at least turn graphics back on if they were off
    e match {
      case he: HaltException if he.haltAll =>
        halt() // includes turning graphics back on
      case _ =>
        if (!owner.isInstanceOf[MonitorWidget])
          world.displayOn(true)

        // tell the world!
        val posAndLength =
          if (instruction.token == null) {
            Array(-1, 0)
          } else {
            instruction.getPositionAndLength()
          }

        new RuntimeErrorEvent(owner, context.activation.procedure.owner, posAndLength(0), posAndLength(1))

        // MonitorWidgets always immediately restart their jobs when a runtime error occurs,
        // but we don't want to just stream errors to the command center, so let's not print
        // anything to the command center, and assume that someday MonitorWidgets will do
        // their own user notification - ST 12/16/01
        if (!owner.isInstanceOf[MonitorWidget]) {
          // It doesn't seem like we should need to use invokeLater() here, because
          // we're already on the event thread.  But without using it, at least on
          // Mac 142U1DP3 (and maybe other Mac VMs, and maybe other platforms too,
          // I don't know), the error dialog didn't wind up with the keyboard focus
          // if the Code tab came forward... probably because something that
          // the call to select() in ProceduresTab was doing was doing invokeLater()
          // itself?  who knows... in any case, this seems to fix it - ST 7/30/04
          EventQueue.invokeLater(() => {
            errorDialogManager.show(context, instruction, thread, e)
          })
        }
    }
  }

  /// keep track of model name

  /**
   * sets new model name and type, and, if necessary, disconnects
   * HubNetManager. This must be done at BeforeLoadEvent time, because the
   * model name needs to be available for setting titles and so on by the
   * time we handle LoadBeginEvent.
   */
  def handle(e: BeforeLoadEvent): Unit = {
    setPeriodicUpdatesEnabled(false)

    setModelPath(e.modelPath.orNull)
    setModelType(e.modelType)

    hubNetManager.foreach(_.disconnect())

    jobManager.haltSecondary()
    jobManager.haltPrimary()

    getExtensionManager.reset()
    fileManager.handleModelChange()

    setPreviewCommands(PreviewCommands.Default)

    clearDrawing()

    viewManager.resetMouseCors()

    displaySwitchOn(true)

    setProcedures(new ListMap[String, Procedure])

    lastTicksListenersHeard = -1.0

    plotManager.forgetAll()
  }

  /**
   * sets new model name and type after a save. Once a model is saved,
   * it becomes TYPE_NORMAL. We don't actually handle the event, because
   * it's important that this get sequenced correctly with stuff in
   * App.handle(). Yuck.
   */
  def modelSaved(newModelPath: String): Unit = {
    setModelPath(newModelPath)
    setModelType(ModelType.Normal)
  }

  def handle(e: AboutToQuitEvent): Unit =
    hubNetManager.foreach(_.disconnect())

  override def hubNetRunning_=(running: Boolean): Unit = {
    if (hubNetRunning != running) {
      if (running) {
        viewManager.add(hubNetManager.get)
      } else {
        viewManager.remove(hubNetManager.get)
      }
    }

    super.hubNetRunning_=(running)

    hubNetControlCenterAction.setEnabled(hubNetRunning)
  }
}
