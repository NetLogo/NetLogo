// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import java.util

import org.nlogo.core.LogoList
import org.nlogo.{api, core}

import scala.collection.mutable
import java.util.ArrayList

import org.nlogo.api.MersenneTwisterFast

class LazyAgentSet(printName: String,
                   val agentSet: AgentSet,
                   private var other: Agent = null,
                   private var withs: ArrayList[(Agent) => Boolean] = new ArrayList())
  extends AgentSet(agentSet.kind, printName) {

  var forcedSet : AgentSet = null

  def isEmpty = {
    !iterator.hasNext
  }

  def count = {
    var result = 0
    val iter = iterator
    while (iter.hasNext) {
      iter.next()
      result += 1
    }
    result

  }

  def contains(a: api.Agent): Boolean = {
    val it = iterator
    while (it.hasNext)
      if (it.next() eq a)
        return true
    false
  }

  def getArray = agentSet.getArray

  def containsSameAgents(otherSet: api.AgentSet): Boolean =
    otherSet match {
      case l : LazyAgentSet => force().containsSameAgents(l.force())
      case _ => force().containsSameAgents(otherSet)
    }

  def iterator: AgentIterator =
    new FilteringIterator(agentSet.getArray)

  def shufflerator(rng: api.MersenneTwisterFast): AgentIterator =
    new FilteringShufflerator(agentSet.getArray, rng)

  def randomOne(precomputedCount: Int, rng: api.MersenneTwisterFast): Agent =
//    force().randomOne(precomputedCount, rng) // precomputedCount is wrong??
  {
    val forcedSet = force().getArray
    forcedSet(rng.nextInt(forcedSet.size))
  }

  def randomTwo(precomputedCount: Int, rng: api.MersenneTwisterFast): Array[Agent] =
//    force().randomTwo(precomputedCount, rng)
  {
    val forcedSet = force().getArray
    val fsize = forcedSet.size
    val (smallRandom, bigRandom) = {
      val r1 = rng.nextInt(precomputedCount)
      val r2 = rng.nextInt(precomputedCount - 1)
      if (r2 >= r1) (r1, r2 + 1) else (r2, r1)
    }
    Array(forcedSet(smallRandom), forcedSet(bigRandom))
  }

  def randomSubsetGeneral(resultSize: Int, precomputedCount: Int, rng: api.MersenneTwisterFast): Array[Agent] =
//    force().randomSubsetGeneral(resultSize, precomputedCount, rng)
  {
    val forcedSet = force().getArray
    val fsize = forcedSet.size
    val result = new Array[Agent](resultSize)

    var i, j = 0
    while (j < resultSize) {
      if (rng.nextInt(fsize - i) < resultSize - j) {
        result(j) = forcedSet(i)
        j += 1
      }
      i += 1
    }

    result
  }

  def toLogoList: LogoList = force().toLogoList

  def lazyOther(agent: Agent): Unit = {
//    others = agent :: others

//    others.add(agent)

    if (other == null)
      other = agent
    else
      throw new IllegalStateException()
  }

  def lazyWith(filter: (Agent) => Boolean): Unit = {
//    withs = withs :+ filter
    withs.add(filter)
  }

  def passesWiths(agent: Agent): Boolean = {
    var i = 0
    while (i < withs.size) {
      if (! withs.get(i)(agent)) {
        return false
      }
      i += 1
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
      if (other == null && withs.isEmpty) {
        forcedSet = agentSet
      } else {
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

  private class FilteringShufflerator(array: Array[Agent], rng: MersenneTwisterFast) extends AgentIterator {
    private[this] var i = 0
    private[this] val copy = array.clone
    private[this] var nextOne: Agent = null

    def passesFilters(agent: Agent): Boolean = {
      (other == null || other.id != agent.id) && passesWiths(agent)
    }

    while (i < copy.length && copy(i) == null)
      i += 1
    fetch()
    override def hasNext =
      nextOne != null
    override def next(): Agent = {
      val result = nextOne
      fetch()
      result
    }
    private def fetch() {
      if (i >= copy.length)
        nextOne = null
      else {
        if (i < copy.length - 1) {
          val r = i + rng.nextInt(copy.length - i)
          nextOne = copy(r)
          copy(r) = copy(i)
        }
        else
          nextOne = copy(i)
        i += 1
        // we could have a bunch of different Shufflerator subclasses
        // the same way we have Iterator subclasses in order to avoid
        // having to do both checks, but I'm not
        // sure it's really worth the effort - ST 3/15/06
        if (nextOne == null || nextOne.id == -1 || !passesFilters(nextOne))
          fetch()
      }
    }
  }


  // put in object LazyAgentSet
  // LazyIterator super class?
  private class FilteringIterator(array: Array[Agent]) extends AgentIterator {
    var nextAgent: Agent = null
    var i = 0
    val arraySize = array.size

    def passesFilters(agent: Agent): Boolean = {
      (other == null || other.id != agent.id) && passesWiths(agent)
    }

    override def hasNext: Boolean = {
      if (nextAgent == null) {

        while (nextAgent == null || nextAgent.id == -1 || !passesFilters(nextAgent)) {
          if (i >= arraySize) {
            nextAgent = null
            return false
          }
          nextAgent = array(i)
          i += 1
        }
      }
      true
    }

    override def next(): Agent = {
      if (hasNext) {
        val ret = nextAgent
        nextAgent = null
        ret
      } else
        throw new NoSuchElementException
    }
  }
}

