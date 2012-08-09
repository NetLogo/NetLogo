package org.nlogo.app

import javax.swing._
import javax.swing.event.{ ChangeEvent, ChangeListener }
import java.awt.BorderLayout
import java.awt.image.BufferedImage
import org.nlogo.awt.{ RowLayout, UserCancelException }
import org.nlogo.{ api, mirror, nvm, window }
import org.nlogo.util.Exceptions.ignoring
import org.nlogo.util.Femto
import org.nlogo.swing.Implicits._
import mirror.{ Mirroring, Mirrorables, Serializer }
import javax.imageio.ImageIO

case class PotemkinInterface(position: java.awt.Point,
                             image: BufferedImage)

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
  var ticks = 0

  var potemkinInterface: Option[PotemkinInterface] = None

  ws.listenerManager.addListener(
    new api.NetLogoAdapter {
      val count = Iterator.from(0)
      override def tickCounterChanged(ticks: Double) {
        // get off the job thread and onto the event thread
        ws.waitFor{() =>
          if (recordingEnabled) {
            if (ticks == 0) {
              reset()
            }
            grab()
            Scrubber.setMaximum(run.size - 1)
            InterfacePanel.repaint()
          }}}})

  private def reset() {
    run = Seq()
    state = Map()
    visibleState = Map()
    Scrubber.setValue(0)
    Scrubber.setEnabled(false)
    potemkinInterface = None
  }

  private def grab() {
    if (run.isEmpty) {
      val wrapper = org.nlogo.awt.Hierarchy.findAncestorOfClass(
        ws.viewWidget, classOf[org.nlogo.window.WidgetWrapperInterface])
      val wrapperPos = wrapper.map(_.getLocation).getOrElse(new java.awt.Point(0, 0))
      potemkinInterface = Some(
        PotemkinInterface(
          position = new java.awt.Point(wrapperPos.x + ws.viewWidget.view.getLocation().x,
                                        wrapperPos.y + ws.viewWidget.view.getLocation().y),
          image =
            org.nlogo.awt.Images.paintToImage(
              ws.viewWidget.findWidgetContainer.asInstanceOf[java.awt.Component])))
    }
    val (newState, update) =
      Mirroring.diffs(state, Mirrorables.allMirrorables(ws.world, ws.plotManager.plots))
    run :+= Serializer.toBytes(update)
    state = newState
    Scrubber.setEnabled(true)
    MemoryMeter.update()
  }

  object InterfacePanel extends JPanel {
    override def paintComponent(g: java.awt.Graphics) {
      super.paintComponent(g)
      val position =
        potemkinInterface match {
          case None =>
            g.setColor(java.awt.Color.GRAY)
            g.fillRect(0, 0, getWidth, getHeight)
            new java.awt.Point(0, 0)
          case Some(PotemkinInterface(position, image)) =>
            g.setColor(java.awt.Color.WHITE)
            g.fillRect(0, 0, getWidth, getHeight)
            g.drawImage(image, 0, 0, null)
            position
        }
      if (run.nonEmpty) {
        if(visibleState.isEmpty)
          visibleState = Mirroring.merge(Map(), Serializer.fromBytes(run.head))
        val dummy = new mirror.FakeWorld(visibleState) { }
        val renderer = Femto.get(classOf[api.RendererInterface],
                                 "org.nlogo.render.Renderer", Array(dummy))
        g.clipRect(position.x, position.y,
                   ws.viewWidget.view.getWidth,
                   ws.viewWidget.view.getHeight)
        g.translate(position.x, position.y)
        renderer.paint(g.asInstanceOf[java.awt.Graphics2D], ws.viewWidget.view)
      }
    }
  }

  private def merge(oldState: Mirroring.State, bytes: Array[Byte]): Mirroring.State =
    Mirroring.merge(oldState, Serializer.fromBytes(bytes))

  object Scrubber extends JSlider {
    setValue(0)
    setBorder(BorderFactory.createTitledBorder("Tick: N/A"))
    addChangeListener(new ChangeListener{
      def stateChanged(e: ChangeEvent) {
        setBorder(BorderFactory.createTitledBorder("Tick: " + getValue))
        visibleState =
          if(getValue < ticks)
              run.take(getValue + 1)
                .foldLeft(Map(): Mirroring.State)(merge)
          else
              run.drop(ticks + 1).take(getValue - ticks)
                .foldLeft(visibleState)(merge)
        ticks = getValue
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
        out.writeObject(saveModel())
        out.writeObject(potemkinInterface.get.position)
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
              position = in.readObject().asInstanceOf[java.awt.Point],
              image = ImageIO.read(
                new java.io.ByteArrayInputStream(
                  in.readObject().asInstanceOf[Array[Byte]]))))
        run = in.readObject().asInstanceOf[Run]
        ticks = 0
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
