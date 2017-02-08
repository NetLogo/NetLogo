// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.scalatest.FunSuite
import org.nlogo.core.AgentKind

class AgentSetTests extends FunSuite {

  val world = new World()
  world.realloc()

  def makeTurtle() = world.createTurtle(world.turtles())

  def createArrayAgentSet(array: Array[Agent]) = new ArrayAgentSet(AgentKind.Turtle, "", array)
  def createLazyAgentSet(array: Array[Agent]) = new LazyAgentSet(AgentKind.Turtle, "", createArrayAgentSet(array))

  testCount(createArrayAgentSet, "ArrayAgentSet")
  testRandomOne(createArrayAgentSet, "ArrayAgentSet")
  testIterator(createArrayAgentSet, "ArrayAgentSet")

  testCount(createLazyAgentSet, "LazyAgentSet")
  testRandomOne(createLazyAgentSet, "LazyAgentSet")
  testIterator(createLazyAgentSet, "LazyAgentSet")
  testLazyOther()
  testForce()

  // lazy-specific tests
  def testLazyOther(): Unit = {
    var header = "Lazy Other: "
    test(header + "empty") {
      val t1 = makeTurtle()
      val a = createLazyAgentSet(Array())
      a.other(t1)
      assertResult(0)(a.count)
      assertThrows[Exception] {
        a.randomOne(0,0)
      }
    }
    test(header + "1-agent set") {
      val t1 = makeTurtle()
      val a = createLazyAgentSet(Array(t1))
      a.other(t1)
      assertResult(0)(a.count)
      assertThrows[Exception] {
        a.randomOne(0,0)
      }
    }
    test(header + "irrelevant other") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createLazyAgentSet(Array(t1))
      a.other(t2)
      assertResult(1)(a.count)
      assertResult(t1)(a.randomOne(1,0))
    }
    test(header + "2-agent set") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createLazyAgentSet(Array(t1,t2))
      a.other(t1)
      assertResult(1)(a.count)
      assertResult(t2)(a.randomOne(1,0))
      assertThrows[Exception] {
        a.randomOne(1,1)
      }
    }
    header = "Lazy Iterator: "
    test(header + "single-set other") {
      val t1 = makeTurtle()
      val a = createLazyAgentSet(Array(t1))
      val it = a.iterator
      a.other(t1)
      assertResult(false)(it.hasNext)
      assertThrows[Exception] {
        it.next()
      }
    }
    test(header + "multi-set other") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createLazyAgentSet((Array(t1, t2)))
      val it = a.iterator
      assertResult(true)(it.hasNext)
      assertResult(t1)(it.next())
      a.other(t2)
      assertResult(false)(it.hasNext)
      assertThrows[Exception] {
        it.next()
      }
    }
    test(header + "irrelevant other") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createLazyAgentSet((Array(t1)))
      val it = a.iterator
      a.other(t2)
      assertResult(true)(it.hasNext)
      assertResult(t1)(it.next())
    }
  }

  def testForce(): Unit = {
    val header = "Lazy Force: "
    test(header + "no others") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createLazyAgentSet(Array(t1,t2))
      assertResult(createArrayAgentSet(Array(t1,t2)).containsSameAgents(a.force()))(true)
    }
    test(header + "1 other") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createLazyAgentSet(Array(t1,t2))
      a.other(t1)
      assertResult(createArrayAgentSet(Array(t2)).containsSameAgents(a.force()))(true)
    }
    test(header + "2 other") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createLazyAgentSet(Array(t1,t2))
      a.other(t1)
      a.other(t2)
      assertResult(createArrayAgentSet(Array()).containsSameAgents(a.force()))(true)
    }
    test(header + "irrelevant other") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createLazyAgentSet(Array(t1))
      a.other(t2)
      assertResult(createArrayAgentSet(Array(t1)).containsSameAgents(a.force()))(true)
    }
  }

  /* GENERAL TESTS */
  def testCount(createAgentSet: Array[Agent] => AgentSet, agentSetType: String): Unit = {
    //general
    test(agentSetType + ": empty set") {
      val a = createAgentSet(Array())
      assertResult(0)(a.count)
    }

    test(agentSetType + ": count 1 agent in set") {
      val a = createAgentSet(Array(makeTurtle()))
      assertResult(1)(a.count)
    }

    test(agentSetType + ": don't count dead agent") {
      val t = makeTurtle()
      val a = createAgentSet(Array(t))
      t.die()
      assertResult(0)(a.count)
    }
  }

  def testRandomOne(createAgentSet: Array[Agent] => AgentSet, agentSetType: String): Unit = {
    test(agentSetType + ": random one") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createAgentSet(Array(t1, t2))
      assertResult(t1)(a.randomOne(2, 0))
      assertResult(t2)(a.randomOne(2, 1))
      t1.die()
      assertResult(t2)(a.randomOne(2, 0))
      assertThrows[Exception] {
        a.randomOne(2, 1)
      }
    }

    test(agentSetType + ": random one errors when agent set empty") {
      val a = createAgentSet(Array())
      assertThrows[Exception] {
        a.randomOne(0, 0)
      }
    }
  }

  def testIterator(createAgentSet: Array[Agent] => AgentSet, agentSetType: String): Unit = {
    test(agentSetType + ": iterate empty") {
      val a = createAgentSet(Array())
      val it = a.iterator
      assertResult(false)(it.hasNext)
      assertThrows[Exception] {
        it.next()
      }
    }
    test(agentSetType + ": iterate 1 agent set") {
      val t1 = makeTurtle()
      val a = createAgentSet(Array(t1))
      val it = a.iterator
      assertResult(true)(it.hasNext)
      assertResult(t1)(it.next())
      assertResult(false)(it.hasNext)
      assertThrows[Exception] {
        it.next()
      }
    }
    test(agentSetType + ": iterate 1 dead agent set") {
      val t1 = makeTurtle()
      val a = createAgentSet(Array(t1))
      val it = a.iterator
      t1.die()
      assertResult(false)(it.hasNext)
      assertThrows[Exception] {
        it.next()
      }
    }
    test(agentSetType + ": iterate multi-agent set") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createAgentSet(Array(t1, t2))
      val it1 = a.iterator
      assertResult(true)(it1.hasNext)
      assertResult(t1)(it1.next())
      t2.die()
      val it2 = a.iterator
      assertResult(false)(it1.hasNext)
      assertResult(true)(it2.hasNext)
      assertResult(t1)(it2.next())
      assertThrows[Exception] {
        it1.next()
      }
    }
  }
}
