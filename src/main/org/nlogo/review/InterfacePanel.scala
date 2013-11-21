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
    // we go the panels in back-to-front order but we
    // need to add them in front-to-back order so
    // that they're painted in the correct z-order:
    widgetPanels.reverse.foreach(add)
  }

  override def paintComponent(g: java.awt.Graphics) {
    setBackground(if (reviewTab.state.currentRun.isDefined) WHITE else GRAY)
    super.paintComponent(g)
  }
}
