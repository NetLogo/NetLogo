// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.{ api }
import org.nlogo.core.{AgentKind, LogoList}

import java.util.{Iterator => JIterator}

object AgentSet {
  def fromAgent(agent: Agent): IndexedAgentSet =
    new ArrayAgentSet(agent.kind, null, Array(agent))
  def fromArray(kind: AgentKind, agents: Array[_ <: Agent]): IndexedAgentSet =
    new ArrayAgentSet(kind, null, agents.asInstanceOf[Array[Agent]])

  val emptyTurtleSet = AgentSet.fromArray(AgentKind.Turtle, Array.empty[Agent])
  val emptyPatchSet = AgentSet.fromArray(AgentKind.Patch, Array.empty[Agent])
  val emptyLinkSet = AgentSet.fromArray(AgentKind.Link, Array.empty[Agent])
}


abstract class AgentSet(
  val kind: AgentKind,
  val printName: String,
  // yuck, vars
  var directed: Directedness = Directedness.Undetermined) extends api.AgentSet {

  // abstract methods
  def containsSameAgents(otherSet: api.AgentSet): Boolean
  def iterator: AgentIterator
  def shufflerator(rng: api.MersenneTwisterFast): AgentIterator
  def randomOne(precomputedCount: Int, random: Int): Agent
  def randomTwo(precomputedCount: Int, smallRandom: Int, bigRandom: Int): Array[Agent]
  def randomSubsetGeneral(resultSize: Int, precomputedCount: Int, rng: api.MersenneTwisterFast): Array[Agent]
  def toLogoList: LogoList

  ///

  val agentBit: Byte = AgentBit(kind).toByte

  def isDirected: Boolean = directed == Directedness.Directed
  def isUndirected: Boolean = directed == Directedness.Undirected

  def clearDirected(): Unit = {
    directed = Directedness.Undetermined
  }

  def setDirected(directed: Boolean) =
    this.directed = if (directed) Directedness.Directed else Directedness.Undirected

  // TODO: We should really be enforcing breeds to be TreeAgentSets at the
  // typelevel, but such a change is too big right now. Currently, some code
  // assumes that an agentset is a breedset if and only if it has a `printName`
  // and other code assumes that an agentset is a breedset if and only if it's
  // a `TreeAgentSet` (which is *not* true in the case of `world.patches`).
  // -- BCH 10/12/2016
  /**
    * @return True if this is a special agentset, such as `world.turtle`,
    *         `world.links`, `world.patches`, or a breed.
    */
  def isBreedSet: Boolean = printName != null

  def equalAgentSets(otherSet: api.AgentSet) =
    (this eq otherSet) || (
      kind == otherSet.kind &&
      count == otherSet.count &&
      containsSameAgents(otherSet))

  def agents: java.lang.Iterable[api.Agent] =
    new java.lang.Iterable[api.Agent] {
      private[this] val it = AgentSet.this.iterator
      override def iterator: JIterator[api.Agent] =
        new JIterator[api.Agent] {
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
          val (smallRan, bigRan) = {
            val r1 = rng.nextInt(precomputedCount)
            val r2 = rng.nextInt(precomputedCount - 1)
            if (r2 >= r1) (r1, r2 + 1) else (r2, r1)
          }
          randomTwo(precomputedCount, smallRan, bigRan)
        case _ =>
          randomSubsetGeneral(resultSize, precomputedCount, rng)
      }
    AgentSet.fromArray(kind, array)
  }

}
