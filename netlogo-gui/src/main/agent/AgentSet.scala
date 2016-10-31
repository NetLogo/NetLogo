// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.{ core, api }

object AgentSet {
  def fromAgent(agent: Agent): IndexedAgentSet =
    new ArrayAgentSet(agent.kind, null, Array(agent))
  def fromIterator(kind: core.AgentKind, agents: Iterator[_ <: Agent]): IndexedAgentSet =
    new ArrayAgentSet(kind, null, agents.toArray[Agent])
  def fromIterable(kind: core.AgentKind, agents: Iterable[_ <: Agent]): IndexedAgentSet =
    new ArrayAgentSet(kind, null, agents.toArray[Agent])
  // for convenience from Java, overload instead of using default arguments
  def fromArray(kind: core.AgentKind, agents: Array[Agent], printName: String): IndexedAgentSet =
    new ArrayAgentSet(kind, printName, agents)
  def fromArray(kind: core.AgentKind, agents: Array[_ <: Agent]): IndexedAgentSet =
    new ArrayAgentSet(kind, null, agents.asInstanceOf[Array[Agent]])
}


abstract class AgentSet(
  val kind: core.AgentKind,
  val printName: String,
  // yuck, vars
  var directed: Directedness = Directedness.Undetermined) extends api.AgentSet {

  // abstract methods
  def equalAgentSetsHelper(otherSet: api.AgentSet): Boolean
  def iterator: AgentIterator
  def shufflerator(rng: api.MersenneTwisterFast): AgentIterator
  def randomOne(precomputedCount: Int, random: Int): Agent
  def randomTwo(precomputedCount: Int, random1: Int, random2: Int): Array[Agent]
  def randomSubsetGeneral(resultSize: Int, precomputedCount: Int, rng: api.MersenneTwisterFast): Array[Agent]
  def toLogoList: core.LogoList

  ///

  val agentBit: Byte = AgentBit(kind).toByte

  def isDirected: Boolean = directed == Directedness.Directed
  def isUndirected: Boolean = directed == Directedness.Undirected

  def clearDirected(): Unit = {
    directed = Directedness.Undetermined
  }

  def setDirected(directed: Boolean) =
    this.directed = if (directed) Directedness.Directed else Directedness.Undirected

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

  def randomSubset(resultSize: Int, precomputedCount: Int, rng: api.MersenneTwisterFast): AgentSet = {
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
