// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Graphics
import java.awt.Graphics2D

import org.nlogo.api.Dump
import org.nlogo.awt.Fonts.adjustDefaultFont
import org.nlogo.mirror.FakeWorld
import org.nlogo.mirror.FixedViewSettings
import org.nlogo.mirror.ModelRun
import org.nlogo.swing.Utils.createWidgetBorder
import org.nlogo.window.InterfaceColors.GRAPHICS_BACKGROUND

import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel

class ViewWidgetPanel(
  run: ModelRun,
  viewWidgetBounds: java.awt.Rectangle,
  viewBounds: java.awt.Rectangle,
  viewSettings: FixedViewSettings)
  extends JPanel {
  setBounds(viewWidgetBounds)
  setLayout(null)
  setBackground(GRAPHICS_BACKGROUND)

  locally {
    val matteBorder = BorderFactory.createMatteBorder(1, 3, 4, 2, GRAPHICS_BACKGROUND)
    setBorder(BorderFactory.createCompoundBorder(createWidgetBorder, matteBorder))
  }

  locally {
    val viewPanel = new ViewPanel(run, viewSettings)
    add(viewPanel)
    viewPanel.setBounds(viewBounds)
  }

  locally {
    val ticksCounter = new TicksCounter(run)
    ticksCounter.setBounds(
      viewBounds.x, getInsets.top,
      viewBounds.width, viewBounds.y - getInsets.top - 1)
    add(ticksCounter)
  }
}

class TicksCounter(run: ModelRun) extends JLabel {
  adjustDefaultFont(this)
  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    for {
      frame <- run.currentFrame
      ticks <- frame.ticks
    } setText("ticks: " + Dump.number(ticks))
  }
}

class ViewPanel(
  run: ModelRun,
  viewSettings: FixedViewSettings)
  extends JPanel {
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
