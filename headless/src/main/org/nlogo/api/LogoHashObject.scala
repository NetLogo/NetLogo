// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/* The "general contract" between Object.hashCode() and Object.equals(..) is for any Objects a & b,
   a.hashCode() == b.hashCode() must be true when a.equals(b)/b.equals(a) and vice versa.

   Having said that. Object wrapped with this type will pass their equality tests off to
   World.recursivelyEqual( Object, Object ), any changes to recursivelyEqual will need to be made to
   this class's hashCode() method to ensure that this general contract remains valid.

   This addresses the same problem for which we use recursivelyEquals( .. ), that being that the
   Double 3.0 needs to equals() and have the same hashCode(), as the Integer 3 as they are the same
   in netlogo. Same for two lists with the same values, as well as a dead turtle and a Nobody.

   - JMD 10/28/03 */

class LogoHashObject(val sourceObject: AnyRef) {

  override def equals(obj: Any): Boolean =
    Equality.equals(
      sourceObject,
      obj match {
        case lho: LogoHashObject =>
          lho.sourceObject
        case _ =>
          obj.asInstanceOf[AnyRef]
      })

  override val hashCode: Int =
    sourceObject match {

      // IEEE 754 math involves two different zeros, positive zero and negative
      // zero. They are supposed to be indistinguishable from NetLogo code.
      // but Sun, in their infinite wisdom, made them have different hash codes
      // when stored in java.lang.Double objects. - ST 12/5/09
      case d: java.lang.Double if d.doubleValue == 0.0 =>
        LogoHashObject.ZeroCode

      // these next three cases are sneaky -- NetLogo considers dead turtles to be
      // equal to each other, and to nobody.  Dead turtles have an id of minus one,
      // so that's what makes these cases work.  - ST 10/28/03, 11/20/12
      case t: Turtle =>
        t.id.hashCode

      case l: Link =>
        l.id.hashCode

      case Nobody =>
        LogoHashObject.NobodyCode

      case ll: LogoList =>
        ll.foldLeft(1){(result, next) =>
          31 * result + (if (next == null) 0 else new LogoHashObject(next).hashCode)}

      case set: AgentSet =>
        import collection.JavaConverters._
        set.agents.asScala.foldLeft(1){(result, next) =>
          31 * result + new LogoHashObject(next).hashCode}

      case _ =>
        sourceObject.hashCode

    }

}

object LogoHashObject {
  private val NobodyCode = -1L.hashCode
  private val ZeroCode = Double.box(0.0).hashCode
}
