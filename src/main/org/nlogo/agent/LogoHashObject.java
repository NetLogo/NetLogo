// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

/* The "general contract" between Object.hashCode() and Object.equals(..) is
   for any Objects a & b, a.hashCode() == b.hashCode() must be true when
   a.equals(b)/b.equals(a) and vice versa.

   Having said that. Object wrapped with this type will pass their equality
   tests off to World.recursivelyEqual( Object, Object ), any changes
   to recursivelyEqual will need to be made to this class's hashCode()
   method to ensure that this general contract remains valid.*/

import org.nlogo.api.Equality;
import org.nlogo.api.LogoList;
import org.nlogo.api.Nobody$;

import java.util.Iterator;

public final strictfp class LogoHashObject {
  private final Object sourceObject;
  private final int hashCode;

  public LogoHashObject(Object sourceObject) {
    this.sourceObject = sourceObject;
    hashCode = calcHashCode();
  }

  public Object getSourceObject() {
    return sourceObject;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LogoHashObject) {
      return Equality.equals
          (sourceObject,
              ((LogoHashObject) obj).getSourceObject());
    } else {
      return Equality.equals(sourceObject, obj);
    }
  }

  /* This addresses the same problem for which we use recursivelyEquals( .. ),
that being that the Double 3.0 needs to equals() and have the same hashCode(),
as the Integer 3 as they are the same in netlogo. Same for two lists with
the same values, as well as a dead turtle and a Nobody. - JMD 10/28/03*/
  @Override
  public int hashCode() {
    return hashCode;
  }

  private static final int NOBODY_CODE = Long.valueOf(-1).hashCode();
  private static final int ZERO_CODE = World.ZERO.hashCode();

  public int calcHashCode() {
    // IEEE 754 math involves two different zeros, positive zero and negative
    // zero. They are supposed to be indistinguishable from NetLogo code.
    // but Sun, in their infinite wisdom, made them have different hash codes
    // when stored in java.lang.Double objects. - ST 12/5/09
    if (sourceObject instanceof Double) {
      return ((Double) sourceObject).doubleValue() == 0.0  // true for both pos and neg zero
          ? ZERO_CODE
          : sourceObject.hashCode();
    }
    // these next two cases are sneaky -- NetLogo considers dead turtles to be
    // equal to each other, and to nobody.  Dead turtles have an id of minus one,
    // so that's what makes the next two cases work.  - ST 10/28/03*/
    else if (sourceObject instanceof Turtle) {
      return Long.valueOf
          (((Turtle) sourceObject).id).hashCode();
    } else if (sourceObject == Nobody$.MODULE$) {
      return NOBODY_CODE;
    } else if (sourceObject instanceof LogoList) {
      // Hash algor for List (from which ArrayList and therefore LogoList extend).
      // Instead of recursing on the original element types,
      // we use a LogoHashObject - JMD 10/28/03*/

      int hashCodeCalc = 1;
      Iterator<Object> listItr = ((LogoList) sourceObject).iterator();
      while (listItr.hasNext()) {
        LogoHashObject lhObj = new LogoHashObject(listItr.next());
        hashCodeCalc = 31 * hashCodeCalc + (lhObj.getSourceObject() == null
            ? 0
            : lhObj.hashCode());
      }
      return hashCodeCalc;
    } else if (sourceObject instanceof AgentSet) {
      int code = 1;
      for (AgentSet.Iterator i = ((AgentSet) sourceObject).iterator();
           i.hasNext();) {
        Agent agent = i.next();
        LogoHashObject obj = new LogoHashObject(agent);
        code = 31 * code + (agent != null ? 0 : obj.hashCode());
      }
      return code;
    } else {
      return sourceObject.hashCode();
    }
  }

}
