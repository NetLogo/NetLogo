// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw

import org.nlogo.agent.{ AgentSet, TreeAgentSet }
import org.nlogo.headless.HeadlessWorkspace
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._
import org.scalatest.GivenWhenThen

class AgentSetChangeSubscribersTestSuite extends AnyFunSuite with GivenWhenThen {

  def getSubscribers(agentSet: AgentSet): collection.Set[AgentSetChangeSubscriber] = {
    val pub = agentSet.asInstanceOf[TreeAgentSet].simpleChangeEventPublisher
    // get private `listeners` field using reflection:
    val field = pub.getClass.getDeclaredField("org$nlogo$core$Publisher$$listeners")
    field.setAccessible(true)
    field.get(pub).asInstanceOf[collection.Set[AgentSetChangeSubscriber]]
  }

  def checkSizes(expectedSizes: (Iterable[?], Int)*) =
    for ((xs, n) <- expectedSizes) xs should have size n

  test("subscribers") {
    val ws: HeadlessWorkspace = HeadlessWorkspace.newInstance
    ws.setModelPath(new java.io.File("tests.txt").getPath)
    try {

      Given("a newly initialized workspace")
      ws.initForTesting(1, "extensions [nw]\n" + HeadlessWorkspace.TestDeclarations)

      Then("there should be no subscribers")
      val t = getSubscribers(ws.world.turtles)
      val l = getSubscribers(ws.world.links)
      val f = getSubscribers(ws.world.getBreed("FROGS"))
      val m = getSubscribers(ws.world.getBreed("MICE"))
      val u = getSubscribers(ws.world.getLinkBreed("UNDIRECTED-EDGES"))
      val d = getSubscribers(ws.world.getLinkBreed("DIRECTED-EDGES"))
      checkSizes(t -> 0, l -> 0, f -> 0, m -> 0, u -> 0, d -> 0)

      When("we `nw:set-context turtles links`")
      ws.command("nw:set-context turtles links")
      Then("turtles and links should get one subscriber each")
      checkSizes(t -> 1, l -> 1, f -> 0, m -> 0, u -> 0, d -> 0)

      When("we create a single mouse")
      ws.command("create-mice 1")
      Then("subscriber counts should not change")
      checkSizes(t -> 1, l -> 1, f -> 0, m -> 0, u -> 0, d -> 0)

      When("we create a couple of frogs with undir links to each other")
      ws.command("create-frogs 2 [ create-undirected-edges-with other frogs ]")
      Then("subscriber counts should not change")
      checkSizes(t -> 1, l -> 1, f -> 0, m -> 0, u -> 0, d -> 0)

      When("we explicitely get the context")
      ws.report("nw:get-context")
      Then("subscriber counts should not change either")
      checkSizes(t -> 1, l -> 1, f -> 0, m -> 0, u -> 0, d -> 0)

      When("we set the context to the breeds")
      ws.command("nw:set-context frogs undirected-edges")
      Then("turtles and links should loose their subscribers and frogs/undir-links should get theirs")
      checkSizes(t -> 0, l -> 0, f -> 1, m -> 0, u -> 1, d -> 0)

      When("we go back to the `turtles links` context")
      ws.command("nw:set-context turtles links")
      Then("only turtles and links should have subscribers")
      checkSizes(t -> 1, l -> 1, f -> 0, m -> 0, u -> 0, d -> 0)

      locally {
        val turtleSub = t.head
        val linkSub = l.head
        When("we use `nw:with-context` to do something innocuous")
        ws.command("nw:with-context frogs undirected-edges [" +
          "let d [ nw:distance-to one-of other frogs ] of one-of frogs ]")
        Then("the subscribers counts should not change")
        checkSizes(t -> 1, l -> 1, f -> 0, m -> 0, u -> 0, d -> 0)
        And("the same old subscribers objects should still be subscribed")
        t.head should be theSameInstanceAs turtleSub
        l.head should be theSameInstanceAs linkSub
        When("we do something that verifies the context")
        ws.report("nw:get-context")
        Then("again, the counts and subscribers objects should not change")
        checkSizes(t -> 1, l -> 1, f -> 0, m -> 0, u -> 0, d -> 0)
        t.head should be theSameInstanceAs turtleSub
        l.head should be theSameInstanceAs linkSub
        When("we do something within nw:with-context that should invalidate the cache")
        ws.command("nw:with-context frogs undirected-edges [ ask one-of frogs [ die ] ]")
        Then("the subscribers counts should not change")
        checkSizes(t -> 1, l -> 1, f -> 0, m -> 0, u -> 0, d -> 0)
        When("we do something that verifies the context")
        ws.command("let c nw:get-context")
        Then("the subscribers counts should not change")
        checkSizes(t -> 1, l -> 1, f -> 0, m -> 0, u -> 0, d -> 0)
        And("we should have new subscriber objects because of the invalidated context")
        t.head should not be theSameInstanceAs(turtleSub)
        l.head should not be theSameInstanceAs(linkSub)
      }

      When("we clear-all")
      ws.command("clear-all")
      Then("all subscribers should be gone")
      checkSizes(t -> 0, l -> 0, f -> 0, m -> 0, u -> 0, d -> 0)

    } finally ws.dispose()
  }
}
