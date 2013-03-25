// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.BorderLayout

import scala.Option.option2Iterable
import scala.collection.JavaConverters.asScalaBufferConverter

import org.nlogo.api
import org.nlogo.awt.UserCancelException
import org.nlogo.mirror.{ FixedViewSettings, Mirrorables, ModelRun, ModelRunIO }
import org.nlogo.swing.Implicits.thunk2runnable
import org.nlogo.util.Exceptions.ignoring
import org.nlogo.window
import org.nlogo.window.{ MonitorWidget, Widget, WidgetWrapperInterface }

import javax.swing.{ JOptionPane, JPanel, JScrollPane, JSplitPane }
import javax.swing.event.{ ChangeEvent, ChangeListener, DocumentEvent, DocumentListener }

class ReviewTab(
  val ws: window.GUIWorkspace,
  saveModel: () => String,
  offerSave: () => Unit,
  selectReviewTab: () => Unit)
  extends JPanel
  with window.ReviewTabInterface
  with window.Events.BeforeLoadEventHandler {

  override def loadedRuns: Seq[api.ModelRun] = tabState.runs
  override def loadRun(inputStream: java.io.InputStream): Unit = {
    val run = ModelRunIO.load(inputStream)
    tabState.addRun(run)
    loadModelIfNeeded(run.modelString)
  }
  override def currentRun: Option[api.ModelRun] = tabState.currentRun

  def workspaceWidgets =
    Option(ws.viewWidget.findWidgetContainer)
      .toSeq.flatMap(_.getWidgetsForSaving.asScala)

  case class WidgetHook(
    val widget: Widget,
    val valueStringGetter: () => String)

  def widgetHooks = workspaceWidgets
    .collect { case m: MonitorWidget => m }
    .map(m => WidgetHook(m, () => m.valueString))

  /**
   * The PlotActionBuffer logs all plotting actions, whether we are
   * recording or not. This is important because if we want to start
   * recording at some point, we need to mirror all actions from the
   * start to bring the plots to their actual state. NP 2012-11-29
   * Same logic goes for the drawingActionBuffer. NP 2013-01-28.
   */
  private val plotActionBuffer = new api.ActionBuffer(ws.plotManager)
  private val drawingActionBuffer = new api.ActionBuffer(ws.drawingActionBroker)
  private val actionBuffers = Vector(plotActionBuffer, drawingActionBuffer)

  def userConfirms(title: String, message: String) =
    JOptionPane.showConfirmDialog(ReviewTab.this, message,
      title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION

  def nameFromPath(path: String) =
    new java.io.File(path).getName
      .replaceAll("\\.[^.]*$", "") // remove extension

  val tabState = new ReviewTabState()

  ws.listenerManager.addListener(
    new api.NetLogoAdapter {
      override def requestedDisplayUpdate() {
        if (tabState.currentlyRecording) {
          updateMonitors()
          // switch from job thread to event thread
          ws.waitFor(() => grab())
          refreshInterface()
        }
      }
      override def modelOpened(name: String) {
        // clearing the ticks doesn't send tickCounterChanged if the ticks
        // were already at -1.0, so we make sure to clear the actions of a
        // potentially "tickless" model when we open a new one.
        actionBuffers.foreach(_.clear())
        tabState.currentRun.foreach(_.stillRecording = false)
      }
      override def tickCounterChanged(ticks: Double) {
        ticks match {
          case -1.0 =>
            actionBuffers.foreach(_.clear())
            tabState.currentRun.foreach(_.stillRecording = false)
          case 0.0 =>
            ws.waitFor(() => startNewRun())
          case _ => // requestedDisplayUpdate() takes care of the rest
        }
      }
    })

  def updateMonitors() {
    widgetHooks
      .collect { case WidgetHook(m: window.MonitorWidget, _) => m }
      .foreach(updateMonitor)
  }

  // only callable from the job thread, and only once evaluator.withContext(...) has properly
  // set up ProcedureRunner's execution context. it would be problematic to try to trigger
  // monitors to update from the event thread, because the event thread is not allowed to
  // block waiting for the job thread. - ST 10/12/12
  def updateMonitor(monitor: MonitorWidget) {
    for {
      reporter <- monitor.reporter
      runner = ws.evaluator.ProcedureRunner
      if runner.hasContext
      result = try {
        runner.report(reporter)
      } catch {
        case _: api.LogoException => "N/A"
      }
    } monitor.value(result)
  }

  def enableRecording() {
    tabState.recordingEnabled = true
    reviewToolBar.enabledCheckBox.setSelected(tabState.recordingEnabled)
  }

  def disableRecording() {
    tabState.recordingEnabled = false
    reviewToolBar.enabledCheckBox.setSelected(tabState.recordingEnabled)
  }

  def startNewRun() {

    val wrapper = org.nlogo.awt.Hierarchy.findAncestorOfClass(
      ws.viewWidget, classOf[window.WidgetWrapperInterface])
    val wrapperPos = wrapper.map(_.getLocation).getOrElse(new java.awt.Point(0, 0))

    // The position is the position of the view, but the image is the
    // whole interface, including the view.
    val view = ws.viewWidget.view
    val viewArea = new java.awt.geom.Area(new java.awt.Rectangle(
      wrapperPos.x + view.getLocation().x, wrapperPos.y + view.getLocation().y,
      view.getWidth, view.getHeight))

    // remove widgets from the clip area of the view:
    val container = ws.viewWidget.findWidgetContainer
    for {
      w <- widgetHooks.map(_.widget)
      bounds = container.getUnzoomedBounds(w)
      widgetArea = new java.awt.geom.Area(bounds)
    } viewArea.subtract(widgetArea)

    val viewSettings = FixedViewSettings(ws.view)

    val image = org.nlogo.awt.Images.paintToImage(
      ws.viewWidget.findWidgetContainer.asInstanceOf[java.awt.Component])

    val name = Option(ws.getModelFileName).map(nameFromPath)
      .orElse(tabState.currentRun.map(_.name))
      .getOrElse("Untitled")
    val run = new ModelRun(name, saveModel(), viewArea, viewSettings, image, "", Map())
    tabState.addRun(run)
    runList.setSelectedValue(run, true)
    refreshInterface()
  }

  def grab() {
    for (run <- tabState.currentRun) {
      try {
        val widgetValues = widgetHooks
          .map(_.valueStringGetter.apply)
          .zipWithIndex
        val mirrorables = Mirrorables.allMirrorables(ws.world, widgetValues)
        val actions = actionBuffers.flatMap(_.grab())
        run.data match {
          case None       => run.start(ws.plotManager.plots, mirrorables, actions)
          case Some(data) => data.append(mirrorables, actions)
        }
      } catch {
        case e: java.lang.OutOfMemoryError =>
          JOptionPane.showMessageDialog(this,
            "Not enough memory. Turning off recording.",
            "Low memory", JOptionPane.WARNING_MESSAGE)
          disableRecording()
      }
      refreshInterface()
    }
  }

  val scrubberPanel = new ScrubberPanel(
    () => tabState.currentFrameIndex,
    () => tabState.currentFrame.flatMap(_.ticks))
  val notesPanel = new NotesPanel(tabState)
  val reviewToolBar = new ReviewToolBar(this)
  val interfacePanel = new InterfacePanel(this)

  scrubberPanel.scrubber.addChangeListener(new ChangeListener {
    def stateChanged(evt: ChangeEvent) {
      tabState.currentRun.foreach(_.currentFrameIndex = scrubberPanel.scrubber.getValue)
      interfacePanel.repaint()
    }
  })

  notesPanel.notesArea.getDocument.addDocumentListener(new DocumentListener {
    private def updateNotesInRun() {
      for (run <- tabState.currentRun) {
        run.generalNotes = notesPanel.notesArea.getText
        reviewToolBar.saveButton.setEnabled(run.dirty)
      }
    }
    def insertUpdate(e: DocumentEvent) { updateNotesInRun() }
    def removeUpdate(e: DocumentEvent) { updateNotesInRun() }
    def changedUpdate(e: DocumentEvent) { updateNotesInRun() }
  })

  def refreshInterface() {
    val run = tabState.currentRun
    val data = tabState.currentRunData
    scrubberPanel.scrubber.refresh(
      value = run.map(_.currentFrameIndex).getOrElse(0),
      max = data.map(_.lastFrameIndex).getOrElse(0),
      enabled = data.filter(_.size > 1).isDefined)
    notesPanel.notesArea.setText(run.map(_.generalNotes).getOrElse(""))
    reviewToolBar.saveButton.setEnabled(run.map(_.dirty).getOrElse(false))
    Seq(notesPanel.notesArea, reviewToolBar.renameButton, reviewToolBar.closeCurrentButton, reviewToolBar.closeAllButton)
      .foreach(_.setEnabled(run.isDefined))
    runList.repaint()
    interfacePanel.repaint()
  }

  def loadModelIfNeeded(modelString: String) {
    val currentModelString = saveModel()
    if (modelString != currentModelString) {
      ignoring(classOf[UserCancelException]) {
        offerSave()
      }
      org.nlogo.window.ModelLoader.load(ReviewTab.this,
        null, api.ModelType.Library, modelString)
      selectReviewTab()
    }
  }

  val runList = new RunList(this)
  object RunListPanel extends JPanel {
    setLayout(new BorderLayout)
    add(new JScrollPane(runList), BorderLayout.CENTER)
  }

  object InterfaceScrollPane extends JScrollPane {
    setViewportView(interfacePanel)
  }

  object RunPanel extends JPanel {
    setLayout(new BorderLayout)
    add(InterfaceScrollPane, BorderLayout.CENTER)
    add(scrubberPanel, BorderLayout.SOUTH)
  }

  object PrimarySplitPane extends JSplitPane(
    JSplitPane.HORIZONTAL_SPLIT,
    RunListPanel,
    SecondarySplitPane) {
    setDividerLocation(200)
  }

  object SecondarySplitPane extends JSplitPane(
    JSplitPane.VERTICAL_SPLIT,
    RunPanel,
    notesPanel) {
    setResizeWeight(1.0)
    setDividerLocation(1.0)
  }

  locally {
    setLayout(new BorderLayout)
    add(reviewToolBar, BorderLayout.NORTH)
    add(PrimarySplitPane, BorderLayout.CENTER)
    actionBuffers.foreach(_.clear()) // make sure object is constructed and subscribed
    refreshInterface()
  }

  override def handle(e: window.Events.BeforeLoadEvent) {
    refreshInterface()
  }
}
