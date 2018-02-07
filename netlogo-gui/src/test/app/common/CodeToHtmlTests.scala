// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import org.scalatest.FunSuite
import org.nlogo.api.{ FileIO, NetLogoLegacyDialect }
import org.nlogo.util.{ ArityIndependent, SlowTest }

class CodeToHtmlTests extends FunSuite with SlowTest {
  val converter = CodeToHtml.newInstance(NetLogoLegacyDialect)
  import converter.convert
  test("basic", SlowTest.Tag, ArityIndependent) {
    assertResult("""<pre><font color="#007f69">to</font><font color="#000000"> foo<br />  """ +
      """</font><font color="#0000aa">crt</font><font color="#000000"> </font><font color="#963700">10</font><font color="#000000"><br />""" +
      """</font><font color="#007f69">end</font>""" +
      "\n</pre>\n")(
        convert("to foo\n  crt 10\nend"))
  }

  // very long Code tabs shouldn't blow the stack.
  test("don't blow stack", SlowTest.Tag, ArityIndependent) {
    val path = "models/test/Really Long Code.nls"
    assertResult(1042339)(convert(FileIO.fileToString(path).replaceAll("\r\n", "\n")).size)
  }
}
