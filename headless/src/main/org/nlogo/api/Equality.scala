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
          l1.size == l2.size && (l1.toIterator sameElements l2.toIterator)
        case (t1: Turtle, t2: Turtle) =>
          // works even if both turtles are dead!
          t1.id == t2.id
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

}
