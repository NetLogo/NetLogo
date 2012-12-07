package org.nlogo.app

import java.awt.BorderLayout
import java.awt.Color.{ GRAY, WHITE }
import java.awt.image.BufferedImage
import scala.Array.fallbackCanBuildFrom
import scala.Option.option2Iterable
import scala.collection.JavaConverters.asScalaBufferConverter
import org.nlogo.api
import org.nlogo.awt.UserCancelException
import org.nlogo.mirror
import org.nlogo.mirror.{ Mirrorables, Serializer }
import org.nlogo.swing.Implicits.thunk2runnable
import org.nlogo.util.Exceptions.ignoring
import org.nlogo.window
import org.nlogo.window.{ InvalidVersionException, WidgetWrapperInterface }
import javax.imageio.ImageIO
import javax.swing.{ AbstractAction, BorderFactory, ImageIcon, JButton, JCheckBox, JFileChooser, JLabel, JList, JOptionPane, JPanel, JScrollPane, JSlider, JSplitPane, JTextArea, ListSelectionModel }
import javax.swing.border.EmptyBorder
import javax.swing.event.{ ChangeEvent, ChangeListener, DocumentEvent, DocumentListener, ListSelectionEvent, ListSelectionListener }
import javax.swing.filechooser.FileNameExtensionFilter
import org.nlogo.plot.PlotAction
import scala.collection.mutable.Subscriber
import scala.collection.mutable.ListBuffer

case class PotemkinInterface(
  val viewArea: java.awt.geom.Area,
  val image: BufferedImage,
  val fakeWidgets: Seq[FakeWidget])

case class FakeWidget(
  val realWidget: window.Widget,
  val valueStringGetter: () => String)

class ReviewTab(
  ws: window.GUIWorkspace,
  saveModel: () => String)
  extends JPanel
  with window.Events.BeforeLoadEventHandler {

  /**
   * The PlotActionBuffer logs all plotting actions, whether we are
   * recording or not. This is important because if we want to start
   * recording at some point, we need to mirror all actions from the
   * start to bring the plots to their actual state. NP 2012-11-29
   */
  object PlotActionBuffer
    extends Subscriber[PlotAction, PlotAction.Pub] {

    PlotAction.subscribe(this)

    val buffer = new ListBuffer[PlotAction]
    def notify(pub: PlotAction.Pub, action: PlotAction) {
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
        println("tickCounterChanged " + ticks)
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
      run <- tabState.currentRun
      fakeWidgets = run.potemkinInterface.fakeWidgets
      monitor <- fakeWidgets.collect { case FakeWidget(m: window.MonitorWidget, _) => m }
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
      ws.viewWidget, classOf[org.nlogo.window.WidgetWrapperInterface])
    val wrapperPos = wrapper.map(_.getLocation).getOrElse(new java.awt.Point(0, 0))

    // The position is the position of the view, but the image is the
    // whole interface, including the view.
    val widgets = fakeWidgets(ws)
    val view = ws.viewWidget.view
    val viewArea = new java.awt.geom.Area(new java.awt.Rectangle(
      wrapperPos.x + view.getLocation().x, wrapperPos.y + view.getLocation().y,
      view.getWidth, view.getHeight))

    // remove widgets from the clip area of the view:
    val container = ws.viewWidget.findWidgetContainer
    for {
      w <- widgets
      bounds = container.getUnzoomedBounds(w.realWidget)
      widgetArea = new java.awt.geom.Area(bounds)
    } viewArea.subtract(widgetArea)

    val image = org.nlogo.awt.Images.paintToImage(
      ws.viewWidget.findWidgetContainer.asInstanceOf[java.awt.Component])

    val newInterface = PotemkinInterface(viewArea, image, widgets)
    val name = Option(ws.getModelFileName)
      .map(nameFromPath)
      .getOrElse("Untitled")
    val run = tabState.newRun(name, saveModel(), newInterface)
    RunList.setSelectedValue(run, true)
    refreshInterface()
  }

  def grab() {
    for (run <- tabState.currentRun) {
      try {
        val widgetValues = run.potemkinInterface.fakeWidgets
          .map(_.valueStringGetter.apply)
          .zipWithIndex
        val mirrorables = Mirrorables.allMirrorables(ws.world, widgetValues)
        run.append(mirrorables, PlotActionBuffer.grab())
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

  def fakeWidgets(ws: org.nlogo.window.GUIWorkspace) =
    ws.viewWidget.findWidgetContainer
      .getWidgetsForSaving.asScala
      .collect {
        case m: window.MonitorWidget =>
          FakeWidget(m, () => m.valueString)
      }.toList

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

    def repaintWidgets(g: java.awt.Graphics, widgets: Seq[FakeWidget]) {
      for {
        run <- tabState.currentRun
        data <- run.data
        values = data.currentFrame.mirroredState
          .filterKeys(_.kind == org.nlogo.mirror.Mirrorables.WidgetValue)
          .toSeq
          .sortBy { case (agentKey, vars) => agentKey.id } // should be z-order
          .map { case (agentKey, vars) => vars(0).asInstanceOf[String] }
        (w, v) <- widgets zip values
      } {
        val g2d = g.create.asInstanceOf[java.awt.Graphics2D]
        try {
          val rw = w.realWidget
          val container = ws.viewWidget.findWidgetContainer
          val bounds = container.getUnzoomedBounds(rw)
          g2d.setRenderingHint(
            java.awt.RenderingHints.KEY_ANTIALIASING,
            java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
          g2d.setFont(rw.getFont)
          g2d.clipRect(bounds.x, bounds.y, rw.getSize().width, rw.getSize().height) // make sure text doesn't overflow
          g2d.translate(bounds.x, bounds.y)
          rw match {
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

    override def paintComponent(g: java.awt.Graphics) {
      super.paintComponent(g)
      g.setColor(if (tabState.currentRun.isDefined) WHITE else GRAY)
      g.fillRect(0, 0, getWidth, getHeight)
      for {
        run <- tabState.currentRun
        pi = run.potemkinInterface
      } {
        g.drawImage(pi.image, 0, 0, null)
        repaintView(g, pi.viewArea)
        repaintWidgets(g, pi.fakeWidgets)
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
    for {
      run <- tabState.currentRun
      data <- run.data
    } {
      def thingsToSave = {
        // Area is not serializable so we save a shape instead:
        val viewAreaShape = java.awt.geom.AffineTransform
          .getTranslateInstance(0, 0)
          .createTransformedShape(run.potemkinInterface.viewArea)
        val image = {
          val byteStream = new java.io.ByteArrayOutputStream
          ImageIO.write(run.potemkinInterface.image, "PNG", byteStream)
          byteStream.close()
          byteStream.toByteArray
        }
        val rawUpdates = data.deltas.map { d =>
          Serializer.toBytes(d.mirroredUpdate)
        }
        Seq(
          run.modelString,
          viewAreaShape,
          image,
          rawUpdates,
          run.generalNotes)
      }
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
        thingsToSave.foreach(out.writeObject)
        out.close()
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
  def loadRun(path: String): Either[String, Run] =
    try {
      val in = new java.io.ObjectInputStream(
        new java.io.FileInputStream(path))
      val Seq(
        modelString: String,
        viewShape: java.awt.Shape,
        imageBytes: Array[Byte],
        rawMirroredUpdates: Seq[Array[Byte]],
        notes: String) = Stream.continually(in.readObject()).take(5)
      in.close()
      loadModelIfNeeded(modelString)
      val newInterface = PotemkinInterface(
        new java.awt.geom.Area(viewShape),
        ImageIO.read(new java.io.ByteArrayInputStream(imageBytes)),
        fakeWidgets(ws))
      val mirroredUpdates = rawMirroredUpdates.map(Serializer.fromBytes)
      val plotActionFrames = Seq[Seq[PlotAction]]() // TODO: load actions from file
      val run = tabState.loadRun(
        nameFromPath(path), modelString,
        mirroredUpdates, plotActionFrames,
        newInterface, notes)
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
            val run = RunList.getSelectedValue.asInstanceOf[Run]
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
