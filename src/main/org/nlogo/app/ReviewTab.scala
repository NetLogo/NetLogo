package org.nlogo.app

import java.awt.BorderLayout
import java.awt.Color.{ GRAY, WHITE }
import scala.Array.fallbackCanBuildFrom
import scala.Option.option2Iterable
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.mutable.{ ListBuffer, Subscriber }
import org.nlogo.api
import org.nlogo.awt.UserCancelException
import org.nlogo.mirror
import org.nlogo.mirror.Mirrorables
import org.nlogo.plot.{ PlotAction, PlotManager, PlotPainter }
import org.nlogo.swing.Implicits.thunk2runnable
import org.nlogo.util.Exceptions.ignoring
import org.nlogo.window
import org.nlogo.window.{ MonitorWidget, Widget, WidgetWrapperInterface }
import javax.swing.{ AbstractAction, BorderFactory, ImageIcon, JButton, JCheckBox, JFileChooser, JLabel, JList, JOptionPane, JPanel, JScrollPane, JSlider, JSplitPane, JTextArea, ListSelectionModel }
import javax.swing.border.EmptyBorder
import javax.swing.event.{ ChangeEvent, ChangeListener, DocumentEvent, DocumentListener, ListSelectionEvent, ListSelectionListener }
import javax.swing.filechooser.FileNameExtensionFilter
import org.nlogo.window.PlotWidget

class ReviewTab(
  ws: window.GUIWorkspace,
  saveModel: () => String)
  extends JPanel
  with window.Events.BeforeLoadEventHandler {

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
   */
  object PlotActionBuffer
    extends Subscriber[PlotAction, PlotManager#Pub] {

    ws.plotManager.subscribe(this)

    val buffer = new ListBuffer[PlotAction]
    def notify(pub: PlotManager#Pub, action: PlotAction) {
      buffer += action
    }
    def clear() = buffer.clear()
    def grab() = {
      val actions = buffer.toSeq
      clear()
      actions
    }
  }

  object Memory {
    def toMB(bytes: Long) = bytes / 1024 / 1024
    def safeThreshold = 64 // arbitrary - needs to be tweaked...
    def free = toMB(Runtime.getRuntime().freeMemory())
    def usedByRuns = toMB(tabState.sizeInBytes)
    def underSafeThreshold = free < safeThreshold
    def check() {
      if (!tabState.userWarnedForMemory) {
        if (underSafeThreshold)
          System.gc() // try to collect garbage before actually warning
        if (underSafeThreshold) {
          tabState.userWarnedForMemory = true
          if (userConfirms("Low Memory",
            "Memory is getting low. Do you want to stop recording?"))
            stopRecording()
        }
      }
    }
  }

  private def userConfirms(title: String, message: String) =
    JOptionPane.showConfirmDialog(ReviewTab.this, message,
      title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION

  private def nameFromPath(path: String) =
    new java.io.File(path).getName
      .replaceAll("\\.[^.]*$", "") // remove extension

  val tabState = new ReviewTabState()

  ws.listenerManager.addListener(
    new api.NetLogoAdapter {
      override def tickCounterChanged(ticks: Double) {
        if (ws.world.ticks == -1) {
          PlotActionBuffer.clear()
        } else {
          if (tabState.recordingEnabled)
            ws.waitFor(() => Memory.check())
          if (tabState.recordingEnabled) { // checkMemory may turn off recording
            if (tabState.currentRun.isEmpty || ws.world.ticks == 0)
              ws.waitFor(() => startNewRun())
            if (tabState.currentlyRecording) {
              updateMonitors()
              // switch from job thread to event thread
              ws.waitFor(() => grab())
              refreshInterface()
            }
          }
        }
      }
    })

  // only callable from the job thread, and only once evaluator.withContext(...) has properly
  // set up ProcedureRunner's execution context. it would be problematic to try to trigger
  // monitors to update from the event thread, because the event thread is not allowed to
  // block waiting for the job thread. - ST 10/12/12
  def updateMonitors() {
    for {
      monitor <- widgetHooks.collect { case WidgetHook(m: window.MonitorWidget, _) => m }
      reporter <- monitor.reporter
    } monitor.value(ws.evaluator.ProcedureRunner.report(reporter))
  }

  def startRecording() {
    tabState.recordingEnabled = true
    Enabled.setSelected(tabState.recordingEnabled)
  }

  def stopRecording() {
    tabState.recordingEnabled = false
    Enabled.setSelected(tabState.recordingEnabled)
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

    val image = org.nlogo.awt.Images.paintToImage(
      ws.viewWidget.findWidgetContainer.asInstanceOf[java.awt.Component])

    val name = Option(ws.getModelFileName)
      .map(path => tabState.uniqueName(nameFromPath(path)))
      .getOrElse("Untitled")
    val run = new ModelRun(name, saveModel(), viewArea, image, "", Map())
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
        run.data
          .getOrElse(run.init(ws.plotManager.plots))
          .append(mirrorables, PlotActionBuffer.grab())
      } catch {
        case e: java.lang.OutOfMemoryError =>
          // happens if user ignored our warning or if "GC overhead limit exceeded"
          // (the latter being harder to prevent in advance)
          JOptionPane.showMessageDialog(this,
            "Not enough memory. Turning off recording.",
            "Low memory", JOptionPane.WARNING_MESSAGE)
          stopRecording()
        case e => throw e // rethrow anything else
      }
      refreshInterface()
    }
  }

  object InterfacePanel extends JPanel {

    def repaintView(g: java.awt.Graphics, viewArea: java.awt.geom.Area) {
      for {
        run <- tabState.currentRun
        data <- run.data
        fakeWorld = new mirror.FakeWorld(data.currentFrame.mirroredState)
        paintArea = new java.awt.geom.Area(InterfacePanel.getBounds())
        g2d = g.create.asInstanceOf[java.awt.Graphics2D]
      } {
        object FakeViewSettings extends api.ViewSettings {
          // disregard spotlight/perspective settings in the
          // "real" view, but delegate other methods to it
          def fontSize = ws.view.fontSize
          def patchSize = ws.view.patchSize
          def viewWidth = ws.view.viewWidth
          def viewHeight = ws.view.viewHeight
          def viewOffsetX = ws.view.viewOffsetX
          def viewOffsetY = ws.view.viewOffsetY
          def drawSpotlight = false
          def renderPerspective = false
          def perspective = api.Perspective.Observe
          def isHeadless = ws.view.isHeadless
        }
        paintArea.intersect(viewArea) // avoid spilling outside interface panel
        try {
          g2d.setClip(paintArea)
          g2d.translate(viewArea.getBounds.x, viewArea.getBounds.y)
          fakeWorld.newRenderer(FakeViewSettings).paint(g2d, FakeViewSettings)
        } finally {
          g2d.dispose()
        }
      }
    }

    def repaintWidgets(g: java.awt.Graphics) {
      for {
        run <- tabState.currentRun
        data <- run.data
        values = data.currentFrame.mirroredState
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
        data <- tabState.currentRunData
        container = ws.viewWidget.findWidgetContainer
        widgets = plotWidgets
          .map { pw => pw.plotName -> pw }
          .toMap
        plot <- data.currentFrame.plots
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

  object Scrubber extends JSlider {
    def border(s: String) {
      setBorder(BorderFactory.createTitledBorder(s))
    }
    def updateBorder() {
      val newBorder = tabState.currentRun match {
        case None => ""
        case Some(run) => "Ticks: " +
          (for { data <- run.data; ticks <- data.currentTicks }
            yield api.Dump.number(StrictMath.floor(ticks))).getOrElse("")
      }
      border(newBorder)
    }
    setValue(0)
    border("")
    addChangeListener(new ChangeListener {
      def stateChanged(e: ChangeEvent) {
        for {
          run <- tabState.currentRun
          data <- run.data
        } data.setCurrentFrame(getValue)
        updateBorder()
        InterfacePanel.repaint()
      }
    })
  }

  object EnabledAction extends AbstractAction("Recording") {
    def actionPerformed(e: java.awt.event.ActionEvent) {
      tabState.recordingEnabled = !tabState.recordingEnabled
    }
  }

  object Enabled extends JCheckBox(EnabledAction) {
    setSelected(tabState.recordingEnabled)
  }

  val saveButton = actionButton("Save", "save") { () =>
    for (run <- tabState.currentRun) {
      ignoring(classOf[UserCancelException]) {
        val path = org.nlogo.swing.FileDialog.show(
          ReviewTab.this, "Save Run", java.awt.FileDialog.SAVE,
          run.name + ".nlrun")
        if (new java.io.File(path).exists &&
          !userConfirms("Save Model Run", "The file " + path +
            " already exists. Do you want to overwrite it?"))
          throw new UserCancelException
        val out = new java.io.ObjectOutputStream(
          new java.io.FileOutputStream(path))
        ModelRunIO.save(out, run)
        run.name = nameFromPath(path)
        tabState.undirty(run)
        refreshInterface()
      }
    }
  }

  def refreshInterface() {
    tabState.currentRun match {
      case None => {
        NotesArea.setEnabled(false)
        NotesArea.setText("")
        (saveButton +: closeCurrentButton +:
          closeAllButton +: scrubButtons)
          .foreach(_.setEnabled(false))
      }
      case Some(run) => {
        NotesArea.setText(run.generalNotes)
        NotesArea.setEnabled(true)
        saveButton.setEnabled(run.dirty)
        (closeCurrentButton +: closeAllButton +: scrubButtons)
          .foreach(_.setEnabled(true))
      }
    }
    tabState.currentRunData match {
      case None => {
        Scrubber.setMaximum(0)
        Scrubber.setValue(0)
        Scrubber.setEnabled(false)
      }
      case Some(data) => {
        Scrubber.setMaximum(data.lastFrameIndex)
        Scrubber.setValue(data.currentFrameIndex)
        Scrubber.setEnabled(true)
      }
    }
    Scrubber.updateBorder()
    MemoryMeter.update()
    Scrubber.repaint()
    RunList.repaint()
    InterfacePanel.repaint()
  }

  def chooseFiles: Seq[String] = {
    val fc = new JFileChooser()
    fc.setDialogTitle("Open NetLogo Model Run(s)")
    fc.setFileFilter(new FileNameExtensionFilter(
      "NetLogo Model Runs (*.nlrun)", "nlrun"))
    fc.setMultiSelectionEnabled(true)
    if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
      fc.getSelectedFiles.map(_.getPath())
    else
      Seq()
  }

  /**
   * Loads a run from `path` and returns either the loaded run
   * in case of success or the path in case of failure
   */
  def loadRun(path: String): Either[String, ModelRun] =
    try {
      val in = new java.io.ObjectInputStream(
        new java.io.FileInputStream(path))
      val name = tabState.uniqueName(nameFromPath(path))
      val run = ModelRunIO.load(in, name) { run =>
        tabState.addRun(run)
        loadModelIfNeeded(run.modelString)
        ws
      }
      Right(run)
    } catch {
      case ex: Exception => Left(path)
    }

  val loadButton = actionButton("Load", "open") { () =>
    val results = chooseFiles.map(loadRun)
    val loadedRuns = results.flatMap(_.right.toOption)
    // select the last loaded run if we have one:
    loadedRuns.lastOption.foreach { run =>
      RunList.setSelectedValue(run, true)
    }
    val errors = results.flatMap(_.left.toOption)
    if (errors.nonEmpty) {
      val (notStr, fileStr) =
        if (errors.size > 1) ("they are not", "files")
        else ("it is not a", "file")
      val msg = "Something went wrong while trying to load the following " +
        fileStr + ":\n\n" + errors.mkString("\n") + "\n\n" +
        "Maybe " + notStr + " proper NetLogo Model Run " + fileStr + "?"
      JOptionPane.showMessageDialog(this, msg, "NetLogo", JOptionPane.ERROR_MESSAGE);
    }
  }

  def loadModelIfNeeded(modelString: String) {
    val currentModelString = saveModel()
    if (modelString != currentModelString) {
      ignoring(classOf[UserCancelException]) {
        App.app.fileMenu.offerSave()
      }
      org.nlogo.window.ModelLoader.load(ReviewTab.this,
        null, api.ModelType.Library, modelString)
      App.app.tabs.setSelectedComponent(ReviewTab.this)
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

  val closeAllButton = actionButton("Close all", "close-all") { () =>
    if (!tabState.dirty ||
      userConfirms("Close all runs",
        "Some runs have unsaved data. Are you sure you want to close all runs?")) {
      tabState.reset()
      refreshInterface()
    }
  }

  val closeCurrentButton = actionButton("Close", "close") { () =>
    for (run <- tabState.currentRun) {
      if (!run.dirty ||
        userConfirms("Close current run",
          "The current run has unsaved data. Are you sure you want to close the current run?")) {
        tabState.closeCurrentRun()
        refreshInterface()
      }
    }
  }

  object ReviewToolBar extends org.nlogo.swing.ToolBar {
    override def addControls() {
      add(saveButton)
      add(loadButton)
      add(closeCurrentButton)
      add(closeAllButton)
      add(new org.nlogo.swing.ToolBar.Separator)
      add(Enabled)
    }
  }

  object MemoryMeter extends JPanel {
    setLayout(new BorderLayout)
    setBorder(new EmptyBorder(5, 5, 5, 5))
    add(new JLabel("Memory used by runs: "), BorderLayout.WEST)
    val meterLabel = new JLabel("%5d MB".format(Memory.usedByRuns))
    add(meterLabel, BorderLayout.EAST)
    def update() {
      meterLabel.setText(Memory.usedByRuns + " MB")
    }
  }

  class ReviewAction(name: String, icon: String, fn: () => Unit)
    extends AbstractAction(name) {
    val image = new ImageIcon(classOf[ReviewTab].getResource("/images/" + icon + ".gif"))
    putValue(javax.swing.Action.SMALL_ICON, image)
    def actionPerformed(e: java.awt.event.ActionEvent) { fn() }
  }
  def actionButton(name: String, icon: String)(fn: () => Unit) = {
    new JButton(new ReviewAction(name, icon, fn))
  }

  val scrubButtons: Seq[JButton] = Seq[(String, String, Int => Int)](
    ("all-back", "Go to beginning of run", { _ => 0 }),
    ("big-back", "Go back five steps", { _ - 5 }),
    ("back", "Go back one step", { _ - 1 }),
    ("forward", "Go forward one step", { _ + 1 }),
    ("big-forward", "Go forward five steps", { _ + 5 }),
    ("all-forward", "Go to end of run", { _ => Scrubber.getMaximum }))
    .map {
      case (name, tip, newValue) =>
        val setNewValue = { () => Scrubber.setValue(newValue(Scrubber.getValue)) }
        val icon = name
        val action = new ReviewAction(tip, icon, setNewValue)
        val button = new JButton(action)
        button.setToolTipText(tip)
        button.setHideActionText(true)
        button
    }

  object ScrubberButtonsPanel extends JPanel {
    setLayout(new org.nlogo.awt.RowLayout(
      1, java.awt.Component.LEFT_ALIGNMENT,
      java.awt.Component.CENTER_ALIGNMENT))
    scrubButtons.foreach(add)
  }

  object NotesArea extends JTextArea("") {
    setLineWrap(true)
    setRows(3)
    override def setText(text: String) {
      for {
        run <- tabState.currentRun
        if getText != run.generalNotes
      } super.setText(text)
    }
    object NotesListener extends DocumentListener {
      private def updateNotesInRun() {
        for (run <- tabState.currentRun) {
          run.generalNotes = NotesArea.getText
          saveButton.setEnabled(run.dirty)
        }
      }
      def insertUpdate(e: DocumentEvent) { updateNotesInRun() }
      def removeUpdate(e: DocumentEvent) { updateNotesInRun() }
      def changedUpdate(e: DocumentEvent) { updateNotesInRun() }
    }
    getDocument.addDocumentListener(NotesListener)
  }

  object NotesPanel extends JPanel {
    setLayout(new BorderLayout)
    add(new JLabel("General notes on current run"), BorderLayout.NORTH)
    add(new JScrollPane(NotesArea), BorderLayout.CENTER)
  }

  object ScrubberPanel extends JPanel {
    setLayout(new BorderLayout)
    add(ScrubberButtonsPanel, BorderLayout.WEST)
    add(Scrubber, BorderLayout.CENTER)
  }

  object RunListPanel extends JPanel {
    setLayout(new BorderLayout)
    add(new JScrollPane(RunList), BorderLayout.CENTER)
    add(MemoryMeter, BorderLayout.SOUTH)
  }

  object InterfaceScrollPane extends JScrollPane {
    setViewportView(InterfacePanel)
  }

  object RunPanel extends JPanel {
    setLayout(new BorderLayout)
    add(InterfaceScrollPane, BorderLayout.CENTER)
    add(ScrubberPanel, BorderLayout.SOUTH)
  }

  object PrimarySplitPane extends JSplitPane(
    JSplitPane.HORIZONTAL_SPLIT,
    RunListPanel,
    SecondarySplitPane)

  object SecondarySplitPane extends JSplitPane(
    JSplitPane.VERTICAL_SPLIT,
    RunPanel,
    NotesPanel) {
    setResizeWeight(1.0)
    setDividerLocation(1.0)
  }

  locally {
    setLayout(new BorderLayout)
    add(ReviewToolBar, BorderLayout.NORTH)
    add(PrimarySplitPane, BorderLayout.CENTER)
    PlotActionBuffer.clear() // make sure object is constructed and subscribed
    refreshInterface()
  }

  override def handle(e: window.Events.BeforeLoadEvent) {
    refreshInterface()
  }
}
