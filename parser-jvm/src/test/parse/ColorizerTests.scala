// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import java.awt.Color

import org.nlogo.core.{ ColorizerTheme, TokenType }

import org.scalatest.funsuite.AnyFunSuite

class ColorizerTests extends AnyFunSuite {

  private val theme = ColorizerTheme.Light

  def simple(s: String, color: Color): Unit = {
    assertResult(Vector.fill(s.length)(color)) {
      Colorizer.colorizeLine(s, ColorizerTheme.Light)
    }
  }

  test("empty")      { simple(""       , theme.getColor(null)  ) }
  test("keyword")    { simple("end"    , theme.getColor(TokenType.Keyword)  ) }
  test("command")    { simple("fd"     , theme.getColor(TokenType.Command)  ) }
  test("reporter")   { simple("timer"  , theme.getColor(TokenType.Reporter) ) }
  test("turtle var") { simple("xcor"   , theme.getColor(TokenType.Reporter) ) }
  test("number")     { simple("345"    , theme.getColor(TokenType.Literal)  ) }
  test("string")     { simple("\"ha\"" , theme.getColor(TokenType.Literal)  ) }
  test("breed 1")    { simple("breed"  , theme.getColor(TokenType.Keyword)  ) }

  test("breed 2") {
    assertResult(Vector(theme.getColor(null), theme.getColor(TokenType.Reporter))) {
      Colorizer.colorizeLine(" breed", theme).distinct
    }
  }

  test("html") {
    val expected =
      """<font color="#007f69">to</font>"""    +
      """<font color="#000000"> foo </font>""" +
      """<font color="#0000aa">crt</font>"""   +
      """<font color="#000000"> </font>"""     +
      """<font color="#963700">10</font>"""    +
      """<font color="#000000"> [ </font>"""   +
      """<font color="#0000aa">set</font>"""   +
      """<font color="#000000"> </font>"""     +
      """<font color="#660096">xcor</font>"""  +
      """<font color="#000000"> </font>"""     +
      """<font color="#963700">5</font>"""     +
      """<font color="#000000"> ] </font>"""   +
      """<font color="#007f69">end</font>"""
    assertResult(expected)(
      Colorizer.toHtml("to foo crt 10 [ set xcor 5 ] end", theme))
  }

  test("adds breaks for newlines") {
    assertResult("""<font color="#007f69">to</font>""" +
      """<font color="#000000"> foo<br /></font>""" +
      """<font color="#0000aa">crt</font><font color="#000000"> </font>""" +
      """<font color="#963700">10</font><font color="#000000"> [""" +
      """<br />]<br /></font>""" +
      """<font color="#007f69">end</font>""")(
        Colorizer.toHtml("to foo\ncrt 10 [\n]\nend", theme))
  }

}
