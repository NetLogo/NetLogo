// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.funsuite.AnyFunSuite

class TokenMapperTests extends AnyFunSuite {

  import FrontEnd.tokenMapper._

  test("all listed primitives exist") {
    checkInstructionMaps()
  }

  def isCommand(s: String) = getCommand(s).isDefined
  def isReporter(s: String) = getReporter(s).isDefined

  test("OneCommand1") { assertResult("_fd")(getCommand("FD").get.getClass.getSimpleName) }
  test("OneCommand2") { assertResult("_fd")(getCommand("fd").get.getClass.getSimpleName) }
  test("BadCommand") { assertResult(false)(isCommand("gkhgjkh")) }

  test("OneReporter1") { assertResult("_timer")(getReporter("TIMER").get.getClass.getSimpleName) }
  test("OneReporter2") { assertResult("_timer")(getReporter("timer").get.getClass.getSimpleName) }
  test("BadReporter") { assertResult(false)(isReporter("gkhgjkh")) }

  test("reporter1") { assert(isReporter("random")) }
  test("reporter2") { assert(!isReporter("fd")) }
  test("reporter3") { assert(!isReporter("gkhgkj")) }

  test("command1") { assert(isCommand("fd")) }
  test("command2") { assert(isCommand("forward")) }
  test("command3") { assert(!isCommand("random")) }
  test("command4") { assert(!isCommand("kjhgkfjg")) }

  test("breed") { assertResult(Some("_breed(SHEEP)"))(breedInstruction("_breed", "SHEEP").map(_.toString)) }

}
