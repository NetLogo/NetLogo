// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Graphics
import java.awt.Graphics2D

import org.nlogo.api.Dump
import org.nlogo.awt.Fonts.adjustDefaultFont
import org.nlogo.mirror.FakeWorld
import org.nlogo.mirror.ModelRun
import org.nlogo.swing.Utils.createWidgetBorder
import org.nlogo.window.InterfaceColors.GRAPHICS_BACKGROUND
import org.nlogo.window.ViewBoundsCalculator.calculateViewBounds

import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel

class ViewWidgetPanel(
  run: ModelRun,
  viewWidgetBounds: java.awt.Rectangle,
  viewSettings: ReviewTabViewSettings,
  worldHeight: Int,
  insideBorderHeight: Int)
  extends JPanel {
  setBounds(viewWidgetBounds)
  setLayout(null)
  setBackground(GRAPHICS_BACKGROUND)

  locally {
    val matteBorder = BorderFactory.createMatteBorder(1, 3, 4, 2, GRAPHICS_BACKGROUND)
    setBorder(BorderFactory.createCompoundBorder(createWidgetBorder, matteBorder))
  }

  val viewPanel = new ViewPanel(run, viewSettings)
  val viewBounds = calculateViewBounds(this, 1,
    viewSettings.patchSize, worldHeight)
  add(viewPanel)
  viewPanel.setBounds(viewBounds)

  val ticksCounter = new TicksCounter(run)
  ticksCounter.setBounds(
    viewBounds.x, getInsets.top,
    viewBounds.width, viewBounds.y - getInsets.top - 1)
  add(ticksCounter)
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
  viewSettings: ReviewTabViewSettings)
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
