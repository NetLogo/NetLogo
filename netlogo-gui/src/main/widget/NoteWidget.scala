// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.widget

import org.nlogo.api.{ Color => NlogoColor, Editable }
import org.nlogo.core.{ TextBox => CoreTextBox }
import org.nlogo.core.I18N
import org.nlogo.window.{ InterfaceColors, SingleErrorWidget }
import java.awt.{Font, Color, FontMetrics, Graphics, Dimension, Rectangle}

class NoteWidget extends SingleErrorWidget with Editable {

  type WidgetModel = CoreTextBox

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

  override def model: WidgetModel = {
    val b = getUnzoomedBounds
    val txt = if (text != null && text.trim != "") Some(text) else None
    CoreTextBox(display = txt,
      x = b.x, y = b.y, width = b.width, height = b.height,
      fontSize = fontSize,
      color = NlogoColor.argbToColor(color.getRGB),
      transparent = transparency)
  }

  override def load(model: WidgetModel): AnyRef = {
    text = model.display.getOrElse("")
    fontSize = model.fontSize
    color = NlogoColor.getColor(Double.box(model.color))
    transparency(model.transparent)
    setSize(model.width, model.height)
    this
  }
}
