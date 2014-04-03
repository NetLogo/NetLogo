// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.front

import org.scalatest.FunSuite
import org.nlogo.api.Femto
import org.nlogo.api
import java.awt.Color

class ColorizerTests extends FunSuite {

  import Colorizer.Colors

  def simple(s: String, color: Color) {
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

}

import org.nlogo.util.SlowTest

class ColorizerTests2 extends FunSuite with SlowTest {

  // very long Code tabs shouldn't blow the stack.
  // slow, hence SlowTest
  test("don't blow stack") {
    import org.nlogo.api.FileIO.file2String
    val path = "models/test/Really Long Code.nls"
    assertResult(1010916)(
      Colorizer.toHtml(file2String(path)).size)
  }

}
