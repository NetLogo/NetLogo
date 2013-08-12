// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.scalatest.FunSuite
import org.nlogo.agent.{ Link, Patch, Turtle }
import org.nlogo.api.{ CompilerException, Let }
import org.nlogo.nvm.{ Command, Reporter }
import org.nlogo.parse
import org.nlogo.prim._

class SetVisitorTests extends FunSuite {
  def tester(r: Reporter, spec: String, setterClass: Class[_ <: Command]) {
    val stmt = new parse.Statement(new _set, 0, 0, "")
    stmt.addArgument(new parse.ReporterApp(r, 0, 0, ""))
    stmt.addArgument(new parse.ReporterApp(new _constdouble(Double.box(5)), 0, 0, ""))
    stmt.accept(new SetVisitor)
    assertResult(setterClass.getName.substring("org.nlogo.prim.".length) + ":" + spec + "[_constdouble:5.0[]]")(
      stmt.toString)
  }
  test("test1") { tester(new _turtlevariable(Turtle.VAR_HEADING), Turtle.VAR_HEADING.toString, classOf[_setturtlevariable]) }
  test("test2") { tester(new _letvariable(new Let("FOO", 0, 0)), "FOO", classOf[_setletvariable]) }
  test("test3") { tester(new _turtleorlinkvariable("SHAPE"), "SHAPE", classOf[_setturtleorlinkvariable]) }
  test("test4") { tester(new _patchvariable(Patch.VAR_PCOLOR), Patch.VAR_PCOLOR.toString, classOf[_setpatchvariable]) }
  test("test5") { tester(new _observervariable(3), "3", classOf[_setobservervariable]) }
  test("test6") { tester(new _linkbreedvariable("FOO"), "FOO", classOf[_setlinkbreedvariable]) }
  test("test7") { tester(new _procedurevariable(4, "FOO"), "FOO", classOf[_setprocedurevariable]) }
  test("test8") { tester(new _turtlevariable(Turtle.VAR_HEADING), Turtle.VAR_HEADING.toString, classOf[_setturtlevariable]) }
  test("test9") { tester(new _breedvariable("FOO"), "FOO", classOf[_setbreedvariable]) }
  test("test10") { tester(new _linkvariable(Link.VAR_THICKNESS), Link.VAR_THICKNESS.toString, classOf[_setlinkvariable]) }
  test("not found") {
    intercept[CompilerException] {
      tester(new _nobody, null, null)
    }
  }
}
