// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.scalatest.FunSuite
import org.nlogo.{ nvm, headless }
import org.nlogo.util.Femto

class TestTortoise extends FunSuite {

  /// Rhino

  import javax.script.ScriptEngineManager
  val manager = new ScriptEngineManager
  val engine = manager.getEngineByName("JavaScript")

  /// NetLogo

  val ws = headless.HeadlessWorkspace.newInstance

  /// test scaffolds

  def compare(logo: String) {
    expectResult(ws.report(logo)) {
      engine.eval(logo)
    }
  }

  /// tests

  test("simple literals") {
    compare("false")
    compare("true")
    compare("2")
    compare("2.0")
    compare("\"foo\"")
  }

}
