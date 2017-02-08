// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.LogoList
import org.nlogo.{api, core}

class LazyAgentSet(kind: core.AgentKind, printName: String, private val agentSet: AgentSet)
  extends AgentSet(kind, printName) {

  var others = List[Agent]()

  def isEmpty = agentSet.isEmpty

  def count = {
    if (others.isEmpty)
      agentSet.count
    else
      agentSet.count - others.count(x => agentSet.contains(x))
  }

  def contains(a: api.Agent): Boolean = agentSet.contains(a)
  def containsSameAgents(otherSet: api.AgentSet): Boolean = agentSet.containsSameAgents(otherSet)

  def iterator: AgentIterator = {
      new FilteringIterator(agentSet.iterator)
  }

  def shufflerator(rng: api.MersenneTwisterFast): AgentIterator = agentSet.shufflerator(rng)

  def randomOne(precomputedCount: Int, random: Int): Agent = {
    if (others.isEmpty)
      agentSet.randomOne(precomputedCount, random)
    else {
      val iter = iterator
      var i = 0
      while (i < random) {
        iter.next()
        i += 1
      }
      iter.next()
    }
  }

  def randomTwo(precomputedCount: Int, smallRandom: Int, bigRandom: Int): Array[Agent] =
    agentSet.randomTwo(precomputedCount, smallRandom, bigRandom)
  def randomSubsetGeneral(resultSize: Int, precomputedCount: Int, rng: api.MersenneTwisterFast): Array[Agent] =
    agentSet.randomSubsetGeneral(resultSize, precomputedCount, rng)
  def toLogoList: LogoList = agentSet.toLogoList

  def other(agent: Agent): Unit = {
    others = agent :: others
  }

  def force(): AgentSet = {
    if (others.isEmpty)
      agentSet
    else {
      val it = iterator
      var array = Array[Agent]()
      while (it.hasNext) {
        array = array :+ it.next()
      }
      new ArrayAgentSet(kind, "", array)
    }
  }

  private class FilteringIterator(agentIterator: AgentIterator) extends AgentIterator {
    var nextAgent: Agent = null

    // find the first agent (if there is one):
    while ((others.contains(nextAgent) || nextAgent == null) && agentIterator.hasNext)
      nextAgent = agentIterator.next()
    if (others.contains(nextAgent))
      nextAgent = null

    override def hasNext: Boolean = {
      if (nextAgent != null && ! others.contains(nextAgent) && nextAgent.id != -1)
        true
      else {
        while ((others.contains(nextAgent) || nextAgent == null) && agentIterator.hasNext)
          nextAgent = agentIterator.next()
        if (! others.contains(nextAgent) && nextAgent != null && nextAgent.id != -1) {
          true
        } else {
          nextAgent = null
          false
        }

      }

    }

    override def next(): Agent = {
      if (hasNext) {
        val ret = nextAgent
        nextAgent = null
        ret
      } else
        agentIterator.next()
    }

  }

}

