// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite
import org.nlogo.api.{ DummyExtensionManager, Program }
import org.nlogo.nvm

class OptimizerTests extends FunSuite {
  implicit val tokenizer = Compiler.Tokenizer2D
  def compileReporter(source:String) =
    compile("globals [glob1] breed [frogs frog] to-report __test [x] report " + source + "\nend")
      .statements.head.head.toString
  def compileCommands(source:String) =
    compile("globals [glob1] breed [frogs frog] to __test [x] " + source + "\nend")
      .statements.head.toString
  private def compile(source:String):ProcedureDefinition = {
    val results = new StructureParser(tokenizer.tokenize(source), None, Program.empty(),
                                      nvm.CompilerInterface.NoProcedures,
                                      new DummyExtensionManager)
      .parse(false)
    expect(1)(results.procedures.size)
    val procedure = results.procedures.values.iterator.next()
    val tokens =
      new IdentifierParser(results.program, nvm.CompilerInterface.NoProcedures,
                           results.procedures,false)
      .process(results.tokens(procedure).iterator, procedure)
    val procdef = new ExpressionParser(procedure).parse(tokens).head
    procdef.accept(new ConstantFolder)
    procdef.accept(new Optimizer)
    procdef
  }
  test("testForward1") { expect("_fd1[]")(compileCommands("fd 1")) }
  test("testForward2") { expect("_jump[_constdouble:0.1[]]")(compileCommands("fd 0.1")) }
  test("testForward3") { expect("_jump[_constdouble:-0.1[]]")(compileCommands("fd -0.1")) }
  test("testForward4") { expect("_fd[_constdouble:2.0[]]")(compileCommands("fd 2")) }

  test("testPatchAt1") {
    expect("_patchhereinternal[]")(compileReporter("patch-at 0 0"))
  }
  test("testPatchAt2") {
    expect("_patcheast[]")(compileReporter("patch-at 1 0"))
  }
  test("testPatchAt3") {
    expect("_patchsw[]")(compileReporter("patch-at -1 -1"))
  }

  test("testPatchColumn1") {
    expect("_patchcol[_constdouble:5.0[]]")(
      compileReporter("patches with [pxcor = 5]"))
  }
  test("testPatchColumn2") {
    expect("_patchcol[_constdouble:5.0[]]")(
      compileReporter("patches with [5 = pxcor]"))
  }
  test("testPatchColumn3") {
    expect("_patchcol[_observervariable:0[]]")(
      compileReporter("patches with [pxcor = glob1]"))
  }
  test("testPatchColumn4") {
    expect("_patchcol[_procedurevariable:X[]]")(
      compileReporter("patches with [pxcor = x]"))
  }

  test("testPatchRow1") {
    expect("_patchrow[_constdouble:6.0[]]")(
      compileReporter("patches with [pycor = 6]"))
  }
  test("testPatchRow2") {
    expect("_patchrow[_constdouble:6.0[]]")(
      compileReporter("patches with [6 = pycor]"))
  }
  test("testPatchRow3") {
    expect("_patchrow[_observervariable:0[]]")(
      compileReporter("patches with [pycor = glob1]"))
  }
  test("testPatchRow4") {
    expect("_patchrow[_procedurevariable:X[]]")(
      compileReporter("patches with [pycor = x]"))
  }

  test("testHatchFast1") {
    expect("_hatchfast:[_constdouble:5.0[]]")(
      compileCommands("hatch 5"))
  }
  test("testHatchFast2") {
    expect("_hatchfast:FROGS[_constdouble:5.0[]]")(
      compileCommands("hatch-frogs 5"))
  }
  test("testSproutFast1") {
    expect("_sproutfast:[_constdouble:5.0[]]")(
      compileCommands("sprout 5"))
  }
  test("testSproutFast2") {
    expect("_sproutfast:FROGS[_constdouble:5.0[]]")(
      compileCommands("sprout-frogs 5"))
  }
  test("testFastCrt1") {
    expect("_crtfast:[_constdouble:5.0[]]")(
      compileCommands("crt 5"))
  }
  test("testFastCrt2") {
    expect("_crtfast:FROGS[_constdouble:5.0[]]")(
      compileCommands("create-frogs 5"))
  }
  test("testFastCro1") {
    expect("_crofast:[_constdouble:5.0[]]")(
      compileCommands("cro 5"))
  }
  test("testFastCro2") {
    expect("_crofast:FROGS[_constdouble:5.0[]]")(
      compileCommands("create-ordered-frogs 5"))
  }
  test("countWith1") {
    expect("_countwith[_turtles[], [_constboolean:true[]]]")(
      compileReporter("count turtles with [true]"))
  }
  test("countWith2") {
    expect("_countwith[_patches[], [_equal[_patchvariable:2[], _constdouble:15.0[]]]]")(
      compileReporter("count patches with [pcolor = red]"))
  }
  test("oneOfWith") {
    expect("_oneofwith[_patches[], [_equal[_patchvariable:2[], _constdouble:15.0[]]]]")(
      compileReporter("one-of patches with [pcolor = red]"))
  }
  test("anyWith1") {
    expect("_not[_anywith[_patches[], [_constboolean:true[]]]]")(
      compileReporter("count patches with [true] = 0"))
  }
  test("anyWith2") {
    expect("_not[_anywith[_patches[], [_constboolean:true[]]]]")(
      compileReporter("0 = count patches with [true]"))
  }
  test("anyWith3") {
    expect("_anywith[_patches[], [_constboolean:true[]]]")(
      compileReporter("0 < count patches with [true]"))
  }
  test("anyWith4") {
    expect("_anywith[_patches[], [_constboolean:true[]]]")(
      compileReporter("count patches with [true] > 0"))
  }
  test("anyWith5") {
    expect("_anywith[_patches[], [_constboolean:true[]]]")(
      compileReporter("count patches with [true] != 0"))
  }
  test("anyWith6") {
    expect("_anywith[_patches[], [_constboolean:true[]]]")(
      compileReporter("0 != count patches with [true]"))
  }
  test("anyWith7") {
    expect("_anywith[_patches[], [_constboolean:true[]]]")(
      compileReporter("any? patches with [true]"))
  }
  test("patchVariable1") {
    expect("_of[[_patchvariable:3[]], _patches[]]")(
      compileReporter("[plabel] of patches"))
  }
  test("patchVariable2") {
    expect("_of[[_patchvariabledouble:0[]], _patches[]]")(
      compileReporter("[pxcor] of patches"))
  }
  test("turtleVariable1") {
    expect("_of[[_turtlevariable:12[]], _turtles[]]")(
      compileReporter("[pen-mode] of turtles"))
  }
  test("turtleVariable2") {
    expect("_of[[_turtlevariabledouble:3[]], _turtles[]]")(
      compileReporter("[xcor] of turtles"))
  }
  test("randomConst1") {
    expect("_randomconst:10[]")(
      compileReporter("random 10"))
  }
  test("randomConst2") {
    expect("_random[_constdouble:10.5[]]")(
      compileReporter("random 10.5"))
  }
  test("randomConst3") {
    expect("_random[_constdouble:0.0[]]")(
      compileReporter("random 0"))
  }
  test("randomConst4") {
    expect("_random[_constdouble:-5.0[]]")(
      compileReporter("random -5"))
  }
  test("otherWith1") {
    expect("_otherwith[_turtles[], [_constboolean:true[]]]")(
      compileReporter("other (turtles with [true])"))
  }
  test("otherWith2") {
    expect("_otherwith[_turtles[], [_constboolean:true[]]]")(
      compileReporter("(other turtles) with [true]"))
  }
  test("anyOther") {
    expect("_anyother[_turtles[]]")(
      compileReporter("any? other turtles"))
  }
  test("anyOtherwith") {
    expect("_anyotherwith[_turtles[], [_constboolean:true[]]]")(
      compileReporter("any? other turtles with [true]"))
  }
  test("countOther") {
    expect("_countother[_turtles[]]")(
      compileReporter("count other turtles"))
  }
  test("countOtherWith") {
    expect("_countotherwith[_turtles[], [_constboolean:true[]]]")(
      compileReporter("count other turtles with [true]"))
  }
}
