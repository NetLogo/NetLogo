// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.front

import org.scalatest.FunSuite

class TokenMapperTests extends FunSuite {

  def isCommand(s: String) =
    TokenMapper(s).exists(!_._2.isReporter)
  def isReporter(s: String) =
    TokenMapper(s).exists(_._2.isReporter)

  def getCommand(s: String) = {
    val Some((name, syntax)) = TokenMapper(s)
    assert(!syntax.isReporter)
    name
  }
  def getReporter(s: String) = {
    val Some((name, syntax)) = TokenMapper(s)
    assert(syntax.isReporter)
    name
  }

  test("OneCommand1") { assertResult("org.nlogo.prim._fd")(getCommand("FD")) }
  test("OneCommand2") { assertResult("org.nlogo.prim._fd")(getCommand("fd")) }
  test("BadCommand") { assertResult(false)(isCommand("gkhgjkh")) }

  test("OneReporter1") { assertResult("org.nlogo.prim.etc._timer")(getReporter("TIMER")) }
  test("OneReporter2") { assertResult("org.nlogo.prim.etc._timer")(getReporter("timer")) }
  test("BadReporter") { assertResult(false)(isReporter("gkhgjkh")) }

  test("reporter1") { assert(isReporter("random")) }
  test("reporter2") { assert(!isReporter("fd")) }
  test("reporter3") { assert(!isReporter("gkhgkj")) }

  test("command1") { assert(isCommand("fd")) }
  test("command2") { assert(isCommand("forward")) }
  test("command3") { assert(!isCommand("random")) }
  test("command4") { assert(!isCommand("kjhgkfjg")) }
}
