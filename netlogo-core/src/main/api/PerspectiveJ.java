// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api;

public strictfp class PerspectiveJ {
  static public final int OBSERVE = 0;
  static public final int RIDE    = 1;
  static public final int FOLLOW  = 2;
  static public final int WATCH   = 3;

  // (I don't think) Java can access the inner objects without reflection, so we provide these
  // convenience methods for use from the handful of Java clients we still have. - ST 7/11/11, 7/27/11

  public static Perspective create(int n) {
    return create(n, null, 0);
  }

  public static Perspective create(int n, Agent agent) {
    return create(n, agent, 0);
  }

  public static Perspective create(int n, Agent agent, int followDistance) {
    Perspective p = null;
    switch (n) {
      case 0:
        p = Perspective.Observe$.MODULE$;
        break;
      case 1:
        p = new Perspective.Ride(agent);
        break;
      case 2:
        p = new Perspective.Follow(agent, followDistance);
        break;
      case 3:
        p = new Perspective.Watch(agent);
        break;
      default:
        throw new RuntimeException("Unexpected perspective: " + n);
    }
    return p;
  }
}
