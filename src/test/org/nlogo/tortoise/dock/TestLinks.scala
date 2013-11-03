// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

class TestLinks extends DockingSuite {
  test("Links1") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [ create-link-to turtle 1 ]")
    testCommand("output-print link 0 1")
    testCommand("output-print link 1 0")
    compare("[(list [who] of end1 [who] of end2 )] of link 0 1")
    testCommand("output-print [color] of link 0 1")
    compare("[color] of link 0 1")
    compare("[end1] of link 0 1 = turtle 0")
    compare("[end2] of link 0 1 = turtle 1")
    compare("[label] of link 0 1 = \"\"")
    compare("[label-color] of link 0 1")
    compare("[hidden?] of link 0 1")
    compare("[shape] of link 0 1")
  }

  test("Links2") { implicit fixture => import fixture._
    testCommand("crt 4 [ create-links-with turtles with [ who > [who] of myself ] ]")
    testCommand("ask links [ output-print self ]")
    testCommand("ask link 0 1 [ set color red ]")
    compare("[color] of link 0 1")
    testCommand("ask link 1 3 [ set color 40 ]")
    compare("[color] of link 1 3")
    testCommand("ask link 1 2 [ set color 30 ]")
    compare("[color] of link 1 2")
  }

  test("LinkWithBadEndPointsReturnsNobody") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [ create-link-with turtle 1 ]")
    testCommand("output-print link 0 2")
    testCommand("output-print link 2 0")
    testCommand("output-print link 2 2")
    testCommand("output-print link 2 3")
    testCommand("output-print link 3 2")
  }

  test("LinksInitBlock") { implicit fixture => import fixture._
    declare("globals [glob1]")
    testCommand("set glob1 0")
    testCommand("crt 5")
    testCommand("ask turtles [ create-links-with other turtles [ set glob1 glob1 + 1 ] ]")
    compare("count links = glob1")
    testCommand("ask links [ die ]")
    testCommand("set glob1 0")
    testCommand("ask turtles [ create-links-to other turtles [ set glob1 glob1 + 1 ] ]")
    compare("count links = glob1")
    testCommand("ask links [ die ]")
    testCommand("set glob1 0")
    testCommand("ask turtles [ create-links-from other turtles [ set glob1 glob1 + 1 ] ]")
    compare("count links = glob1")
  }

  test("CreateDuplicateLinks") { implicit fixture => import fixture._
    testCommand("crt 2 [ create-links-with other turtles ]")
    compare("count links = 1")
    testCommand("ask turtle 0 [ create-link-with turtle 1 ]")
    compare("count links = 1")
    testCommand("ca")
    testCommand("crt 3 [ create-links-to other turtles ]")
    compare("count links = 6")
    testCommand("ask turtle 0 [ create-link-to turtle 1 ]")
    compare("count links = 6")
    testCommand("ca")
    testCommand("crt 3 [ create-links-from other turtles ]")
    compare("count links = 6")
    testCommand("ask turtle 0 [ create-link-from turtle 1 ]")
    compare("count links = 6")
    testCommand("ca")
    testCommand("crt 2")
    testCommand("ask turtle 0 [ create-link-with turtle 1 ]")
    compare("count [link-neighbors] of turtle 0")
    compare("count [link-neighbors] of turtle 1")
    testCommand("ask turtle 1 [ create-link-with turtle 0 ]")
    compare("count [link-neighbors] of turtle 0")
    compare("count [link-neighbors] of turtle 1")
  }

  test("LinkedTest1") { implicit fixture => import fixture._
    testCommand("crt 2")
    compare("[in-link-neighbor? turtle 1] of turtle 0")
    compare("[in-link-neighbor? turtle 0] of turtle 1")
    compare("[out-link-neighbor? turtle 1] of turtle 0")
    compare("[out-link-neighbor? turtle 0] of turtle 1")
    compare("[link-neighbor? turtle 1] of turtle 0")
    compare("[link-neighbor? turtle 0] of turtle 1")
    testCommand("ask turtle 0 [ create-link-to turtle 1]")
    compare("[in-link-neighbor? turtle 1] of turtle 0")
    compare("[in-link-neighbor? turtle 0] of turtle 1")
    compare("[out-link-neighbor? turtle 1] of turtle 0")
    compare("[out-link-neighbor? turtle 0] of turtle 1")
  }

  test("LinkedTest2") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0[ create-link-from turtle 1]")
    compare("[in-link-neighbor? turtle 1] of turtle 0")
    compare("[in-link-neighbor? turtle 0] of turtle 1")
    compare("[out-link-neighbor? turtle 1] of turtle 0")
    compare("[out-link-neighbor? turtle 0] of turtle 1")
  }

  test("LinkedTest3") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0[ create-link-with turtle 1]")
    compare("[link-neighbor? turtle 1] of turtle 0")
    compare("[link-neighbor? turtle 0] of turtle 1")
  }

  test("NodeDies1") { implicit fixture => import fixture._
    declare("breed [nodes node]")
    testCommand("create-nodes 2")
    testCommand("ask turtle 0 [create-link-to turtle 1]")
    compare("count nodes")
    compare("count links")
    testCommand("ask turtle 0 [die]")
    compare("count nodes")
    compare("count links")
  }

  test("NodeDies2") { implicit fixture => import fixture._
    declare("breed [nodes node]")
    testCommand("create-nodes 2")
    testCommand("ask turtle 0 [create-link-from turtle 1]")
    compare("count nodes")
    compare("count links")
    testCommand("ask turtle 0 [die]")
    compare("count nodes")
    compare("count links")
  }

  test("NodeDies3") { implicit fixture => import fixture._
    declare("breed [nodes node]")
    testCommand("create-nodes 2")
    testCommand("ask turtle 0 [create-link-with turtle 1]")
    compare("count nodes")
    compare("count links")
    testCommand("ask turtle 0 [die]")
    compare("count nodes")
    compare("count links")
  }

  test("LinkDies1") { implicit fixture => import fixture._
    declare("breed [nodes node]")
    testCommand("create-nodes 2")
    testCommand("ask turtle 0 [create-link-to turtle 1]")
    testCommand("ask link 0 1 [die]")
    compare("count nodes")
    compare("count links")
    compare("[ out-link-neighbor? turtle 1] of turtle 0")
  }

  test("LinkDies2") { implicit fixture => import fixture._
    declare("breed [nodes node]")
    testCommand("create-nodes 2")
    testCommand("ask turtle 0 [create-link-from turtle 1]")
    testCommand("ask link 1 0 [die]")
    compare("count nodes")
    compare("count links")
    compare("[ in-link-neighbor? turtle 1] of turtle 0")
  }

  test("LinkDies3") { implicit fixture => import fixture._
    declare("breed [nodes node]")
    testCommand("create-nodes 2")
    testCommand("ask turtle 0 [create-link-with turtle 1]")
    testCommand("ask link 0 1 [die]")
    compare("count nodes")
    compare("count links")
    compare("[ link-neighbor? turtle 1] of turtle 0")
  }

  test("LinkDestTest1") { implicit fixture => import fixture._
    declare("breed [nodes node]")
    testCommand("create-nodes 2")
    testCommand("ask turtle 0 [create-link-to turtle 1]")
    compare("[ end1 ] of link 0 1 = turtle 0")
    compare("[ end2 ] of link 0 1 = turtle 1")
    testCommand("ask link 0 1 [die]")
    testCommand("ask turtle 0 [create-link-from turtle 1]")
    compare("[ end1 ] of link 1 0 = turtle 1")
    compare("[ end2 ] of link 1 0 = turtle 0")
  }

  test("LinkDestTest2") { implicit fixture => import fixture._
    declare("breed [nodes node]")
    testCommand("create-nodes 2")
    testCommand("ask turtle 0 [create-link-with turtle 1]")
    compare("[ end1 ] of link 0 1 = turtle 0")
    compare("[ end2 ] of link 0 1 = turtle 1")
  }

  test("BothEnds1") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [create-link-to turtle 1]")
    compare("[sort [who] of both-ends] of link 0 1")
  }

  test("BothEnds2") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [create-link-from turtle 1]")
    compare("[sort [who] of both-ends] of link 1 0")
  }

  test("BothEnds3") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [create-link-with turtle 1]")
    compare("[sort [who] of both-ends] of link 0 1")
  }

  test("OtherEnd1") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [create-link-to turtle 1]")
    compare("[[[who] of other-end] of link 0 1] of turtle 0")
    compare("[[[who] of other-end] of link 0 1] of turtle 1")
    compare("[[[who] of other-end] of turtle 0] of link 0 1")
    compare("[[[who] of other-end] of turtle 1] of link 0 1")
  }

  test("OtherEnd2") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [create-link-from turtle 1]")
    compare("[[[who] of other-end] of link 1 0] of turtle 0")
    compare("[[[who] of other-end] of link 1 0] of turtle 1")
    compare("[[[who] of other-end] of turtle 0] of link 1 0")
    compare("[[[who] of other-end] of turtle 1] of link 1 0")
  }

  test("OtherEnd3") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [create-link-with turtle 1]")
    compare("[[[who] of other-end] of link 0 1] of turtle 0")
    compare("[[[who] of other-end] of link 0 1] of turtle 1")
    compare("[[[who] of other-end] of turtle 0] of link 0 1")
    compare("[[[who] of other-end] of turtle 1] of link 0 1")
  }

  test("IsLink1") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [create-link-to turtle 1]")
    testCommand("ask turtle 0 [create-link-from turtle 1]")
    compare("is-link? turtle 0")
    compare("is-link? turtle 1")
    compare("is-link? link 0 1")
    compare("is-link? link 1 0")
  }

  test("IsLink2") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [create-link-with turtle 1]")
    compare("is-link? turtle 0")
    compare("is-link? turtle 1")
    compare("is-link? link 0 1")
  }

  test("LinkKillsNode1") { implicit fixture => import fixture._
    declare("breed [nodes node]")
    testCommand("create-nodes 2")
    compare("count nodes")
    compare("count links")
    testCommand("ask turtle 0 [create-link-to turtle 1 [ask myself [die]]]")
    compare("count nodes")
    compare("count links")
  }

  test("LinkKillsNode2") { implicit fixture => import fixture._
    declare("breed [nodes node]")
    testCommand("create-nodes 2")
    compare("count nodes")
    compare("count links")
    testCommand("ask turtle 0 [create-link-from turtle 1 [ask myself [die]]]")
    compare("count nodes")
    compare("count links")
  }

  test("LinkKillsNode3") { implicit fixture => import fixture._
    declare("breed [nodes node]")
    testCommand("create-nodes 2")
    compare("count nodes")
    compare("count links")
    testCommand("ask turtle 0 [create-link-with turtle 1 [ask myself [die]]]")
    compare("count nodes")
    compare("count links")
  }

  test("LinkFromToWith1") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("output-print [in-link-from turtle 1] of turtle 0")
    testCommand("output-print [out-link-to turtle 1] of turtle 0")
    testCommand("output-print [in-link-from turtle 0] of turtle 1")
    testCommand("output-print [out-link-to turtle 0] of turtle 1")
    testCommand("output-print [link-with turtle 1] of turtle 0")
    testCommand("output-print [link-with turtle 0] of turtle 1")
    testCommand("ask turtle 0 [ create-link-to turtle 1 ]")
    testCommand("output-print [in-link-from turtle 1] of turtle 0")
    testCommand("output-print [(list [who] of end1 [who] of end2 )] of [out-link-to turtle 1] of turtle 0")
    testCommand("output-print [(list [who] of end1 [who] of end2 )] of [in-link-from turtle 0] of turtle 1")
    testCommand("output-print [out-link-to turtle 0] of turtle 1")
    testCommand("output-print [link-with turtle 1] of turtle 0")
    testCommand("output-print [link-with turtle 0] of turtle 1")
  }

  test("LinkFromToWith2") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [create-link-from turtle 1]")
    compare("[(list [who] of end1 [who] of end2 )] of [in-link-from turtle 1] of turtle 0")
    testCommand("output-print [out-link-to turtle 1] of turtle 0")
    testCommand("output-print [in-link-from turtle 0] of turtle 1")
    compare("[(list [who] of end1 [who] of end2 )] of [out-link-to turtle 0] of turtle 1")
  }

  test("LinkFromToWith3") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [create-link-with turtle 1]")
    compare("[(list [who] of end1 [who] of end2 )] of [link-with turtle 1] of turtle 0")
    compare("[(list [who] of end1 [who] of end2 )] of [link-with turtle 0] of turtle 1")
  }

  test("LinkedFromToWith1") { implicit fixture => import fixture._
    testCommand("crt 2")
    compare("sort [who] of [in-link-neighbors] of turtle 0")
    compare("sort [who] of [out-link-neighbors] of turtle 0")
    compare("sort [who] of [in-link-neighbors ] of turtle 1")
    compare("sort [who] of [out-link-neighbors ] of turtle 1")
    compare("sort [who] of [link-neighbors ] of turtle 0")
    compare("sort [who] of [link-neighbors ] of turtle 1")
    testCommand("ask turtle 0 [create-link-to turtle 1]")
    compare("sort [who] of [in-link-neighbors ] of turtle 0")
    compare("sort [who] of [out-link-neighbors ] of turtle 0")
    compare("sort [who] of [in-link-neighbors ] of turtle 1")
    compare("sort [who] of [out-link-neighbors ] of turtle 1")
  }

  test("LinkedFromToWith2") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [create-link-from turtle 1]")
    compare("sort [who] of [in-link-neighbors] of turtle 1")
    compare("sort [who] of [out-link-neighbors ] of turtle 1")
    compare("sort [who] of [in-link-neighbors] of turtle 0")
    compare("sort [who] of [out-link-neighbors ] of turtle 0")
  }

  test("LinkedFromToWith3") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [create-link-with turtle 1]")
    compare("sort [who] of [link-neighbors ] of turtle 1")
    compare("sort [who] of [link-neighbors ] of turtle 0")
  }

  test("LinkKillsItself1") { implicit fixture => import fixture._
    declare("breed [nodes node]")
    testCommand("create-nodes 2")
    testCommand("ask turtle 0 [create-link-to turtle 1 [die]]")
    compare("count nodes")
    compare("count links")
    testCommand("output-print [in-link-from turtle 1] of turtle 0")
    testCommand("output-print [out-link-to turtle 1] of turtle 0")
    testCommand("output-print [in-link-from turtle 0] of turtle 1")
    testCommand("output-print [out-link-to turtle 0] of turtle 1")
    compare("sort [who] of [in-link-neighbors] of turtle 0")
    compare("sort [who] of [out-link-neighbors] of turtle 0")
    compare("sort [who] of [in-link-neighbors ] of turtle 1")
    compare("sort [who] of [out-link-neighbors ] of turtle 1")
    compare("sort [who] of [my-in-links] of turtle 0")
    compare("sort [who] of [my-out-links] of turtle 0")
    compare("sort [who] of [my-in-links ] of turtle 1")
    compare("sort [who] of [my-out-links ] of turtle 1")
  }

  test("LinkKillsItself2") { implicit fixture => import fixture._
    declare("breed [nodes node]")
    testCommand("create-nodes 2")
    testCommand("ask turtle 0 [create-link-from turtle 1 [die]]")
    compare("count nodes")
    compare("count links")
    testCommand("output-print [in-link-from turtle 1] of turtle 0")
    testCommand("output-print [out-link-to turtle 1] of turtle 0")
    testCommand("output-print [in-link-from turtle 0] of turtle 1")
    testCommand("output-print [out-link-to turtle 0] of turtle 1")
    compare("sort [who] of [in-link-neighbors] of turtle 0")
    compare("sort [who] of [out-link-neighbors] of turtle 0")
    compare("sort [who] of [in-link-neighbors ] of turtle 1")
    compare("sort [who] of [out-link-neighbors ] of turtle 1")
    compare("sort [who] of [my-in-links] of turtle 0")
    compare("sort [who] of [my-out-links] of turtle 0")
    compare("sort [who] of [my-in-links ] of turtle 1")
    compare("sort [who] of [my-out-links ] of turtle 1")
  }

  test("LinkKillsItself3") { implicit fixture => import fixture._
    declare("breed [nodes node]")
    testCommand("create-nodes 2")
    testCommand("ask turtle 0 [create-link-with turtle 1 [die]]")
    compare("count nodes")
    compare("count links")
    testCommand("output-print [link-with turtle 1] of turtle 0")
    testCommand("output-print [link-with turtle 0] of turtle 1")
    compare("sort [who] of [link-neighbors] of turtle 0")
    compare("sort [who] of [link-neighbors ] of turtle 1")
    compare("sort [who] of [my-links] of turtle 0")
    compare("sort [who] of [my-links ] of turtle 1")
  }

  test("LinkKillsParents1") { implicit fixture => import fixture._
    testCommand("crt 2 [ create-links-with turtles with [ who > [who] of myself  ] [ ask both-ends [die] ] ]")
    compare("count links = 0")
    compare("count turtles = 0")
    //testCommand("crt 2 [ create-links-with turtles with [ who > [who] of myself  ] [ ask end1 [die] ask end2 [die ] ] ]")
    //compare("count links = 0")
    //compare("count turtles = 1")
  }

  test("LinkKillsParents2") { implicit fixture => import fixture._
    testCommand("crt 10")
    testCommand("ask turtles [ create-links-with other turtles [ ask turtles with [true] [ die ] ] ]")
    compare("count turtles")
    compare("count links")
  }

  test("SwitchDirectednessOfUnbreededLinks1") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [ create-link-to turtle 1 ]")
    testCommand("ask turtle 1 [ die ]")
    testCommand("crt 1")
    testCommand("ask turtle 0 [ create-link-with turtle 2 ]")
    compare("count turtles")
    compare("count links")
  }

  test("SwitchDirectednessOfUnbreededLinks2") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [ create-link-with turtle 1 ]")
    testCommand("ask turtle 1 [ die ]")
    testCommand("crt 1")
    testCommand("ask turtle 0 [ create-link-to turtle 2 ]")
    compare("count turtles")
    compare("count links")
  }

  test("RemoveFrom") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [create-link-from turtle 1]")
    testCommand("ask turtle 0 [ask link 1 0 [ die ] ]")
    testCommand("output-print [in-link-from turtle 1] of turtle 0")
    testCommand("output-print [out-link-to turtle 1] of turtle 0")
    testCommand("output-print [in-link-from turtle 0] of turtle 1")
    testCommand("output-print [out-link-to turtle 0] of turtle 1")
    compare("sort [who] of [in-link-neighbors] of turtle 0")
    compare("sort [who] of [out-link-neighbors] of turtle 0")
    compare("sort [who] of [in-link-neighbors ] of turtle 1")
    compare("sort [who] of [out-link-neighbors ] of turtle 1")
    compare("sort [who] of [my-in-links] of turtle 0")
    compare("sort [who] of [my-out-links] of turtle 0")
    compare("sort [who] of [my-in-links ] of turtle 1")
  }

  test("RemoveTo") { implicit fixture => import fixture._
    testCommand("crt 2")
    testCommand("ask turtle 0 [create-link-to turtle 1]")
    testCommand("ask turtle 0 [ask link 0 1 [die]]")
    testCommand("output-print [in-link-from turtle 1] of turtle 0")
    testCommand("output-print [out-link-to turtle 1] of turtle 0")
    testCommand("output-print [in-link-from turtle 0] of turtle 1")
    testCommand("output-print [out-link-to turtle 0] of turtle 1")
    compare("sort [who] of [in-link-neighbors] of turtle 0")
    compare("sort [who] of [out-link-neighbors] of turtle 0")
    compare("sort [who] of [in-link-neighbors ] of turtle 1")
    compare("sort [who] of [out-link-neighbors ] of turtle 1")
  }
}
