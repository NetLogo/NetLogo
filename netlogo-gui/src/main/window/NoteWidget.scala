// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import com.vladsch.flexmark.Extension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.{ Parser, ParserEmulationProfile }
import com.vladsch.flexmark.util.options.MutableDataSet
import com.vladsch.flexmark.ext.escaped.character.EscapedCharacterExtension
import com.vladsch.flexmark.ext.autolink.AutolinkExtension
import com.vladsch.flexmark.ext.typographic.TypographicExtension

import java.awt.{ Color, Dimension, GridBagConstraints, GridBagLayout, Insets, Rectangle }
import java.util.ArrayList
import javax.swing.JLabel

import org.apache.commons.text.StringEscapeUtils

import org.nlogo.core.{ I18N, TextBox => CoreTextBox }
import org.nlogo.swing.Transparent
import org.nlogo.theme.{ ClassicTheme, DarkTheme, InterfaceColors, LightTheme }

class NoteWidget extends SingleErrorWidget with Transparent with Editable {

  type WidgetModel = CoreTextBox

  val textLabel = new JLabel

  locally {
    setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.anchor = GridBagConstraints.NORTHWEST
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.weighty = 1
    c.insets = new Insets(0, 4, 0, 4)

    add(textLabel, c)
  }

  val MIN_WIDTH = 15
  val DEFAULT_WIDTH = 150
  val MIN_HEIGHT = 18

  private var _width: Int = DEFAULT_WIDTH
  private var _text: String = ""
  private var _fontSize: Int = textLabel.getFont.getSize
  private var _textColorLight = Color.BLACK
  private var _textColorDark = Color.WHITE
  private var _backgroundLight = InterfaceColors.Transparent
  private var _backgroundDark = InterfaceColors.Transparent
  private var _markdown = false

  private val (renderer: HtmlRenderer, parser: Parser) = {
    val extensions = new ArrayList[Extension]

    extensions.add(EscapedCharacterExtension.create())
    extensions.add(TypographicExtension.create())
    extensions.add(AutolinkExtension.create())

    val options = new MutableDataSet

    options.setFrom(ParserEmulationProfile.PEGDOWN)
    options.set(HtmlRenderer.SOFT_BREAK, "<br />\n")
    options.set(HtmlRenderer.HARD_BREAK, "<br />\n")
    options.set(TypographicExtension.ENABLE_QUOTES, Boolean.box(true))
    options.set(TypographicExtension.ENABLE_SMARTS, Boolean.box(true))
    options.set(Parser.MATCH_CLOSING_FENCE_CHARACTERS, Boolean.box(false))
    options.set(Parser.EXTENSIONS, extensions)

    val opts = options.toImmutable

    (HtmlRenderer.builder(opts).build(), Parser.builder(opts).build())
  }

  override def classDisplayName: String = I18N.gui.get("tabs.run.widgets.note")

  override def editPanel: EditPanel =
    null

  override def isNote = true

  private val css = """<head>
                      |  <style type="text/css">
                      |    ul, ol {
                      |      margin-left: 8px;
                      |    }
                      |  </style>
                      |</head>""".stripMargin

  private def wrapText(): Unit = {
    if (_markdown) {
      textLabel.setText(s"""<html>$css${renderer.render(parser.parse(_text))}</html>""")
    } else {
      textLabel.setText(s"""<html>${StringEscapeUtils.escapeHtml4(_text).replaceAll("\n", "<br>")}</html>""")
    }

    repaint()
  }

  def text: String = _text
  def text_=(newText: String): Unit = {
    _text = newText
    displayName = newText
    wrapText()
  }

  def fontSize: Int = _fontSize
  def fontSize_=(size: Int): Unit = {
    _fontSize = size
    if (isZoomed && originalFont != null) {
      val zoomDiff: Int = getFont.getSize - originalFont.getSize
      textLabel.setFont(textLabel.getFont.deriveFont((size + zoomDiff).toFloat))
    } else {
      textLabel.setFont(textLabel.getFont.deriveFont(size.toFloat))
    }
    if (originalFont != null) originalFont = (originalFont.deriveFont(size.toFloat))
    resetZoomInfo()
    resetSizeInfo()
    wrapText()
  }

  def textColorLight: Color = _textColorLight
  def textColorLight_=(color: Color): Unit = {
    _textColorLight = color
    syncTheme()
  }

  def textColorDark: Color = _textColorDark
  def textColorDark_=(color: Color): Unit = {
    _textColorDark = color
    syncTheme()
  }

  def backgroundLight: Color = _backgroundLight
  def backgroundLight_=(color: Color): Unit = {
    _backgroundLight = color
    syncTheme()
  }

  def backgroundDark: Color = _backgroundDark
  def backgroundDark_=(color: Color): Unit = {
    _backgroundDark = color
    syncTheme()
  }

  def markdown: Boolean = _markdown
  def markdown_=(value: Boolean): Unit = {
    _markdown = value
    wrapText()
  }

  override def setBounds(r: Rectangle): Unit = {
    if (r.width > 0) _width = r.width
    super.setBounds(r)
    wrapText()
  }
  override def setBounds(x: Int, y: Int, width: Int, height: Int): Unit = {
    if (width > 0) _width = width
    super.setBounds(x, y, width, height)
    wrapText()
  }

  override def editFinished(): Boolean = {
    syncTheme()
    super.editFinished()
  }

  override def getMinimumSize = new Dimension(MIN_WIDTH, MIN_HEIGHT)
  override def getPreferredSize: Dimension =
    new Dimension(MIN_WIDTH.max(_width), MIN_HEIGHT.max(textLabel.getPreferredSize.height + 8))

  override def syncTheme(): Unit = {
    InterfaceColors.getTheme match {
      case ClassicTheme | LightTheme =>
        setBackgroundColor(_backgroundLight)
        textLabel.setForeground(_textColorLight)

      case DarkTheme =>
        setBackgroundColor(_backgroundDark)
        textLabel.setForeground(_textColorDark)
    }

    repaint()
  }

  override def model: WidgetModel = {
    val b = getUnzoomedBounds
    val txt = if (text != null && text.trim != "") Some(text) else None
    CoreTextBox(display = txt,
      x = b.x, y = b.y, width = b.width, height = b.height,
      fontSize = fontSize, markdown = markdown,
      textColorLight = Some(textColorLight.getRGB), textColorDark = Some(textColorDark.getRGB),
      backgroundLight = Some(backgroundLight.getRGB), backgroundDark = Some(backgroundDark.getRGB))
  }

  override def load(model: WidgetModel): AnyRef = {
    text = model.display.getOrElse("")
    fontSize = model.fontSize
    markdown = model.markdown
    model.textColorLight.foreach { c => textColorLight = new Color(c, true) }
    model.textColorDark.foreach { c => textColorDark = new Color(c, true) }
    model.backgroundLight.foreach { c => backgroundLight = new Color(c, true) }
    model.backgroundDark.foreach { c => backgroundDark = new Color(c, true) }
    syncTheme()
    setSize(model.width, model.height)
    this
  }
}
