package org.nlogo.gl.render;

import org.nlogo.api.Vect;
import org.nlogo.api.World;

class Picker {

  // this class is not instantiable
  private Picker() {
    throw new IllegalStateException();
  }

  // adapted from code at http://softsurfer.com/Archive/algorithm_0106/algorithm_0106.htm
  // Copyright 2001, softSurfer (www.softsurfer.com)
  // This code may be freely used and modified for any purpose
  // providing that this copyright notice is included with it.
  // SoftSurfer makes no warranty for this code, and cannot be held
  // liable for any real or imagined damage resulting from its use.
  // Users of this code must verify correctness for their application.
  static double distanceFromRayToSegment(double[][] ray, double[] end1, double[] end2) {
    Vect u = new Vect((end2[0] - end1[0]),
        (end2[1] - end1[1]),
        (end2[2] - end1[2]));
    Vect v = new Vect((ray[1][0] - ray[0][0]),
        (ray[1][1] - ray[0][1]),
        (ray[1][2] - ray[0][2]));
    Vect w = new Vect((end1[0] - ray[0][0]),
        (end1[1] - ray[0][1]),
        (end1[2] - ray[0][2]));

    double a = u.dot(u); // always >= 0
    double b = u.dot(v);
    double c = v.dot(v);  // always >= 0
    double d = u.dot(w);
    double e = v.dot(w);
    double D = a * c - b * b;       // always >= 0
    double sc, sN, sD = D;        // sc = sN / sD, default sD = D >= 0
    double tc, tN, tD = D;        // tc = tN / tD, default tD = D >= 0

    // compute the line parameters of the two closest points
    if (D < World.INFINITESIMAL) { // the lines are almost parallel
      sN = 0.0;        // force using point P0 on segment S1
      sD = 1.0;        // to prevent possible division by 0.0 later
      tN = e;
      tD = c;
    } else {  // get the closest points on the infinite lines
      sN = (b * e - c * d);
      tN = (a * e - b * d);
      if (sN < 0.0) { // sc < 0 => the s=0 edge is visible
        sN = 0.0;
        tN = e;
        tD = c;
      } else if (sN > sD) {  // sc > 1 => the s=1 edge is visible
        sN = sD;
        tN = e + b;
        tD = c;
      }
    }

    // finally do the division to get sc and tc
    sc = Math.abs(sN) < World.INFINITESIMAL ? 0.0 : sN / sD;
    tc = Math.abs(tN) < World.INFINITESIMAL ? 0.0 : tN / tD;

    // get the difference of the two closest points
    Vect dP = new Vect((u.x() * sc) - (v.x() * tc) + w.x(),
        (u.y() * sc) - (v.y() * tc) + w.y(),
        (u.z() * sc) - (v.z() * tc) + w.z()); // = S1(sc) - S2(tc)

    return dP.magnitude();   // return the closest distance
  }

}
