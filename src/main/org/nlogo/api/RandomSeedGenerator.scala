// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.util.MersenneTwisterFast
import org.nlogo.util.Exceptions.ignoring

// This is a singleton so that random seeds will be unique VM-wide.  Because for example, we don't
// want multiple experiments in a parallel BehaviorSpace experiment to get the same seeds.
// - ST 6/30/10

object RandomSeedGenerator {

  // To ensure that this never reports the same value twice in a row, we keep track of the last
  // value reported, and wait until we generate a different value.  (If we knew the precision of the
  // system clock, we could just wait that amount. But precision varies across platforms, so we
  // don't).  The initial value of lastResult is arbitrary. It could cause a value to be skipped for
  // no good reason, but that won't ever happen, and it wouldn't matter if it did. - AZS 6/20/05
  private var lastResult = 0.0

  // For the purposes of feeding them to the Mersenne Twister, two seeds are still completely
  // different even if they differ little numerically.  But psychologically, I think people might
  // get suspicious if the seeds we give them don't appear random from invocation to invocation.  So
  // we'll fool them by running the seed itself through the Mersenne Twister. - ST 5/31/06
  private def next =
    (new MersenneTwisterFast).nextInt

  def generateSeed(): Double = synchronized {
    while(true) {
      val result = next
      if(result != lastResult) {
        lastResult = result
        return result.toDouble
      }
      ignoring(classOf[InterruptedException]) {
        Thread.sleep(1)
      }
    }
    throw new IllegalStateException
  }

}
