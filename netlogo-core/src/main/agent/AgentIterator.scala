// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

/**
 * This exists purely as a performance hack.  Normal code can just call AgentSet.agents which
 * returns an Iterable[Agent].  But in performance-critical code, using AgentIterator has a
 * performance advantage because type erasure means that if we use Iterator[Agent] there are
 * typecasts going on under the hood, and I found that this actually impacts performance on
 * benchmarks (to the tune of 5% or so on Life Benchmark, for example).  Sigh... - ST 2/9/09
 */

trait AgentIterator {
  def hasNext: Boolean
  def next(): Agent
}
