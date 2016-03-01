// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.core.Femto
import org.scalatest.FunSuite

class OptimizerTests extends FunSuite {

  def compileReporter(source: String) =
    compile("globals [glob1] breed [frogs frog] to-report __test [x] report " + source + "\nend")
      .statements.stmts.head.args.head.toString
  def compileCommands(source: String) =
    compile("globals [glob1] breed [frogs frog] to __test [x] " + source + "\nend")
      .statements.stmts.head.toString

  def compile(source: String): ProcedureDefinition = {
    val procdef +: _ = Scaffold(source)
    procdef.accept(Optimizer)
    procdef
  }

  test("Forward1") { assertResult("_fd1[]")(compileCommands("fd 1")) }
  test("Forward2") { assertResult("_jump[_constdouble:0.1[]]")(compileCommands("fd 0.1")) }
  test("Forward3") { assertResult("_jump[_constdouble:-0.1[]]")(compileCommands("fd -0.1")) }
  test("Forward4") { assertResult("_fd[_constdouble:2.0[]]")(compileCommands("fd 2")) }

  test("PatchAt1") {
    assertResult("_patchhereinternal[]")(compileReporter("patch-at 0 0"))
  }
  test("PatchAt2") {
    assertResult("_patcheast[]")(compileReporter("patch-at 1 0"))
  }
  test("PatchAt3") {
    assertResult("_patchsw[]")(compileReporter("patch-at -1 -1"))
  }

  test("PatchColumn1") {
    assertResult("_patchcol[_constdouble:5.0[]]")(
      compileReporter("patches with [pxcor = 5]"))
  }
  test("PatchColumn2") {
    assertResult("_patchcol[_constdouble:5.0[]]")(
      compileReporter("patches with [5 = pxcor]"))
  }
  test("PatchColumn3") {
    assertResult("_patchcol[_observervariable:0[]]")(
      compileReporter("patches with [pxcor = glob1]"))
  }
  test("PatchColumn4") {
    assertResult("_patchcol[_procedurevariable:X[]]")(
      compileReporter("patches with [pxcor = x]"))
  }

  test("PatchRow1") {
    assertResult("_patchrow[_constdouble:6.0[]]")(
      compileReporter("patches with [pycor = 6]"))
  }
  test("PatchRow2") {
    assertResult("_patchrow[_constdouble:6.0[]]")(
      compileReporter("patches with [6 = pycor]"))
  }
  test("PatchRow3") {
    assertResult("_patchrow[_observervariable:0[]]")(
      compileReporter("patches with [pycor = glob1]"))
  }
  test("PatchRow4") {
    assertResult("_patchrow[_procedurevariable:X[]]")(
      compileReporter("patches with [pycor = x]"))
  }

  test("HatchFast1") {
    assertResult("_hatchfast:[_constdouble:5.0[]]")(
      compileCommands("hatch 5"))
  }
  test("HatchFast2") {
    assertResult("_hatchfast:FROGS[_constdouble:5.0[]]")(
      compileCommands("hatch-frogs 5"))
  }
  test("SproutFast1") {
    assertResult("_sproutfast:[_constdouble:5.0[]]")(
      compileCommands("sprout 5"))
  }
  test("SproutFast2") {
    assertResult("_sproutfast:FROGS[_constdouble:5.0[]]")(
      compileCommands("sprout-frogs 5"))
  }
  test("FastCrt1") {
    assertResult("_crtfast:[_constdouble:5.0[]]")(
      compileCommands("crt 5"))
  }
  test("FastCrt2") {
    assertResult("_crtfast:FROGS[_constdouble:5.0[]]")(
      compileCommands("create-frogs 5"))
  }
  test("FastCro1") {
    assertResult("_crofast:[_constdouble:5.0[]]")(
      compileCommands("cro 5"))
  }
  test("FastCro2") {
    assertResult("_crofast:FROGS[_constdouble:5.0[]]")(
      compileCommands("create-ordered-frogs 5"))
  }
  test("countWith1") {
    assertResult("_countwith[_turtles[], [_constboolean:true[]]]")(
      compileReporter("count turtles with [true]"))
  }
  test("countWith2") {
    assertResult("_countwith[_patches[], [_equal[_patchvariable:2[], _constdouble:15.0[]]]]")(
      compileReporter("count patches with [pcolor = red]"))
  }
  test("oneOfWith") {
    assertResult("_oneofwith[_patches[], [_equal[_patchvariable:2[], _constdouble:15.0[]]]]")(
      compileReporter("one-of patches with [pcolor = red]"))
  }
  test("inRadius1") {
    assertResult("_inradiusboundingbox[_turtles[], _constdouble:9001.0[]]")(
      compileReporter("turtles in-radius 9001"))
  }
  test("inRadius2") {
    assertResult("_inradiusboundingbox[_patches[], _constdouble:42.0[]]")(
      compileReporter("patches in-radius 42"))
  }
  test("inRadius3") {
    assertResult("_inradiusboundingbox[_breed:FROGS[], _constdouble:17.0[]]")(
      compileReporter("frogs in-radius 17"))
  }
  test("inRadius4") {
    assertResult("_inradius[_with[_turtles[], [_equal[_turtlevariabledouble:0[], _constdouble:1.0[]]]], _constdouble:4.0[]]")(
      compileReporter("turtles with [WHO = 1] in-radius 4"))
  }
  test("anyWith1") {
    assertResult("_not[_anywith[_patches[], [_constboolean:true[]]]]")(
      compileReporter("count patches with [true] = 0"))
  }
  test("anyWith2") {
    assertResult("_not[_anywith[_patches[], [_constboolean:true[]]]]")(
      compileReporter("0 = count patches with [true]"))
  }
  test("anyWith3") {
    assertResult("_anywith[_patches[], [_constboolean:true[]]]")(
      compileReporter("0 < count patches with [true]"))
  }
  test("anyWith4") {
    assertResult("_anywith[_patches[], [_constboolean:true[]]]")(
      compileReporter("count patches with [true] > 0"))
  }
  test("anyWith5") {
    assertResult("_anywith[_patches[], [_constboolean:true[]]]")(
      compileReporter("count patches with [true] != 0"))
  }
  test("anyWith6") {
    assertResult("_anywith[_patches[], [_constboolean:true[]]]")(
      compileReporter("0 != count patches with [true]"))
  }
  test("anyWith7") {
    assertResult("_anywith[_patches[], [_constboolean:true[]]]")(
      compileReporter("any? patches with [true]"))
  }
  test("patchVariable1") {
    assertResult("_of[[_patchvariable:3[]], _patches[]]")(
      compileReporter("[plabel] of patches"))
  }
  test("patchVariable2") {
    assertResult("_of[[_patchvariabledouble:0[]], _patches[]]")(
      compileReporter("[pxcor] of patches"))
  }
  test("turtleVariable1") {
    assertResult("_of[[_turtlevariable:12[]], _turtles[]]")(
      compileReporter("[pen-mode] of turtles"))
  }
  test("turtleVariable2") {
    assertResult("_of[[_turtlevariabledouble:3[]], _turtles[]]")(
      compileReporter("[xcor] of turtles"))
  }
  test("randomConst1") {
    assertResult("_randomconst:10[]")(
      compileReporter("random 10"))
  }
  test("randomConst2") {
    assertResult("_random[_constdouble:10.5[]]")(
      compileReporter("random 10.5"))
  }
  test("randomConst3") {
    assertResult("_random[_constdouble:0.0[]]")(
      compileReporter("random 0"))
  }
  test("randomConst4") {
    assertResult("_random[_constdouble:-5.0[]]")(
      compileReporter("random -5"))
  }
  test("otherWith1") {
    assertResult("_otherwith[_turtles[], [_constboolean:true[]]]")(
      compileReporter("other (turtles with [true])"))
  }
  test("otherWith2") {
    assertResult("_otherwith[_turtles[], [_constboolean:true[]]]")(
      compileReporter("(other turtles) with [true]"))
  }
  test("anyOther") {
    assertResult("_anyother[_turtles[]]")(
      compileReporter("any? other turtles"))
  }
  test("anyOtherwith") {
    assertResult("_anyotherwith[_turtles[], [_constboolean:true[]]]")(
      compileReporter("any? other turtles with [true]"))
  }
  test("countOther") {
    assertResult("_countother[_turtles[]]")(
      compileReporter("count other turtles"))
  }
  test("countOtherWith") {
    assertResult("_countotherwith[_turtles[], [_constboolean:true[]]]")(
      compileReporter("count other turtles with [true]"))
  }
}
