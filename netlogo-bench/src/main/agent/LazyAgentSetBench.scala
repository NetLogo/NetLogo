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
    // lazyAgentSet.lazyOther(aTurtle)
  }

//  @TearDown(Level.Iteration)
//  def teardown() = {
//    aTurtle = null
//    arrayAgentSet = null
//    lazyAgentSet = null
//  }

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
//    lazyAgentSet.lazyOther(turtles(0))
    l.count
  }

  @Benchmark
  def Iter_Array() = {
    a.iterator
  }

  @Benchmark
  def Iter_Lazy() = {
    l.iterator
  }

//  @Benchmark
  def Contains1_Array() = {
//    var i = 0
//    val builder = new AgentSetBuilder(AgentKind.Turtle, turtleCount)
//    while (i < turtleCount) {
//      builder.add(turtles(i))
//      i += 1
//    }
//    arrayAgentSet = builder.build()
    a.contains(aTurtle)
  }

//  @Benchmark
  def Contains1_Lazy() = {
    l.contains(aTurtle)
  }

  @Benchmark
  def Contains2_Array() = {
    a.contains(turtles(0))
  }

  @Benchmark
  def Contains2_Lazy() = {
    l.contains(turtles(0))
  }
}
