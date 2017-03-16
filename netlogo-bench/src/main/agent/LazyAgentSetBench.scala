package org.nlogo.agent

import java.util.concurrent.TimeUnit

import org.nlogo.core.AgentKind

import org.openjdk.jmh.annotations._

import scala.util.Random

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class LazyAgentSetBench {

  val turtleCount = 500
  var aTurtle: Agent = _

  var arrayAgentSet: IndexedAgentSet = _
  var lazyAgentSet: LazyAgentSet = _
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
    // lazyAgentSet.lazyOther(aTurtle)
  }

  @TearDown(Level.Iteration)
  def teardown() = {
    aTurtle = null
    arrayAgentSet = null
    lazyAgentSet = null
  }

  @Benchmark
  def measureArrayAgentsetCount() = {
    arrayAgentSet = createArrayAgentSet(turtles)
    arrayAgentSet.count
  }

  @Benchmark
  def measureLazyAgentsetCount() = {
    lazyAgentSet = createLazyAgentSet(turtles)
    lazyAgentSet.lazyOther(turtles(0))
    lazyAgentSet.count
  }

  @Benchmark
  def measureArrayAgentsetContains() = {
    var i = 0
    val builder = new AgentSetBuilder(AgentKind.Turtle, turtleCount)
    while (i < turtleCount) {
      builder.add(turtles(i))
      i += 1
    }
    arrayAgentSet = builder.build()
    arrayAgentSet.contains(aTurtle)
  }

  @Benchmark
  def measureLazyAgentsetContains() = {
    lazyAgentSet = createLazyAgentSet(turtles)
    lazyAgentSet.contains(aTurtle)
  }
}
