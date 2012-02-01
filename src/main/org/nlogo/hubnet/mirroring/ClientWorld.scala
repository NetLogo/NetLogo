package org.nlogo.hubnet.mirroring

import java.util.Comparator

private object ClientWorldS {

  case class TurtleKey(who: Long, breedIndex: Int)
  private val turtleOrdering = implicitly[Ordering[(Int, Long)]]
  class TurtleKeyComparator extends Comparator[TurtleKey] {
    override def compare(key1: TurtleKey, key2: TurtleKey) =
      turtleOrdering.compare((key1.breedIndex, key1.who),
                             (key2.breedIndex, key2.who))
  }

  case class LinkKey(id: Long, end1: Long, end2: Long, breedIndex: Int)
  private val linkOrdering = implicitly[Ordering[(Long, Long, Int, Long)]]
  class LinkKeyComparator extends Comparator[LinkKey] {
    override def compare(key1: LinkKey, key2: LinkKey): Int =
      linkOrdering.compare((key1.end1, key1.end2, key1.breedIndex, key1.id),
                           (key2.end1, key2.end2, key2.breedIndex, key2.id))
  }

}

class ClientWorld(printErrors: Boolean = true, numPatches: Option[java.lang.Integer] = None)
extends ClientWorldJ(printErrors, numPatches)
