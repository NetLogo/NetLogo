// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

class WorldPreview(width: Int, height: Int)
  extends javax.swing.JPanel(new java.awt.BorderLayout(0, 0))
{

  setSize(width, height + 10)

  private var wrapX, wrapY = false
  private var minx, maxx, miny, maxy = 0
  private val shapeLabel = new javax.swing.JLabel("Torus")
  private val worldPanel = new javax.swing.JPanel(new java.awt.GridBagLayout) {
    setSize(width, height)
  }
  private val c = new java.awt.GridBagConstraints
  private val theCanvas = new WorldPreviewCanvas(width - 10, height - 10)
  private val topY = new WorldPreviewWrapCanvas(
    width - 10, 5, WorldPreviewWrapCanvas.WRAP_Y)
  private val bottomY = new WorldPreviewWrapCanvas(
    width - 10, 5, WorldPreviewWrapCanvas.WRAP_Y)
  private val leftX = new WorldPreviewWrapCanvas(
    5, height - 10, WorldPreviewWrapCanvas.WRAP_X)
  private val rightX = new WorldPreviewWrapCanvas(
    5, height - 10, WorldPreviewWrapCanvas.WRAP_X)

  c.gridx = 0
  c.gridy = 0
  worldPanel.add(new BlankAreaCanvas(5, 5), c)

  c.gridx = 1
  c.gridy = 0
  worldPanel.add(topY, c)

  c.gridx = 2
  c.gridy = 0
  worldPanel.add(new BlankAreaCanvas(5, 5), c)

  c.gridx = 0
  c.gridy = 1
  worldPanel.add(rightX, c)

  c.gridx = 1
  c.gridy = 1
  worldPanel.add(theCanvas, c)

  c.gridx = 2
  c.gridy = 1
  worldPanel.add(leftX, c)

  c.weighty = 1.0
  c.anchor = java.awt.GridBagConstraints.NORTH
  c.gridx = 0
  c.gridy = 2
  worldPanel.add(new BlankAreaCanvas(5, 5), c)

  c.weighty = 1.0
  c.anchor = java.awt.GridBagConstraints.NORTH
  c.gridx = 1
  c.gridy = 2
  worldPanel.add(bottomY, c)

  c.weighty = 1.0
  c.anchor = java.awt.GridBagConstraints.NORTH
  c.gridx = 2
  c.gridy = 2
  worldPanel.add(new BlankAreaCanvas(5, 5), c)

  add(worldPanel, java.awt.BorderLayout.NORTH)

  add(shapeLabel, java.awt.BorderLayout.CENTER)

  setVisible(true)

  def update(field: String, opt: Option[Any]) {
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
    if(field == "wrappingX") {
      wrapX = asBoolean
      updateLabel()
    }
    else if(field == "wrappingY") {
      wrapY = asBoolean
      updateLabel()
    }
    else if(field == "minPxcor") {
      minx = asInt
      updateLabel()
    }
    else if(field == "maxPxcor") {
      maxx = asInt
      updateLabel()
    }
    else if(field == "minPycor") {
      miny = asInt
      updateLabel()
    }
    else if(field == "maxPycor") {
      maxy = asInt
      updateLabel()
    }
  }

  private def updateLabel() {
    val text = (wrapX, wrapY) match {
      case (true, true) => "Torus"
      case (true, false) => "Vertical Cylinder"
      case (false, true) => "Horizontal Cylinder"
      case (false, false) => "Box"
    }
    shapeLabel.setText(text + ": " + (maxx - minx + 1)  + " x " + (maxy - miny + 1))
  }

  private class BlankAreaCanvas(w: Int, h: Int) extends java.awt.Canvas {
    setSize(w, h)
    setBackground(java.awt.Color.black)
    repaint()
    override def paint(g: java.awt.Graphics) { }
  }

  private class WorldPreviewCanvas(width: Int, height: Int) extends java.awt.Canvas {
    val TO_LEFT, TO_BOTTOM = -1
    val TO_RIGHT, TO_TOP = 1
    val AT_ORIGIN = 0
    val PAD = 5

    private var minPxcor, maxPxcor, minPycor, maxPycor = 0
    private var xOrigin, yOrigin = 0

    private val monoFont = org.nlogo.awt.Fonts.platformMonospacedFont

    setSize(width, height)
    setBackground(java.awt.Color.black)
    repaint()  // necessary? - ST 2/18/10

    def update(field: String, value: Any) {
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

    override def paint(g: java.awt.Graphics) {
      if(minPxcor > 0 || maxPxcor < 0 || minPycor > 0 || maxPycor < 0)
        paintError(g.asInstanceOf[java.awt.Graphics2D],
                   "Invalid world dimensions. " +
                   "The origin (0,0) must be inside the dimensions of the world.")
      else {
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
        paintCorners(g.asInstanceOf[java.awt.Graphics2D], 2, PAD,
                     java.awt.Color.WHITE, java.awt.Color.WHITE)
        paintOrigin(g.asInstanceOf[java.awt.Graphics2D], PAD)
        g.setColor(java.awt.Color.WHITE)
        g.drawRect(0, 0, width - 1, height - 1)
      }
    }

    private def paintError(g: java.awt.Graphics2D, error: String)
    {
      val font = new java.awt.Font(monoFont, java.awt.Font.PLAIN, 12)
      g.setFont(font)
      val fm = g.getFontMetrics
      g.setColor(java.awt.Color.GRAY)
      // draw error
      val xTextOff = ((fm.getStringBounds(error, g).getWidth()) / -2).toInt
      val yTextOff = ((fm.getStringBounds(error, g).getHeight()) / -2).toInt
      g.drawString(error, width / 2 + xTextOff, height / 2 + yTextOff)
    }

    def paintOrigin(g: java.awt.Graphics2D, pad: Int) {
      g.setColor(java.awt.Color.RED)
      g.fillOval(xOrigin - 4, yOrigin - 4, 8, 8)
      paintOriginLabel(g, xOrigin, yOrigin, 5, AT_ORIGIN, AT_ORIGIN)
    }

    def paintOriginLabel(g: java.awt.Graphics2D, x: Int, y: Int,
                         radius: Int, txtHorizOrient: Int, txtVertOrient: Int)
    {

      val font = new java.awt.Font(monoFont, java.awt.Font.PLAIN, 10)
      g.setFont(font)
      val fm = g.getFontMetrics

      //Draw Cross
      g.setColor(java.awt.Color.WHITE)
      g.drawLine(x-radius, y,        x+radius, y)
      g.drawLine(x,        y-radius, x,        y+radius)

      //Draw Label
      val label = "(0,0)"
      val labelWidth = fm.getStringBounds(label, g).getWidth.toInt
      val xTextOff =
        if(x + 8 + labelWidth > width) //will label be off the edge of the preview
          -(8 + labelWidth) //if so put on left
        else 8 //keep on right
      val yTextOff = -2 + (fm.getStringBounds(label, g).getHeight() / 2).toInt
      g.drawString(label, x + xTextOff, y + yTextOff)
    }

    def paintCorners(g: java.awt.Graphics2D, radius: Int, pad: Int,
                     center: java.awt.Color, border: java.awt.Color)
    {
      val r = radius max 1
      paintDot(g, PAD,       PAD,        r, TO_LEFT,  TO_TOP,    center, border)
      paintDot(g, width-PAD, PAD,        r, TO_RIGHT, TO_TOP,    center, border)
      paintDot(g, width-PAD, height-PAD, r, TO_RIGHT, TO_BOTTOM, center, border)
      paintDot(g, PAD,       height-PAD, r, TO_LEFT,  TO_BOTTOM, center, border)
    }

    private def paintDot(g: java.awt.Graphics2D, x: Int, y: Int, radius: Int,
                         txtHorizOrient: Int, txtVertOrient: Int,
                         center: java.awt.Color, border: java.awt.Color)
    {
      if(xOrigin == x && yOrigin == y) return
      val font = new java.awt.Font(monoFont, java.awt.Font.PLAIN, 10)
      g.setFont(font)
      val fm = g.getFontMetrics
      val diameter = radius * 2
      g.setColor(center)
      g.fillOval(x-radius, y-radius, diameter, diameter)
      //Draw Label
      var xTextOff: Int = 0
      var yTextOff: Int = 0
      var label: String = "(0,0)"
      if(txtHorizOrient == TO_LEFT && txtVertOrient == TO_TOP){
        label = "(" + minPxcor + "," + maxPycor + ")"
        xTextOff = 4
        yTextOff = fm.getStringBounds(label, g).getHeight.toInt
      }
      else if(txtHorizOrient == TO_RIGHT && txtVertOrient == TO_TOP) {
        label = "(" + maxPxcor + "," + maxPycor + ")"
        xTextOff = -4 - fm.getStringBounds(label, g).getWidth.toInt
        yTextOff = fm.getStringBounds(label, g).getHeight.toInt
      }
      else if(txtHorizOrient == TO_RIGHT && txtVertOrient == TO_BOTTOM) {
        label = "(" + maxPxcor + "," + minPycor + ")"
        xTextOff = -4 - fm.getStringBounds(label, g).getWidth.toInt
        yTextOff = -4
      }
      else if(txtHorizOrient == TO_LEFT && txtVertOrient == TO_BOTTOM) {
        label = "(" + minPxcor + "," + minPycor + ")"
        xTextOff = 4
        yTextOff = -4
      }
      else {
        xTextOff = 13
        yTextOff = -3 + (fm.getStringBounds(label, g).getHeight() / 2).toInt
      }
      g.drawString(label, x + xTextOff, y + yTextOff)
    }
  }

  private object WorldPreviewWrapCanvas {
    val WRAP_X = 0
    val WRAP_Y = 1
  }
  private class WorldPreviewWrapCanvas(width: Int, height: Int, wrapDim: Int)
          extends java.awt.Canvas
  {
    setSize(width, height)
    setBackground(java.awt.Color.black)
    repaint()   // I wonder if this is actually needed - ST 2/17/10
    var wrapX, wrapY = false
    def update(field: String, value: Any) {
      if(field != null)
        if(field == "wrappingX")
          wrapX = value.asInstanceOf[java.lang.Boolean].booleanValue
        else if(field == "wrappingY")
          wrapY = value.asInstanceOf[java.lang.Boolean].booleanValue
      this.repaint()
    }
    override def paint(g: java.awt.Graphics) {
      if(wrapDim == WorldPreviewWrapCanvas.WRAP_X)
        drawWrapXMonitor(g)
      if(wrapDim == WorldPreviewWrapCanvas.WRAP_Y)
        drawWrapYMonitor(g)
    }
    def drawWrapXMonitor(g: java.awt.Graphics) {
      if(wrapX) {
        g.setColor(new java.awt.Color(33, 204, 0))
        g.fillRect(width / 4, 0, width / 2, height)
        g.setColor(java.awt.Color.black)
        for(i <- 0 to 16)
          g.fillRect(width / 4, (i * height) / 16, width / 2, 2)
      }
      else {
        g.setColor(java.awt.Color.red)
        g.fillRect(width / 4, 0, width / 2, height)
      }
    }
    def drawWrapYMonitor(g: java.awt.Graphics) {
      if(wrapY) {
        g.setColor(new java.awt.Color(33, 204, 0))
        g.fillRect(0, height / 4, width, height / 2)
        g.setColor(java.awt.Color.black)
        for(i <- 0 to 16)
          g.fillRect((i * width) / 16, height / 4, 2, height / 2)
      }
      else {
        g.setColor(java.awt.Color.red)
        g.fillRect(0, height / 4, width, height / 2)
      }
    }
  }
}

