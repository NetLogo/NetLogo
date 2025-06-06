// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import
  org.nlogo.{ api, core },
    core.{ AgentKind, LogoList },
    api.SimpleChangeEventPublisher,
  org.nlogo.api.MersenneTwisterFast

// Used only for the all-turtles, all-links, and breed agentsets.
//
// We use a tree map here so that regardless of what order the agents
// are put in they come out in the same order (by id) -- otherwise
// we get different results after an import and export. since we don't
// know the order that the turtles entered the breed agentset.

class TreeAgentSet(kind: AgentKind, printName: String)
extends AgentSet(kind, printName) {

  private val _agents = new java.util.TreeMap[AnyRef, Agent]
  override val agents: java.lang.Iterable[api.Agent] =
    _agents.values.asInstanceOf[java.lang.Iterable[api.Agent]]

  val simpleChangeEventPublisher = new SimpleChangeEventPublisher

  override def count = _agents.size

  override def isEmpty = _agents.isEmpty

  override def checkCount(checkValue: Int, check: (Int, Int) => Boolean): Boolean = {
    check(count, checkValue)
  }

  // Assumes we've already checked that the counts are equal. - ST 7/6/06
  override def containsSameAgents(otherSet: api.AgentSet): Boolean = {
    val iter = otherSet.agents.iterator
    while (iter.hasNext)
      if (!contains(iter.next().asInstanceOf[Agent]))
        return false
    true
  }

  def getAgent(id: AnyRef): Agent =
    _agents.get(id)

  private var nextIndex = 0L

  /**
   * It is the caller's responsibility not to add an agent that
   * is already in the set.
   */
  def add(agent: Agent): Unit = {
    require(kind == agent.kind)
    _agents.put(agent.agentKey, agent)
    nextIndex = nextIndex max (agent._id + 1)
    simpleChangeEventPublisher.publish()
  }

  // made public for mutable agentset operations
  def remove(key: AnyRef): Unit = {
    _agents.remove(key)
    simpleChangeEventPublisher.publish()
  }

  def clear(): Unit = {
    _agents.clear()
    simpleChangeEventPublisher.publish()
  }

  override def contains(agent: api.Agent): Boolean = agent match {
    case a: Agent => _agents.containsKey(a.agentKey)
    case _ => _agents.containsValue(agent)
  }

  // the next few methods take precomputedCount as an argument since we want to avoid _randomoneof
  // and _randomnof resulting in more than one total call to count(), since count() can be O(n)
  // - ST 2/27/03

  override def randomOne(precomputedCount: Int, random: Int): Agent = {
    // note: assume agentset nonempty, since _randomoneof.java checks for that
    val iter = iterator
    var i = 0
    while(i < random) {
      iter.next()
      i += 1
    }
    iter.next()
  }

  // This is used to optimize the special case of randomSubset where size == 2
  override def randomTwo(precomputedCount: Int, smallRandom: Int, bigRandom: Int): Array[Agent] = {
    // we know precomputedCount, or this method would not have been called.
    // see randomSubset().
    if (precomputedCount == nextIndex)
      Array(
        _agents.get(Double.box(smallRandom)),
        _agents.get(Double.box(bigRandom)))
    else {
      val it = iterator
      var i = 0
      // skip to the first random place
      while(i < smallRandom) {
        it.next()
        i += 1
      }
      val first = it.next()
      i += 1
      while (i < bigRandom) {
        it.next()
        i += 1
      }
      val second = it.next()
      Array(first, second)
    }
  }

  override def randomSubsetGeneral(resultSize: Int, precomputedCount: Int,
      rng: MersenneTwisterFast): Array[Agent] = {
    val result = new Array[Agent](resultSize)
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
    result
  }

  override def toLogoList =
    LogoList.fromJava(_agents.values)

  // parent enumeration class
  final private class Iterator extends AgentIterator {
    private val iter = _agents.values.iterator
    override def hasNext = iter.hasNext
    override def next() = iter.next()
  }

  // returns an Iterator object of the appropriate class
  override def iterator: AgentIterator =
    new Iterator

  /// shuffling iterator = shufflerator! (Google hits: 0)

  // note that at the moment (and this should probably be fixed) Job.runExclusive() counts on this
  // making a copy of the contents of the agentset - ST 12/15/05
  override def shufflerator(rng: MersenneTwisterFast): AgentIterator =
    new Shufflerator(rng)

  final private class Shufflerator(rng: MersenneTwisterFast)
  extends AgentIterator {
    private var i = 0
    private val copy = _agents.values.toArray(new Array[Agent](_agents.size))
    private var _next: Agent = null
    fetch()
    def hasNext = _next != null
    def next() = {
      val result = _next
      fetch()
      result
    }
    private def fetch(): Unit = {
      if (i >= copy.length)
        _next = null
      else {
        if (i < copy.length - 1) {
          val r = i + rng.nextInt(copy.length - i)
          _next = copy(r)
          copy(r) = copy(i)
        } else {
          _next = copy(i)
        }
        i += 1
      }
    }
  }
}
