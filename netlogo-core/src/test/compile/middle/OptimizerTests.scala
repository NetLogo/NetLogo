// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.compile.api.Optimizations
import org.nlogo.core.Femto
import org.scalatest.funsuite.AnyFunSuite
import org.nlogo.compile.api.{ CommandMunger, ProcedureDefinition, ReporterMunger, RewritingReporterMunger }

trait AbstractOptimizerTest extends AnyFunSuite {
  val OptimizationPrefix = "org.nlogo.compile.middle.optimize"
  val ConstantOptimizer = s"org.nlogo.compile.optimize.Constants"

  // this is a bit of a hack to let gui and headless share code.
  // Headless *requires* the constant optimizations for any other optimizations,
  // GUI doesn't use it at all.
  val optimizeConstants = try {
    Femto.scalaSingleton[RewritingReporterMunger](ConstantOptimizer)
    true
  } catch {
    case e: ClassNotFoundException => false
  }

  trait OptTest {
    private var reporterOptimizations: Seq[ReporterMunger] =
      if (optimizeConstants) Seq(Femto.scalaSingleton[ReporterMunger](ConstantOptimizer)) else Seq()

    private var commandOptimizations: Seq[CommandMunger]  = Seq()

    val SourceHeader = "globals [glob1] breed [frogs frog]"

    def withCommandOptimization(optName: String, optPrefix: String = OptimizationPrefix) =
      commandOptimizations = commandOptimizations :+ Femto.scalaSingleton[CommandMunger](s"$optPrefix.$optName")

    def withReporterOptimization(optName: String, optPrefix: String = OptimizationPrefix) =
      reporterOptimizations = reporterOptimizations :+ Femto.scalaSingleton[ReporterMunger](s"$optPrefix.$optName")

    def designatedOptimizations = Optimizations(commandOptimizations, reporterOptimizations)

    def compileReporter(source: String) =
      compile("to-report __test [x] report " + source + "\nend", designatedOptimizations)
        .statements.stmts.head.args.head.toString

    def compileCommands(source: String) =
      compile("to __test [x] " + source + "\nend", designatedOptimizations)
        .statements.stmts.head.toString

    def compile(source: String, optimizations: Optimizations): ProcedureDefinition = {
      val procdef = Scaffold(SourceHeader + " " + source) match {
        case Seq(p, _) => p
        case _ => throw new IllegalStateException
      }
      procdef.accept(new Optimizer(optimizations))
      procdef
    }
  }
}

class OptimizerTests extends AbstractOptimizerTest {
  test("Forward1") { new OptTest {
    withCommandOptimization("Fd1")
    assertResult("_fd1[]")(compileCommands("fd 1"))
  } }

  test("Forward2") { new OptTest{
    withCommandOptimization("FdLessThan1")
    assertResult("_jump[_constdouble:0.1[]]")(compileCommands("fd 0.1"))
  } }

  test("Forward3") { new OptTest {
    withCommandOptimization("FdLessThan1")
    assertResult("_jump[_constdouble:-0.1[]]")(compileCommands("fd -0.1"))
  } }

  test("Forward4") { new OptTest {
    withCommandOptimization("Fd1")
    withCommandOptimization("FdLessThan1")
    assertResult("_fd[_constdouble:2.0[]]")(compileCommands("fd 2"))
  } }

  test("PatchAt1") { new OptTest {
    withReporterOptimization("PatchAt")
    assertResult("_patchhereinternal[]")(compileReporter("patch-at 0 0"))
  } }
  test("PatchAt2") { new OptTest {
    withReporterOptimization("PatchAt")
    assertResult("_patcheast[]")(compileReporter("patch-at 1 0"))
  } }
  test("PatchAt3") { new OptTest {
    withReporterOptimization("PatchAt")
    assertResult("_patchsw[]")(compileReporter("patch-at -1 -1"))
  } }

// The PatchColumnN and PatchRowN tests are for 2D optimizations, not 3D
// AAB Feb-21-2020
  if(!org.nlogo.api.Version.is3D) {
    test("PatchColumn1") { new OptTest {
      withReporterOptimization("PatchVariableDouble")
      withReporterOptimization("With")
      assertResult("_patchcol[_constdouble:5.0[]]")(
        compileReporter("patches with [pxcor = 5]"))
    } }
    test("PatchColumn2") { new OptTest {
      withReporterOptimization("PatchVariableDouble")
      withReporterOptimization("With")
      assertResult("_patchcol[_constdouble:5.0[]]")(
        compileReporter("patches with [5 = pxcor]"))
    } }
    test("PatchColumn3") { new OptTest {
      withReporterOptimization("PatchVariableDouble")
      withReporterOptimization("With")
      assertResult("_patchcol[_observervariable:0[]]")(
        compileReporter("patches with [pxcor = glob1]"))
    } }
    test("PatchColumn4") { new OptTest {
      withReporterOptimization("PatchVariableDouble")
      withReporterOptimization("With")
      assertResult("_patchcol[_procedurevariable:X[]]")(
        compileReporter("patches with [pxcor = x]"))
    } }

    test("PatchRow1") { new OptTest {
      withReporterOptimization("PatchVariableDouble")
      withReporterOptimization("With")
      assertResult("_patchrow[_constdouble:6.0[]]")(
        compileReporter("patches with [pycor = 6]"))
    } }
    test("PatchRow2") { new OptTest {
      withReporterOptimization("PatchVariableDouble")
      withReporterOptimization("With")
      assertResult("_patchrow[_constdouble:6.0[]]")(
        compileReporter("patches with [6 = pycor]"))
    } }
    test("PatchRow3") { new OptTest {
      withReporterOptimization("PatchVariableDouble")
      withReporterOptimization("With")
      assertResult("_patchrow[_observervariable:0[]]")(
        compileReporter("patches with [pycor = glob1]"))
    } }
    test("PatchRow4") { new OptTest {
      withReporterOptimization("PatchVariableDouble")
      withReporterOptimization("With")
      assertResult("_patchrow[_procedurevariable:X[]]")(
        compileReporter("patches with [pycor = x]"))
    } }
  }
  test("HatchFast1") { new OptTest {
    withCommandOptimization("HatchFast")
    assertResult("_hatchfast:[_constdouble:5.0[]]")(
      compileCommands("hatch 5"))
  } }
  test("HatchFast2") { new OptTest {
    withCommandOptimization("HatchFast")
    assertResult("_hatchfast:FROGS[_constdouble:5.0[]]")(
      compileCommands("hatch-frogs 5"))
  } }
  test("SproutFast1") { new OptTest {
    withCommandOptimization("SproutFast")
    assertResult("_sproutfast:[_constdouble:5.0[]]")(
      compileCommands("sprout 5"))
  } }
  test("SproutFast2") { new OptTest {
    withCommandOptimization("SproutFast")
    assertResult("_sproutfast:FROGS[_constdouble:5.0[]]")(
      compileCommands("sprout-frogs 5"))
  } }
  test("FastCrt1") { new OptTest {
    withCommandOptimization("CrtFast")
    assertResult("_crtfast:[_constdouble:5.0[]]")(
      compileCommands("crt 5"))
  } }
  test("FastCrt2") { new OptTest {
    withCommandOptimization("CrtFast")
    assertResult("_crtfast:FROGS[_constdouble:5.0[]]")(
      compileCommands("create-frogs 5"))
  } }
  test("FastCro1") { new OptTest {
    withCommandOptimization("CroFast")
    assertResult("_crofast:[_constdouble:5.0[]]")(
      compileCommands("cro 5"))
  } }
  test("FastCro2") { new OptTest {
    withCommandOptimization("CroFast")
    assertResult("_crofast:FROGS[_constdouble:5.0[]]")(
      compileCommands("create-ordered-frogs 5"))
  } }
  test("countWith1") { new OptTest {
    withReporterOptimization("CountWith")
    assertResult("_countwith[_turtles[], [_constboolean:true[]]]")(
      compileReporter("count turtles with [true]"))
  } }
  test("countWith2") { new OptTest {
    withReporterOptimization("CountWith")
    assertResult("_countwith[_patches[], [_equal[_patchvariable:2[], _constdouble:15.0[]]]]")(
      compileReporter("count patches with [pcolor = red]"))
  } }
  test("oneOfWith") { new OptTest {
    withReporterOptimization("OneOfWith")
    assertResult("_oneofwith[_patches[], [_equal[_patchvariable:2[], _constdouble:15.0[]]]]")(
      compileReporter("one-of patches with [pcolor = red]"))
  } }
  test("anyWith1") { new OptTest {
    withReporterOptimization("CountWith")
    withReporterOptimization("AnyWith5")
    assertResult("_not[_anywith[_patches[], [_constboolean:true[]]]]")(
      compileReporter("count patches with [true] = 0"))
  } }
  test("anyWith2") { new OptTest {
    withReporterOptimization("CountWith")
    withReporterOptimization("AnyWith5")
    assertResult("_not[_anywith[_patches[], [_constboolean:true[]]]]")(
      compileReporter("0 = count patches with [true]"))
  } }
  test("anyWith3") { new OptTest {
    withReporterOptimization("CountWith")
    withReporterOptimization("AnyWith4")
    assertResult("_anywith[_patches[], [_constboolean:true[]]]")(
      compileReporter("0 < count patches with [true]"))
  } }
  test("anyWith4") { new OptTest {
    withReporterOptimization("CountWith")
    withReporterOptimization("AnyWith3")
    assertResult("_anywith[_patches[], [_constboolean:true[]]]")(
      compileReporter("count patches with [true] > 0"))
  } }
  test("anyWith5") { new OptTest {
    withReporterOptimization("CountWith")
    withReporterOptimization("AnyWith2")
    assertResult("_anywith[_patches[], [_constboolean:true[]]]")(
      compileReporter("count patches with [true] != 0"))
  } }
  test("anyWith6") { new OptTest {
    withReporterOptimization("CountWith")
    withReporterOptimization("AnyWith2")
    assertResult("_anywith[_patches[], [_constboolean:true[]]]")(
      compileReporter("0 != count patches with [true]"))
  } }
  test("anyWith7") { new OptTest {
    withReporterOptimization("AnyWith1")
    assertResult("_anywith[_patches[], [_constboolean:true[]]]")(
      compileReporter("any? patches with [true]"))
  } }
  test("patchVariable1") { new OptTest {
    withReporterOptimization("PatchVariableDouble")
    assertResult("_of[[_patchvariable:3[]], _patches[]]")(
      compileReporter("[plabel] of patches"))
  } }
  test("patchVariable2") { new OptTest {
    withReporterOptimization("PatchVariableDouble")
    assertResult("_of[[_patchvariabledouble:0[]], _patches[]]")(
      compileReporter("[pxcor] of patches"))
  } }

  test("turtleVariable1") { new OptTest {
    withReporterOptimization("TurtleVariableDouble")
    assertResult("_of[[_turtlevariable:12[]], _turtles[]]")(
      compileReporter("[pen-mode] of turtles"))
  } }
  test("turtleVariable2") { new OptTest {
    withReporterOptimization("TurtleVariableDouble")
    assertResult("_of[[_turtlevariabledouble:3[]], _turtles[]]")(
      compileReporter("[xcor] of turtles"))
  } }
  test("randomConst1") { new OptTest {
    withReporterOptimization("RandomConst")
    assertResult("_randomconst:10[]")(compileReporter("random 10"))
  } }
  test("randomConst2") { new OptTest {
    withReporterOptimization("RandomConst")
    assertResult("_random[_constdouble:10.5[]]")(compileReporter("random 10.5"))
  } }
  test("randomConst3") { new OptTest {
    withReporterOptimization("RandomConst")
    assertResult("_random[_constdouble:0.0[]]")(compileReporter("random 0"))
  } }
  test("randomConst4") { new OptTest {
    withReporterOptimization("RandomConst")
    assertResult("_random[_constdouble:-5.0[]]")(compileReporter("random -5"))
  } }
  test("otherWith1") { new OptTest {
    withReporterOptimization("OtherWith")
    assertResult("_otherwith[_turtles[], [_constboolean:true[]]]")(
      compileReporter("other (turtles with [true])"))
  } }
  test("otherWith2") { new OptTest {
    withReporterOptimization("WithOther")
    assertResult("_otherwith[_turtles[], [_constboolean:true[]]]")(
      compileReporter("(other turtles) with [true]"))
  } }
  test("anyOther") { new OptTest {
    withReporterOptimization("AnyOther")
    assertResult("_anyother[_turtles[]]")(compileReporter("any? other turtles"))
  } }
  test("anyOtherwith") { new OptTest {
    withReporterOptimization("OtherWith")
    withReporterOptimization("AnyOtherWith")
    assertResult("_anyotherwith[_turtles[], [_constboolean:true[]]]")(
      compileReporter("any? other turtles with [true]"))
  } }
  test("anyTurtlesOn1") { new OptTest {
    withReporterOptimization("AnyTurtlesOn")
    assertResult("_anyturtleson[_patches[]]")(
      compileReporter("any? turtles-on patches"))
  } }
  test("anyTurtlesOn2") { new OptTest {
    withReporterOptimization("AnyTurtlesOn")
    assertResult("_anyturtleson[_turtles[]]")(compileReporter("any? turtles-on turtles"))
  } }
  test("anyBreedOn1") { new OptTest {
    withReporterOptimization("AnyBreedOn")
    assertResult("_anybreedon:FROGS[_patches[]]")(
      compileReporter("any? frogs-on patches"))
  } }
  test("anyBreedOn2") { new OptTest {
    withReporterOptimization("AnyBreedOn")
    assertResult("_anybreedon:FROGS[_turtles[]]")(compileReporter("any? frogs-on turtles"))
  } }
  test("countOther") { new OptTest {
    withReporterOptimization("CountOther")
    assertResult("_countother[_turtles[]]")(
      compileReporter("count other turtles"))
  } }
  test("countOtherWith") { new OptTest {
    withReporterOptimization("OtherWith")
    withReporterOptimization("CountOtherWith")
    assertResult("_countotherwith[_turtles[], [_constboolean:true[]]]")(
      compileReporter("count other turtles with [true]"))
  } }
  test("hasEqual1") { new OptTest {
    withReporterOptimization("HasEqual")
    assertResult("_optimizecount[_patches[], _constdouble:10.0[]]")(
      compileReporter("count patches = 10"))
  } }
  test("hasEqual2") { new OptTest {
    withReporterOptimization("HasEqual")
    assertResult("_optimizecount[_patches[], _constdouble:10.0[]]")(
      compileReporter("10 = count patches"))
  } }
  test("hasGreaterThan1") { new OptTest {
    withReporterOptimization("HasGreaterThan")
    assertResult("_optimizecount[_patches[], _constdouble:10.0[]]")(
      compileReporter("count patches > 10"))
  } }
  test("hasGreaterThan2") { new OptTest {
    withReporterOptimization("HasGreaterThan")
    assertResult("_optimizecount[_patches[], _constdouble:10.0[]]")(
      compileReporter("10 > count patches"))
  } }
  test("hasLessThan1") { new OptTest {
    withReporterOptimization("HasLessThan")
    assertResult("_optimizecount[_patches[], _constdouble:10.0[]]")(
      compileReporter("count patches < 10"))
  } }
  test("hasLessThan2") { new OptTest {
    withReporterOptimization("HasLessThan")
    assertResult("_optimizecount[_patches[], _constdouble:10.0[]]")(
      compileReporter("10 < count patches"))
  } }
  test("hasNotEqual1") { new OptTest {
    withReporterOptimization("HasNotEqual")
    assertResult("_optimizecount[_patches[], _constdouble:10.0[]]")(
      compileReporter("count patches != 10"))
  } }
  test("hasNotEqual2") { new OptTest {
    withReporterOptimization("HasNotEqual")
    assertResult("_optimizecount[_patches[], _constdouble:10.0[]]")(
      compileReporter("10 != count patches"))
  } }
}
