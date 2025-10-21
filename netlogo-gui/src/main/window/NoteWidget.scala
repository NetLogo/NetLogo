// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import com.vladsch.flexmark.Extension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.{ Parser, ParserEmulationProfile }
import com.vladsch.flexmark.util.options.MutableDataSet
import com.vladsch.flexmark.ext.escaped.character.EscapedCharacterExtension
import com.vladsch.flexmark.ext.autolink.AutolinkExtension
import com.vladsch.flexmark.ext.typographic.TypographicExtension

import java.awt.{ Color, Dimension, Rectangle }
import java.util.ArrayList
import javax.swing.{ Box, BoxLayout, JEditorPane }
import javax.swing.border.EmptyBorder
import javax.swing.text.DefaultCaret

import org.nlogo.core.{ I18N, TextBox => CoreTextBox, Widget => CoreWidget }
import org.nlogo.swing.Transparent
import org.nlogo.theme.{ ClassicTheme, DarkTheme, InterfaceColors, LightTheme }

class NoteWidget extends SingleErrorWidget with Transparent with Editable {
  private val textPane = new JEditorPane("text/html", "") {
    setEditable(false)
    setOpaque(false)

    setCaret(new SilentCaret)
  }

  locally {
    setBorder(new EmptyBorder(0, 3, 0, 4))
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

    add(textPane)
    add(Box.createVerticalGlue)
  }

  val MIN_WIDTH = 15
  val DEFAULT_WIDTH = 150
  val MIN_HEIGHT = 18

  private var _width: Int = DEFAULT_WIDTH
  private var _text: String = ""
  private var _fontSize: Int = textPane.getFont.getSize
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

  override def editPanel: EditPanel = new NoteEditPanel(this)

  override def getEditable: Option[Editable] = Some(this)

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
      val text = renderer.render(parser.parse(_text))

      // for some reason setting the content width in CSS to the width of the JLabel always results in the rendered
      // content being 1.3 times larger. dividing the JLabel width by 1.3 solves the problem. (Isaac B 6/15/25)
      textPane.setText(s"""<html>$css<body style="width: ${textPane.getWidth / 1.3}px">$text</body></html>""")
    } else {
      textPane.setText(_text.replace("\n", "<br>"))
    }

    repaint()
  }

  def text: String = _text
  def setText(newText: String): Unit = {
    _text = newText
    displayName = newText
    wrapText()
  }

  def fontSize: Int = _fontSize
  def setFontSize(size: Int): Unit = {
    _fontSize = size
    if (isZoomed && originalFont != null) {
      val zoomDiff: Int = getFont.getSize - originalFont.getSize
      textPane.setFont(textPane.getFont.deriveFont((size + zoomDiff).toFloat))
    } else {
      textPane.setFont(textPane.getFont.deriveFont(size.toFloat))
    }
    if (originalFont != null) originalFont = (originalFont.deriveFont(size.toFloat))
    resetZoomInfo()
    resetSizeInfo()
    wrapText()
  }

  def textColorLight: Color = _textColorLight
  def setTextColorLight(color: Color): Unit = {
    _textColorLight = color
    syncTheme()
  }

  def textColorDark: Color = _textColorDark
  def setTextColorDark(color: Color): Unit = {
    _textColorDark = color
    syncTheme()
  }

  def backgroundLight: Color = _backgroundLight
  def setBackgroundLight(color: Color): Unit = {
    _backgroundLight = color
    syncTheme()
  }

  def backgroundDark: Color = _backgroundDark
  def setBackgroundDark(color: Color): Unit = {
    _backgroundDark = color
    syncTheme()
  }

  def markdown: Boolean = _markdown
  def setMarkdown(value: Boolean): Unit = {
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
    new Dimension(MIN_WIDTH.max(_width), MIN_HEIGHT.max(textPane.getPreferredSize.height + 8))

  override def syncTheme(): Unit = {
    InterfaceColors.getTheme match {
      case ClassicTheme | LightTheme =>
        setBackgroundColor(_backgroundLight)
        textPane.setForeground(_textColorLight)

      case DarkTheme =>
        setBackgroundColor(_backgroundDark)
        textPane.setForeground(_textColorDark)

      case _ => throw new IllegalStateException
    }

    repaint()
  }

  override def model: CoreWidget = {
    val b = getUnzoomedBounds
    val txt = if (text != null && text.trim != "") Some(text) else None
    CoreTextBox(display = txt,
      x = b.x, y = b.y, width = b.width, height = b.height,
      fontSize = fontSize, markdown = markdown,
      textColorLight = Some(textColorLight.getRGB), textColorDark = Some(textColorDark.getRGB),
      backgroundLight = Some(backgroundLight.getRGB), backgroundDark = Some(backgroundDark.getRGB))
  }

  override def load(model: CoreWidget): Unit = {
    model match {
      case note: CoreTextBox =>
        setText(note.display.getOrElse(""))
        setFontSize(note.fontSize)
        setMarkdown(note.markdown)
        note.textColorLight.foreach { c => setTextColorLight(new Color(c, true)) }
        note.textColorDark.foreach { c => setTextColorDark(new Color(c, true)) }
        note.backgroundLight.foreach { c => setBackgroundLight(new Color(c, true)) }
        note.backgroundDark.foreach { c => setBackgroundDark(new Color(c, true)) }
        syncTheme()
        setSize(note.width, note.height)

      case _ =>
    }
  }

  // prevents the interface from automatically scrolling to a note widget
  // when setText is called (Isaac B 10/21/25)
  private class SilentCaret extends DefaultCaret {
    override def adjustVisibility(nloc: Rectangle): Unit = {}
  }
}
