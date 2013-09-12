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

}
