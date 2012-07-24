// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.{ api, util }

abstract class AgentSet(
  val kind: api.AgentKind,
  val world: World,
  val printName: String,
  val removableAgents: Boolean,
  // yuck, vars
  var isDirected: Boolean = false,
  var isUndirected: Boolean = false)
extends api.AgentSet {

  // abstract methods
  def equalAgentSetsHelper(otherSet: api.AgentSet): Boolean
  def iterator: AgentIterator
  def shufflerator(rng: util.MersenneTwisterFast): AgentIterator
  def agent(id: Long): Agent
  def getAgent(id: AnyRef): Agent
  def add(agent: Agent)
  def remove(key: AnyRef)
  def clear()
  def randomOne(precomputedCount: Int, random: Int): Agent
  def randomTwo(precomputedCount: Int, random1: Int, random2: Int): Array[Agent]
  def randomSubsetGeneral(resultSize: Int, precomputedCount: Int, rng: util.MersenneTwisterFast): Array[Agent]
  def toLogoList: api.LogoList
  def toArray: Array[Agent]

  ///

  val agentBit: Byte = AgentBit(kind).toByte

  def clearDirected() {
    isDirected = false
    isUndirected = false
  }

  def setDirected(directed: Boolean) {
    isDirected = directed
    isUndirected = !directed
  }

  def equalAgentSets(otherSet: api.AgentSet) =
    (this eq otherSet) || (
      kind == otherSet.kind &&
      count == otherSet.count &&
      equalAgentSetsHelper(otherSet))

  def agents: java.lang.Iterable[api.Agent] =
    new java.lang.Iterable[api.Agent] {
      private[this] val it = AgentSet.this.iterator
      override def iterator: java.util.Iterator[api.Agent] =
        new java.util.Iterator[api.Agent] {
          override def hasNext = it.hasNext
          override def next(): Agent = it.next()
          override def remove() = throw new UnsupportedOperationException
        }
    }

  def randomSubset(resultSize: Int, precomputedCount: Int, rng: util.MersenneTwisterFast): AgentSet = {
    val array: Array[Agent] =
      resultSize match {
        case 0 =>
          Array()
        case 1 =>
          Array(randomOne(precomputedCount, rng.nextInt(precomputedCount)))
        case 2 =>
          randomTwo(precomputedCount,
                    rng.nextInt(precomputedCount),
                    rng.nextInt(precomputedCount - 1))
        case _ =>
          randomSubsetGeneral(resultSize, precomputedCount, rng)
      }
    new ArrayAgentSet(kind, array, world)
  }

}
