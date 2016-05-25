// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

import org.nlogo.agent.SimpleChangeEventCounter
import org.nlogo.agent.TreeAgentSet
import org.scalatest.FunSuite
import org.scalatest.GivenWhenThen

class TestTreeAgentSet extends FixtureSuite with GivenWhenThen {

  test("TreeAgentSet should trigger SimpleChangeEvent") { implicit fixture =>

    import fixture.{workspace => ws}
    import scala.language.implicitConversions
    implicit def anyToPub(agentSet: org.nlogo.agent.AgentSet) =
      agentSet.asInstanceOf[TreeAgentSet].simpleChangeEventPublisher

    fixture.declare(
      """|breed [mice mouse]
         |breed [frogs frog]
         |undirected-link-breed [undirected-edges undirected-edge]
         |""".stripMargin)

    Given("a subscriber to turtles")
    val turtlesSub = new SimpleChangeEventCounter(ws.world.turtles)
    And("a subscriber to mice")
    val miceSub = new SimpleChangeEventCounter(ws.world.breedAgents.get("MICE"))
    And("a subscriber to frogs")
    val frogSub = new SimpleChangeEventCounter(ws.world.breedAgents.get("FROGS"))
    And("a subscriber to links")
    val linksSub = new SimpleChangeEventCounter(ws.world.links)
    And("a subscriber to undirected-links")
    val undirLinksSub = new SimpleChangeEventCounter(ws.world.linkBreedAgents.get("UNDIRECTED-EDGES"))

    val allSubs = Seq(turtlesSub, miceSub, frogSub, linksSub, undirLinksSub)

    When("creating a new turtle")
    ws.command("crt 1")
    Then("the turtles subscriber should get an event")
    assertResult(1)(turtlesSub.eventCount)
    And("other subscribers should get none")
    for (s <- allSubs if s != turtlesSub) assertResult(0)(s.eventCount)

    allSubs.foreach(_.eventCount = 0)

    When("creating two other turtles")
    ws.command("crt 2")
    Then("the turtles subscriber should two events")
    assertResult(2)(turtlesSub.eventCount)
    And("other subscribers should get none")
    for (s <- allSubs if s != turtlesSub) assertResult(0)(s.eventCount)

    allSubs.foreach(_.eventCount = 0)

    When("creating a new mouse")
    ws.command("create-mice 1")
    Then("the turtles subscriber should get an event")
    assertResult(1)(turtlesSub.eventCount)
    And("the mice subscriber should get an event")
    assertResult(1)(miceSub.eventCount)
    And("other subscribers should get none")
    for (s <- allSubs if !Set(turtlesSub, miceSub).contains(s)) assertResult(0)(s.eventCount)

    allSubs.foreach(_.eventCount = 0)

    When("creating two other mice")
    ws.command("create-mice 2")
    Then("the turtles subscriber should get two events")
    assertResult(2)(turtlesSub.eventCount)
    And("the mice subscriber should get two events")
    assertResult(2)(miceSub.eventCount)
    And("other subscribers should get none")
    for (s <- allSubs if !Set(turtlesSub, miceSub).contains(s)) assertResult(0)(s.eventCount)

    allSubs.foreach(_.eventCount = 0)

    When("killing one of the three mice")
    ws.command("ask one-of mice [ die ]")
    Then("the turtles subscriber should get an event")
    assertResult(1)(turtlesSub.eventCount)
    And("the mice subscriber should get an event")
    assertResult(1)(miceSub.eventCount)
    And("other subscribers should get none")
    for (s <- allSubs if !Set(turtlesSub, miceSub).contains(s)) assertResult(0)(s.eventCount)

    allSubs.foreach(_.eventCount = 0)

    When("creating a link between the two remaining mice")
    ws.command("ask one-of mice [ create-links-with other mice ]")
    Then("the links subscriber should get an event")
    assertResult(1)(linksSub.eventCount)
    And("other subscribers should get none")
    for (s <- allSubs if s != linksSub) assertResult(0)(s.eventCount)

    allSubs.foreach(_.eventCount = 0)

    When("killing this links")
    ws.command("ask links [ die ]")
    Then("the links subscriber should get an event")
    assertResult(1)(linksSub.eventCount)
    And("other subscribers should get none")
    for (s <- allSubs if s != linksSub) assertResult(0)(s.eventCount)

    allSubs.foreach(_.eventCount = 0)

    When("creating an undirected-link between the two remaining mice")
    ws.command("ask one-of mice [ create-undirected-edges-with other mice ]")
    Then("the links subscriber should get an event")
    assertResult(1)(linksSub.eventCount)
    And("the undirected-links subscriber should get an event")
    assertResult(1)(linksSub.eventCount)
    And("other subscribers should get none")
    for (s <- allSubs if !Set(linksSub, undirLinksSub).contains(s)) assertResult(0)(s.eventCount)
  }

}
