// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

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

}
