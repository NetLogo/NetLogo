// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import com.vladsch.flexmark.Extension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.{ Parser, ParserEmulationProfile }
import com.vladsch.flexmark.util.options.MutableDataSet
import com.vladsch.flexmark.ext.escaped.character.EscapedCharacterExtension
import com.vladsch.flexmark.ext.autolink.AutolinkExtension
import com.vladsch.flexmark.ext.typographic.TypographicExtension

import java.util.ArrayList
import javax.swing.border.LineBorder

import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, CheckBox, PreferredSize, ScrollPane, TextArea }
import org.nlogo.swing.Implicits.thunk2documentListener
import org.nlogo.theme.InterfaceColors

import scala.util.{ Success, Try }

object MarkdownEditor {
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

  def render(markdown: String): String =
    renderer.render(parser.parse(markdown))
}

class MarkdownEditor(accessor: PropertyAccessor[String])
  extends BoxColumn(6) with PropertyEditor(accessor) with PreferredSize {

  private val toggle = new CheckBox(accessor.name, toggleEditor)

  private val editor = new TextArea(6, 30) {
    setDragEnabled(false)
    setLineWrap(true)
    setWrapStyleWord(true)

    getDocument.addDocumentListener(() => accessor.changed())
  }

  private val scrollPane = new ScrollPane(editor)

  setBorder(null)

  add(new BoxRow(toggle, BoxAlign.Start))
  add(scrollPane)

  override def get: Try[String] =
    Success(editor.getText)

  override def set(value: String): Unit = {
    editor.setText(value)

    toggleEditor(value.trim.nonEmpty)
  }

  private def toggleEditor(enabled: Boolean): Unit = {
    toggle.setSelected(enabled)
    scrollPane.setVisible(enabled)

    revalidate()
    repaint()
  }

  override def syncTheme(): Unit = {
    toggle.setForeground(InterfaceColors.dialogText())

    scrollPane.setBorder(new LineBorder(InterfaceColors.textAreaBorderEditable()))
    scrollPane.setBackground(InterfaceColors.textAreaBackground())

    editor.syncTheme()
  }
}
