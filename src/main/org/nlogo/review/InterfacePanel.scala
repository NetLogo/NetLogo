// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Color.GRAY
import java.awt.Color.WHITE

import org.nlogo.mirror.ModelRun
import org.nlogo.window
import org.nlogo.window.PlotWidget

import javax.swing.JPanel

class InterfacePanel(val reviewTab: ReviewTab) extends JPanel {

  setLayout(null) // disable layout manager to use absolute positioning

  private var widgetPanels: Seq[JPanel] = Seq.empty

  reviewTab.state.afterRunChangePub.newSubscriber { event =>
    widgetPanels.foreach(remove)
    for (run <- event.newRun)
      widgetPanels = newPlotPanels(run) :+ new ViewPanel(run)
    widgetPanels.foreach(add)
  }

  def newPlotPanels(run: ModelRun): Seq[PlotPanel] = {
    val container = reviewTab.ws.viewWidget.findWidgetContainer
    reviewTab.workspaceWidgets.collect {
      case plotWidget: PlotWidget =>
        new PlotPanel(
          run,
          plotWidget.plot,
          container.getUnzoomedBounds(plotWidget),
          plotWidget,
          plotWidget.gui.legend.open)
    }
  }

  def repaintWidgets(g: java.awt.Graphics) {
    for {
      frame <- reviewTab.state.currentFrame
      values = frame.mirroredState
        .filterKeys(_.kind == org.nlogo.mirror.Mirrorables.WidgetValue)
        .toSeq
        .sortBy { case (agentKey, vars) => agentKey.id } // should be z-order
        .map { case (agentKey, vars) => vars(0).asInstanceOf[String] }
      (w, v) <- reviewTab.widgetHooks.map(_.widget) zip values
    } {
      val g2d = g.create.asInstanceOf[java.awt.Graphics2D]
      try {
        val container = reviewTab.ws.viewWidget.findWidgetContainer
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

  override def paintComponent(g: java.awt.Graphics) {
    super.paintComponent(g)
    g.setColor(if (reviewTab.state.currentRun.isDefined) WHITE else GRAY)
    g.fillRect(0, 0, getWidth, getHeight)
    for {
      run <- reviewTab.state.currentRun
      img = run.interfaceImage
    } {
      setPreferredSize(new java.awt.Dimension(img.getWidth, img.getHeight))
      g.drawImage(img, 0, 0, null)
      repaintWidgets(g)
    }
  }
}
