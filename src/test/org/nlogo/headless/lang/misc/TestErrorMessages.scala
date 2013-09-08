// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

// Most testing of error messages can go in test/commands/*.txt.  Some tests are here because
// they go beyond the capabilities of the txt-based stuff.  (In the long run, perhaps
// that framework should be extended so these tests could be done in it.)  - ST 3/18/08, 8/21/13

import org.nlogo.nvm
import org.nlogo.util.SlowTest

class TestErrorMessages extends FixtureSuite {

  // Here we're testing not just that the error message is right, but that
  // the error is attributed to the right agent.  So we intercept
  // EngineException, not just generic LogoException, and look inside.

  test("perspectiveChangeWithOf") { fixture =>
    import fixture._
    declare("breed [frogs frog] frogs-own [age spots]")
    testCommand(
      "create-frogs 3 [ set spots turtle ((who + 1) mod count turtles) ]")
    testCommand(
      "ask frog 2 [ die ]")
    val ex = intercept[nvm.EngineException] {
      testCommand(
        "ask turtle 0 [ __ignore [who] of frogs with " +
        "[age = ([age] of [spots] of self)]]")
    }
    assertResult("That frog is dead.")(ex.getMessage)
    // frog 2 is dead, but frog 1 actually encountered the error
    assertResult("frog 1")(ex.context.agent.toString)
  }

  // Here we're checking that when a runtime error is reported, the right
  // token is singled out as the source of the error.

  test("argumentTypeException") { fixture =>
    import fixture._
    declare("globals [g]")
    testCommand("set g [1.4]")
    val ex = intercept[nvm.ArgumentTypeException] {
      testCommand("__ignore 0 < position 5 item 0 g") }
    val message =
      "POSITION expected input to be a string or list but got the number 1.4 instead."
    assertResult(message)(ex.getMessage)
    assertResult("POSITION")(ex.instruction.token.text.toUpperCase)
  }

}
