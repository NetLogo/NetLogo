// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.pegdown.{ PegDownProcessor, Extensions }
import java.io.InputStream

// This gets tested by TestInfoFormatter. - ST 9/7/10

object InfoFormatter {

  type MarkDownString = String
  type HTML = String
  type CSS = String

  /**
   * for standalone use, for example on a web server
   */
  def main(argv: Array[String]) {
    println(apply(read(System.in)))
  }
  def read(in:InputStream): String = io.Source.fromInputStream(in).mkString

  def styleSheetFile: CSS = org.nlogo.util.Utils.getResourceAsString("/system/info.css")
  val defaultFontSize = 14
  val defaultStyleSheet: CSS = styleSheet(defaultFontSize)
  def styleSheet(fontSize: Int): CSS = "<style type=\"text/css\">\n<!--\n"+
          styleSheetFile.
            replace("{BODY-FONT-SIZE}", fontSize.toString).
            replace("{H1-FONT-SIZE}", (fontSize * 1.5).toInt.toString).
            replace("{H2-FONT-SIZE}", (fontSize * 1.25).toInt.toString).
            replace("{H3-FONT-SIZE}", fontSize.toString) + "\n-->\n</style>"

  def toInnerHtml(str: MarkDownString): HTML =
    new PegDownProcessor(Extensions.SMARTYPANTS |       // beautifies quotes, dashes, etc.
                         Extensions.AUTOLINKS |         // angle brackets around URLs and email addresses not needed
                         Extensions.HARDWRAPS |         // GitHub flavored newlines
                         Extensions.FENCED_CODE_BLOCKS) // delimit code blocks with ```
      .markdownToHtml(str)

  def wrapHtml(body: HTML, fontSize:Int=defaultFontSize): HTML = {
    "<html><head>"+styleSheet(fontSize)+"</head><body>"+body+"</body></html>"
  }

  def apply(content:String, fontSize:Int=defaultFontSize, attachModelDir:String=>String=identity) = {
    wrapHtml(toInnerHtml(content), fontSize)
  }

}
