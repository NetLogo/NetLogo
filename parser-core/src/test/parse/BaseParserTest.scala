// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import
  org.nlogo.core.{ CompilerException, Statements, TestUtils },
    TestUtils.cleanJsNumbers

import
  org.scalatest.funsuite.AnyFunSuite

trait BaseParserTest { this: AnyFunSuite =>
  val PREAMBLE = "to __test "
  val POSTAMBLE = "\nend"

  /// helpers
  def compile(source: String, preamble: String = PREAMBLE, postamble: String = POSTAMBLE): Seq[Statements] =
    FrontEnd.frontEnd(preamble + source + postamble) match {
      case (procs, _) =>
        procs.map(_.statements)
    }
  def testParse(input: String, result: String, preamble: String = PREAMBLE): Unit = {
    assertResult(cleanJsNumbers(result))(cleanJsNumbers(compile(input, preamble).mkString))
  }
  def runFailure(input: String, message: String, start: Int, end: Int, preamble: String = PREAMBLE): Unit = {
    doFailure(input, message, start, end, preamble)
  }
  def doFailure(input: String, message: String, start: Int, end: Int, preamble: String = PREAMBLE): Unit = {
    val e = intercept[CompilerException] { compile(input, preamble = preamble) }
    assertResult(message)(e.getMessage)
    val programText = preamble + input + POSTAMBLE
    def programTextWithContext(s: Int, e: Int) = {
      val beforeContext = programText.slice((s - 5) max 0, s)
      val t = programText.slice(s, e)
      val afterContext = programText.slice(e, (e + 5) min (programText.length - 1))
      s"... $beforeContext|$t|$afterContext ..."
    }
    val expectedErrorSource = programTextWithContext(start + preamble.length, end + preamble.length)
    val actualErrorSource = programTextWithContext(e.start, e.end)
    assert(start == e.start - preamble.length, s"incorrect start, expected: $expectedErrorSource but was actually $actualErrorSource\n")
    assert(end   == e.end   - preamble.length, s"incorrect end, expected: $expectedErrorSource but was actually $actualErrorSource")
  }

}
