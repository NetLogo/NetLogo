// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

import collection.mutable
import collection.JavaConverters._
import org.scalatest.{GivenWhenThen, FunSuite}
import org.nlogo.agent._

class ObservationQueue extends World.VariableWatcher {
  val queue: mutable.Queue[(Agent, String, AnyRef)] = new mutable.Queue[(Agent, String, AnyRef)]
  def update(agent: Agent, variableName: String, value: AnyRef) = {
    queue.enqueue((agent, variableName, value))
  }
}

class TestAgentVariableObservers extends FixtureSuite with GivenWhenThen {

  val declarations =
    """
      |breed [ dogs dog ]
      |breed [ cats cat ]
      |globals [ my-global ]
      |dogs-own [ my-dog-var my-shared-var ]
      |cats-own [ my-shared-var ]
      |turtles-own [ my-turtle-var ]
      |links-own [ my-link-var ]
      |patches-own [ my-patch-var ]
    """.stripMargin

  test("agents call variable observers") { implicit fixture =>
    import fixture._
    declare(declarations)

    val watcher = new ObservationQueue

    import workspace.world
    testCommand("crt 10 [ create-links-with other turtles ]")
    testCommand("create-dogs 10")
    testCommand("create-cats 10")
    world.addWatcher("MY-GLOBAL", watcher)
    world.addWatcher("XCOR", watcher)
    world.addWatcher("MY-TURTLE-VAR", watcher)
    world.addWatcher("MY-DOG-VAR", watcher)
    world.addWatcher("MY-SHARED-VAR", watcher)
    world.addWatcher("MY-LINK-VAR", watcher)
    world.addWatcher("MY-PATCH-VAR", watcher)

    When("setting a global variable with a watch")
    testCommand("set my-global 5")
    Then("a single response is triggered")
    assertResult(1)(watcher.queue.size)
    And("the response contains the new value")
    assertResult((world.observer, "MY-GLOBAL", 5))(watcher.queue(0))
    watcher.queue.clear()

    When("setting a built-in turtle variable with a watch")
    testCommand("ask turtles [ set xcor who ]")
    Then("one response per turtle is given")
    assertResult(world.turtles.count)(watcher.queue.size)
    And("those responses contain the new value")
    watcher.queue.foreach {
      case (t: Turtle, vn: String, value: AnyRef) => assertResult(t.id)(value)
      case _ => fail("got response from non-turtle")
    }
    watcher.queue.clear()

    When("setting a built-in turtle variable of a single turtle with a watch")
    testCommand("ask turtle 0 [ set xcor 20 ]")
    Then("one response is given")
    assertResult(1)(watcher.queue.size)
    And("that response contains the new value")
    watcher.queue.foreach {
      case (t: Turtle, vn: String, value: AnyRef) => assertResult(20)(value)
      case _ => fail("got response from non-turtle")
    }
    watcher.queue.clear()

    When("setting a custom turtle variable with a watch")
    testCommand("ask turtles [ set my-turtle-var who ]")
    Then("one response per turtle is given")
    assertResult(world.turtles.count)(watcher.queue.size)
    And("those responses contain the new value")
    watcher.queue.foreach {
      case (t: Turtle, vn: String, value: AnyRef) => assertResult(t.id)(value)
      case _ => fail("Got response from non-turtle")
    }
    watcher.queue.clear()

    When("setting a breeds-own variable with a watch")
    testCommand("ask dogs [ set my-dog-var who ]")
    Then("one response per turtle of that breed is given")
    assertResult(world.getBreed("DOGS").count)(watcher.queue.size)
    And("those responses contain the new value")
    watcher.queue.foreach {
      case (t: Turtle, vn: String, value: AnyRef) => assertResult(t.id)(value)
      case _ => fail("Got response from non-turtle")
    }
    watcher.queue.clear()

    When("setting a shared breeds-own variable with a watch")
    testCommand("ask dogs [ set my-shared-var who ]")
    testCommand("ask cats [ set my-shared-var who ]")
    Then("one response per turtle of the breeds with that variable is given")
    assertResult(world.getBreed("DOGS").count + world.getBreed("CATS").count)(watcher.queue.size)
    And("those responses contain the new value")
    watcher.queue.foreach {
      case (t: Turtle, vn: String, value: AnyRef) => { assertResult(t.id)(value); assertResult("MY-SHARED-VAR")(vn) }
      case _ => fail("Got response from non-turtle")
    }
    watcher.queue.clear()

    When("setting a links-own variable with a watch")
    testCommand("ask links [ set my-link-var end1 ]")
    Then("one response per link is given")
    assertResult(world.links.count)(watcher.queue.size)
    And("those responses contain the new value")
    watcher.queue.foreach {
      case (l: Link, vn: String, value: AnyRef) => assertResult(l.end1)(value)
      case _ => fail("Got response from non-link")
    }
    watcher.queue.clear()

    When("setting a patches-own variable with a watch")
    testCommand("ask patches [ set my-patch-var pxcor ]")
    Then("one response per patch is given")
    assertResult(world.patches.count)(watcher.queue.size)
    And("those responses contain the new value")
    watcher.queue.foreach {
      case (p: Patch, vn: String, value: AnyRef) => assertResult(p.pxcor)(value)
      case _ => fail("Got response from non-patch")
    }
    watcher.queue.clear()

    When("deleting a watcher")
    world.deleteWatcher("MY-GLOBAL", watcher)
    testCommand("set my-global 5")
    Then("it's no longer called when that variable is set")
    assertResult(watcher.queue.isEmpty)(true)
  }
}
