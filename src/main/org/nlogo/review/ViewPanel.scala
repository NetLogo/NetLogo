// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Graphics
import java.awt.Graphics2D

import org.nlogo.mirror.FakeWorld
import org.nlogo.mirror.ModelRun

import javax.swing.JPanel

class ViewPanel(run: ModelRun) extends JPanel {
  setBounds(run.viewArea.getBounds)
  override def paintComponent(g: Graphics) {
    super.paintComponent(g)
    for (frame <- run.currentFrame) {
      val g2d = g.create.asInstanceOf[Graphics2D]
      try {
        val renderer = new FakeWorld(frame.mirroredState).newRenderer
        renderer.trailDrawer.readImage(frame.drawingImage)
        renderer.paint(g2d, run.fixedViewSettings)
      } finally {
        g2d.dispose()
      }
    }
  }
}
