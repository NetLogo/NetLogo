// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.scalatest.funsuite.AnyFunSuite
import org.nlogo.api.NetLogoLegacyDialect
import org.nlogo.agent.{ Link, Patch, Turtle }
import org.nlogo.core.{CompilerException, Let, Program, SourceLocation}
import org.nlogo.nvm.{ Command, Reporter }
import org.nlogo.prim._
import org.nlogo.compile.api.{ ReporterApp, Statement }

class SetVisitorTests extends AnyFunSuite {
  val emptyLocation = SourceLocation(0, 0, "")
  def tester(r: Reporter, spec: String, setterClass: Class[_ <: Command]): Unit = {
    val args = Seq(new ReporterApp(null, r, emptyLocation),
      new ReporterApp(null, new _constdouble(Double.box(5)), emptyLocation))
    val stmt = new Statement(null, new _set, args, emptyLocation)
    stmt.accept(new SetVisitor(Program.fromDialect(NetLogoLegacyDialect)))
    assertResult(setterClass.getName.substring("org.nlogo.prim.".length) + ":" + spec + "[_constdouble:5.0[]]")(
      stmt.toString)
  }
  test("1") { tester(new _turtlevariable(Turtle.VAR_HEADING), Turtle.VAR_HEADING.toString, classOf[_setturtlevariable]) }
  test("2") { tester(new _letvariable(new Let("FOO")), "FOO", classOf[_setletvariable]) }
  test("3") { tester(new _turtleorlinkvariable("SHAPE"), "SHAPE", classOf[_setturtleorlinkvariable]) }
  test("4") { tester(new _patchvariable(Patch.VAR_PCOLOR), Patch.VAR_PCOLOR.toString, classOf[_setpatchvariable]) }
  test("5") { tester(new _observervariable(3), "3", classOf[_setobservervariable]) }
  test("6") { tester(new _linkbreedvariable("FOO"), "FOO", classOf[_setlinkbreedvariable]) }
  test("7") { tester(new _procedurevariable(4, "FOO"), "FOO", classOf[_setprocedurevariable]) }
  test("8") { tester(new _turtlevariable(Turtle.VAR_HEADING), Turtle.VAR_HEADING.toString, classOf[_setturtlevariable]) }
  test("9") { tester(new _breedvariable("FOO"), "FOO", classOf[_setbreedvariable]) }
  test("10") { tester(new _linkvariable(Link.VAR_THICKNESS), Link.VAR_THICKNESS.toString, classOf[_setlinkvariable]) }
  test("not found") {
    intercept[CompilerException] {
      tester(new _nobody, null, null)
    }
  }
}
