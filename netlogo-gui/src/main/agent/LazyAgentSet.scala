// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.LogoList
import org.nlogo.{api, core}

import scala.collection.mutable

class LazyAgentSet(printName: String,
                   private val agentSet: AgentSet,
                   private var others: List[Agent] = List(),
                   private var withs: List[(Agent) => Boolean] = List())
  extends AgentSet(agentSet.kind, printName) {

//  def noFilters: Boolean =
//    others.isEmpty && withs.isEmpty

  var forcedSet : AgentSet = null

//  def isEmpty = force().isEmpty

  def isEmpty = {
    !iterator.hasNext
  }

  def count = force().count

//  def contains(a: api.Agent): Boolean = force().contains(a)
  def contains(a: api.Agent): Boolean = {
    val it = iterator
    while (it.hasNext)
      if (it.next() eq a)
        return true
    false
  }

  def containsSameAgents(otherSet: api.AgentSet): Boolean =
    force().containsSameAgents(otherSet)

  def iterator: AgentIterator =
    new FilteringIterator(agentSet.iterator)

  def shufflerator(rng: api.MersenneTwisterFast): AgentIterator =
    new FilteringIterator(agentSet.shufflerator(rng))

  def randomOne(precomputedCount: Int, rng: api.MersenneTwisterFast): Agent = {
    val random = rng.nextInt(precomputedCount)
    val iter = iterator
    var i = 0
    while (i < random) {
      iter.next()
      i += 1
    }
    iter.next()
  }


  def randomTwo(precomputedCount: Int, rng: api.MersenneTwisterFast): Array[Agent] = {
//    var smallRandom = rng.nextInt(precomputedCount)
//    var bigRandom = rng.nextInt(precomputedCount)
//    if (smallRandom > bigRandom) {
//      val temp = smallRandom
//      smallRandom = bigRandom
//      bigRandom = temp
//    }
//
//    val it = iterator
//    var i = 0
//    // skip to the first random place
//    while(i < smallRandom) {
//      it.next()
//      i += 1
//    }
//    val first = it.next()
//    i += 1
//    while (i < bigRandom) {
//      it.next()
//      i += 1
//    }
//    val second = it.next()
//    Array(first, second)
    randomSubsetGeneral(2, precomputedCount, rng)
  }

  def randomSubsetGeneral(resultSize: Int, precomputedCount: Int, rng: api.MersenneTwisterFast): Array[Agent] = {
    val result = new Array[Agent](resultSize)
//    if (precomputedCount == arraySize) {
//      var i, j = 0
//      while (j < resultSize) {
//        if (rng.nextInt(precomputedCount - i) < resultSize - j) {
//          result(j) = array(i)
//          j += 1
//        }
//        i += 1
//      }
//    } else {
    val iter = iterator
    var i, j = 0
    while (j < resultSize) {
      val next = iter.next()
      if (rng.nextInt(precomputedCount - i) < resultSize - j) {
        result(j) = next
        j += 1
      }
      i += 1
    }
//    }
    result
  }

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
  // 5. once a lazy agent set is forced, it will never need to be forced again.
  def force(): AgentSet = {
    if (forcedSet == null) {
      if (others.isEmpty && withs.isEmpty) {
        forcedSet = agentSet
      }
      else {
        //unrolled buffer/mutable buffer.toArray, pre allocate array to count size
        val it = iterator
        var l = new mutable.UnrolledBuffer[Agent]()
        while (it.hasNext) {
          l = l :+ it.next()
        }
        forcedSet = new ArrayAgentSet(kind, null, l.toArray)
      }
    }
    forcedSet
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

