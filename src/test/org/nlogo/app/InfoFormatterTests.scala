package org.nlogo.app

import org.scalatest.FunSuite
import java.io.ByteArrayInputStream

class InfoFormatterTests extends FunSuite {

  import InfoFormatter._

  val helloWorldMarkdown = """
    # Hello
    World
    """
  val helloWorldInnerHtml = """<pre><code># Hello
World
</code></pre>"""

  test("hello world (smoke test)") {
    assert(toInnerHtml(helloWorldMarkdown) === helloWorldInnerHtml)
  }

  test("github flavored newlines") {
    val twoParagraphsUsingSingleNewLine = """hi
there"""
    assert(toInnerHtml(twoParagraphsUsingSingleNewLine) === """<p>hi<br/>
there</p>""")

    val twoParagraphsUsingTwoNewLines = """hi

there"""
    assert(toInnerHtml(twoParagraphsUsingTwoNewLines) === """<p>hi</p><p>there</p>""")
  }

  test("main"){
    val fullHtml = InfoFormatter(helloWorldMarkdown)
    // make sure the style sheet is there:
    assert(fullHtml.contains("<style type=\"text/css\">"))
    assert(fullHtml.contains(":link { color: rgb(110,0,110); }"))
    assert(fullHtml.contains("-->\n</style>"))
    // make sure the html is there
    assert(fullHtml.contains(helloWorldInnerHtml))
  }

  test("read function that should probably be in a utils class somewhere"){
    assert(read(new ByteArrayInputStream("goo".getBytes)) == "goo")
    assert(read(new ByteArrayInputStream("\ngoo".getBytes)) === "\ngoo")
    assert(read(new ByteArrayInputStream("\ngoo\n".getBytes)) === "\ngoo\n")
  }

  test("unordered list bug"){
    val weirdUnorderedListBugText = "  * Hello World\n    * Worwe qworijwetor"
    val innerHtml = toInnerHtml(weirdUnorderedListBugText)
    assert(innerHtml.contains("World"))
  }

  test("table parsing makes no changes and isn't outrageously slow"){
    val content = """|<table border>
                     |<tr> <th>Your action <th>Partner's action <th>Your jail time <th>Partner's jail time
                     |<tr> <td>silent      <td>silent           <td>1              <td>1
                     |<tr> <td>silent      <td>confess          <td>5              <td>0
                     |<tr> <td>confess     <td>silent           <td>0              <td>5
                     |</table>""".stripMargin
    val time = System.currentTimeMillis
    assert(toInnerHtml(content) === content)
    // trac.assembla.com/nlogo/ticket/1181
    assert(System.currentTimeMillis - time < 1000)
  }

}
