// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Color.GRAY
import java.awt.Color.WHITE

import scala.Option.option2Iterable

import javax.swing.JPanel

class InterfacePanel(val reviewTab: ReviewTab) extends JPanel {

  setLayout(null) // disable layout manager to use absolute positioning

  private var widgetPanels: Seq[JPanel] = Seq.empty

  reviewTab.state.afterRunChangePub.newSubscriber { event =>
    widgetPanels.foreach(remove)
    widgetPanels = event.newRun.toSeq.flatMap {
      WidgetPanels.create(reviewTab.ws, _)
    }
    widgetPanels.foreach(add)
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
    }
  }
}
