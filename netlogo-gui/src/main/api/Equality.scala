// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object Equality {

  def equals(o1: AnyRef, o2: AnyRef): Boolean =
    (o1 eq o2) ||
      ((o1, o2) match {
        case (d1: java.lang.Double, d2: java.lang.Double) =>
          // we can't rely on Double.equals() because it considers
          // negative and positive zero to be different. - ST 7/12/06
          d1.doubleValue == d2.doubleValue
        case (l1: LogoList, l2: LogoList) =>
          sameElements(l1, l2)
        case (t1: Turtle, t2: Turtle) =>
          // works even if both turtles are dead!
          t1.id == t2.id
        case (l1: Link, l2: Link) =>
          // works even if both links are dead!
          l1.id == l2.id
        case (Nobody, a: Agent) =>
          a.id == -1
        case (a: Agent, Nobody) =>
          a.id == -1
        case (as1: AgentSet, as2: AgentSet) =>
          as1.equalAgentSets(as2)
        case (eo: ExtensionObject, _) =>
          eo.recursivelyEqual(o2)
        case (_, eo: ExtensionObject) =>
          eo.recursivelyEqual(o1)
        case _ =>
          o1 == o2
      })

  private def sameElements(l1: LogoList, l2: LogoList): Boolean =
    if (l1.size != l2.size)
      false
    else {
      val iter1 = l1.scalaIterator
      val iter2 = l2.scalaIterator
      while(iter1.hasNext)
        if (!equals(iter1.next(), iter2.next()))
          return false
      true
    }

}
