// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import
  org.nlogo.api,
  org.nlogo.util.MersenneTwisterFast

// ArrayAgentSets are only used for agentsets which are never added to
// after they are initially created.  However note that turtles and
// links can die, so we may end up with an array containing some dead
// agents (agents with id -1). NP 2013-08-28.

class ArrayAgentSet(
  kind: api.AgentKind,
  printName: String,
  val array: Array[Agent])
extends AgentSet(kind, printName, false, false, false) {

  /// conversions

  override def toLogoList = {
    val freshArray =
      if (!kind.mortal)
        array.clone
      else {
        val buf = collection.mutable.ArrayBuffer[Agent]()
        val iter = iterator
        while (iter.hasNext)
          buf += iter.next()
        buf.toArray
      }
    java.util.Arrays.sort(freshArray.asInstanceOf[Array[AnyRef]])
    api.LogoList.fromIterator(freshArray.iterator)
  }

  /// counting

  override def isEmpty =
    if (!kind.mortal)
      array.isEmpty
    else
      !iterator.hasNext

  override def count =
    if (!kind.mortal)
      array.size
    else {
      var result = 0
      val iter = iterator
      while(iter.hasNext) {
        iter.next()
        result += 1
      }
      result
    }

  /// equality

  // assumes we've already checked for equal counts - ST 7/6/06
  override def equalAgentSetsHelper(otherSet: api.AgentSet) = {
    val set = collection.mutable.HashSet[api.Agent]()
    val iter = iterator
    while (iter.hasNext)
      set += iter.next()
    import collection.JavaConverters._
    otherSet.agents.asScala.forall(set.contains)
  }

  /// one-agent queries

  override def getAgent(id: AnyRef) =
    array(id.asInstanceOf[java.lang.Double].intValue)

  override def contains(agent: api.Agent): Boolean = {
    val iter = iterator
    while (iter.hasNext)
      if (iter.next() eq agent)
        return true
    false
  }

  /// random selection

  // the next few methods take precomputedCount as an argument since we want to avoid _randomoneof
  // and _randomnof resulting in more than one total call to count(), since count() can be O(n)
  // - ST 2/27/03

  // assume agentset is nonempty, since _randomoneof.java checks for that
  override def randomOne(precomputedCount: Int, random: Int) =
    if (!kind.mortal)
      array(random)
    else {
      val iter = iterator
      var i = 0
      while (i < random) {
        iter.next()
        i += 1
      }
      iter.next()
    }

  // This is used to optimize the special case of randomSubset where size == 2
  override def randomTwo(precomputedCount: Int, ran1: Int, ran2: Int): Array[Agent] = {
    // we know precomputedCount, or this method would not have been called.
    // see randomSubset().
    val (random1, random2) =
      if (ran2 >= ran1)
        // if random2 >= random1, we need to increment random2 to choose a later agent.
        (ran1, ran2 + 1)
      else
        (ran2, ran1)
    if (!kind.mortal)
      Array(
        array(random1),
        array(random2))
    else {
      val it = iterator
      var i = 0
      // skip to the first random place
      while(i < random1) {
        it.next()
        i += 1
      }
      Array(it.next(), {
        // skip to the second random place
        i += 1
        while (i < random2) {
          it.next()
          i += 1
        }
        it.next()
      })
    }
  }

  override def randomSubsetGeneral(resultSize: Int, precomputedCount: Int, random: MersenneTwisterFast) = {
    val result = new Array[Agent](resultSize)
    if (precomputedCount == array.size) {
      var i, j = 0
      while (j < resultSize) {
        if (random.nextInt(precomputedCount - i) < resultSize - j) {
          result(j) = array(i)
          j += 1
        }
        i += 1
      }
    }
    else {
      val iter = iterator
      var i, j = 0
      while (j < resultSize) {
        val next = iter.next()
        if (random.nextInt(precomputedCount - i) < resultSize - j) {
          result(j) = next
          j += 1
        }
        i += 1
      }
    }
    result
  }

  /// iterator methods

  // returns an Iterator object of the appropriate class
  override def iterator: AgentIterator =
    if (!kind.mortal)
      new Iterator
    else
      new DeadSkippingIterator

  // shuffling iterator = shufflerator! (Google hits: 0)
  // Update: Now 5 Google hits, the first 4 of which are NetLogo related,
  // and the last one is a person named "SHUFFLER, Ator", which Google thought
  // was close enough!  ;-)  ~Forrest (10/3/2008)

  override def shufflerator(rng: MersenneTwisterFast): AgentIterator =
    // note it at the moment (and this should probably be fixed)
    // Job.runExclusive() counts on this making a copy of the
    // contents of the agentset - ST 12/15/05
    new Shufflerator(rng)

  /// iterator implementations

  private class Iterator extends AgentIterator {
    protected var index = 0
    override def hasNext = index < array.size
    override def next() = {
      val result = array(index)
      index += 1
      result
    }
  }

  // extended to skip dead agents
  private class DeadSkippingIterator extends Iterator {
    // skip initial dead agents
    while (index < array.size && array(index).id == -1)
      index += 1
    override def next() = {
      val result = index
      // skip to next live agent
      do index += 1
      while (index < array.size && array(index).id == -1)
      array(result)
    }
  }

  private class Shufflerator(rng: MersenneTwisterFast) extends Iterator {
    private[this] var i = 0
    private[this] val copy = array.clone
    private[this] var nextOne: Agent = null
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
        if (nextOne == null || nextOne.id == -1)
          fetch()
      }
    }
  }

}
