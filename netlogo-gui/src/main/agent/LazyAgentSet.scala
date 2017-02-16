// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.LogoList
import org.nlogo.{api, core}

import scala.collection.mutable

class LazyAgentSet(kind: core.AgentKind,
                   printName: String,
                   private val agentSet: AgentSet,
                   private var others: List[Agent] = List(),
                   private var withs: List[(Agent) => Boolean] = List())
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
      if (! filter(agent)) {
        return false
      }
    }
    true
  }

  def passesFilters(agent: Agent): Boolean = {
    ! others.contains(agent) && passesWiths(agent)
  }

  // assumptions:
  // 1. agents only accessed after force
  // 2. agents only modified by side-effects of filters within force
  // 3. agents that pass filters are immediately included in the resulting AgentSet
  // 4. filters don't change between calls to hasNext
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
      new ArrayAgentSet(kind, null, l.toArray)
    }
  }

  private class FilteringIterator(agentIterator: AgentIterator) extends AgentIterator {
    var nextAgent: Agent = null

    override def hasNext: Boolean = {
      if (nextAgent != null && nextAgent.id != -1)
        true
      else {
        var passes = false
        var next = false
        do {

          next = agentIterator.hasNext
          if (next) {
            nextAgent = agentIterator.next()
            passes = passesFilters(nextAgent)
          }

        } while ((nextAgent == null || ! passes) && next)

        if (nextAgent != null && passes && nextAgent.id != -1) {
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

