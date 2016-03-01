// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless.test

import org.nlogo.core.Model
import org.nlogo.{api, core}
import org.scalatest.Assertions

// implemented by HeadlessWorkspace in org.nlogo.headless.lang, implemented
// elsewhere by Tortoise - ST 8/28/13, RG 1/26/15
trait AbstractFixture {
  import org.scalatest.Assertions._
  def defaultView: core.View
  def declare(code: String): Unit = declare(Model(code = code, widgets = List(defaultView)))
  def declare(model: Model = Model(widgets = List(defaultView)))
  def open(path: String)
  def open(model: Model)
  def checkCompile(model: Model, compile: Compile)
  def runCommand(command: Command, mode: TestMode)
  def runReporter(reporter: Reporter, mode: TestMode)
  def readFromString(literal: String): AnyRef
  def checkResult(mode: TestMode, reporter: String, expectedResult: String, actualResult: AnyRef) {
    // To be as safe as we can, let's do two separate checks here...  we'll compare the results both
    // as values and as printed representations.  Most of the time these checks will come out
    // the same, but it might be good to have both, partially as a way of giving both Equality and
    // Dump lots of testing! - ST 5/8/03, 8/21/13
    withClue(s"""$mode: not equals(): reporter "$reporter" """) {
      assertResult(expectedResult)(
        api.Dump.logoObject(actualResult, true, false))
    }
    assert(api.Equality.equals(actualResult,
      readFromString(expectedResult)),
      s"""$mode: not recursivelyEqual(): reporter "$reporter" """)
  }
}
