// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.scalatest.FunSuite
import org.nlogo.api.{ FileIO, Version }
import org.nlogo.util.SlowTest

class ProceduresToHtmlTests extends FunSuite with SlowTest {
  val converter = ProceduresToHtml.newInstance
  import converter.convert
  test("basic") {
    expect("""|<pre><font color="#007f69">to</font><font color="#000000"> foo
           |  </font><font color="#0000aa">crt</font><font color="#000000"> </font><font color="#963700">10</font><font color="#000000">
           |</font><font color="#007f69">end</font>
           |</pre>
           |""".stripMargin)(
      convert("to foo\n  crt 10\nend"))
  }
  // can be very slow, so restrict to 2D to keep overall nightly.sh runtime down - ST 6/24/11
  if(!Version.is3D)
    // very long Code tabs shouldn't blow the stack.  
    test("don't blow stack") {
      val path = "test/applet/Really Long Code.nls"
      expect(1010929)(convert(FileIO.file2String(path)).size)
    }
}
