// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.widget

import java.awt.{ Color, Dimension, FlowLayout, Rectangle }
import javax.swing.JLabel
import javax.swing.border.EmptyBorder

import org.nlogo.api.{ Color => NlogoColor, Editable }
import org.nlogo.awt.LineBreaker
import org.nlogo.core.{ TextBox => CoreTextBox }
import org.nlogo.core.I18N
import org.nlogo.window.{ InterfaceColors, SingleErrorWidget }

import scala.collection.JavaConverters._

class NoteWidget extends SingleErrorWidget with Editable {

  type WidgetModel = CoreTextBox

  setBackground(InterfaceColors.TRANSPARENT)

  setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0))

  val textLabel = new JLabel

  textLabel.setBorder(new EmptyBorder(0, 4, 0, 4))

  add(textLabel)

  val MIN_WIDTH = 15
  val DEFAULT_WIDTH = 150
  val MIN_HEIGHT = 18

  private var _width: Int = DEFAULT_WIDTH
  private var _text: String = ""
  private var _fontSize: Int = textLabel.getFont.getSize
  var color: Color = Color.black

  override def propertySet = Properties.text
  override def classDisplayName = I18N.gui.get("tabs.run.widgets.note")
  override def isNote = true
  override def widgetWrapperOpaque = ! transparency

  private def wrapText() {
    textLabel.setText("<html>" + LineBreaker.breakLines(_text, getFontMetrics(textLabel.getFont), _width - 8).asScala
                                            .mkString("<br>") + "</html>")
    repaint()
  }

  def text = _text
  def text_=(newText: String) {
    _text = newText
    displayName = newText
    wrapText()
  }

  def transparency = backgroundColor eq InterfaceColors.TRANSPARENT
  def transparency(trans: Boolean) {
    backgroundColor =
      if (trans)
        InterfaceColors.TRANSPARENT
      else
        InterfaceColors.TEXT_BOX_BACKGROUND
  }

  def fontSize = _fontSize
  def fontSize_=(size: Int) {
    _fontSize = size
    if (isZoomed && originalFont != null) {
      val zoomDiff: Int = getFont.getSize - originalFont.getSize
      textLabel.setFont(textLabel.getFont.deriveFont((size + zoomDiff).toFloat))
    }
    else textLabel.setFont(textLabel.getFont.deriveFont(size.toFloat))
    if (originalFont != null) originalFont = (originalFont.deriveFont(size.toFloat))
    resetZoomInfo()
    resetSizeInfo()
    wrapText()
  }

  override def setBounds(r: Rectangle) {
    if (r.width > 0) _width = r.width
    super.setBounds(r)
    wrapText()
  }
  override def setBounds(x: Int, y: Int, width: Int, height: Int) {
    if (width > 0) _width = width
    super.setBounds(x, y, width, height)
    wrapText()
  }

  override def editFinished(): Boolean = {
    textLabel.setForeground(color)
    super.editFinished()
  }

  override def getMinimumSize = new Dimension(MIN_WIDTH, MIN_HEIGHT)
  override def getPreferredSize: Dimension =
    new Dimension(MIN_WIDTH.max(_width), MIN_HEIGHT.max(textLabel.getHeight + 8))
  override def needsPreferredWidthFudgeFactor = false

  override def model: WidgetModel = {
    val b = getBoundsTuple
    val txt = if (text != null && text.trim != "") Some(text) else None
    CoreTextBox(display = txt,
      left = b._1, top = b._2, right = b._3, bottom = b._4,
      fontSize = fontSize,
      color = NlogoColor.argbToColor(color.getRGB),
      transparent = transparency)
  }

  override def load(model: WidgetModel): AnyRef = {
    text = model.display.getOrElse("")
    fontSize = model.fontSize
    color = NlogoColor.getColor(Double.box(model.color))
    textLabel.setForeground(color)
    transparency(model.transparent)
    setSize(model.right - model.left, model.bottom - model.top)
    this
  }
}
