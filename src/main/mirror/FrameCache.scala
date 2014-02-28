// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

trait FrameCache {
  def deltas: IndexedSeq[Delta]
  val minFrameCacheSize: Int
  val maxFrameCacheSize: Int

  private var cache = Map[Int, Frame]()

  /** Get or reconstruct the Frame for specified index. */
  def frame(index: Int): Option[Frame] =
    if (!deltas.isDefinedAt(index))
      None
    else
      cache.get(index).orElse {
        frame(index - 1).map { previousFrame =>
          val newFrame = previousFrame.applyDelta(deltas(index))
          addFrameToCache(index, newFrame) // so we build up our cache
          newFrame
        }
      }

  /**
   * Add a new frame in the cache, and make sure we keep only the
   * most valuable ones if we are busting maxSize. NP 2013-01-31
   */
  def addFrameToCache(index: Int, frame: Frame) {
    cache += index -> frame
    if (cache.size > maxFrameCacheSize) {
      // if we are busting cache size
      val keepers = cache.keys.toSeq
        .sortBy(-utility(_, index))
        .take(minFrameCacheSize)
      cache = cache.filterKeys(keepers.contains)
    }
  }

  /**
   * Assigns a value to the different key frames in the cache.
   * The basic idea is to balance the likelihood of needing a frame
   * (value, inversely proportional to the distance with the target)
   * with the cost of recalculating the frame (based on the
   * size of deltas between index and its closest predecessor).
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
    def value = 1.0 / sqrt(abs(target - index))
    def cost = {
      val predecessors = cache.keys.filter(_ < index)
      if (predecessors.isEmpty)
        Double.PositiveInfinity
      else
        (predecessors.max until index)
          .map(deltas(_).size)
          .sum
    }
    pow(cost, 2) * value
  }

}
