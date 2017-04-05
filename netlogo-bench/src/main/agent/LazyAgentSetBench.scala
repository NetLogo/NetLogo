package org.nlogo.agent

import java.util.concurrent.TimeUnit

import org.nlogo.core.AgentKind

import org.openjdk.jmh.annotations._

import scala.util.Random

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class LazyAgentSetBench {

  val turtleCount = 1000
  var aTurtle: Agent = _

  var a: IndexedAgentSet = _
  var l: LazyAgentSet = _
  var ait: AgentIterator = _
  var lit: AgentIterator = _
  var turtles: Array[Agent] = _

  def createArrayAgentSet(array: Array[Agent]) = new ArrayAgentSet(AgentKind.Turtle, null, array)
  def createLazyAgentSet(array: Array[Agent]) = new LazyAgentSet(null, createArrayAgentSet(array))

  @Setup(Level.Iteration)
  def prepare() = {
    val r = new Random(0)
    val world = new World()
    world.createPatches(-10,10,-10,10)
    world.realloc()

    def newTurtle = world.createTurtle(world.turtles())
    turtles = (1 to turtleCount).map(_ => newTurtle).toArray[Agent]
    aTurtle = newTurtle
    a = createArrayAgentSet(turtles)
    l = createLazyAgentSet(turtles)
    ait = a.iterator
    lit = l.iterator
    // lazyAgentSet.lazyOther(aTurtle)
  }

//  @TearDown(Level.Iteration)
//  def teardown() = {
//    aTurtle = null
//    arrayAgentSet = null
//    lazyAgentSet = null
//  }

  // fast
//  @Benchmark
  def Init_Array() = {
    a = createArrayAgentSet(turtles)
  }

//  @Benchmark
  def Init_Lazy() = {
    l = createLazyAgentSet(turtles)
  }

//  @Benchmark
  def Count_Array() = {
    a.count
  }

//  @Benchmark
  def Count_Lazy() = {
    l.count
  }

//  @Benchmark
  def Iter_Array() = {
    a.iterator
  }

//  @Benchmark
  def Iter_Lazy() = {
    l.iterator
  }

//  @Benchmark
  def hasNext_Array() = {
    ait.hasNext
  }

//  @Benchmark
  def hasNext_Lazy() = {
    lit.hasNext
  }

//  @Benchmark
  def next_Array() = {
    ait = a.iterator
    ait.next
  }

//  @Benchmark
  def next_Lazy() = {
    lit = l.iterator
    lit.next
  }

  @Benchmark
  def isEmpty_Array() = {
    a.isEmpty
  }

  @Benchmark
  def isEmpty_Lazy() = {
    l.isEmpty
  }

  //medium
  @Benchmark
  def ContainsTrue_Array() = {
    a.contains(turtles(0))
  }

  @Benchmark
  def ContainsTrue_Lazy() = {
    l.contains(turtles(0))
  }

  @Benchmark
  def ContainsFalse_Array() = {
    a.contains(aTurtle)
  }

  @Benchmark
  def ContainsFalse_Lazy() = {
    l.contains(aTurtle)
  }

  @Benchmark
  def ContainsSameAgentsTrue_Array() = {
    a.containsSameAgents(a)
  }

  @Benchmark
  def ContainsSameAgentsTrue_Lazy() = {
    l.containsSameAgents(l)
  }
}
