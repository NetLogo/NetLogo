// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.{ api, util }

object AgentSet {
  def fromAgent(agent: Agent): AgentSet =
    new ArrayAgentSet(agent.kind, null, Array(agent))
  def fromIterator(kind: api.AgentKind, agents: Iterator[Agent]): AgentSet =
    new ArrayAgentSet(kind, null, agents.toArray)
  // for convenience from Java, overload instead of using default arguments
  def fromArray(kind: api.AgentKind, agents: Array[Agent], printName: String = null): AgentSet =
    new ArrayAgentSet(kind, printName, agents)
  def fromArray(kind: api.AgentKind, agents: Array[Agent]): AgentSet =
    new ArrayAgentSet(kind, null, agents)
}

abstract class AgentSet(
  val kind: api.AgentKind,
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
  def getAgent(id: AnyRef): Agent
  def randomOne(precomputedCount: Int, random: Int): Agent
  def randomTwo(precomputedCount: Int, random1: Int, random2: Int): Array[Agent]
  def randomSubsetGeneral(resultSize: Int, precomputedCount: Int, rng: util.MersenneTwisterFast): Array[Agent]
  def toLogoList: api.LogoList

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
    AgentSet.fromArray(kind, array)
  }

}
