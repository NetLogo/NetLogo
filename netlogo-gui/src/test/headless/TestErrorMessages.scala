// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

// Most testing of error messages can go in test/commands/*.txt.  Some tests are here because
// they go beyond the capabilities of the txt-based stuff.  (In the long run, perhaps
// that framework should be extended so these tests could be done in it.)  - ST 3/18/08, 8/21/13

import org.nlogo.core.{ CompilerException, Model, View }
import org.nlogo.api.WorldDimensions3D
import org.nlogo.nvm.{ ArgumentTypeException, EngineException }

class TestErrorMessages extends AbstractTestLanguage {

  override def initializeRunner(r: Runner): Runner = {
    r.openModel(Model(
      code = "globals [glob1] breed [ frogs frog ] frogs-own [ age spots ]",
      widgets = Seq(View(dimensions = WorldDimensions3D.box(5))),
      version = r.instance.version.version))
    r.testCommand("clear-all reset-ticks")
    r
  }

  testInSpace("perspectiveChangeWithOf", normalConfigurations) { r =>
    import r._
    testCommand("create-frogs 3 [ set spots turtle ((who + 1) mod count turtles) ]")
    testCommand("ask frog 2 [ die ]")
    val ex = intercept[EngineException] {
      testCommand("ask turtle 0 [ __ignore [who] of frogs with [age = ([age] of [spots] of self)]]")
    }
    // is the error message correct?
    assertResult("That frog is dead.")(ex.getMessage)
    // is the error message attributed to the right agent? frog 2 is dead,
    // but it's frog 1 that actually encountered the error
    assertResult("frog 1")(ex.context.getAgent.toString)
  }

  testInSpace("argumentTypeException", normalConfigurations) { r =>
    import r._
    testCommand("set glob1 [1.4]")
    val ex = intercept[ArgumentTypeException] {
      testCommand("__ignore 0 < position 5 item 0 glob1")
    }
    assertResult("POSITION expected input to be a string or list but got the number 1.4 instead.")(ex.getMessage)
    assertResult("POSITION")(ex.responsibleInstruction.get.token.text.toUpperCase)
  }

  testInSpace("breedOwnRedeclaration", normalConfigurations) { r =>
    import r._
    val ex = intercept[CompilerException] {
      compiler.compileProgram(
        "breed [hunters hunter] hunters-own [fear] hunters-own [loathing]", newProgram,
        workspace.getExtensionManager, workspace.getCompilationEnvironment, compilerFlags)
    }
    assertResult("Redeclaration of HUNTERS-OWN")(ex.getMessage)
  }

}
