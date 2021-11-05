// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.{ funsuite, Outcome }

class EvaluatorTests extends funsuite.FixtureAnyFunSuite {

  type FixtureParam = Evaluator

  override def withFixture(test: OneArgTest): Outcome = {
   test(new Evaluator(new DummyAbstractWorkspace))
 }

  test("Illegal to have empty source for a reporter thunk"){ e =>
    intercept[IllegalStateException]{
      e.makeReporterThunk("", null, null)
    }
  }

  test("empty source for commands should do nothing, most importantly - not explode"){ e =>
    e.makeCommandThunk("", null, null).call()
  }

  test("empty source for commands shouldnt depend on context at all"){ e =>
    e.withContext(null){ e.makeCommandThunk("", null, null).call() }
  }

}
