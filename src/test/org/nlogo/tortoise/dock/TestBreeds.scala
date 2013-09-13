// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

class TestBreeds extends DockingSuite {
  test("is breed") { implicit fixture => import fixture._
    testCommand("breed [mice mouse]")
    testCommand("breed [frogs frog]")
    testCommand("globals [glob1]")
    compare("is-frog? nobody")
    compare("is-frog? turtle 0")
    testCommand("create-turtles 1")
    compare("is-frog? turtle 0")
    testCommand("create-frogs 1")
    compare("is-frog? turtle 1")
    compare("is-mouse? turtle 1")
    testCommand("set glob1 turtle 1")
    testCommand("ask glob1 [ die ]")
    compare("is-frog? glob1")
    compare("is-mouse? glob1")
    testCommand("set glob1 55")
    compare("is-frog? glob1")
  }

  test("is link breed") { implicit fixture => import fixture._
    testCommand("undirected-link-breed [undirected-links undirected-link]")
    testCommand("directed-link-breed [directed-links directed-link]")
    testCommand("globals [glob1]")
    compare("is-directed-link? nobody")
    compare("is-directed-link? link 0 1")
    testCommand("crt 2")
    testCommand("ask turtle 0 [ create-directed-link-to turtle 1 ]")
    testCommand("ask turtle 0 [ create-undirected-link-with turtle 1 ]")
    compare("is-directed-link? link 0 1")
    compare("is-directed-link? directed-link 0 1")
    compare("is-undirected-link? directed-link 0 1")
    compare("is-directed-link? undirected-link 0 1")
    compare("is-undirected-link? undirected-link 0 1")
    compare("set glob1 directed-link 0 1")
    compare("is-directed-link? glob1")
    compare("is-undirected-link? glob1")
  }

  test("set breed to not breed") { implicit fixture => import fixture._
    testCommand("breed [frogs frog]")
    testCommand("directed-link-breed [directed-links directed-link]")
    testCommand("crt 1 [ set breed turtles ]")
    testCommand("crt 1 [ set breed frogs ]")
    testCommand("crt 1 [ set breed patches ]")
    testCommand("crt 1 [ set breed turtles with [true] ]")
    testCommand("crt 1 [ set breed links ]")
    testCommand("crt 1 [ set breed directed-links ]")
  }
}
