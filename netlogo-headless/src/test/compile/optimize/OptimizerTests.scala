// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.optimize

import org.scalatest.FunSuite
import org.nlogo.compile.api.{ Optimizations, ProcedureDefinition }
import org.nlogo.compile.Scaffold
import org.nlogo.compile.middle.{ AbstractOptimizerTest, Optimizer }

class OptimizerTests extends AbstractOptimizerTest {
  override val OptimizationPrefix = "org.nlogo.compile.optimize"

  test("inRadius1") { new OptTest {
    withReporterOptimization("InRadiusBoundingBox")
    assertResult("_inradiusboundingbox[_turtles[], _constdouble:9001.0[]]")(
      compileReporter("turtles in-radius 9001"))
  } }
  test("inRadius2") { new OptTest {
    withReporterOptimization("InRadiusBoundingBox")
    assertResult("_inradiusboundingbox[_patches[], _constdouble:42.0[]]")(
      compileReporter("patches in-radius 42"))
  } }
  test("inRadius3") { new OptTest {
    withReporterOptimization("InRadiusBoundingBox")
    assertResult("_inradiusboundingbox[_breed:FROGS[], _constdouble:17.0[]]")(
      compileReporter("frogs in-radius 17"))
  } }
  test("inRadius4") { new OptTest {
    withReporterOptimization("InRadiusBoundingBox")
    withReporterOptimization("TurtleVariableDouble", "org.nlogo.compile.middle.optimize")
    assertResult("_inradius[_with[_turtles[], [_equal[_turtlevariabledouble:0[], _constdouble:1.0[]]]], _constdouble:4.0[]]")(
      compileReporter("turtles with [WHO = 1] in-radius 4"))
  } }
}
