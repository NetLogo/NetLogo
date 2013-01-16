// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.scalatest.FunSuite
import org.nlogo.{ api, nvm, headless, prim }
import org.nlogo.util.{ Femto, MersenneTwisterFast }

class TestTortoise extends FunSuite {

  System.setProperty("org.nlogo.noGenerator", "true")
  val ws = headless.HeadlessWorkspace.newInstance
  val owner = new api.SimpleJobOwner("Tortoise", new MersenneTwisterFast)
  def netlogo(logo: String) =
    ws.report(logo)

  def compare(logo: String) {
    expectResult(netlogo(logo)) {
      Rhino.run(Compiler.compile(logo))
    }
  }

  test("comments") {
    compare("3 ; comment")
    compare("[1 ; comment\n2]")
  }

  test("simple literals") {
    compare("false")
    compare("true")
    compare("2")
    compare("2.0")
    compare("\"foo\"")
  }

  test("literal lists") {
    compare("[]")
    compare("[1]")
    compare("[1 2]")
    compare("[\"foo\"]")
    compare("[1 \"foo\" 2]")
    compare("[1 [2] [3 4 [5]] 6 [] 7]")
    compare("[false true]")
  }

}
