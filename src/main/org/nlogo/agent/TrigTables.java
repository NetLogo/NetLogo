package org.nlogo.agent;

public final strictfp class TrigTables {

  // this class is not instantiable
  private TrigTables() {
    throw new IllegalStateException();
  }

  /// cache of sines and cosines of integers 0-359

  // This is to speed up various operations on angles
  // that are integers.  Trigonometry is expensive!!
  // - ST 12/21/06, 12/24/06

  public static final double[] sin = new double[360];
  public static final double[] cos = new double[360];

  static {
    for (int i = 0; i < 360; i++) {
      double headingRadians = StrictMath.toRadians(i);
      double c = StrictMath.cos(headingRadians);
      double s = StrictMath.sin(headingRadians);
      if (StrictMath.abs(c) < org.nlogo.api.World.INFINITESIMAL) {
        c = 0;
      }
      if (StrictMath.abs(s) < org.nlogo.api.World.INFINITESIMAL) {
        s = 0;
      }
      sin[i] = s;
      cos[i] = c;
    }
  }

}
