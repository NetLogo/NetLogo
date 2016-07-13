// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite
import org.nlogo.agent.{ Link, Patch, Turtle }
import org.nlogo.core.{ CompilerException, Reporter => CoreReporter, SourceLocation }
import org.nlogo.core.{ prim => coreprim},
  coreprim.{ _set => _coreset, _const => _coreconst }
import org.nlogo.nvm.{ Command, Reporter }
import org.nlogo.prim._

class SetVisitorTests extends FunSuite {
  def tester(r: Reporter, cr: CoreReporter, spec: String, setterClass: Class[_ <: Command]) {
    val stmt = new Statement(new _coreset(), new _set, Seq(), SourceLocation(0, 0, ""))
    stmt.addArgument(new ReporterApp(cr, r, SourceLocation(0, 0, "")))
    stmt.addArgument(
      new ReporterApp(new _coreconst(Double.box(5)), new _constdouble(Double.box(5)), SourceLocation(0, 0, "")))
    stmt.accept(new SetVisitor)
    assertResult(setterClass.getName.substring("org.nlogo.prim.".length) + ":" + spec + "[_constdouble:5.0[]]")(
      stmt.toString)
  }
  test("test1") { tester(
    new _turtlevariable(Turtle.VAR_HEADING), new coreprim._turtlevariable(Turtle.VAR_HEADING), Turtle.VAR_HEADING.toString,
    classOf[_setturtlevariable]) }
  test("test2") { tester(new _letvariable(null, "FOO"), new coreprim._letvariable(null), "FOO", classOf[_setletvariable]) }
  test("test3") { tester(new _turtleorlinkvariable("SHAPE"), new coreprim._turtleorlinkvariable("SHAPE"), "SHAPE", classOf[_setturtleorlinkvariable]) }
  test("test4") { tester(new _patchvariable(Patch.VAR_PCOLOR), new coreprim._patchvariable(Patch.VAR_PCOLOR), Patch.VAR_PCOLOR.toString, classOf[_setpatchvariable]) }
  test("test5") { tester(new _observervariable(3), new coreprim._observervariable(3), "3", classOf[_setobservervariable]) }
  test("test6") { tester(new _linkbreedvariable("FOO"), new coreprim._linkbreedvariable("FOO"), "FOO", classOf[_setlinkbreedvariable]) }
  test("test7") { tester(new _procedurevariable(4, "FOO"), new coreprim._procedurevariable(4, "FOO"), "FOO", classOf[_setprocedurevariable]) }
  test("test8") { tester(new _turtlevariable(Turtle.VAR_HEADING), new coreprim._turtlevariable(Turtle.VAR_HEADING), Turtle.VAR_HEADING.toString, classOf[_setturtlevariable]) }
  test("test9") { tester(new _breedvariable("FOO"), new coreprim._breedvariable("FOO"), "FOO", classOf[_setbreedvariable]) }
  test("test10") { tester(new _linkvariable(Link.VAR_THICKNESS), new coreprim._linkvariable(Link.VAR_THICKNESS), Link.VAR_THICKNESS.toString, classOf[_setlinkvariable]) }
  test("not found") {
    intercept[CompilerException] {
      tester(new _nobody, new coreprim._nobody, null, null)
    }
  }
}
