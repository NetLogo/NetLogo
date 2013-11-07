// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Graphics
import java.awt.Graphics2D
import org.nlogo.mirror.FakeWorld
import org.nlogo.mirror.ModelRun
import javax.swing.JPanel
import org.nlogo.mirror.FixedViewSettings

/** This class is just a stub for now: it should eventually draw 
 *  a proper ViewWidgetPanel */
class ViewWidgetPanel(bounds: java.awt.Rectangle) extends JPanel {
  setLayout(null)
  setBounds(bounds)
}

class ViewPanel(
  run: ModelRun,
  bounds: java.awt.Rectangle,
  viewSettings: FixedViewSettings)
  extends JPanel {
  setBounds(bounds)
  override def paintComponent(g: Graphics) {
    super.paintComponent(g)
    for (frame <- run.currentFrame) {
      val g2d = g.create.asInstanceOf[Graphics2D]
      try {
        val renderer = new FakeWorld(frame.mirroredState).newRenderer
        renderer.trailDrawer.readImage(frame.drawingImage)
        renderer.paint(g2d, viewSettings)
      } finally {
        g2d.dispose()
      }
    }
  }
}
