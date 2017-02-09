// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.LogoList
import org.nlogo.{api, core}

import scala.collection.mutable

class LazyAgentSet(kind: core.AgentKind, printName: String, private val agentSet: AgentSet, private var others: List[Agent] = List(), private var withs: List[(Agent) => Boolean] = List())
  extends AgentSet(kind, printName) {

//  def noFilters: Boolean =
//    others.isEmpty && withs.isEmpty

  def isEmpty = force().isEmpty

  def count = force().count

  def contains(a: api.Agent): Boolean = force().contains(a)

  def containsSameAgents(otherSet: api.AgentSet): Boolean =
    force().containsSameAgents(otherSet)

  def iterator: AgentIterator =
      new FilteringIterator(agentSet.iterator)

  def shufflerator(rng: api.MersenneTwisterFast): AgentIterator =
    force().shufflerator(rng)

  def randomOne(precomputedCount: Int, random: Int): Agent =
    force().randomOne(precomputedCount, random)

  def randomTwo(precomputedCount: Int, smallRandom: Int, bigRandom: Int): Array[Agent] =
    force().randomTwo(precomputedCount, smallRandom, bigRandom)

  def randomSubsetGeneral(resultSize: Int, precomputedCount: Int, rng: api.MersenneTwisterFast): Array[Agent] =
    force().randomSubsetGeneral(resultSize, precomputedCount, rng)

  def toLogoList: LogoList = force().toLogoList

  def lazyOther(agent: Agent): Unit = {
    others = agent :: others
  }

  def lazyWith(filter: (Agent) => Boolean): Unit = {
    withs = withs :+ filter
  }

  def passesWiths(agent: Agent): Boolean = {
    for (filter <- withs) {
      if (! filter(agent))
        return false
    }
    true
  }

  def passesFilters(agent: Agent): Boolean = {
    ! others.contains(agent) && passesWiths(agent)
  }

  def force(): AgentSet = {
    if (others.isEmpty && withs.isEmpty)
      agentSet
    else {
      //unrolled buffer/mutable buffer.toArray, pre allocate array to count size
      val it = iterator
      var l = new mutable.UnrolledBuffer[Agent]()
      while (it.hasNext) {
        l = l :+ it.next()
      }
      new ArrayAgentSet(kind, "", l.toArray)
    }
  }

  private class FilteringIterator(agentIterator: AgentIterator) extends AgentIterator {
    var nextAgent: Agent = null

    override def hasNext: Boolean = {
      if (nextAgent != null && nextAgent.id != -1 && passesFilters(nextAgent))
        true
      else {
        while ((nextAgent == null || ! passesFilters(nextAgent)) && agentIterator.hasNext)
          nextAgent = agentIterator.next()
        if (nextAgent != null && passesFilters(nextAgent) && nextAgent.id != -1) {
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

