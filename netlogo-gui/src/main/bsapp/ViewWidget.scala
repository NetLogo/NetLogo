// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.bsapp

import java.awt.{ Dimension, Graphics, Graphics2D }
import java.awt.image.BufferedImage
import javax.swing.border.LineBorder

import org.nlogo.core.{ View, Widget }
import org.nlogo.swing.Utils
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.{ Editable, ViewWidgetInterface }

class ViewWidget(workspace: SemiHeadlessWorkspace) extends ViewWidgetInterface {
  private var buffer: Option[ViewBuffer] = None

  override def getAdditionalHeight: Int =
    getInsets.top + getInsets.bottom

  override def getEditable: Option[Editable] =
    None

  override def load(widget: Widget): Unit = {
    widget match {
      case view: View =>
        workspace.world.displayOn(false)
        workspace.loadWorld(view, workspace)
        workspace.world.tickCounter.clear()
        workspace.world.clearPatches()
        workspace.world.displayOn(true)

      case _ =>
    }
  }

  override def model: Widget =
    null

  override def doLayout(): Unit = {
    setSize(getPreferredSize)
  }

  override def getPreferredSize: Dimension = {
    new Dimension((workspace.world.worldWidth * workspace.patchSize).toInt,
                  (workspace.world.worldHeight * workspace.patchSize).toInt)
  }

  override def getMinimumSize: Dimension =
    getPreferredSize

  override def getMaximumSize: Dimension =
    getPreferredSize

  def reset(): Unit = {
    buffer = None

    repaint()
  }

  def paintBuffer(): Unit = {
    if (buffer.isEmpty && getWidth > 0) {
      val image = new BufferedImage(getWidth, getHeight, BufferedImage.TYPE_INT_ARGB)

      buffer = Option(ViewBuffer(image, image.createGraphics()))
    }

    buffer.foreach {
      case ViewBuffer(_, graphics) =>
        workspace.renderer.paint(graphics, workspace)
    }
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)

    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(InterfaceColors.viewBackground())
    g2d.fillRect(0, 0, getWidth, getHeight)

    buffer.foreach {
      case ViewBuffer(image, _) =>
        g2d.drawImage(image, 0, 0, null)
    }
  }

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.viewBackground())
    setBorder(new LineBorder(InterfaceColors.viewBorder(), 2))
  }

  private case class ViewBuffer(image: BufferedImage, graphics: Graphics2D)
}
