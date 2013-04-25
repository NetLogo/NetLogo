// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.awt.Graphics2D

// implements GraphicsInterface, wrapper around Graphics2D

class Graphics2DWrapper(g: Graphics2D, renderLabelsAsRectangles: Boolean = false) extends GraphicsInterface {

  // since we're called from Java, provide an extra constructor since Java callers
  // don't get the benefit of default arguments - ST 6/23/12
  def this(g: Graphics2D) = this(g, false)

  val isMac =
    System.getProperty("os.name").startsWith("Mac")
  // re: the try/catch, see issue #199 - ST 9/5/12
  val isQuartz =
    try isMac && java.lang.Boolean.getBoolean("apple.awt.graphics.UseQuartz")
    catch { case _: java.security.AccessControlException => false }

  def location(x: Double, y: Double) =
    "(" + (g.getTransform.getTranslateX + x) + " , " + (g.getTransform.getTranslateY + y) + ")"
  def draw(shape: java.awt.Shape) { g.draw(shape) }
  def drawImage(image: java.awt.image.BufferedImage) { g.drawImage(image, null, 0, 0) }
  def drawImage(image: java.awt.Image, x: Int, y: Int, width: Int, height: Int) {
    g.drawImage(image, x, y, width, height, null)
  }
  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
    g.draw(new java.awt.geom.Line2D.Double(x1, y1, x2, y2))
  }
  def drawLabel(label: String, x: Double, y: Double, patchSize: Double) {
    if (renderLabelsAsRectangles) {
      // fonts aren't the same cross-platform so for graphics checksumming
      // purposes we just draw a little rectangle (with the right position
      // and color, at least) - ST 6/23/12
      push()
      translate(x, y)
      fillRect(0, 0, 5, 5)
      pop()
    }
    else {
      val fm = g.getFontMetrics
      g.translate(x - fm.stringWidth(label), 0)
      if (patchSize >= (fm.getMaxAscent + fm.getMaxDescent))
        g.translate(0, y - fm.getMaxDescent)
      else { // maxAscent is centered on the patch
        val centerAdjustment = 0.0 min ((patchSize / 4) - (fm.getMaxAscent / 4))
        g.translate(0, y - centerAdjustment)
      }
      g.drawString(label, 0, 0)
    }
  }
  def fillCircle(x: Double, y: Double, xDiameter: Double, yDiameter: Double, scale: Double, angle: Double) = {
    var sizeCorrection = 0.0
    var xCorrection = 0.0
    var yCorrection = 0.0
    if (isQuartz) {
      // one pixel bigger
      sizeCorrection = Shape.Width / scale
      // adjust position to still be centered
      xCorrection = -0.5 * sizeCorrection
      yCorrection = xCorrection
    }
    g.fill(new java.awt.geom.Ellipse2D.Double(
      x + xCorrection, y + yCorrection,
      xDiameter + sizeCorrection, yDiameter + sizeCorrection))
  }
  def drawCircle(x: Double, y: Double, xDiameter: Double, yDiameter: Double, scale: Double, angle: Double) = {
    var sizeCorrection = 0.0
    var xCorrection = 0.0
    var yCorrection = 0.0
    if (!isQuartz) {
      // one pixel smaller
      sizeCorrection = -Shape.Width / scale
      xCorrection = getXCorrection(sizeCorrection, angle)
      yCorrection = getYCorrection(sizeCorrection, angle)
    }
    g.draw(new java.awt.geom.Ellipse2D.Double(
      x + xCorrection, y + yCorrection,
      xDiameter + sizeCorrection, yDiameter + sizeCorrection))
  }
  def fillRect(x: Double, y: Double, width: Double, height: Double, scale: Double, angle: Double) = {
    var sizeCorrection = 0.0
    var xCorrection = 0.0
    var yCorrection = 0.0
    if (isQuartz) {
      // size: one pixel bigger
      sizeCorrection = Shape.Width / scale
      xCorrection = getXCorrection(sizeCorrection, angle)
      yCorrection = getYCorrection(sizeCorrection, angle)
    }
    g.fill(new java.awt.geom.Rectangle2D.Double(
      x + xCorrection, y + yCorrection,
      width + sizeCorrection, height + sizeCorrection))
  }
  def drawRect(x: Double, y: Double, width: Double, height: Double, scale: Double, angle: Double) = {
    var sizeCorrection = 0.0
    if (!isQuartz)
      // size: one pixel smaller
      sizeCorrection = -Shape.Width / scale
    g.draw(new java.awt.geom.Rectangle2D.Double(
      x, y, width + sizeCorrection, height + sizeCorrection))
  }

  // as for the corrections to the position, well, I wrote it, but that doesn't mean I understand
  // it.  You wouldn't believe the amount of guesswork and trial and error that went into
  // this. Basically I figured out what the answers should be for the cases where angle is a
  // multiple of 90, and then I tried to come up with formulas based on sin and cos that would give
  // the answers I expected for those four cases, and then hoped they'd work for intermediate angles
  // too.  Which they seem to. - ST 8/16/05, 8/25/05
  private def getXCorrection(sizeCorrection: Double, angle: Double) =
    if (angle == 0) 0 else
      (sizeCorrection * (StrictMath.cos((angle + 135) / 180.0 * StrictMath.PI) + 0.7071067811865476)
       / -1.4142135623730951)
  private def getYCorrection(sizeCorrection: Double, angle: Double) =
    if (angle == 0) 0 else
      (sizeCorrection * (StrictMath.sin((angle - 45) / 180.0 * StrictMath.PI) + 0.7071067811865476)
       / -1.4142135623730951)

  def fill(shape: java.awt.Shape) { g.fill(shape) }
  def fillRect(x: Int, y: Int, width: Int, height: Int) { g.fillRect(x, y, width, height) }

  private var transforms: List[java.awt.geom.AffineTransform] = Nil
  private var strokes: List[java.awt.Stroke] = Nil
  def pop() {
    g.setTransform(transforms.head)
    transforms = transforms.tail
    g.setStroke(strokes.head)
    strokes = strokes.tail
  }
  def push() {
    transforms ::= g.getTransform
    strokes ::= g.getStroke
  }

  def rotate(theta: Double) { g.rotate(theta) }
  def rotate(theta: Double, x: Double, y: Double) { g.rotate(theta, x, y) }
  def rotate(theta: Double, x: Double, y: Double, offset: Double) {
    val offset2 = if (isQuartz) offset - 1 else offset
    g.rotate(theta, x + offset2 / 2, y + offset2 / 2)
  }
  def scale(x: Double, y: Double) { g.scale(x, y) }
  def scale(x: Double, y: Double, shapeWidth: Double) {
    val (xx, yy) = if (isQuartz) (x - 1, y - 1) else (x, y)
    g.scale(xx / shapeWidth, yy / shapeWidth)
  }
  def antiAliasing(on: Boolean) {
    g.setRenderingHint(
      java.awt.RenderingHints.KEY_ANTIALIASING,
      if (on) java.awt.RenderingHints.VALUE_ANTIALIAS_ON
      else java.awt.RenderingHints.VALUE_ANTIALIAS_OFF)
  }
  def setInterpolation() {
    // on Macs we need this or we get blurry scaling, but on Windows we can't do it or it kills
    // performance - ST 11/2/03
    if (isMac)
      g.setRenderingHint(
        java.awt.RenderingHints.KEY_INTERPOLATION,
        java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
  }
  def setStrokeControl() {
    g.setRenderingHint(
      java.awt.RenderingHints.KEY_STROKE_CONTROL,
      java.awt.RenderingHints.VALUE_STROKE_PURE)
  }
  def setColor(c: java.awt.Color) { g.setColor(c) }
  def setComposite(comp: java.awt.Composite) { g.setComposite(comp) }
  def setStroke(width: Double) { g.setStroke(new java.awt.BasicStroke((width max 1.0).toFloat)) }
  def setStrokeFromLineThickness(lineThickness: Double, scale: Double, cellSize: Double, shapeWidth: Double) {
    val sscale = if (isQuartz) scale - 1 else scale
    setStroke((shapeWidth / sscale) * (if (lineThickness == 0) 1 else (lineThickness * cellSize)))
  }
  def setStroke(width: Float, dashes: Array[Float]) {
    g.setStroke(
      new java.awt.BasicStroke(width, java.awt.BasicStroke.CAP_ROUND,
                               java.awt.BasicStroke.JOIN_ROUND, 1.0f, dashes, 0))
  }
  def setPenWidth(penSize: Double) {
    val width = (penSize max 1.0).toFloat
    if (g.getStroke.asInstanceOf[java.awt.BasicStroke].getLineWidth != width)
      g.setStroke(
        new java.awt.BasicStroke(width, java.awt.BasicStroke.CAP_ROUND,
                                 java.awt.BasicStroke.JOIN_MITER))
  }
  def translate(x: Double, y: Double) { g.translate(x, y) }
  def drawPolygon(xcors: Array[Int], ycors: Array[Int], length: Int) { g.drawPolygon(xcors, ycors, length) }
  def fillPolygon(xcors: Array[Int], ycors: Array[Int], length: Int) { g.fillPolygon(xcors, ycors, length) }
  def drawPolyline(xcors: Array[Int], ycors: Array[Int], length: Int) { g.drawPolyline(xcors, ycors, length) }
  def dispose() { g.dispose() }
  override def getFontMetrics = g.getFontMetrics
}
