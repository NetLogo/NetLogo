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

  test("sort") { implicit fixture => import fixture._
    compare("sort [4 2 3 1]")
  }

  test("remove-duplicates") { implicit fixture => import fixture._
    compare("remove-duplicates [1 1 1 2 3 3 4 4 4 1 2 5 5]")
  }

  test("length") { implicit fixture => import fixture._
    compare("length [1 1 1 2 3 3 4 4 4 1 2 5 5]")
    compare("length []")
    compare("length [ who ] of turtles")
    testCommand("cro 10")
    compare("length [ who ] of turtles")
  }
}
