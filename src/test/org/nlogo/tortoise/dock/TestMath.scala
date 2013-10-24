// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.api

class TestMath extends DockingSuite {

  test("sin") { implicit fixture => import fixture._
    for(theta <- -360 to 360 by 5)
      compare(s"sin $theta")
  }

  test("cos") { implicit fixture => import fixture._
    for(theta <- -360 to 360 by 5)
      compare(s"cos $theta")
  }

  test("unary minus") { implicit fixture => import fixture._
    declare("globals [g]")
    testCommand("set g 5")
    compare("(- g)")
    compare("(- (- g))")
  }

  test("mean") { implicit fixture => import fixture._
    compare("mean [1]")
    compare("mean [1 4 9 16]")
  }

  test("mod") { implicit fixture => import fixture._
    declare("globals [g]")
    testCommand("set g 5")
    compare("g mod 3")
    // TODO compare("(- g) mod 3")
    // TODO compare("g mod -3")
  }

  test("sum") { implicit fixture => import fixture._
    declare("to-report compute report sum [pycor] of neighbors end",
      api.WorldDimensions.square(1))
    // first make sure just doing the computation doesn't make the RNG diverge
    testCommand("ask patches [ let s compute ]")
    // might as well check the result too
    testCommand("ask patches [ output-print compute ]")
  }

}
