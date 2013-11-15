// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Color.GRAY
import java.awt.Color.WHITE

import scala.Option.option2Iterable

import javax.swing.JPanel

class InterfacePanel(val reviewTab: ReviewTab) extends JPanel {

  setOpaque(true)
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
    setBackground(if (reviewTab.state.currentRun.isDefined) WHITE else GRAY)
    super.paintComponent(g)
  }
}
