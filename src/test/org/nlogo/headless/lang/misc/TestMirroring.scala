// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

import org.scalatest.exceptions.TestFailedException
import org.nlogo.api, api.AgentVariables
import org.nlogo.mirror._, Mirroring._, Mirrorables._

class TestMirroring extends FixtureSuite {

  def mirrorables(implicit fixture: Fixture): Iterable[Mirrorable] =
    allMirrorables(fixture.workspace.world, Seq())

  def sizes(u: Update) =
    (u.births.size, u.deaths.size, u.changes.size)

  def checkAllAgents(state: State)(implicit fixture: Fixture) {
    def check[A <: api.Agent](agentSet: api.AgentSet, kind: Kind, toMirrorable: A => MirrorableAgent[A]) {
      assertResult(agentSet.count) { state.count(_._1.kind == kind) }
      import collection.JavaConverters._
      for (agent <- agentSet.agents.asScala) {
        val m = toMirrorable(agent.asInstanceOf[A])
        val mirrorVars = state(AgentKey(kind, agent.id))
        val realVars = agent.variables
        assert((mirrorVars zip realVars).zipWithIndex.forall {
          // for each pair, check if they're equal OR if they are overridden
          case ((mv, rv), i) => mv == rv || m.variables.keySet.contains(i)
        })
      }
    }
    import fixture.workspace.world
    check(world.patches, Patch,  (a: api.Patch)  => new MirrorablePatch (a.asInstanceOf[api.Patch] ))
    check(world.turtles, Turtle, (a: api.Turtle) => new MirrorableTurtle(a.asInstanceOf[api.Turtle]))
    check(world.links  , Link,   (a: api.Link)   => new MirrorableLink  (a.asInstanceOf[api.Link]  ))
  }

  test("init") { implicit fixture =>
    import fixture.{ workspace => ws }

    ModelCreator.open(ws, api.WorldDimensions.square(1))
    val (m0, u0) = diffs(Map(), mirrorables)
    // 9 patches + world + observer = 11 objects, 11 births
    assertResult((11, (11, 0, 0))) { (m0.size, sizes(u0)) }
    checkAllAgents(m0)

    ws.command("crt 10")
    val (m1, u1) = diffs(m0, mirrorables)
    // 9 patches + 10 new turtles + world + observer = 21 objects, 10 births
    assertResult((21, (10, 0, 0))) { (m1.size, sizes(u1)) }
    checkAllAgents(m1)

    ws.command("ask one-of turtles [ set color red + 2 set size 3 ]")
    val (m2, u2) = diffs(m1, mirrorables)
    // still 21 objects, 1 turtles has changed
    assertResult((21, (0, 0, 1))) { (m2.size, sizes(u2)) }
    // VAR_COLOR = 1, VAR_SIZE = 10
    assertResult("List(Change(1,17.0), Change(10,3.0))") {
      u2.changes.head._2.toList.toString
    }
    checkAllAgents(m2)

    ws.command("ask n-of 5 turtles [ die ]")
    val (m3, u3) = diffs(m2, mirrorables)
    // down to 16 objects, with 5 deaths
    assertResult((16, (0, 5, 0))) { (m3.size, sizes(u3)) }
    checkAllAgents(m3)

    val (m4, u4) = diffs(m3, mirrorables)
    // still 16 objects, nothing changed
    assertResult((16, (0, 0, 0))) { (m4.size, sizes(u4)) }
    checkAllAgents(m4)

    ws.command("ask one-of patches [ set pcolor green ]")
    intercept[TestFailedException] {
      checkAllAgents(m4)
    }
    ws.command("clear-patches")
    checkAllAgents(m4)

  }

  test("user-declared variables don't matter") { implicit fixture =>
    import fixture.{ workspace => ws }
    val declarations =
      "patches-own [pfoo] " +
        "turtles-own [tfoo] " +
        "links-own   [lfoo]"
    ModelCreator.open(ws, api.WorldDimensions.square(1), declarations)
    ws.command("create-turtles 3 [ create-links-with other turtles ]")
    val (m0, u0) = diffs(Map(), mirrorables)
    // 9 patches + 3 turtles + 3 links + world + observer = 17 objects
    assertResult((17, (17, 0, 0))) { (m0.size, sizes(u0)) }
    checkAllAgents(m0)
    ws.command("ask patches [ set pfoo 1 ] " +
      "ask turtles [ set tfoo 1 ] " +
      "ask links   [ set lfoo 1 ]")
    checkAllAgents(m0)
    val (m1, u1) = diffs(m0, mirrorables)
    assertResult((17, (0, 0, 0))) { (m1.size, sizes(u1)) }
    checkAllAgents(m1)
  }

  test("merge") { implicit fixture =>
    import fixture.{ workspace => ws }
    ModelCreator.open(ws, api.WorldDimensions.square(1))
    val (m0, u0) = diffs(Map(), mirrorables)
    var state: State = Mirroring.merge(Map(), u0)
    checkAllAgents(m0)
    checkAllAgents(state)
    ws.command("ask patches [ sprout 1 set pcolor pxcor ]")
    ws.command("ask n-of (count turtles / 2) turtles [ die ]")
    ws.command("ask turtles [ create-links-with other turtles ]")
    val (m1, u1) = diffs(m0, mirrorables)
    // 9 patches + 5 turtles + 10 links + world + observer = 26 agents total,
    // 15 of which are newborn. 6 patches changed color (some already had pxcor = pcolor)
    // and world.patchesAllBlack not true anymore, so 7 changes in all
    assertResult((26, (15, 0, 7))) { (m1.size, sizes(u1)) }
    checkAllAgents(m1)
    intercept[TestFailedException] {
      checkAllAgents(state)
    }
    state = Mirroring.merge(state, u1)
    checkAllAgents(state)
    ws.command("ask n-of 3 turtles [ die ]")
    val (m2, u2) = diffs(m1, mirrorables)
    // 9 patches + 2 turtles + 1 link + observer and the world remain
    assertResult((14, (0, 12, 0))) { (m2.size, sizes(u2)) }
    checkAllAgents(m2)
    state = Mirroring.merge(state, u2)
    checkAllAgents(state)
  }

  test("tick counter") { implicit fixture =>
    import fixture.{ workspace => ws }
    ModelCreator.open(ws, api.WorldDimensions.square(0))
    val (m0, u0) = diffs(Map(), mirrorables)
    val state: State = Mirroring.merge(Map(), u0)
    // 1 patch + world + observer = 3 objects
    assertResult((3, (3, 0, 0))) { (m0.size, sizes(u0)) }
    checkAllAgents(m0)
    checkAllAgents(state)
    ws.command("reset-ticks tick")
    val (m1, u1) = diffs(m0, mirrorables)
    assertResult((3, (0, 0, 1))) { (m1.size, sizes(u1)) }
    assertResult(1.0)(m1(AgentKey(World, 0))(MirrorableWorld.WorldVar.Ticks.id))
    ws.command("tick-advance 0.1")
    val (m2, u2) = diffs(m1, mirrorables)
    assertResult((3, (0, 0, 1))) { (m2.size, sizes(u2)) }
    assertResult(1.1)(m2(AgentKey(World, 0))(MirrorableWorld.WorldVar.Ticks.id))
  }

}
