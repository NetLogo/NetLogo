// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.BorderLayout
import java.awt.Color.{ GRAY, WHITE }

import scala.Option.option2Iterable
import scala.collection.JavaConverters.asScalaBufferConverter

import org.nlogo.api
import org.nlogo.awt.UserCancelException
import org.nlogo.mirror.{ FakeWorld, FixedViewSettings, Mirrorables, ModelRun, ModelRunIO }
import org.nlogo.plot.PlotPainter
import org.nlogo.swing.Implicits.thunk2runnable
import org.nlogo.util.Exceptions.ignoring
import org.nlogo.window
import org.nlogo.window.{ MonitorWidget, PlotWidget, Widget, WidgetWrapperInterface }

import javax.swing.{ BorderFactory, JList, JOptionPane, JPanel, JScrollPane, JSplitPane, ListSelectionModel }
import javax.swing.event.{ ChangeEvent, ChangeListener, DocumentEvent, DocumentListener, ListSelectionEvent, ListSelectionListener }

class ReviewTab(
  ws: window.GUIWorkspace,
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

  private def workspaceWidgets =
    Option(ws.viewWidget.findWidgetContainer)
      .toSeq.flatMap(_.getWidgetsForSaving.asScala)

  case class WidgetHook(
    val widget: Widget,
    val valueStringGetter: () => String)

  private def widgetHooks = workspaceWidgets
    .collect { case m: MonitorWidget => m }
    .map(m => WidgetHook(m, () => m.valueString))

  private def plotWidgets = workspaceWidgets
    .collect { case pw: PlotWidget => pw }

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
    RunList.setSelectedValue(run, true)
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

  scrubberPanel.scrubber.addChangeListener(new ChangeListener {
    def stateChanged(evt: ChangeEvent) {
      tabState.currentRun.foreach(_.currentFrameIndex = scrubberPanel.scrubber.getValue)
      InterfacePanel.repaint()
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

  object InterfacePanel extends JPanel {

    def repaintView(g: java.awt.Graphics, viewArea: java.awt.geom.Area) {
      for {
        run <- tabState.currentRun
        frame <- run.currentFrame
        fakeWorld = new FakeWorld(frame.mirroredState)
        paintArea = new java.awt.geom.Area(InterfacePanel.getBounds())
        viewSettings = run.fixedViewSettings
        g2d = g.create.asInstanceOf[java.awt.Graphics2D]
      } {
        paintArea.intersect(viewArea) // avoid spilling outside interface panel
        try {
          g2d.setClip(paintArea)
          g2d.translate(viewArea.getBounds.x, viewArea.getBounds.y)
          val renderer = fakeWorld.newRenderer
          val image = new java.io.ByteArrayInputStream(frame.drawingImageBytes)
          renderer.trailDrawer.readImage(image)
          renderer.paint(g2d, viewSettings)
        } finally {
          g2d.dispose()
        }
      }
    }

    def repaintWidgets(g: java.awt.Graphics) {
      for {
        frame <- tabState.currentFrame
        values = frame.mirroredState
          .filterKeys(_.kind == org.nlogo.mirror.Mirrorables.WidgetValue)
          .toSeq
          .sortBy { case (agentKey, vars) => agentKey.id } // should be z-order
          .map { case (agentKey, vars) => vars(0).asInstanceOf[String] }
        (w, v) <- widgetHooks.map(_.widget) zip values
      } {
        val g2d = g.create.asInstanceOf[java.awt.Graphics2D]
        try {
          val container = ws.viewWidget.findWidgetContainer
          val bounds = container.getUnzoomedBounds(w)
          g2d.setRenderingHint(
            java.awt.RenderingHints.KEY_ANTIALIASING,
            java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
          g2d.setFont(w.getFont)
          g2d.clipRect(bounds.x, bounds.y, w.getSize().width, w.getSize().height) // make sure text doesn't overflow
          g2d.translate(bounds.x, bounds.y)
          w match {
            case m: window.MonitorWidget =>
              window.MonitorPainter.paint(
                g2d, m.getSize, m.getForeground, m.displayName, v)
            case _ => // ignore for now
          }
        } finally {
          g2d.dispose()
        }
      }
    }

    def repaintPlots(g: java.awt.Graphics) {
      for {
        frame <- tabState.currentFrame
        container = ws.viewWidget.findWidgetContainer
        widgets = plotWidgets
          .map { pw => pw.plotName -> pw }
          .toMap
        plot <- frame.plots
        widget <- widgets.get(plot.name)
        widgetBounds = container.getUnzoomedBounds(widget)
        canvasBounds = widget.canvas.getBounds()
        g2d = g.create.asInstanceOf[java.awt.Graphics2D]
        painter = new PlotPainter(plot)
      } {
        g2d.translate(
          widgetBounds.x + canvasBounds.x,
          widgetBounds.y + canvasBounds.y)
        painter.setupOffscreenImage(canvasBounds.width, canvasBounds.height)
        painter.drawImage(g2d)
      }
    }

    override def paintComponent(g: java.awt.Graphics) {
      super.paintComponent(g)
      g.setColor(if (tabState.currentRun.isDefined) WHITE else GRAY)
      g.fillRect(0, 0, getWidth, getHeight)
      for {
        run <- tabState.currentRun
      } {
        g.drawImage(run.backgroundImage, 0, 0, null)
        repaintView(g, run.viewArea)
        repaintWidgets(g)
        repaintPlots(g)
      }
    }
  }

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
    RunList.repaint()
    InterfacePanel.repaint()
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

  object RunList extends JList(tabState) {
    setBorder(BorderFactory.createLoweredBevelBorder())
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    this.getSelectionModel.addListSelectionListener(
      new ListSelectionListener {
        def valueChanged(p1: ListSelectionEvent) {
          if (getSelectedIndex != -1) {
            val run = RunList.getSelectedValue.asInstanceOf[ModelRun]
            tabState.setCurrentRun(run)
            loadModelIfNeeded(run.modelString)
            refreshInterface()
          }
        }
      })
  }

  object RunListPanel extends JPanel {
    setLayout(new BorderLayout)
    add(new JScrollPane(RunList), BorderLayout.CENTER)
  }

  object InterfaceScrollPane extends JScrollPane {
    setViewportView(InterfacePanel)
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
