package org.nlogo.app

import javax.swing._
import javax.swing.event.{ ChangeEvent, ChangeListener }
import java.awt.BorderLayout
import java.awt.image.BufferedImage
import org.nlogo.awt.{ RowLayout, UserCancelException }
import org.nlogo.{ api, mirror, nvm, window }
import org.nlogo.util.Exceptions.ignoring
import org.nlogo.swing.Implicits._
import mirror.{ Mirroring, Mirrorables, Serializer }
import javax.imageio.ImageIO
import scala.collection.JavaConverters.asScalaBufferConverter

case class PotemkinInterface(
  viewPosition: java.awt.Point,
  image: BufferedImage,
  fakeWidgets: Seq[FakeWidget])

case class FakeWidget(
  realWidget: window.Widget,
  valueStringGetter: () => String
)

class ReviewTab(ws: window.GUIWorkspace,
                loadModel: String => Unit,
                saveModel: () => String)
extends JPanel
with window.Events.BeforeLoadEventHandler {

  type Run = Seq[Array[Byte]]

  var recordingEnabled: Boolean = true
  var run: Run = Seq()
  var state: Mirroring.State = Map()
  var visibleState: Mirroring.State = Map()
  var frame = 0

  var potemkinInterface: Option[PotemkinInterface] = None

  ws.listenerManager.addListener(
    new api.NetLogoAdapter {
      val count = Iterator.from(0)
      override def tickCounterChanged(ticks: Double) {

        // try to update the monitors... but...
        // a) they're still out of sync sometimes
        // b) I'm not sure it is OK to do that anyway... NP 2012-09-20
        ws.updateUI() 

        // get off the job thread and onto the event thread
        ws.waitFor{() =>
          if (recordingEnabled) {
            if (ticks == 0) {
              reset()
            }
            grab()
            Scrubber.setMaximum(run.size - 1)
            InterfacePanel.repaint()
          }}}}
    )

  private def reset() {
    run = Seq()
    state = Map()
    visibleState = Map()
    Scrubber.setValue(0)
    Scrubber.setEnabled(false)
    Scrubber.border("")
    potemkinInterface = None
  }

  private def grab() {
    if (run.isEmpty) {
      val wrapper = org.nlogo.awt.Hierarchy.findAncestorOfClass(
        ws.viewWidget, classOf[org.nlogo.window.WidgetWrapperInterface])
      val wrapperPos = wrapper.map(_.getLocation).getOrElse(new java.awt.Point(0, 0))

      // The position is the position of the view, but the image is the
      // whole interface, including the view.
      potemkinInterface = Some(
        PotemkinInterface(
          viewPosition = new java.awt.Point(
            wrapperPos.x + ws.viewWidget.view.getLocation().x,
            wrapperPos.y + ws.viewWidget.view.getLocation().y),
          image = org.nlogo.awt.Images.paintToImage(
            ws.viewWidget.findWidgetContainer.asInstanceOf[java.awt.Component]),
          fakeWidgets = fakeWidgets(ws)))
    }
    for {
      pi <- potemkinInterface
      widgetValues = pi.fakeWidgets.map(_.valueStringGetter.apply).zipWithIndex
      mirrorables = Mirrorables.allMirrorables(ws.world, ws.plotManager.plots, widgetValues)
      (newState, update) = Mirroring.diffs(state, mirrorables)
    } {
      run :+= Serializer.toBytes(update)
      state = newState
      Scrubber.setEnabled(true)
      Scrubber.updateBorder()
      MemoryMeter.update()
    }
  }

  private def fakeWidgets(ws: org.nlogo.window.GUIWorkspace) =
    ws.viewWidget.findWidgetContainer
      .getWidgetsForSaving.asScala
      .flatMap {
        case m: window.MonitorWidget =>
          Some(FakeWidget(m, () => api.Dump.logoObject(m.value))) // FIXME: not good enough - needs to run the reporter
        case _ => None
      }
      .toList

  object InterfacePanel extends JPanel {

    private def repaintView(g: java.awt.Graphics, position: java.awt.Point) {
      val g2d = g.create.asInstanceOf[java.awt.Graphics2D]
      try {
        val view = ws.viewWidget.view
        g2d.clipRect(position.x, position.y, view.getWidth, view.getHeight)
        g2d.translate(position.x, position.y)
        val fakeWorld = new mirror.FakeWorld(visibleState)
        fakeWorld.newRenderer(view).paint(g2d, view)
      } finally {
        g2d.dispose()
      }
    }

    private def repaintWidgets(
      g: java.awt.Graphics,
      widgets: Seq[FakeWidget],
      visibleState: Mirroring.State) {

      val container = ws.viewWidget.findWidgetContainer
      val values = visibleState
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
        case Some(PotemkinInterface(position, image, _)) =>
          g.setColor(java.awt.Color.WHITE)
          g.fillRect(0, 0, getWidth, getHeight)
          g.drawImage(image, 0, 0, null)
      }
      if (run.nonEmpty) {
        if (visibleState.isEmpty)
          visibleState = Mirroring.merge(Map(), Serializer.fromBytes(run.head))
        for (pi <- potemkinInterface) {
          repaintView(g, pi.viewPosition)
          repaintWidgets(g, pi.fakeWidgets, visibleState)
        }
      }
    }
  }

  private def merge(oldState: Mirroring.State, bytes: Array[Byte]): Mirroring.State =
    Mirroring.merge(oldState, Serializer.fromBytes(bytes))

  object Scrubber extends JSlider {
    def border(s: String) {
      setBorder(BorderFactory.createTitledBorder(s))
    }
    def updateBorder() {
      border("Ticks: " + ticks.map(x => api.Dump.number(StrictMath.floor(x))).getOrElse(""))
    }
    setValue(0)
    border("")
    addChangeListener(new ChangeListener{
      def stateChanged(e: ChangeEvent) {
        visibleState =
          if(getValue < frame)
              run.take(getValue + 1)
                .foldLeft(Map(): Mirroring.State)(merge)
          else
              run.drop(frame + 1).take(getValue - frame)
                .foldLeft(visibleState)(merge)
        updateBorder()
        frame = getValue
        InterfacePanel.repaint()
      }})
  }

  def ticks: Option[Double] =
    for {
      entry <- visibleState.get(mirror.AgentKey(Mirrorables.World, 0))
      result = entry(Mirrorables.MirrorableWorld.wvTicks).asInstanceOf[Double]
      if result != -1
    } yield result

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
        out.writeObject(potemkinInterface.get.viewPosition)
        val imageByteStream = new java.io.ByteArrayOutputStream
        ImageIO.write(
          potemkinInterface.get.image, "PNG", imageByteStream)
        imageByteStream.close()
        out.writeObject(imageByteStream.toByteArray)
        out.writeObject(run)
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
              viewPosition = in.readObject().asInstanceOf[java.awt.Point],
              image = ImageIO.read(
                new java.io.ByteArrayInputStream(
                  in.readObject().asInstanceOf[Array[Byte]])),
              fakeWidgets = fakeWidgets(ws)))
        run = in.readObject().asInstanceOf[Run]
        frame = 0
        visibleState = Mirroring.merge(Map(), Serializer.fromBytes(run.head))
        state = run.foldLeft(Map(): Mirroring.State)(merge)
        Scrubber.setValue(0)
        Scrubber.setEnabled(true)
        Scrubber.setMaximum(run.size - 1)
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
      val megabytes = run.map(_.size.toLong).sum / 1024 / 1024
      setText(megabytes + " MB")
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
