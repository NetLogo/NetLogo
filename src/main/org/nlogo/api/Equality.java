package org.nlogo.api;

public final strictfp class Equality {

  // this class is not instantiable
  private Equality() {
    throw new IllegalStateException();
  }

  public static boolean equals(Object o1, Object o2) {
    if (o1 == o2) {
      return true;
    } else if ((o1 instanceof Double) && (o2 instanceof Double)) {
      // we can't rely on Double.equals() because it considers
      // negative and positive zero to be different. - ST 7/12/06
      return ((Double) o1).doubleValue() == ((Double) o2).doubleValue();
    } else if ((o1 instanceof LogoList) && (o2 instanceof LogoList)) {
      LogoList v1 = (LogoList) o1;
      LogoList v2 = (LogoList) o2;
      if (v1.size() != v2.size()) {
        return false;
      }
      for (int i = 0; i < v1.size(); i++) {
        if (!equals(v1.get(i), v2.get(i))) {
          return false;
        }
      }
      return true;
    } else if ((o1 instanceof Turtle) && (o2 instanceof Turtle)) {
      // works even if both turtles are dead!
      return ((Turtle) o1).id() == ((Turtle) o2).id();
    } else if (o1 == Nobody$.MODULE$) {
      return o2 == Nobody$.MODULE$ ||
          (o2 instanceof Turtle && ((Turtle) o2).id() == -1) ||
          (o2 instanceof Link && ((Link) o2).id() == -1);
    } else if (o2 == Nobody$.MODULE$) {
      return (o1 instanceof Turtle && ((Turtle) o1).id() == -1)
          || (o1 instanceof Link && ((Link) o1).id() == -1);
    } else if ((o1 instanceof AgentSet) && (o2 instanceof AgentSet)) {
      return ((AgentSet) o1).equalAgentSets((AgentSet) o2);
    } else if (o1 instanceof ExtensionObject) {
      return ((ExtensionObject) o1).recursivelyEqual(o2);
    } else if (o2 instanceof ExtensionObject) {
      return ((ExtensionObject) o2).recursivelyEqual(o1);
    } else {
      return o1.equals(o2);
    }
  }

}
