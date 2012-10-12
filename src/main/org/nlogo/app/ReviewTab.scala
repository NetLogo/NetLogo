package org.nlogo.app

import javax.swing._
import javax.swing.event.{ ChangeEvent, ChangeListener }
import java.awt.BorderLayout
import java.awt.image.BufferedImage
import org.nlogo.awt.{ RowLayout, UserCancelException }
import org.nlogo.{ api, mirror, nvm, window }
import org.nlogo.util.Exceptions.ignoring
import org.nlogo.swing.Implicits._
import mirror.{ Mirroring, Mirrorable, Mirrorables, Serializer }
import javax.imageio.ImageIO
import scala.collection.JavaConverters.asScalaBufferConverter

case class PotemkinInterface(
  viewArea: java.awt.geom.Area,
  image: BufferedImage,
  fakeWidgets: Seq[FakeWidget])

case class FakeWidget(
  realWidget: window.Widget,
  valueStringGetter: () => String
)

class ReviewTabState {
  type Run = Seq[Array[Byte]]
  private var run: Run = Seq()
  private var finalState: Mirroring.State = Map()
  private var frame = 0
  private var _visibleState: Mirroring.State = Map()
  def visibleState = _visibleState
  def size = run.size
  def megabytes = run.map(_.size.toLong).sum / 1024 / 1024
  def add(mirrorables: Iterable[Mirrorable]) {
    val (newState, update) = Mirroring.diffs(finalState, mirrorables)
    run :+= Serializer.toBytes(update)
    finalState = newState
  }
  def reset() {
    run = Seq()
    finalState = Map()
    _visibleState = Map()
  }
  def refreshVisibleState() {
    if (visibleState.isEmpty)
      _visibleState = merge(Map(), run.head)
  }
  def scrub(newFrame: Int) {
    _visibleState =
      if(newFrame < frame)
        run.take(newFrame + 1)
          .foldLeft(Map(): Mirroring.State)(merge)
      else
        run.drop(frame + 1).take(newFrame - frame)
          .foldLeft(visibleState)(merge)
    frame = newFrame
  }
  def load(in: java.io.ObjectInputStream) {
    run = in.readObject().asInstanceOf[Run]
    frame = 0
    _visibleState = merge(Map(), run.head)
    finalState = run.foldLeft(Map(): Mirroring.State)(merge)
  }
  def write(out: java.io.ObjectOutputStream) {
    out.writeObject(run)
  }
  private def merge(oldState: Mirroring.State, bytes: Array[Byte]): Mirroring.State =
    Mirroring.merge(oldState, Serializer.fromBytes(bytes))
  def ticks(state: Mirroring.State = visibleState): Option[Double] =
    for {
      entry <- state.get(mirror.AgentKey(Mirrorables.World, 0))
      result = entry(Mirrorables.MirrorableWorld.wvTicks).asInstanceOf[Double]
      if result != -1
    } yield result
}

class ReviewTab(ws: window.GUIWorkspace,
                loadModel: String => Unit,
                saveModel: () => String)
extends JPanel
with window.Events.BeforeLoadEventHandler {

  var recordingEnabled: Boolean = true
  var potemkinInterface: Option[PotemkinInterface] = None
  val tabState = new ReviewTabState

  ws.listenerManager.addListener(
    new api.NetLogoAdapter {
      override def tickCounterChanged(ticks: Double) {
        if (recordingEnabled) {
          updateMonitors()
          // switch from job thread to event thread
          ws.waitFor(() => grab())
        }}})

  // only callable from the job thread, and only once evaluator.withContext(...) has properly
  // set up ProcedureRunner's execution context. it would be problematic to try to trigger
  // monitors to update from the event thread, because the event thread is not allowed to
  // block waiting for the job thread. - ST 10/12/12
  def updateMonitors() {
    for(pi <- potemkinInterface) {
      val monitors = pi.fakeWidgets.collect{
        case FakeWidget(m: window.MonitorWidget, _) => m}
      for(monitor <- monitors; reporter <- monitor.reporter)
        monitor.value(
          ws.evaluator.ProcedureRunner.report(reporter))
    }
  }

  def reset() {
    tabState.reset()
    Scrubber.setValue(0)
    Scrubber.setEnabled(false)
    Scrubber.border("")
    potemkinInterface = None
  }

  def grab() {
    if (ws.world.ticks == 0)
      reset()
    if (tabState.size == 0) {
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
      potemkinInterface = Some(PotemkinInterface(viewArea, image, widgets))
    }
    for(pi <- potemkinInterface) {
      tabState.add(
        Mirrorables.allMirrorables(
          ws.world, ws.plotManager.plots,
          pi.fakeWidgets.map(_.valueStringGetter.apply).zipWithIndex))
      Scrubber.setEnabled(true)
      Scrubber.updateBorder()
      MemoryMeter.update()
    }
    Scrubber.setMaximum(tabState.size - 1)
    InterfacePanel.repaint()
  }

  def fakeWidgets(ws: org.nlogo.window.GUIWorkspace) =
    ws.viewWidget.findWidgetContainer
      .getWidgetsForSaving.asScala
      .collect {
        case m: window.MonitorWidget =>
          FakeWidget(m, () => m.valueString)
      }.toList

  object InterfacePanel extends JPanel {

    def repaintView(g: java.awt.Graphics, area: java.awt.geom.Area) {
      val g2d = g.create.asInstanceOf[java.awt.Graphics2D]
      try {
        val view = ws.view
        object FakeViewSettings extends api.ViewSettings {
          // disregard spotlight/perspective settings in the
          // "real" view, but delegate other methods to it
          def fontSize = view.fontSize
          def patchSize = view.patchSize
          def viewWidth = view.viewWidth
          def viewHeight = view.viewHeight
          def viewOffsetX = view.viewOffsetX
          def viewOffsetY = view.viewOffsetY
          def drawSpotlight = false
          def renderPerspective = false
          def perspective = api.Perspective.Observe
          def isHeadless = view.isHeadless
        }
        g2d.setClip(area)
        g2d.translate(area.getBounds.x, area.getBounds.y)
        val fakeWorld = new mirror.FakeWorld(tabState.visibleState)
        fakeWorld.newRenderer(FakeViewSettings).paint(g2d, FakeViewSettings)
      } finally {
        g2d.dispose()
      }
    }

    def repaintWidgets(g: java.awt.Graphics, widgets: Seq[FakeWidget]) {

      val container = ws.viewWidget.findWidgetContainer
      val values = tabState.visibleState
        .filterKeys(_.kind == org.nlogo.mirror.Mirrorables.WidgetValue)
        .toSeq
        .sortBy { case (agentKey, vars) => agentKey.id } // should be z-order
        .map { case (agentKey, vars) => vars(0).asInstanceOf[String] }
      for ((w, v) <- widgets zip values) {
        val g2d = g.create.asInstanceOf[java.awt.Graphics2D]
        try {
          val rw = w.realWidget
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
      potemkinInterface match {
        case None =>
          g.setColor(java.awt.Color.GRAY)
          g.fillRect(0, 0, getWidth, getHeight)
        case Some(PotemkinInterface(_, image, _)) =>
          g.setColor(java.awt.Color.WHITE)
          g.fillRect(0, 0, getWidth, getHeight)
          g.drawImage(image, 0, 0, null)
      }
      if (tabState.size > 0) {
        tabState.refreshVisibleState()
        for (pi <- potemkinInterface) {
          repaintView(g, pi.viewArea)
          repaintWidgets(g, pi.fakeWidgets)
        }
      }
    }
  }

  object Scrubber extends JSlider {
    def border(s: String) {
      setBorder(BorderFactory.createTitledBorder(s))
    }
    def updateBorder() {
      border("Ticks: " + tabState.ticks().map(x => api.Dump.number(StrictMath.floor(x))).getOrElse(""))
    }
    setValue(0)
    border("")
    addChangeListener(new ChangeListener{
      def stateChanged(e: ChangeEvent) {
        tabState.scrub(getValue)
        updateBorder()
        InterfacePanel.repaint()
      }})
  }

  object EnabledAction extends AbstractAction("Recording") {
    def actionPerformed(e: java.awt.event.ActionEvent) {
      recordingEnabled = !recordingEnabled
    }
  }

  object Enabled extends JCheckBox(EnabledAction) {
    setSelected(recordingEnabled)
  }

  object SaveAction extends AbstractAction("Save") {
    def actionPerformed(e: java.awt.event.ActionEvent) {
      ignoring(classOf[UserCancelException]) {
        val path = org.nlogo.swing.FileDialog.show(
          ReviewTab.this, "Save Run", java.awt.FileDialog.SAVE, "run.dat")
        val out = new java.io.ObjectOutputStream(
          new java.io.FileOutputStream(path))
        // FIXME: what if the model has changed since we started recording the run? NP 2012-09-12
        // We should save a copy at the point where we start recording
        out.writeObject(saveModel())
        out.writeObject(potemkinInterface.get.viewArea)
        val imageByteStream = new java.io.ByteArrayOutputStream
        ImageIO.write(
          potemkinInterface.get.image, "PNG", imageByteStream)
        imageByteStream.close()
        out.writeObject(imageByteStream.toByteArray)
        tabState.write(out)
        out.close()
      }
    }
  }

  object LoadAction extends AbstractAction("Load") {
    def actionPerformed(e: java.awt.event.ActionEvent) {
      ignoring(classOf[UserCancelException]) {
        val path = org.nlogo.swing.FileDialog.show(
          ReviewTab.this, "Load Run", java.awt.FileDialog.LOAD, null)
        val in = new java.io.ObjectInputStream(
          new java.io.FileInputStream(path))
        loadModel(in.readObject().asInstanceOf[String])
        potemkinInterface =
          Some(
            PotemkinInterface(
              viewArea = in.readObject().asInstanceOf[java.awt.geom.Area],
              image = ImageIO.read(
                new java.io.ByteArrayInputStream(
                  in.readObject().asInstanceOf[Array[Byte]])),
              fakeWidgets = fakeWidgets(ws)))
        tabState.load(in)
        Scrubber.setValue(0)
        Scrubber.setEnabled(true)
        Scrubber.setMaximum(tabState.size - 1)
        MemoryMeter.update()
        InterfacePanel.repaint()
      }
    }
  }

  object SaveButton extends JButton(SaveAction)

  object LoadButton extends JButton(LoadAction)

  object Toolbar extends org.nlogo.swing.ToolBar {
    override def addControls() {
      add(Enabled)
      add(SaveButton)
      add(LoadButton)
      add(MemoryMeter)
    }
  }

  object MemoryMeter extends JLabel {
    def update() {
      setText(tabState.megabytes + " MB")
    }
  }

  class ScrubAction(name: String, fn: Int => Int)
  extends AbstractAction(name) {
    def actionPerformed(e: java.awt.event.ActionEvent) {
      Scrubber.setValue(fn(Scrubber.getValue))
    }
  }

  object AllTheWayBackAction
    extends ScrubAction("|<-", _ => 0)
  object AllTheWayForwardAction
    extends ScrubAction("->|", _ => Scrubber.getMaximum)
  object BackAction
    extends ScrubAction("<-", _ - 1)
  object ForwardAction
    extends ScrubAction("->", _ + 1)

  object ButtonPanel extends JPanel {
    setLayout(
    new RowLayout(
      1, java.awt.Component.LEFT_ALIGNMENT,
      java.awt.Component.CENTER_ALIGNMENT))
    add(new JButton(AllTheWayBackAction))
    add(new JButton(BackAction))
    add(new JButton(ForwardAction))
    add(new JButton(AllTheWayForwardAction))
  }

  object SouthPanel extends JPanel {
    setLayout(new BorderLayout)
    add(ButtonPanel, BorderLayout.WEST)
    add(Scrubber, BorderLayout.CENTER)
  }

  locally {
    import java.awt.BorderLayout
    setLayout(new BorderLayout)
    add(InterfacePanel, BorderLayout.CENTER)
    add(SouthPanel, BorderLayout.SOUTH)
    add(Toolbar, BorderLayout.NORTH)
  }

  override def handle(e: window.Events.BeforeLoadEvent) {
    reset()
  }

}
