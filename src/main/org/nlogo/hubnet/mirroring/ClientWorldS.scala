package org.nlogo.hubnet.mirroring

import java.util.Comparator

private object ClientWorldS {

  case class TurtleKey(who: Long, breedIndex: Int)

  class TurtleKeyComparator extends Comparator[TurtleKey] {
    override def compare(tk1: TurtleKey, tk2: TurtleKey) =
      if (tk1.breedIndex == tk2.breedIndex)
        (tk1.who - tk2.who).toInt
      else
        tk1.breedIndex - tk2.breedIndex
  }

  case class LinkKey(id: Long, end1: Long, end2: Long, breedIndex: Int)

  class LinkKeyComparator extends Comparator[LinkKey] {
    override def compare(key1: LinkKey, key2: LinkKey): Int = {
      if (key1.end1 == key2.end1) {
        if (key1.end2 == key2.end2) {
          if (key1.breedIndex == key2.breedIndex) {
            return (key1.id - key2.id).toInt
          } else {
            return key1.breedIndex - key2.breedIndex
          }
        } else {
          return (key1.end2 - key2.end2).toInt
        }
      } else {
        return (key1.end1 - key2.end1).toInt
      }
    }
  }

}
