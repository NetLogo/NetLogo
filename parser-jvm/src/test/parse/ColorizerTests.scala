// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import java.awt.Color
import org.scalatest.funsuite.AnyFunSuite

class ColorizerTests extends AnyFunSuite {

  import Colorizer.Colors

  def simple(s: String, color: Color): Unit = {
    assertResult(Vector.fill(s.length)(color)) {
      Colorizer.colorizeLine(s)
    }
  }

  test("empty")      { simple(""       , Colors.Default  ) }
  test("keyword")    { simple("end"    , Colors.Keyword  ) }
  test("command")    { simple("fd"     , Colors.Command  ) }
  test("reporter")   { simple("timer"  , Colors.Reporter ) }
  test("turtle var") { simple("xcor"   , Colors.Reporter ) }
  test("number")     { simple("345"    , Colors.Literal  ) }
  test("string")     { simple("\"ha\"" , Colors.Literal  ) }
  test("breed 1")    { simple("breed"  , Colors.Keyword  ) }

  test("breed 2") {
    assertResult(Vector(Colors.Default, Colors.Reporter)) {
      Colorizer.colorizeLine(" breed").distinct
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
      Colorizer.toHtml("to foo crt 10 [ set xcor 5 ] end"))
  }

  test("adds breaks for newlines") {
    assertResult("""<font color="#007f69">to</font>""" +
      """<font color="#000000"> foo<br /></font>""" +
      """<font color="#0000aa">crt</font><font color="#000000"> </font>""" +
      """<font color="#963700">10</font><font color="#000000"> [""" +
      """<br />]<br /></font>""" +
      """<font color="#007f69">end</font>""")(
        Colorizer.toHtml("to foo\ncrt 10 [\n]\nend"))
  }

}
