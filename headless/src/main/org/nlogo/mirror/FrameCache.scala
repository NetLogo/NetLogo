// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

class FrameCache(
  val deltas: () => IndexedSeq[Delta],
  val maxSize: Int) {

  private var cache = Map[Int, Frame]()

  /** Get or reconstruct the Frame for specified index. */
  def get(index: Int): Option[Frame] =
    if (!deltas().isDefinedAt(index))
      None
    else
      cache.get(index).orElse {
        get(index - 1).map { previousFrame =>
          val newFrame = previousFrame.applyDelta(deltas()(index))
          add(index, newFrame) // so we build up our cache
          newFrame
        }
      }

  /**
   * Add a new frame in the cache, and make sure we keep only the 
   * most valuable ones if we are busting maxSize. NP 2013-01-31
   */
  def add(index: Int, frame: Frame) {
    cache += index -> frame
    if (cache.size > maxSize) {
      // if we are busting cache size
      val keepers = cache.keys.toSeq
        .sortBy(-utility(_, index))
        .take(maxSize)
        .toSet
      cache = cache.filterKeys(keepers.contains)
    }
  }

  /**
   * Assigns a value to the different key frames in the cache.
   * The basic idea is to balance the likelihood of needing a frame
   * (value(i), inversely proportional to the distance with the target)
   * with the cost of recalculating the frame (cost(i), based on the
   * distance with the closest predecessor of i).
   *
   * The target frame should be the one we have just added to the cache.
   * Since its distance to itself is 0.0, it will end up having infinite value
   * (which is what we want). The 0th frame will also end up with infinite
   * utility because, having no predecessors, it could not be recalculated.
   *
   * All of this is emphatically just heuristics and could potentially be tweaked.
   * NP 2013-01-31.
   */
  private def utility(index: Int, target: Int): Double = {
    import math._
    def value(i: Int) = 1.0 / sqrt(abs(target - i))
    def cost(i: Int) = {
      val predecessors = cache.keys.filter(_ < i)
      if (predecessors.isEmpty)
        Double.PositiveInfinity
      else
        i - predecessors.max
    }
    pow(cost(index), 2) * value(index)
  }

}