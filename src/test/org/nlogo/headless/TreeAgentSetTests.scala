// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

// Note: this is here instead of in the agent package because
// of the dependency on headless.TestUsingWorkspace. NP 2013-08-05
// See https://github.com/NetLogo/NetLogo/commit/9f35a477f071b746bea225b2294813970b04daf0#commitcomment-3793507.

import org.nlogo.agent.SimpleChangeEventCounter
import org.nlogo.agent.TreeAgentSet
import org.scalatest.FunSuite
import org.scalatest.GivenWhenThen

class TreeAgentSetTests extends FunSuite with GivenWhenThen with TestUsingWorkspace {

  testUsingWorkspace("TreeAgentSet should trigger SimpleChangeEvent") { ws =>

    implicit def anyToPub(agentSet: AnyRef) =
      agentSet.asInstanceOf[TreeAgentSet].simpleChangeEventPublisher

    given("a subscriber to turtles")
    val turtlesSub = new SimpleChangeEventCounter(ws.world.turtles)
    and("a subscriber to mice")
    println(ws.world.program.breeds)
    val miceSub = new SimpleChangeEventCounter(ws.world.program.breeds.get("MICE"))
    and("a subscriber to frogs")
    val frogSub = new SimpleChangeEventCounter(ws.world.program.breeds.get("FROGS"))
    and("a subscriber to links")
    val linksSub = new SimpleChangeEventCounter(ws.world.links)
    and("a subscriber to undirected-links")
    val undirLinksSub = new SimpleChangeEventCounter(ws.world.program.linkBreeds.get("UNDIRECTED-LINKS"))

    val allSubs = Seq(turtlesSub, miceSub, frogSub, linksSub, undirLinksSub)

    when("creating a new turtle")
    ws.command("crt 1")
    then("the turtles subscriber should get an event")
    expect(1)(turtlesSub.eventCount)
    and("other subscribers should get none")
    for (s <- allSubs if s != turtlesSub) expect(0)(s.eventCount)

    allSubs.foreach(_.eventCount = 0)

    when("creating two other turtles")
    ws.command("crt 2")
    then("the turtles subscriber should two events")
    expect(2)(turtlesSub.eventCount)
    and("other subscribers should get none")
    for (s <- allSubs if s != turtlesSub) expect(0)(s.eventCount)

    allSubs.foreach(_.eventCount = 0)

    when("creating a new mouse")
    ws.command("create-mice 1")
    then("the turtles subscriber should get an event")
    expect(1)(turtlesSub.eventCount)
    and("the mice subscriber should get an event")
    expect(1)(miceSub.eventCount)
    and("other subscribers should get none")
    for (s <- allSubs if !Set(turtlesSub, miceSub).contains(s)) expect(0)(s.eventCount)

    allSubs.foreach(_.eventCount = 0)

    when("creating two other mice")
    ws.command("create-mice 2")
    then("the turtles subscriber should get two events")
    expect(2)(turtlesSub.eventCount)
    and("the mice subscriber should get two events")
    expect(2)(miceSub.eventCount)
    and("other subscribers should get none")
    for (s <- allSubs if !Set(turtlesSub, miceSub).contains(s)) expect(0)(s.eventCount)

    allSubs.foreach(_.eventCount = 0)

    when("killing one of the three mice")
    ws.command("ask one-of mice [ die ]")
    then("the turtles subscriber should get an event")
    expect(1)(turtlesSub.eventCount)
    and("the mice subscriber should get an event")
    expect(1)(miceSub.eventCount)
    and("other subscribers should get none")
    for (s <- allSubs if !Set(turtlesSub, miceSub).contains(s)) expect(0)(s.eventCount)

    allSubs.foreach(_.eventCount = 0)

    when("creating a link between the two remaining mice")
    ws.command("ask one-of mice [ create-links-with other mice ]")
    then("the links subscriber should get an event")
    expect(1)(linksSub.eventCount)
    and("other subscribers should get none")
    for (s <- allSubs if s != linksSub) expect(0)(s.eventCount)

    allSubs.foreach(_.eventCount = 0)

    when("killing this links")
    ws.command("ask links [ die ]")
    then("the links subscriber should get an event")
    expect(1)(linksSub.eventCount)
    and("other subscribers should get none")
    for (s <- allSubs if s != linksSub) expect(0)(s.eventCount)

    allSubs.foreach(_.eventCount = 0)

    when("creating an undirected-link between the two remaining mice")
    ws.command("ask one-of mice [ create-undirected-links-with other mice ]")
    then("the links subscriber should get an event")
    expect(1)(linksSub.eventCount)
    and("the undirected-links subscriber should get an event")
    expect(1)(linksSub.eventCount)
    and("other subscribers should get none")
    for (s <- allSubs if !Set(linksSub, undirLinksSub).contains(s)) expect(0)(s.eventCount)
  }
}
