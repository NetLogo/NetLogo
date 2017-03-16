// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.LogoList
import org.nlogo.{api, core}

import scala.collection.mutable

class LazyAgentSet(printName: String,
                   val agentSet: AgentSet,
                   private var others: List[Agent] = List(),
                   private var withs: List[(Agent) => Boolean] = List())
  extends AgentSet(agentSet.kind, printName) {

  var forcedSet : AgentSet = null

  def isEmpty = {
    !iterator.hasNext
  }

  def count = force().count

  def contains(a: api.Agent): Boolean = {
    val it = iterator
    while (it.hasNext)
      if (it.next() eq a)
        return true
    false
  }

  def containsSameAgents(otherSet: api.AgentSet): Boolean =
    otherSet match {
      case l : LazyAgentSet => force().containsSameAgents(l.force())
      case _ => force().containsSameAgents(otherSet)
    }

  def iterator: AgentIterator =
    new FilteringIterator(agentSet.iterator, others, withs)

  def shufflerator(rng: api.MersenneTwisterFast): AgentIterator =
    new FilteringIterator(agentSet.shufflerator(rng), others, withs)

  def randomOne(precomputedCount: Int, rng: api.MersenneTwisterFast): Agent =
    force().randomOne(precomputedCount, rng)

  def randomTwo(precomputedCount: Int, rng: api.MersenneTwisterFast): Array[Agent] =
    force().randomTwo(precomputedCount, rng)

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

  // assumptions:
  // 1. agents only accessed after force
  // 2. agents only modified by side-effects of filters within force
  // 3. agents that pass filters are immediately included in the resulting AgentSet
  // 4. filters don't change between calls to hasNext
  // 5. once a lazy agent set is forced, it will never need to be forced again.
  def force(): AgentSet = {
    if (forcedSet == null) {
      if (others.isEmpty && withs.isEmpty) {
        forcedSet = agentSet
      }
      else {
        //unrolled buffer/mutable buffer.toArray, pre allocate array to count size
        val it = iterator
        val l = new AgentSetBuilder(kind)
        while (it.hasNext) {
          l.add(it.next())
        }
        forcedSet = l.build()
      }
    }
    forcedSet
  }

  private class FilteringIterator(agentIterator: AgentIterator, others: List[Agent], withs: List[Agent => Boolean]) extends AgentIterator {
    var nextAgent: Agent = null

    var foundContained: Boolean = false

    def passesFilters(agent: Agent): Boolean = {
      ! others.contains(agent) && passesWiths(agent)
    }

    override def hasNext: Boolean = {
      if (nextAgent == null) {
        var next = true
        nextAgent = null

        while (nextAgent == null && next) {

          next = agentIterator.hasNext

          if (next) {
            nextAgent = agentIterator.next()
            if (! passesFilters(nextAgent)) {
              nextAgent = null
            }
          }

        }

        nextAgent != null
      } else
        true
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

