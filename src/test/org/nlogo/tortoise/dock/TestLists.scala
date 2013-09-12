// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

class TestLists extends DockingSuite {

  test("list") { implicit fixture => import fixture._
    compare("(list)")
    compare("(list 1)")
    compare("list 1 2")
    compare("(list 1 2)")
    compare("(list 1 2 3)")
  }

  test("max, min") { implicit fixture => import fixture._
    compare("max [1 2 3]")
    compare("max [3 2 1]")
    compare("min [1 2 3]")
    compare("min [3 2 1]")
  }

}
