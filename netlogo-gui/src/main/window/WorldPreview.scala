// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, BorderLayout, Dimension, Font, Graphics, GridBagConstraints, GridBagLayout, Insets }
import javax.swing.{ JLabel, JPanel }

import org.nlogo.awt.Fonts
import org.nlogo.swing.{ Transparent, Utils }
import org.nlogo.theme.InterfaceColors

class WorldPreview(width: Int, height: Int) extends JPanel(new BorderLayout) with Transparent {
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

  private class PreviewPanel extends JPanel(new GridBagLayout) {
    private val topLeft = new JLabel
    private val topRight = new JLabel
    private val bottomLeft = new JLabel
    private val bottomRight = new JLabel
    private val errorLabel = new JLabel(
      "<html>Invalid world dimensions. The origin (0, 0) must be inside the dimensions of the world.</html>")

    private val font = new Font(Fonts.platformMonospacedFont, Font.PLAIN, 10)

    locally {
      val c = new GridBagConstraints

      c.gridx = 0
      c.gridy = 0
      c.anchor = GridBagConstraints.NORTHWEST
      c.weightx = 1
      c.weighty = 1
      c.insets = new Insets(10, 10, 10, 10)

      add(topLeft, c)

      c.gridx = 1
      c.anchor = GridBagConstraints.NORTHEAST

      add(topRight, c)

      c.gridx = 0
      c.gridy = 1
      c.anchor = GridBagConstraints.SOUTHWEST

      add(bottomLeft, c)

      c.gridx = 1
      c.anchor = GridBagConstraints.SOUTHEAST

      add(bottomRight, c)

      c.gridx = 0
      c.gridy = 0
      c.gridwidth = 2
      c.gridheight = 2
      c.anchor = GridBagConstraints.CENTER
      c.fill = GridBagConstraints.BOTH

      add(errorLabel, c)

      topLeft.setForeground(Color.WHITE)
      topRight.setForeground(Color.WHITE)
      bottomLeft.setForeground(Color.WHITE)
      bottomRight.setForeground(Color.WHITE)
      errorLabel.setForeground(Color.WHITE)

      topLeft.setFont(font)
      topRight.setFont(font)
      bottomLeft.setFont(font)
      bottomRight.setFont(font)
      errorLabel.setFont(font)
    }

    override def getPreferredSize: Dimension =
      new Dimension(width, height)

    override def paintComponent(g: Graphics): Unit = {
      val g2d = Utils.initGraphics2D(g)

      // basic frame

      g2d.setColor(Color.BLACK)
      g2d.fillRect(0, 0, getWidth, getHeight)

      g2d.setColor(Color.WHITE)
      g2d.drawRect(5, 5, getWidth - 11, getHeight - 11)

      // origin and coordinates

      if (errors.nonEmpty) {
        errorLabel.setVisible(true)

        topLeft.setVisible(false)
        topRight.setVisible(false)
        bottomLeft.setVisible(false)
        bottomRight.setVisible(false)
      } else {
        errorLabel.setVisible(false)

        val x = 10 + ((getWidth - 20) * -minx.toFloat / (maxx - minx)).toInt
        val y = 10 + ((getHeight - 20) * maxy.toFloat / (maxy - miny)).toInt

        g2d.setColor(Color.RED)
        g2d.fillOval(x - 4, y - 4, 9, 9)

        g2d.setColor(Color.WHITE)
        g2d.drawLine(x - 5, y, x + 5, y)
        g2d.drawLine(x, y - 5, x, y + 5)

        g2d.setFont(font)

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
        val chunkSize = (getHeight - 10) / 16.0

        g2d.setColor(new Color(33, 204, 0))
        g2d.fillRect(1, 5, 3, getHeight - 10)
        g2d.fillRect(getWidth - 4, 5, 3, getHeight - 10)

        g2d.setColor(Color.BLACK)

        for (i <- 1 to 15) {
          g2d.fillRect(1, 4 + (i * chunkSize).toInt, 3, 2)
          g2d.fillRect(getWidth - 4, 4 + (i * chunkSize).toInt, 3, 2)
        }
      } else {
        g2d.setColor(Color.RED)
        g2d.fillRect(1, 5, 3, getHeight - 10)
        g2d.fillRect(getWidth - 4, 5, 3, getHeight - 10)
      }

      // vertical wrap

      if (wrapY) {
        val chunkSize = (getWidth - 10) / 16.0

        g2d.setColor(new Color(33, 204, 0))
        g2d.fillRect(5, 1, getWidth - 10, 3)
        g2d.fillRect(5, getHeight - 4, getWidth - 10, 3)

        g2d.setColor(Color.BLACK)

        for (i <- 1 to 15) {
          g2d.fillRect(4 + (i * chunkSize).toInt, 1, 2, 3)
          g2d.fillRect(4 + (i * chunkSize).toInt, getHeight - 4, 2, 3)
        }
      } else {
        g2d.setColor(Color.RED)
        g2d.fillRect(5, 1, getWidth - 10, 3)
        g2d.fillRect(5, getHeight - 4, getWidth - 10, 3)
      }
    }
  }
}
