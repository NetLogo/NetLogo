// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite

class TokenMapperTests extends FunSuite {

  import Parser.tokenMapper._

  test("all listed primitives exist") {
    checkInstructionMaps()
  }

  def isCommand(s: String) = getCommand(s).isDefined
  def isReporter(s: String) = getReporter(s).isDefined

  test("OneCommand1") { expectResult("_fd")(getCommand("FD").get.toString) }
  test("OneCommand2") { expectResult("_fd")(getCommand("fd").get.toString) }
  test("BadCommand") { expectResult(false)(isCommand("gkhgjkh")) }

  test("OneReporter1") { expectResult("_timer")(getReporter("TIMER").get.toString) }
  test("OneReporter2") { expectResult("_timer")(getReporter("timer").get.toString) }
  test("BadReporter") { expectResult(false)(isReporter("gkhgjkh")) }

  test("reporter1") { assert(isReporter("random")) }
  test("reporter2") { assert(!isReporter("fd")) }
  test("reporter3") { assert(!isReporter("gkhgkj")) }

  test("command1") { assert(isCommand("fd")) }
  test("command2") { assert(isCommand("forward")) }
  test("command3") { assert(!isCommand("random")) }
  test("command4") { assert(!isCommand("kjhgkfjg")) }
}
