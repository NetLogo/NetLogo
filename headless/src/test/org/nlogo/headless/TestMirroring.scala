// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.scalatest.FunSuite
import org.scalatest.exceptions.TestFailedException
import collection.JavaConverters._
import org.nlogo.util.Pico
import org.nlogo.api
import api.AgentVariables
import org.nlogo.mirror
import mirror._
import Mirroring._
import Mirrorables._

object TestMirroring {
  def withWorkspace[T](body: (HeadlessWorkspace, () => Iterable[Mirrorable]) => T): T = {
    val ws = HeadlessWorkspace.newInstance
    ws.silent = true
    try body(ws, () => allMirrorables(ws.world,
      Seq[(String, Int)]() // empty seq of widgets values for now - replace when we have them in headless NP 2012-09-17
      ))
    finally ws.dispose()
  }
}

class TestMirroring extends FunSuite {

  import TestMirroring.withWorkspace

  def sizes(u: Update) =
    (u.births.size, u.deaths.size, u.changes.size)

  def checkAllAgents(ws: HeadlessWorkspace, state: State) {
    def check[A <: api.Agent](agentSet: api.AgentSet, kind: Kind, toMirrorable: A => MirrorableAgent[A]) {
      expectResult(agentSet.count) { state.count(_._1.kind == kind) }
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
    check(ws.world.patches, Patch, (p: api.Patch) => new MirrorablePatch(p.asInstanceOf[api.Patch]))
    check(ws.world.turtles, Turtle, (p: api.Turtle) => new MirrorableTurtle(p.asInstanceOf[api.Turtle]))
    check(ws.world.links, Link, (p: api.Link) => new MirrorableLink(p.asInstanceOf[api.Link]))
  }

  test("init") {
    withWorkspace { (ws, mirrorables) =>

      ws.initForTesting(1)
      val (m0, u0) = diffs(Map(), mirrorables())
      // 9 patches + world + observer = 11 objects, 11 births
      expectResult((11, (11, 0, 0))) { (m0.size, sizes(u0)) }
      checkAllAgents(ws, m0)

      ws.command("crt 10")
      val (m1, u1) = diffs(m0, mirrorables())
      // 9 patches + 10 new turtles + world + observer = 21 objects, 10 births
      expectResult((21, (10, 0, 0))) { (m1.size, sizes(u1)) }
      checkAllAgents(ws, m1)

      ws.command("ask one-of turtles [ set color red + 2 set size 3 ]")
      val (m2, u2) = diffs(m1, mirrorables())
      // still 21 objects, 1 turtles has changed
      expectResult((21, (0, 0, 1))) { (m2.size, sizes(u2)) }
      // VAR_COLOR = 1, VAR_SIZE = 10
      expectResult("List(Change(1,17.0), Change(10,3.0))") {
        u2.changes.head._2.toList.toString
      }
      checkAllAgents(ws, m2)

      ws.command("ask n-of 5 turtles [ die ]")
      val (m3, u3) = diffs(m2, mirrorables())
      // down to 16 objects, with 5 deaths
      expectResult((16, (0, 5, 0))) { (m3.size, sizes(u3)) }
      checkAllAgents(ws, m3)

      val (m4, u4) = diffs(m3, mirrorables())
      // still 16 objects, nothing changed
      expectResult((16, (0, 0, 0))) { (m4.size, sizes(u4)) }
      checkAllAgents(ws, m4)

      ws.command("ask one-of patches [ set pcolor green ]")
      intercept[TestFailedException] {
        checkAllAgents(ws, m4)
      }
      ws.command("clear-patches")
      checkAllAgents(ws, m4)

    }
  }

  test("user-declared variables don't matter") {
    withWorkspace { (ws, mirrorables) =>
      val declarations =
        "patches-own [pfoo] " +
          "turtles-own [tfoo] " +
          "links-own   [lfoo]"
      ws.initForTesting(1, declarations)
      ws.command("create-turtles 3 [ create-links-with other turtles ]")
      val (m0, u0) = diffs(Map(), mirrorables())
      // 9 patches + 3 turtles + 3 links + world + observer = 17 objects
      expectResult((17, (17, 0, 0))) { (m0.size, sizes(u0)) }
      checkAllAgents(ws, m0)
      ws.command("ask patches [ set pfoo 1 ] " +
        "ask turtles [ set tfoo 1 ] " +
        "ask links   [ set lfoo 1 ]")
      checkAllAgents(ws, m0)
      val (m1, u1) = diffs(m0, mirrorables())
      expectResult((17, (0, 0, 0))) { (m1.size, sizes(u1)) }
      checkAllAgents(ws, m1)
    }
  }

  test("merge") {
    withWorkspace { (ws, mirrorables) =>
      ws.initForTesting(1)
      val (m0, u0) = diffs(Map(), mirrorables())
      var state: State = Mirroring.merge(Map(), u0)
      checkAllAgents(ws, m0)
      checkAllAgents(ws, state)
      ws.command("ask patches [ sprout 1 set pcolor pxcor ]")
      ws.command("ask n-of (count turtles / 2) turtles [ die ]")
      ws.command("ask turtles [ create-links-with other turtles ]")
      val (m1, u1) = diffs(m0, mirrorables())
      // 9 patches + 5 turtles + 10 links + world + observer = 26 agents total,
      // 15 of which are newborn. 6 patches changed color (some already had pxcor = pcolor)
      // and world.patchesAllBlack not true anymore, so 7 changes in all
      expectResult((26, (15, 0, 7))) { (m1.size, sizes(u1)) }
      checkAllAgents(ws, m1)
      intercept[TestFailedException] {
        checkAllAgents(ws, state)
      }
      state = Mirroring.merge(state, u1)
      checkAllAgents(ws, state)
      ws.command("ask n-of 3 turtles [ die ]")
      val (m2, u2) = diffs(m1, mirrorables())
      // 9 patches + 2 turtles + 1 link + observer and the world remain
      expectResult((14, (0, 12, 0))) { (m2.size, sizes(u2)) }
      checkAllAgents(ws, m2)
      state = Mirroring.merge(state, u2)
      checkAllAgents(ws, state)
    }
  }

  test("tick counter") {
    withWorkspace { (ws, mirrorables) =>
      ws.initForTesting(0)
      val (m0, u0) = diffs(Map(), mirrorables())
      var state: State = Mirroring.merge(Map(), u0)
      // 1 patch + world + observer = 3 objects
      expectResult((3, (3, 0, 0))) { (m0.size, sizes(u0)) }
      checkAllAgents(ws, m0)
      checkAllAgents(ws, state)
      ws.command("reset-ticks tick")
      val (m1, u1) = diffs(m0, mirrorables())
      expectResult((3, (0, 0, 1))) { (m1.size, sizes(u1)) }
      expectResult(1.0)(m1(AgentKey(World, 0))(MirrorableWorld.wvTicks))
      ws.command("tick-advance 0.1")
      val (m2, u2) = diffs(m1, mirrorables())
      expectResult((3, (0, 0, 1))) { (m2.size, sizes(u2)) }
      expectResult(1.1)(m2(AgentKey(World, 0))(MirrorableWorld.wvTicks))
    }
  }

}
