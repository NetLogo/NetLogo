package org.nlogo.app

import org.pegdown.{ PegDownProcessor, Extensions }
import java.io.InputStream

// This gets tested by TestAppletSaver and TestInfoFormatter. - ST 9/7/10

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
    // SMARTYPANTS beautifies quotes, dashes, etc.
    // AUTOLINKS lets you omit the angle brackets around URLs and email addresses
    // HARDWRAPS enables GitHub flavored newlines (github.github.com/github-flavored-markdown/)
    PostProcessor(
      new PegDownProcessor(Extensions.SMARTYPANTS | Extensions.AUTOLINKS | Extensions.HARDWRAPS)
        .markdownToHtml(str)
    )
  def wrapHtml(body: HTML, fontSize:Int=defaultFontSize): HTML = {
    "<html><head>"+styleSheet(fontSize)+"</head><body>"+body+"</body></html>"
  }

  def apply(content:String, fontSize:Int=defaultFontSize, attachModelDir:String=>String=identity) = {
    wrapHtml(toInnerHtml(content), fontSize)
  }

  object PostProcessor {
    lazy val convert: String => String =
      List(fixLessThans).reduceLeft(_ andThen _)

    def apply(s:String) = convert(s)

    private val fixLessThans = replace("""<[^A-Za-z/]""", "&lt;$0")
    private def replace(s1: String, s2: String) = (_: String).replaceAll(s1, s2)
  }
}
