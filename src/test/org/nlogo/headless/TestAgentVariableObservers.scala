// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

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
class TestAgentVariableObservers extends AbstractTestModels with GivenWhenThen {
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

  testModel("agents call variable observers", Model(declarations)) {
    val watcher = new ObservationQueue

    observer>> "crt 10 [ create-links-with other turtles ]"
    observer>> "create-dogs 10"
    observer>> "create-cats 10"
    world.addWatcher("MY-GLOBAL", watcher)
    world.addWatcher("XCOR", watcher)
    world.addWatcher("MY-TURTLE-VAR", watcher)
    world.addWatcher("MY-DOG-VAR", watcher)
    world.addWatcher("MY-SHARED-VAR", watcher)
    world.addWatcher("MY-LINK-VAR", watcher)
    world.addWatcher("MY-PATCH-VAR", watcher)

    when("setting a global variable with a watch")
    observer>> "set my-global 5"
    then("a single response is triggered")
    expect(1)(watcher.queue.size)
    and("the response contains the new value")
    expect((world.observer, "MY-GLOBAL", 5))(watcher.queue(0))
    watcher.queue.clear()

    when("setting a built-in turtle variable with a watch")
    observer>> "ask turtles [ set xcor who ]"
    then("one response per turtle is given")
    expect(world.turtles.count)(watcher.queue.size)
    and("those responses contain the new value")
    watcher.queue.foreach {
      case (t: Turtle, vn: String, value: AnyRef) => expect(t.id)(value)
      case _ => fail("got response from non-turtle")
    }
    watcher.queue.clear()

    when("setting a built-in turtle variable of a single turtle with a watch")
    observer>> "ask turtle 0 [ set xcor 20 ]"
    then("one response is given")
    expect(1)(watcher.queue.size)
    and("that response contains the new value")
    watcher.queue.foreach {
      case (t: Turtle, vn: String, value: AnyRef) => expect(20)(value)
      case _ => fail("got response from non-turtle")
    }
    watcher.queue.clear()

    when("setting a custom turtle variable with a watch")
    observer>> "ask turtles [ set my-turtle-var who ]"
    then("one response per turtle is given")
    expect(world.turtles.count)(watcher.queue.size)
    and("those responses contain the new value")
    watcher.queue.foreach {
      case (t: Turtle, vn: String, value: AnyRef) => expect(t.id)(value)
      case _ => fail("Got response from non-turtle")
    }
    watcher.queue.clear()

    when("setting a breeds-own variable with a watch")
    observer>> "ask dogs [ set my-dog-var who ]"
    then("one response per turtle of that breed is given")
    expect(world.getBreed("DOGS").count)(watcher.queue.size)
    and("those responses contain the new value")
    watcher.queue.foreach {
      case (t: Turtle, vn: String, value: AnyRef) => expect(t.id)(value)
      case _ => fail("Got response from non-turtle")
    }
    watcher.queue.clear()

    when("setting a shared breeds-own variable with a watch")
    observer>> "ask dogs [ set my-shared-var who ]"
    observer>> "ask cats [ set my-shared-var who ]"
    then("one response per turtle of the breeds with that variable is given")
    expect(world.getBreed("DOGS").count + world.getBreed("CATS").count)(watcher.queue.size)
    and("those responses contain the new value")
    watcher.queue.foreach {
      case (t: Turtle, vn: String, value: AnyRef) => { expect(t.id)(value); expect("MY-SHARED-VAR")(vn) }
      case _ => fail("Got response from non-turtle")
    }
    watcher.queue.clear()

    when("setting a links-own variable with a watch")
    observer>> "ask links [ set my-link-var end1 ]"
    then("one response per link is given")
    expect(world.links.count)(watcher.queue.size)
    and("those responses contain the new value")
    watcher.queue.foreach {
      case (l: Link, vn: String, value: AnyRef) => expect(l.end1)(value)
      case _ => fail("Got response from non-link")
    }
    watcher.queue.clear()

    when("setting a patches-own variable with a watch")
    observer>> "ask patches [ set my-patch-var pxcor ]"
    then("one response per patch is given")
    expect(world.patches.count)(watcher.queue.size)
    and("those responses contain the new value")
    watcher.queue.foreach {
      case (p: Patch, vn: String, value: AnyRef) => expect(p.pxcor)(value)
      case _ => fail("Got response from non-patch")
    }
    watcher.queue.clear()

    when("deleting a watcher")
    world.deleteWatcher("MY-GLOBAL", watcher)
    observer>> "set my-global 5"
    then("it's no longer called when that variable is set")
    expect(watcher.queue.isEmpty)(true)
  }
}
