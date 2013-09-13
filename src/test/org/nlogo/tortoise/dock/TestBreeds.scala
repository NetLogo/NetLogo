// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

class TestBreeds extends DockingSuite {
  test("create breeds") { implicit fixture => import fixture._
    declare("""| breed [mice mouse]
               | breed [frogs frog]""".stripMargin)
  }

  test("is breed") { implicit fixture => import fixture._
    declare("""| breed [mice mouse]
               | breed [frogs frog]
               | globals [glob1]""".stripMargin)
    testCommand("output-print is-frog? nobody")
    testCommand("output-print is-frog? turtle 0")
    testCommand("create-turtles 1")
    testCommand("output-print is-frog? turtle 0")
    testCommand("create-frogs 1")
    testCommand("is-frog? turtle 1")
    //compare("is-mouse? turtle 1")
    //testCommand("set glob1 turtle 1")
    //testCommand("ask glob1 [ die ]")
    //compare("is-frog? glob1")
    //compare("is-mouse? glob1")
    //testCommand("set glob1 55")
    //compare("is-frog? glob1")
  }
}
