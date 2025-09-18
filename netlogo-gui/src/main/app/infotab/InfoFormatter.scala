// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.infotab

import java.awt.Color
import java.io.InputStream
import java.util.{ ArrayList => JArrayList }

import com.vladsch.flexmark.Extension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.{ Parser, ParserEmulationProfile }
import com.vladsch.flexmark.util.options.MutableDataSet
import com.vladsch.flexmark.ext.escaped.character.EscapedCharacterExtension
import com.vladsch.flexmark.ext.autolink.AutolinkExtension
import com.vladsch.flexmark.ext.typographic.TypographicExtension

import org.nlogo.api.{ ExternalResourceManager, FileIO }
import org.nlogo.theme.InterfaceColors

import scala.io.Source

// This gets tested by TestInfoFormatter. - ST 9/7/10

object InfoFormatter {
  val MaxParsingTimeMillis = 4000 // set high for Travis, won't take that long on most computers

  /**
   * for standalone use, for example on a web server
   */
  def main(argv: Array[String]): Unit = {
    println(apply(read(System.in)))
  }

  def read(in: InputStream): String = Source.fromInputStream(in).mkString

  def styleSheetFile: String = FileIO.getResourceAsString("/system/info.css")
  val defaultFontSize = 11
  val defaultStyleSheet: String = styleSheet(defaultFontSize)
  def styleSheet(fontSize: Int): String = "<style type=\"text/css\">\n<!--\n"+
          styleSheetFile.
            replace("{BODY-BACKGROUND}", colorString(InterfaceColors.infoBackground())).
            replace("{BODY-FONT-SIZE}", fontSize.toString).
            replace("{H1-BACKGROUND}", colorString(InterfaceColors.infoH1Background())).
            replace("{H1-COLOR}", colorString(InterfaceColors.infoH1Color())).
            replace("{H1-FONT-SIZE}", (fontSize * 2).toInt.toString).
            replace("{H2-BACKGROUND}", colorString(InterfaceColors.infoH2Background())).
            replace("{H2-COLOR}", colorString(InterfaceColors.infoH2Color())).
            replace("{H2-FONT-SIZE}", (fontSize * 1.75).toInt.toString).
            replace("{H3-COLOR}", colorString(InterfaceColors.infoH3Color())).
            replace("{H3-FONT-SIZE}", (fontSize * 1.5).toInt.toString).
            replace("{H4-COLOR}", colorString(InterfaceColors.infoH4Color())).
            replace("{H4-FONT-SIZE}", (fontSize * 1.28).toString).
            replace("{H5-FONT-SIZE}", (fontSize * 1.14).toString).
            replace("{H6-FONT-SIZE}", fontSize.toString).
            replace("{P-COLOR}", colorString(InterfaceColors.infoPColor())).
            replace("{CODE-BACKGROUND}", colorString(InterfaceColors.infoCodeBackground())).
            replace("{BLOCK-BAR}", colorString(InterfaceColors.infoBlockBar())).
            replace("{INFO-BACKGROUND}", colorString(InterfaceColors.infoBackground())).
            replace("{LINK-COLOR}", colorString(InterfaceColors.infoLink())) + "\n-->\n</style>"

  def apply(content: String, modelDir: String = null,
            resourceManager: ExternalResourceManager = new ExternalResourceManager,
            fontSize: Int = defaultFontSize) = {

    wrapHtml(toInnerHtml(content, modelDir, resourceManager), fontSize)
  }

  def wrapHtml(body: String, fontSize: Int = defaultFontSize): String = {
    val mathJax = "https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js"

    s"<html><head>${styleSheet(fontSize)}</head><body>" + body + s"""</body><script>
      window.MathJax = {
        tex: {
          inlineMath: [['$$', '$$']],
          packages: {'[+]': ['mhchem']}
        },
        loader: {load: ['[tex]/mhchem']}
      };
    </script><script type="text/javascript" src="$mathJax"></script></html>"""
  }

  def toInnerHtml(content: String, modelDir: String = null,
                  resourceManager: ExternalResourceManager = new ExternalResourceManager): String = {

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
    extensions.add(new BlockQuoteRenderer())
    extensions.add(new ResourceRenderer(modelDir, resourceManager))

    options.set(Parser.EXTENSIONS, extensions)

    val opts = options.toImmutable

    val parser = Parser.builder(opts).build()
    val renderer = HtmlRenderer.builder(opts).build()
    val document = parser.parse(content)
    renderer.render(document)
  }

  private def colorString(color: Color): String =
    s"rgb(${color.getRed},${color.getGreen},${color.getBlue})"
}
