// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.SimpleChangeEvent
import org.nlogo.api.SimpleChangeEventPublisher
import org.nlogo.headless.TestUsingWorkspace
import org.scalatest.FunSuite
import org.scalatest.GivenWhenThen

class TreeAgentSetTests extends FunSuite with GivenWhenThen with TestUsingWorkspace {

  class Sub(pub: SimpleChangeEventPublisher) extends SimpleChangeEventPublisher#Sub {
    pub.subscribe(this)
    var eventCount: Int = 0
    override def notify(pub: SimpleChangeEventPublisher#Pub, event: SimpleChangeEvent) {
      eventCount += 1
    }
  }

  testUsingWorkspace("TreeAgentSet should trigger SimpleChangeEvent") { ws =>

    implicit def anyToPub(agentSet: AnyRef) =
      agentSet.asInstanceOf[TreeAgentSet].simpleChangeEventPublisher

    given("a subscriber to turtles")
    val turtlesSub = new Sub(ws.world.turtles)
    and("a subscriber to mice")
    println(ws.world.program.breeds)
    val miceSub = new Sub(ws.world.program.breeds.get("MICE"))
    and("a subscriber to frogs")
    val frogSub = new Sub(ws.world.program.breeds.get("FROGS"))
    and("a subscriber to links")
    val linksSub = new Sub(ws.world.links)
    and("a subscriber to undirected-links")
    val undirLinksSub = new Sub(ws.world.program.linkBreeds.get("UNDIRECTED-LINKS"))

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
