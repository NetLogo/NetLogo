// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.fixture

class EvaluatorTests extends fixture.FunSuite {

  type FixtureParam = Evaluator

  override def withFixture(test: OneArgTest) = {
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
