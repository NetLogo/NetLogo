// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.MersenneTwisterFast
import org.scalatest.FunSuite
import org.nlogo.core.AgentKind

class AgentSetTests extends FunSuite {

  val world = new World()
  world.realloc()

  def makeTurtle() = world.createTurtle(world.turtles())

  def createArrayAgentSet(array: Array[Agent]) = new ArrayAgentSet(AgentKind.Turtle, "", array)
  def createLazyAgentSet(array: Array[Agent]) = new LazyAgentSet(AgentKind.Turtle, "", createArrayAgentSet(array))

  testAgentSet(createArrayAgentSet, "ArrayAgentSet")
  testAgentSet(createLazyAgentSet, "LazyAgentSet")
  testLazies()

  def testLazies(): Unit = {
    testLazyOther()
    testLazyWith()
    testForce()
    testLazyIterator()
    testLazyOtherAndWith()
  }
  // lazy-specific tests
  def testLazyWith(): Unit = {
    val header = "Lazy With: "
    test(header + "no withs") {
      val t1 = makeTurtle()
      val a = createLazyAgentSet(Array())
      assertResult(true)(a.passesWiths(t1))
    }
    test(header + "always true with") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createLazyAgentSet(Array(t1,t2))
      a.lazyWith((_) => true)
      assertResult(2)(a.count)
    }
    test(header + "always false with") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createLazyAgentSet(Array(t1,t2))
      a.lazyWith((_) => false)
      assertResult(0)(a.count)
    }
    test(header + "1 with") {
      val a = createLazyAgentSet(Array(makeTurtle(), makeTurtle()))
      a.lazyWith((a: Agent) => a.id % 2 == 0)
      assertResult(1)(a.count)
    }
    test(header + "id check") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val t3 = makeTurtle()
      val a = createLazyAgentSet(Array(t1,t2,t3))
      val threshold = a.randomOne(3,1).id
      a.lazyWith((ag: Agent) => ag.id >= threshold)
      assertResult(2)(a.count)
    }
  }

  def testLazyOther(): Unit = {
    val header = "Lazy Other: "
    test(header + "empty") {
      val t1 = makeTurtle()
      val a = createLazyAgentSet(Array())
      a.lazyOther(t1)
      assertResult(0)(a.count)
      assertThrows[Exception] {
        a.randomOne(0,0)
      }
    }
    test(header + "1-agent set") {
      val t1 = makeTurtle()
      val a = createLazyAgentSet(Array(t1))
      a.lazyOther(t1)
      assertResult(0)(a.count)
      assertThrows[Exception] {
        a.randomOne(0,0)
      }
    }
    test(header + "irrelevant other") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createLazyAgentSet(Array(t1))
      a.lazyOther(t2)
      assertResult(1)(a.count)
      assertResult(t1)(a.randomOne(1,0))
    }
    test(header + "2-agent set") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createLazyAgentSet(Array(t1,t2))
      a.lazyOther(t1)
      assertResult(1)(a.count)
      assertResult(t2)(a.randomOne(1,0))
      assertThrows[Exception] {
        a.randomOne(1,1)
      }
    }
  }

  def testLazyIterator(): Unit = {
    val header = "Lazy Iterator: "
    test(header + "single-set other") {
      val t1 = makeTurtle()
      val a = createLazyAgentSet(Array(t1))
      val it = a.iterator
      a.lazyOther(t1)
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
      a.lazyOther(t2)
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
      a.lazyOther(t2)
      assertResult(true)(it.hasNext)
      assertResult(t1)(it.next())
    }
    test(header + "single-set with") {
      val t1 = makeTurtle()
      val a = createLazyAgentSet(Array(t1))
      val it = a.iterator
      a.lazyWith((_) => true)
      assertResult(true)(it.hasNext)
      a.lazyWith((_) => false)
      assertResult(false)(it.hasNext)
    }
  }
  def testLazyOtherAndWith(): Unit = {
    val t1 = makeTurtle()
    val t2 = makeTurtle()
    val t3 = makeTurtle()
    val a = createLazyAgentSet(Array(t1,t2,t3))
    val it1 = a.iterator
    assertResult(true)(it1.hasNext)
    assertResult(t1)(it1.next())
    a.lazyOther(t1)
    assertResult(true)(it1.hasNext)
    assertResult(t2)(it1.next())
    assertResult(2)(a.count)
    val threshold = a.randomOne(2,0).id
    a.lazyWith((ag: Agent) => ag.id <= threshold)
    assertResult(false)(it1.hasNext)
    assertThrows[Exception] {
      it1.next()
    }
    assertResult(1)(a.count)
    val it2 = a.iterator
    assertResult(t2)(it2.next())
    assertResult(false)(it2.hasNext)
  }


  def testForce(): Unit = {
    val header = "Lazy Force: "
    test(header + "empty") {
      val a = createLazyAgentSet(Array())
      assertResult(true)(createArrayAgentSet(Array()).containsSameAgents(a))
    }
    test(header + "no filters") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createLazyAgentSet(Array(t1,t2))
      assertResult(true)(createArrayAgentSet(Array(t1,t2)).containsSameAgents(a.force()))
    }
    test(header + "1 other") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createLazyAgentSet(Array(t1,t2))
      a.lazyOther(t1)
      assertResult(true)(createArrayAgentSet(Array(t2)).containsSameAgents(a.force()))
    }
    test(header + "2 other") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createLazyAgentSet(Array(t1,t2))
      a.lazyOther(t1)
      a.lazyOther(t2)
      assertResult(true)(createArrayAgentSet(Array()).containsSameAgents(a.force()))
    }
    test(header + "irrelevant other") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createLazyAgentSet(Array(t1))
      a.lazyOther(t2)
      assertResult(true)(createArrayAgentSet(Array(t1)).containsSameAgents(a.force()))
    }
  }

  /* GENERAL TESTS */
  def testAgentSet(createAgentSet: Array[Agent] => AgentSet, agentSetType: String): Unit = {
    testCount(createAgentSet, agentSetType)
    testRandomOne(createAgentSet, agentSetType)
    testRandomTwo(createAgentSet, agentSetType)
    testContains(createAgentSet, agentSetType)
    testContainsSameAgents(createAgentSet, agentSetType)
    testIterator(createAgentSet, agentSetType)
  }
  def testCount(createAgentSet: Array[Agent] => AgentSet, agentSetType: String): Unit = {
    //general
    test(agentSetType + ": count empty set") {
      val a = createAgentSet(Array())
      assertResult(0)(a.count)
      assertResult(true)(a.isEmpty)
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
    test(agentSetType + ": randomOne on 2 agent set") {
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

    test(agentSetType + ": randomOne errors when agent set empty") {
      val a = createAgentSet(Array())
      assertThrows[Exception] {
        a.randomOne(0, 0)
      }
    }
  }

  def testRandomTwo(createAgentSet: Array[Agent] => AgentSet, agentSetType: String): Unit = {
    test(agentSetType + ": randomTwo on 2 agent set") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createAgentSet(Array(t1, t2))
      assertResult(Array(t1,t2))(a.randomTwo(2,0,1))
    }
  }

  def randomSubsetGeneral(createAgentSet: Array[Agent] => AgentSet, agentSetType: String): Unit = {
    test(agentSetType + ": randomSubsetGeneral") {
      val a = createAgentSet(Array(makeTurtle(),makeTurtle(),makeTurtle(),makeTurtle()))
      val b = a.randomSubsetGeneral(2,4, new MersenneTwisterFast())
      assertResult(2)(b.length)
      assertResult(true)(a.contains(b(0)))
      assertResult(true)(a.contains(b(1)))
    }
  }

  def testContains(createAgentSet: Array[Agent] => AgentSet, agentSetType: String): Unit = {
    test(agentSetType + ": contains on empty") {
      val a = createAgentSet(Array())
      assertResult(false)(a.contains(makeTurtle()))
    }
    test(agentSetType + ": contains on 1 agent set") {
      val t1 = makeTurtle()
      val a = createAgentSet(Array(t1))
      assertResult(true)(a.contains(t1))
      assertResult(false)(a.contains(makeTurtle()))
    }
    test(agentSetType + ": contains on 2 agent set") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createAgentSet(Array(t1, t2))
      assertResult(true)(a.contains(t1))
      assertResult(true)(a.contains(t2))
      assertResult(false)(a.contains(makeTurtle()))
      t1.die()
      assertResult(false)(a.contains(t1))
    }
  }

  def testContainsSameAgents(createAgentSet: Array[Agent] => AgentSet, agentSetType: String): Unit = {
    test(agentSetType + ": containsSameAgents on empties") {
      val a = createAgentSet(Array())
      val b = createAgentSet(Array())
      assertResult(true)(a.containsSameAgents(b))
    }
    test(agentSetType + ": containsSameAgents on 1 empty") {
      val a = createAgentSet(Array(makeTurtle()))
      val b = createAgentSet(Array())
      // containsSameAgents assumes count is called first.
      // this should return true because forall on an empty set -> true
      assertResult(true)(a.containsSameAgents(b))
      assertResult(true)(a.containsSameAgents(a))
    }
    test(agentSetType + ": containsSameAgents on 2 agent sets") {
      val t1 = makeTurtle()
      val t2 = makeTurtle()
      val a = createAgentSet(Array(t1))
      val b = createAgentSet(Array(t2))
      val c = createAgentSet(Array(t2, t1))
      val d = createAgentSet(Array(t1, t2))
      assertResult(false)(a.containsSameAgents(b))
      assertResult(true)(c.containsSameAgents(d))
      t2.die()
      assertResult(true)(c.containsSameAgents(d))
      assertResult(true)(a.containsSameAgents(d))
      assertResult(true)(a.containsSameAgents(c))
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
