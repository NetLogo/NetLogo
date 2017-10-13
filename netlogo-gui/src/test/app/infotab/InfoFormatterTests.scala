// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.infotab

import org.scalatest.FunSuite
import org.nlogo.api.NetLogoLegacyDialect
import java.io.ByteArrayInputStream

class InfoFormatterTests extends FunSuite {

  def toInnerHtml(md: String): String = {
    InfoFormatter.toInnerHtml(md, NetLogoLegacyDialect).trim
  }

  val helloWorldMarkdown = """|
                              |    # Hello
                              |    World
                              |    """.stripMargin.replaceAll("\r\n", "\n")
  val helloWorldInnerHtml =
    """<pre><code><font color="#000000"># Hello<br />World<br /></font></code></pre>"""

  test("hello world (smoke test)") {
    assertResult(helloWorldInnerHtml) {
      toInnerHtml(helloWorldMarkdown)
    }
  }

  test("github flavored newlines 1") {
    assertResult("""|<p>hi<br />
                    |there</p>""".stripMargin) {
      toInnerHtml("""|hi
                     |there""".stripMargin)
    }
  }

  test("github flavored newlines 2") {
    assertResult("""|<p>hi</p>
                    |<p>there</p>""".stripMargin) {
      toInnerHtml("""|hi
                     |
                     |there""".stripMargin)
    }
  }

  test("main") {
    val fullHtml = InfoFormatter(helloWorldMarkdown, NetLogoLegacyDialect)
    // make sure the style sheet is there:
    assert(fullHtml.contains("<style type=\"text/css\">"))
    assert(fullHtml.contains(":link { color: rgb(110,0,110); }"))
    assert(fullHtml.contains("-->\n</style>"))
    // make sure the html is there
    assert(fullHtml.contains(helloWorldInnerHtml))
  }

  test("read function that should probably be in a utils class somewhere") {
    def process(s: String) =
      assertResult(s) { InfoFormatter.read(new ByteArrayInputStream(s.getBytes)) }
    process("goo")
    process("\ngoo")
    process("\ngoo\n")
  }

  test("unordered list bug") {
    val weirdUnorderedListBugText = "  * Hello World\n    * Worwe qworijwetor"
    val innerHtml = toInnerHtml(weirdUnorderedListBugText)
    assert(innerHtml.contains("World"))
  }

  // trac.assembla.com/nlogo/ticket/1278
  test("< characters get converted to &lt;") {
    assertResult("""|<p>is 5&lt;6?<br />
                    |is 5 &lt; 6?<br />
                    |is 5 &lt; x?</p>""".stripMargin) {
      toInnerHtml("""|is 5<6?
                     |is 5 < 6?
                     |is 5 < x?""".stripMargin)
    }
  }

  test("table parsing makes no changes and isn't outrageously slow") {
    val content = """|<table border>
                     |<tr> <th>Your action <th>Partner's action <th>Your jail time <th>Partner's jail time
                     |<tr> <td>silent      <td>silent           <td>1              <td>1
                     |<tr> <td>silent      <td>confess          <td>5              <td>0
                     |<tr> <td>confess     <td>silent           <td>0              <td>5
                     |</table>""".stripMargin
    val time = System.currentTimeMillis
    assertResult(content) { toInnerHtml(content) }
    // trac.assembla.com/nlogo/ticket/1181
    assert(System.currentTimeMillis - time < 1000)
  }

  // here we get a <pre> block because we have a blank line before the fence
  test("fenced code blocks 1") {
    assertResult("""|<p>foo</p>
              |<pre><code class="language-text">   bar
              |  baz
              |qux
              |</code></pre>
              |<p>oof</p>""".stripMargin.replaceAll("\r\n", "\n")) {
      toInnerHtml("""|foo
                     |
                     |```text
                     |   bar
                     |  baz
                     |qux
                     |```
                     |oof""".stripMargin.replaceAll("\r\n", "\n"))
    }
  }

  test("colorized code blocks") {
    assertResult("""|<pre><code><font color="#007f69">to</font><font color="#000000"> setup<br />  </font><font color="#0000aa">ca</font><font color="#000000"><br />  </font><font color="#0000aa">crt</font><font color="#000000"> </font><font color="#963700">10</font><font color="#000000"><br /></font><font color="#007f69">end</font></code></pre>""".stripMargin) {
      toInnerHtml("""|    to setup
                     |      ca
                     |      crt 10
                     |    end""".stripMargin)
    }
  }

}
