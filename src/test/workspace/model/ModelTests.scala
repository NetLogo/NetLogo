// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace.model

import org.scalatest.fixture.FunSuite

class ModelTest extends fixture.FunSuite {

  type FixtureParam = Evaluator

  test("Parse a button"){
    println(ButtonParser.format(new Button("", 0, 0, 0, 0, "", false))
  }

  test("empty source for commands should do nothing, most importantly - not explode"){ e =>
  }

  test("empty source for commands shouldnt depend on context at all"){ e =>
    e.withContext(null){ e.makeCommandThunk("", null, null).call() }
  }

}
