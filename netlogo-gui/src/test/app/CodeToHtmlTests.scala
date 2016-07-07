// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.scalatest.FunSuite
import org.nlogo.api.{ FileIO, Version }
import org.nlogo.util.SlowTest

class CodeToHtmlTests extends FunSuite with SlowTest {
  val converter = CodeToHtml.newInstance
  import converter.convert
  test("basic", SlowTest.Tag) {
    assertResult("""|<pre><font color="#007f69">to</font><font color="#000000"> foo
           |  </font><font color="#0000aa">crt</font><font color="#000000"> </font><font color="#963700">10</font><font color="#000000">
           |</font><font color="#007f69">end</font>
           |</pre>
           |""".stripMargin.replaceAll("\r\n", "\n"))(
      convert("to foo\n  crt 10\nend"))
  }
  // can be very slow, so restrict to 2D to keep overall nightly.sh runtime down - ST 6/24/11
  if(!Version.is3D)
    // very long Code tabs shouldn't blow the stack.
    test("don't blow stack", SlowTest.Tag) {
      val path = "models/test/Really Long Code.nls"
      assertResult(1010929)(convert(FileIO.file2String(path).replaceAll("\r\n", "\n")).size)
    }
}
