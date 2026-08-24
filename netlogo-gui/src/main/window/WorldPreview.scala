// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, BorderLayout, Dimension, Graphics }
import javax.swing.{ Box, BoxLayout, JLabel, JPanel }

import org.nlogo.editor.EditorConfiguration
import org.nlogo.swing.{ BoxRow, Transparent, Utils, Zoomable, ZoomableBorder }
import org.nlogo.theme.InterfaceColors

class WorldPreview(myWidth: Int, myHeight: Int) extends JPanel(new BorderLayout) with Transparent {
  private var wrapX, wrapY = false
  private var minx, maxx, miny, maxy = 0

  private var errors = Set[String]()

  private val shapeLabel = new JLabel("Torus") {
    setForeground(InterfaceColors.dialogText())
  }

  add(new PreviewPanel, BorderLayout.NORTH)
  add(shapeLabel, BorderLayout.SOUTH)

  setVisible(true)

  // this is a bit ugly as there's no static checking that these field names
  // match the real names in the code - ST 3/19/12

  def updateInt(field: String, value: Int): Unit = {
    if (field == "minPxcor") {
      minx = value
    } else if (field == "maxPxcor") {
      maxx = value
    } else if (field == "minPycor") {
      miny = value
    } else if (field == "maxPycor") {
      maxy = value
    }

    removeError(field)
    updateLabel()
    repaint()
  }

  def updateBoolean(field: String, value: Boolean): Unit = {
    if (field == "wrappingX") {
      wrapX = value
    } else if (field == "wrappingY") {
      wrapY = value
    }

    updateLabel()
    repaint()
  }

  def setError(field: String): Unit = {
    errors += field

    repaint()
  }

  def removeError(field: String): Unit = {
    errors -= field

    repaint()
  }

  private def updateLabel(): Unit = {
    val text = (wrapX, wrapY) match {
      case (true, true) => "Torus"
      case (true, false) => "Vertical Cylinder"
      case (false, true) => "Horizontal Cylinder"
      case (false, false) => "Box"
    }
    shapeLabel.setText(s"$text: ${maxx - minx + 1} x ${maxy - miny + 1}")
  }

  private class PreviewPanel extends JPanel with Zoomable {
    private val topLeft = new JLabel
    private val topRight = new JLabel
    private val bottomLeft = new JLabel
    private val bottomRight = new JLabel
    private val errorLabel = new JLabel(
      "<html>Invalid world dimensions. The origin (0, 0) must be inside the dimensions of the world.</html>")

    private var monoFont = EditorConfiguration.getMonospacedFont.deriveFont(Utils.zoom(10f))

    topLeft.setForeground(Color.WHITE)
    topRight.setForeground(Color.WHITE)
    bottomLeft.setForeground(Color.WHITE)
    bottomRight.setForeground(Color.WHITE)
    errorLabel.setForeground(Color.WHITE)

    topLeft.setFont(monoFont)
    topRight.setFont(monoFont)
    bottomLeft.setFont(monoFont)
    bottomRight.setFont(monoFont)
    errorLabel.setFont(monoFont)

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
    setBorder(new ZoomableBorder(10, 10, 10, 10))

    add(new BoxRow(Seq(topLeft, Box.createHorizontalGlue, topRight)))
    add(Box.createVerticalGlue)
    add(new BoxRow(Seq(bottomLeft, Box.createHorizontalGlue, bottomRight)))

    override def getPreferredSize: Dimension =
      new Dimension(Utils.zoom(myWidth), Utils.zoom(myHeight))

    override def paintComponent(g: Graphics): Unit = {
      val g2d = Utils.initGraphics2D(g)

      val border: Int = Utils.zoom(5)

      // basic frame

      g2d.setColor(Color.BLACK)
      g2d.fillRect(0, 0, getWidth, getHeight)

      g2d.setColor(Color.WHITE)
      g2d.drawRect(border, border, getWidth - border * 2 - 1, getHeight - border * 2 - 1)

      // origin and coordinates

      if (errors.nonEmpty) {
        errorLabel.setVisible(true)

        topLeft.setVisible(false)
        topRight.setVisible(false)
        bottomLeft.setVisible(false)
        bottomRight.setVisible(false)
      } else {
        errorLabel.setVisible(false)

        val x = border * 2 + ((getWidth - border * 4) * -minx.toFloat / (maxx - minx)).toInt
        val y = border * 2 + ((getHeight - border * 4) * maxy.toFloat / (maxy - miny)).toInt

        g2d.setColor(Color.RED)
        g2d.fillOval(x - border + 1, y - border + 1, border * 2 - 1, border * 2 - 1)

        g2d.setColor(Color.WHITE)
        g2d.drawLine(x - border, y, x + border, y)
        g2d.drawLine(x, y - border, x, y + border)

        g2d.setFont(monoFont)

        val metrics = g2d.getFontMetrics
        val width = metrics.stringWidth("0, 0)")
        val height = metrics.getHeight

        if (x + 8 + width > getWidth - 20) {
          g2d.drawString("(0, 0)", x - width - 8, y + height / 2)
        } else {
          g2d.drawString("(0, 0)", x + 8, y + height / 2)
        }

        topLeft.setVisible(x > topLeft.getX + topLeft.getWidth + 8 || y > topLeft.getY + topLeft.getHeight + 8)
        topRight.setVisible(x < topRight.getX - width - 8 || y > topRight.getY + topRight.getHeight + 8)
        bottomLeft.setVisible(x > bottomLeft.getX + bottomLeft.getWidth + 8 || y < bottomLeft.getY - height - 8)
        bottomRight.setVisible(x < getWidth - 10 - bottomRight.getWidth - 8 ||
                               y < getHeight - 10 - bottomRight.getHeight - height - 8)

        topLeft.setText(s"($minx, $maxy)")
        topRight.setText(s"($maxx, $maxy)")
        bottomLeft.setText(s"($minx, $miny)")
        bottomRight.setText(s"($maxx, $miny)")
      }

      // horizontal wrap

      if (wrapX) {
        val chunkSize = (getHeight - border * 2) / 16.0

        g2d.setColor(new Color(33, 204, 0))
        g2d.fillRect(1, border, border - 2, getHeight - border * 2)
        g2d.fillRect(getWidth - border + 1, border, border - 2, getHeight - border * 2)

        g2d.setColor(Color.BLACK)

        for (i <- 1 to 15) {
          g2d.fillRect(1, border - 1 + (i * chunkSize).toInt, border - 2, 2)
          g2d.fillRect(getWidth - border + 1, border - 1 + (i * chunkSize).toInt, border - 2, 2)
        }
      } else {
        g2d.setColor(Color.RED)
        g2d.fillRect(1, border, border - 2, getHeight - border * 2)
        g2d.fillRect(getWidth - border + 1, border, border - 2, getHeight - border * 2)
      }

      // vertical wrap

      if (wrapY) {
        val chunkSize = (getWidth - border * 2) / 16.0

        g2d.setColor(new Color(33, 204, 0))
        g2d.fillRect(border, 1, getWidth - border * 2, border - 2)
        g2d.fillRect(border, getHeight - border + 1, getWidth - border * 2, border - 2)

        g2d.setColor(Color.BLACK)

        for (i <- 1 to 15) {
          g2d.fillRect(border - 1 + (i * chunkSize).toInt, 1, 2, border - 2)
          g2d.fillRect(border - 1 + (i * chunkSize).toInt, getHeight - border + 1, 2, border - 2)
        }
      } else {
        g2d.setColor(Color.RED)
        g2d.fillRect(border, 1, getWidth - border * 2, border - 2)
        g2d.fillRect(border, getHeight - border + 1, getWidth - border * 2, border - 2)
      }
    }

    override def zoom(oldZoom: Float): Unit = {
      monoFont = EditorConfiguration.getMonospacedFont.deriveFont(Utils.zoom(10f))
    }
  }
}
