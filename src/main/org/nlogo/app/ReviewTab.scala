package org.nlogo.app

import javax.swing._
import javax.swing.event.{ ChangeEvent, ChangeListener }
import java.awt.image.BufferedImage
import org.nlogo.{ api, mirror, nvm, window }
import mirror.{ Mirroring, Mirrorables, Serializer }

class ReviewTab(ws: window.GUIWorkspace) extends JPanel
with window.Events.BeforeLoadEventHandler {

  type Run = Seq[Array[Byte]]

  var run: Run = Seq()
  var state: Mirroring.State = Map()
  var visibleState: Mirroring.State = Map()
  var ticks = 0

  var potemkinInterface: Option[PotemkinInterface] = None

  case class PotemkinInterface(position: java.awt.Point,
                               image: BufferedImage)

  ws.listenerManager.addListener(
    new api.NetLogoAdapter {
      val count = Iterator.from(0)
      override def tickCounterChanged(ticks: Double) {
        if (ticks == 0) {
          reset()
        }
        grab()
        Scrubber.setMaximum(run.size - 1)
        InterfacePanel.repaint()
      }})

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
          image = org.nlogo.awt.Images.paintToImage(
            ws.viewWidget.findWidgetContainer.asInstanceOf[java.awt.Component])))
    }
    val (newState, update) =
      Mirroring.diffs(state, Mirrorables.allMirrorables(ws.world, ws.plotManager.plots))
    run :+= Serializer.toBytes(update)
    state = newState
    Scrubber.setEnabled(true)
  }

  object InterfacePanel extends JPanel {
    override def paintComponent(g: java.awt.Graphics) {
      super.paintComponent(g)
      potemkinInterface match {
        case None =>
          visibleState = Map()
          g.setColor(java.awt.Color.GRAY)
          g.fillRect(0, 0, getWidth, getHeight)
        case Some(PotemkinInterface(position, image)) =>
          g.setColor(java.awt.Color.WHITE)
          g.fillRect(0, 0, getWidth, getHeight)
          g.drawImage(image, 0, 0, null)
          if(visibleState.isEmpty)
            visibleState = Mirroring.merge(Map(), Serializer.fromBytes(run.head))
          val dummy = new mirror.FakeWorld(visibleState) { }
          val renderer = new org.nlogo.render.Renderer(dummy)
          g.clipRect(position.x, position.y,
                     ws.viewWidget.view.getWidth,
                     ws.viewWidget.view.getHeight)
          g.translate(position.x, position.y)
          renderer.paint(g.asInstanceOf[java.awt.Graphics2D], ws.viewWidget.view)
      }
    }
  }

  object Scrubber extends JSlider {
    setValue(0)
    setBorder(BorderFactory.createTitledBorder("Tick: N/A"))
    addChangeListener(new ChangeListener{
      def stateChanged(e: ChangeEvent) {
        setBorder(BorderFactory.createTitledBorder("Tick: " + getValue))
        def merge(oldState: Mirroring.State, bytes: Array[Byte]): Mirroring.State =
          Mirroring.merge(oldState, Serializer.fromBytes(bytes))
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

  setLayout(new java.awt.BorderLayout)
  add(InterfacePanel, java.awt.BorderLayout.CENTER)
  add(Scrubber, java.awt.BorderLayout.SOUTH)

  override def handle(e: window.Events.BeforeLoadEvent) {
    reset()
  }

}
