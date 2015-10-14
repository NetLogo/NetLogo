// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.widget

import org.nlogo.api.{Editable, I18N, ModelReader}
import org.nlogo.window.{InterfaceColors, SingleErrorWidget,Widget}
import java.awt.{Font, Color, FontMetrics, Graphics, Dimension, Rectangle}

class NoteWidget extends SingleErrorWidget with Editable {

  setBackground(InterfaceColors.TRANSPARENT)
  setOpaque(false)
  org.nlogo.awt.Fonts.adjustDefaultFont(this)

  val MIN_WIDTH = 15
  val DEFAULT_WIDTH = 150
  val MIN_HEIGHT = 18

  private var _width: Int = DEFAULT_WIDTH
  private var _text: String = ""
  private var _fontSize: Int = getFont.getSize
  var color: Color = java.awt.Color.black

  override def propertySet = Properties.text
  override def classDisplayName = I18N.gui.get("tabs.run.widgets.note")
  override def isNote = true
  override def widgetWrapperOpaque = ! transparency

  def text = _text
  def text_=(newText: String) {
    this._text = newText
    this.displayName = newText
    repaint()
  }

  def transparency = getBackground eq InterfaceColors.TRANSPARENT
  def transparency(trans: Boolean) {
    setBackground(if (trans) InterfaceColors.TRANSPARENT else InterfaceColors.TEXT_BOX_BACKGROUND)
    setOpaque(!trans)
  }

  def fontSize = _fontSize
  def fontSize_=(size: Int) {
    this._fontSize = size
    if (isZoomed && originalFont != null) {
      val zoomDiff: Int = getFont.getSize - originalFont.getSize
      setFont(getFont.deriveFont((size + zoomDiff).toFloat))
    }
    else setFont(getFont.deriveFont(size.toFloat))
    if (originalFont != null) originalFont = (originalFont.deriveFont(size.toFloat))
    resetZoomInfo()
    resetSizeInfo()
  }

  override def setBounds(r: Rectangle) {
    if (r.width > 0) _width = r.width
    super.setBounds(r)
  }
  override def setBounds(x: Int, y: Int, width: Int, height: Int) {
    if (width > 0) this._width = width
    super.setBounds(x, y, width, height)
  }

  override def getMinimumSize = new Dimension(MIN_WIDTH, MIN_HEIGHT)
  override def getPreferredSize(font: Font): Dimension = {
    val metrics = getFontMetrics(font)
    val height: Int = org.nlogo.awt.LineBreaker.breakLines(_text, metrics, _width).size * (metrics.getMaxDescent + metrics.getMaxAscent)
    new Dimension(StrictMath.max(MIN_WIDTH, _width), StrictMath.max(MIN_HEIGHT, height))
  }
  override def needsPreferredWidthFudgeFactor = false

  override def paintComponent(g: Graphics) {
    super.paintComponent(g)
    g.setFont(getFont)
    val metrics: FontMetrics = g.getFontMetrics
    val stringHeight: Int = metrics.getMaxDescent + metrics.getMaxAscent
    val stringAscent: Int = metrics.getMaxAscent
    val lines = org.nlogo.awt.LineBreaker.breakLines(_text, metrics, _width)
    g.setColor(color)
    import collection.JavaConverters._
    for((line, i) <- lines.asScala.zipWithIndex)
      g.drawString(line, 0, i * stringHeight + stringAscent)
  }

  def save: String = {
    val s = new StringBuilder
    s.append("TEXTBOX\n")
    s.append(getBoundsString)
    if (_text.trim == "") s.append("NIL\n")
    else  s.append(ModelReader.stripLines(_text) + "\n")
    s.append(fontSize + "\n")
    s.append(org.nlogo.api.Color.getClosestColorNumberByARGB(color.getRGB) + "\n")
    s.append((if (transparency) "1" else "0") + "\n")
    s.toString
  }

  def load(strings: Array[String], helper: Widget.LoadHelper) = {
    text = if (strings(5) == "NIL") "" else ModelReader.restoreLines(strings(5))
    if (strings.length >= 7) fontSize = strings(6).toInt
    if (strings.length >= 8) color = org.nlogo.api.Color.getColor(strings(7).toDouble: java.lang.Double)
    if (strings.length >= 9) transparency(strings(8).toInt != 0)
    else transparency(false)
    val Array(x1,y1,x2,y2) = strings.drop(1).take(4).map(_.toInt)
    setSize(x2 - x1, y2 - y1)
    this
  }
}
