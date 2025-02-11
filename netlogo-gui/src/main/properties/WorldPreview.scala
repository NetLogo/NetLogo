// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ Color, BorderLayout, Dimension, Font, Graphics, Graphics2D, GridBagConstraints, GridBagLayout }
import javax.swing.{ JLabel, JPanel }

import org.nlogo.swing.{ Transparent, Utils }
import org.nlogo.theme.InterfaceColors

class WorldPreview(width: Int, height: Int) extends JPanel(new BorderLayout(0, 0)) with Transparent {
  private var wrapX, wrapY = false
  private var minx, maxx, miny, maxy = 0
  private val shapeLabel = new JLabel("Torus") {
    setForeground(InterfaceColors.dialogText)
  }
  private val worldPanel = new JPanel(new GridBagLayout) with Transparent {
    setSize(width, height)
  }

  private val theCanvas = new WorldPreviewCanvas(width - 10, height - 10)
  private val topY = new WorldPreviewWrapCanvas(
    width - 10, 5, WorldPreviewWrapCanvas.WRAP_Y)
  private val bottomY = new WorldPreviewWrapCanvas(
    width - 10, 5, WorldPreviewWrapCanvas.WRAP_Y)
  private val leftX = new WorldPreviewWrapCanvas(
    5, height - 10, WorldPreviewWrapCanvas.WRAP_X)
  private val rightX = new WorldPreviewWrapCanvas(
    5, height - 10, WorldPreviewWrapCanvas.WRAP_X)

  locally {
    val c = new GridBagConstraints

    c.gridy = 0

    worldPanel.add(new BlankAreaCanvas(5, 5), c)
    worldPanel.add(topY, c)
    worldPanel.add(new BlankAreaCanvas(5, 5), c)

    c.gridy = 1

    worldPanel.add(rightX, c)
    worldPanel.add(theCanvas, c)
    worldPanel.add(leftX, c)

    c.weighty = 1.0
    c.anchor = GridBagConstraints.NORTH
    c.gridy = 2

    worldPanel.add(new BlankAreaCanvas(5, 5), c)
    worldPanel.add(bottomY, c)
    worldPanel.add(new BlankAreaCanvas(5, 5), c)

    add(worldPanel, BorderLayout.NORTH)
    add(shapeLabel, BorderLayout.CENTER)

    setSize(width, height + 10)
    setVisible(true)
  }

  def update(field: String, opt: Option[Any]): Unit = {
    if(!opt.isDefined) return
    val value = opt.get
    theCanvas.update(field, value)
    topY.update(field, value)
    bottomY.update(field, value)
    leftX.update(field, value)
    rightX.update(field, value)

    def asBoolean = value.asInstanceOf[java.lang.Boolean].booleanValue
    def asInt = value.asInstanceOf[java.lang.Integer].intValue

    // this is a bit ugly as there's no static checking that these field names
    // match the real names in the code - ST 3/19/12
    if (field == "wrappingX") {
      wrapX = asBoolean
      updateLabel()
    } else if (field == "wrappingY") {
      wrapY = asBoolean
      updateLabel()
    } else if (field == "minPxcor") {
      minx = asInt
      updateLabel()
    } else if (field == "maxPxcor") {
      maxx = asInt
      updateLabel()
    } else if (field == "minPycor") {
      miny = asInt
      updateLabel()
    } else if (field == "maxPycor") {
      maxy = asInt
      updateLabel()
    }
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

  private class BlankAreaCanvas(w: Int, h: Int) extends JPanel {
    setBackground(Color.BLACK)

    override def getPreferredSize: Dimension =
      new Dimension(w, h)
  }

  private class WorldPreviewCanvas(width: Int, height: Int) extends JPanel {
    val TO_LEFT, TO_BOTTOM = -1
    val TO_RIGHT, TO_TOP = 1
    val AT_ORIGIN = 0
    val PAD = 5

    private var minPxcor, maxPxcor, minPycor, maxPycor = 0
    private var xOrigin, yOrigin = 0

    private val monoFont = org.nlogo.awt.Fonts.platformMonospacedFont

    setBackground(Color.BLACK)

    override def getPreferredSize: Dimension =
      new Dimension(width, height)

    def update(field: String, value: Any): Unit = {
      def asInt = value.asInstanceOf[java.lang.Integer].intValue
      field match {
        case "minPxcor" => minPxcor = asInt
        case "maxPxcor" => maxPxcor = asInt
        case "minPycor" => minPycor = asInt
        case "maxPycor" => maxPycor = asInt
        case _ => return
      }
      repaint()
    }

    override def paintComponent(g: Graphics): Unit = {
      super.paintComponent(g)
      val g2d = Utils.initGraphics2D(g)
      if (minPxcor > 0 || maxPxcor < 0 || minPycor > 0 || maxPycor < 0) {
        paintError(g2d, "Invalid world dimensions. The origin (0,0) must be inside the dimensions of the world.")
      } else {
        xOrigin =
          ((if(maxPxcor == minPxcor) (width / 2.0).toInt
            else (((width.toDouble)/((maxPxcor - minPxcor).toDouble)) * (-minPxcor)).toInt)
           max PAD
           min (width - PAD))
        yOrigin =
          ((if(maxPycor == minPycor) (height / 2.0).toInt
            else (((height.toDouble)/((maxPycor - minPycor).toDouble)) * maxPycor).toInt)
           max PAD
           min (height - PAD))
        paintCorners(g2d, 2, PAD, Color.WHITE, Color.WHITE)
        paintOrigin(g2d, PAD)
        g2d.setColor(Color.WHITE)
        g2d.drawRect(0, 0, width - 1, height - 1)
      }
    }

    private def paintError(g2d: Graphics2D, error: String): Unit = {
      g2d.setFont(new Font(monoFont, Font.PLAIN, 12))
      val fm = g2d.getFontMetrics
      g2d.setColor(Color.GRAY)
      // draw error
      val xTextOff = ((fm.getStringBounds(error, g2d).getWidth()) / -2).toInt
      val yTextOff = ((fm.getStringBounds(error, g2d).getHeight()) / -2).toInt
      g2d.drawString(error, width / 2 + xTextOff, height / 2 + yTextOff)
    }

    def paintOrigin(g2d: Graphics2D, pad: Int): Unit = {
      g2d.setColor(Color.RED)
      g2d.fillOval(xOrigin - 4, yOrigin - 4, 8, 8)
      paintOriginLabel(g2d, xOrigin, yOrigin, 5, AT_ORIGIN, AT_ORIGIN)
    }

    def paintOriginLabel(g2d: Graphics2D, x: Int, y: Int, radius: Int,
                         txtHorizOrient: Int, txtVertOrient: Int): Unit = {
      g2d.setFont(new Font(monoFont, Font.PLAIN, 10))
      val fm = g2d.getFontMetrics

      //Draw Cross
      g2d.setColor(Color.WHITE)
      g2d.drawLine(x - radius, y, x + radius, y)
      g2d.drawLine(x, y - radius, x, y + radius)

      //Draw Label
      val label = "(0,0)"
      val labelWidth = fm.getStringBounds(label, g2d).getWidth.toInt
      val xTextOff =
        if (x + 8 + labelWidth > width) //will label be off the edge of the preview
          -(8 + labelWidth) //if so put on left
        else 8 //keep on right
      val yTextOff = -2 + (fm.getStringBounds(label, g2d).getHeight() / 2).toInt
      g2d.drawString(label, x + xTextOff, y + yTextOff)
    }

    def paintCorners(g: Graphics2D, radius: Int, pad: Int,
                     center: Color, border: Color): Unit = {
      val r = radius max 1
      paintDot(g, PAD,       PAD,        r, TO_LEFT,  TO_TOP,    center, border)
      paintDot(g, width-PAD, PAD,        r, TO_RIGHT, TO_TOP,    center, border)
      paintDot(g, width-PAD, height-PAD, r, TO_RIGHT, TO_BOTTOM, center, border)
      paintDot(g, PAD,       height-PAD, r, TO_LEFT,  TO_BOTTOM, center, border)
    }

    private def paintDot(g2d: Graphics2D, x: Int, y: Int, radius: Int, txtHorizOrient: Int, txtVertOrient: Int,
                         center: Color, border: Color): Unit = {
      if (xOrigin == x && yOrigin == y) return
      g2d.setFont(new Font(monoFont, Font.PLAIN, 10))
      val fm = g2d.getFontMetrics
      val diameter = radius * 2
      g2d.setColor(center)
      g2d.fillOval(x - radius, y - radius, diameter, diameter)
      //Draw Label
      var xTextOff: Int = 0
      var yTextOff: Int = 0
      var label: String = "(0,0)"
      if (txtHorizOrient == TO_LEFT && txtVertOrient == TO_TOP){
        label = s"($minPxcor, $maxPycor)"
        xTextOff = 4
        yTextOff = fm.getStringBounds(label, g2d).getHeight.toInt
      } else if (txtHorizOrient == TO_RIGHT && txtVertOrient == TO_TOP) {
        label = s"($maxPxcor, $maxPycor)"
        xTextOff = -4 - fm.getStringBounds(label, g2d).getWidth.toInt
        yTextOff = fm.getStringBounds(label, g2d).getHeight.toInt
      } else if (txtHorizOrient == TO_RIGHT && txtVertOrient == TO_BOTTOM) {
        label = s"($maxPxcor, $minPycor)"
        xTextOff = -4 - fm.getStringBounds(label, g2d).getWidth.toInt
        yTextOff = -4
      } else if (txtHorizOrient == TO_LEFT && txtVertOrient == TO_BOTTOM) {
        label = s"($minPxcor, $minPycor)"
        xTextOff = 4
        yTextOff = -4
      } else {
        xTextOff = 13
        yTextOff = -3 + (fm.getStringBounds(label, g2d).getHeight() / 2).toInt
      }
      g2d.drawString(label, x + xTextOff, y + yTextOff)
    }
  }

  private object WorldPreviewWrapCanvas {
    val WRAP_X = 0
    val WRAP_Y = 1
  }

  private class WorldPreviewWrapCanvas(width: Int, height: Int, wrapDim: Int) extends JPanel {
    setBackground(Color.black)

    var wrapX, wrapY = false

    override def getPreferredSize: Dimension =
      new Dimension(width, height)

    def update(field: String, value: Any): Unit = {
      if (field != null) {
        if (field == "wrappingX") {
          wrapX = value.asInstanceOf[java.lang.Boolean].booleanValue
        } else if (field == "wrappingY") {
          wrapY = value.asInstanceOf[java.lang.Boolean].booleanValue
        }
      }

      repaint()
    }

    override def paintComponent(g: Graphics): Unit = {
      super.paintComponent(g)

      val g2d = Utils.initGraphics2D(g)

      if (wrapDim == WorldPreviewWrapCanvas.WRAP_X)
        drawWrapXMonitor(g2d)
      if (wrapDim == WorldPreviewWrapCanvas.WRAP_Y)
        drawWrapYMonitor(g2d)
    }

    def drawWrapXMonitor(g2d: Graphics2D): Unit = {
      if (wrapX) {
        g2d.setColor(new Color(33, 204, 0))
        g2d.fillRect(width / 4, 0, width / 2, height)
        g2d.setColor(Color.black)
        for (i <- 0 to 16)
          g2d.fillRect(width / 4, (i * height) / 16, width / 2, 2)
      } else {
        g2d.setColor(Color.RED)
        g2d.fillRect(width / 4, 0, width / 2, height)
      }
    }

    def drawWrapYMonitor(g2d: Graphics2D): Unit = {
      if (wrapY) {
        g2d.setColor(new Color(33, 204, 0))
        g2d.fillRect(0, height / 4, width, height / 2)
        g2d.setColor(Color.BLACK)
        for (i <- 0 to 16)
          g2d.fillRect((i * width) / 16, height / 4, 2, height / 2)
      } else {
        g2d.setColor(Color.RED)
        g2d.fillRect(0, height / 4, width, height / 2)
      }
    }
  }
}
