// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.infotab

import java.io.InputStream
import java.util.{ ArrayList => JArrayList }

import com.vladsch.flexmark.Extension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.{ Parser, ParserEmulationProfile }
import com.vladsch.flexmark.util.options.MutableDataSet
import com.vladsch.flexmark.ext.escaped.character.EscapedCharacterExtension
import com.vladsch.flexmark.ext.autolink.AutolinkExtension
import com.vladsch.flexmark.ext.typographic.TypographicExtension

import scala.collection.JavaConverters._

import org.nlogo.api.FileIO
import org.nlogo.app.common.CodeToHtml

// This gets tested by TestInfoFormatter. - ST 9/7/10

object InfoFormatter {
  type Markdown = String
  type HTML = String
  type CSS = String

  val MaxParsingTimeMillis = 4000 // set high for Travis, won't take that long on most computers

  /**
   * for standalone use, for example on a web server
   */
  def main(argv: Array[String]) {
    println(apply(read(System.in)))
  }

  def read(in: InputStream): String = io.Source.fromInputStream(in).mkString

  def styleSheetFile: CSS = FileIO.getResourceAsString("/system/info.css")
  val defaultFontSize = 14
  val defaultStyleSheet: CSS = styleSheet(defaultFontSize)
  def styleSheet(fontSize: Int): CSS = "<style type=\"text/css\">\n<!--\n"+
          styleSheetFile.
            replace("{BODY-FONT-SIZE}", fontSize.toString).
            replace("{H1-FONT-SIZE}", (fontSize * 1.5).toInt.toString).
            replace("{H2-FONT-SIZE}", (fontSize * 1.25).toInt.toString).
            replace("{H3-FONT-SIZE}", fontSize.toString).
            replace("{BULLET-IMAGE}", getClass.getResource("/system/bullet.png").toString) + "\n-->\n</style>"

  def apply(content: String, fontSize: Int = defaultFontSize) = {
    wrapHtml(toInnerHtml(content), fontSize)
  }

  def wrapHtml(body: HTML, fontSize: Int = defaultFontSize): HTML =
    "<html><head>"+styleSheet(fontSize)+"</head><body>"+body+"</body></html>"

  def toInnerHtml(content: String): String = {
    val extensions = new JArrayList[Extension]()
    val options = new MutableDataSet()

    options.setFrom(ParserEmulationProfile.PEGDOWN)

    extensions.add(EscapedCharacterExtension.create())

    options.set(HtmlRenderer.SOFT_BREAK, "<br />\n")
    options.set(HtmlRenderer.HARD_BREAK, "<br />\n")

    extensions.add(TypographicExtension.create())
    options.set(TypographicExtension.ENABLE_QUOTES, Boolean.box(true))
    options.set(TypographicExtension.ENABLE_SMARTS, Boolean.box(true))

    extensions.add(AutolinkExtension.create())

    options.set(Parser.MATCH_CLOSING_FENCE_CHARACTERS, Boolean.box(false))

    extensions.add(new CodeBlockRenderer())

    options.set(Parser.EXTENSIONS, extensions)

    val opts = options.toImmutable

    val parser = Parser.builder(opts).build()
    val renderer = HtmlRenderer.builder(opts).build()
    val document = parser.parse(content)
    renderer.render(document)
  }
}
