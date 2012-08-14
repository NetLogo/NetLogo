// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import org.nlogo.api.{ Constants, Vect }

object Picker {

  // adapted from code at softsurfer.com/Archive/algorithm_0106/algorithm_0106.htm
  // Copyright 2001, softSurfer (www.softsurfer.com)
  // This code may be freely used and modified for any purpose
  // providing that this copyright notice is included with it.
  // SoftSurfer makes no warranty for this code, and cannot be held
  // liable for any real or imagined damage resulting from its use.
  // Users of this code must verify correctness for their application.
  def distanceFromRayToSegment(ray: Array[Array[Double]], end1: Array[Double], end2: Array[Double]): Double = {
    val u = Vect((end2(0) - end1(0)),
                 (end2(1) - end1(1)),
                 (end2(2) - end1(2)))
    val v = Vect((ray(1)(0) - ray(0)(0)),
                 (ray(1)(1) - ray(0)(1)),
                 (ray(1)(2) - ray(0)(2)))
    val w = Vect((end1(0) - ray(0)(0)),
                 (end1(1) - ray(0)(1)),
                 (end1(2) - ray(0)(2)))

    val a = u.dot(u) // always >= 0
    val b = u.dot(v)
    val c = v.dot(v)  // always >= 0
    val d = u.dot(w)
    val e = v.dot(w)
    val D = a * c - b * b       // always >= 0
    var sc, sN, sD = D        // sc = sN / sD, default sD = D >= 0
    var tc, tN, tD = D        // tc = tN / tD, default tD = D >= 0

    // compute the line parameters of the two closest points
    if (D < Constants.Infinitesimal) { // the lines are almost parallel
      sN = 0.0        // force using point P0 on segment S1
      sD = 1.0        // to prevent possible division by 0.0 later
      tN = e
      tD = c
    }
    else {  // get the closest points on the infinite lines
      sN = b * e - c * d
      tN = a * e - b * d
      if (sN < 0.0) { // sc < 0 => the s=0 edge is visible
        sN = 0.0
        tN = e
        tD = c
      }
      else if (sN > sD) {  // sc > 1 => the s=1 edge is visible
        sN = sD
        tN = e + b
        tD = c
      }
    }

    // finally do the division to get sc and tc
    sc = if (math.abs(sN) < Constants.Infinitesimal) 0.0 else (sN / sD)
    tc = if (math.abs(tN) < Constants.Infinitesimal) 0.0 else (tN / tD)

    // get the difference of the two closest points
    val dP = Vect((u.x * sc) - (v.x * tc) + w.x,
                  (u.y * sc) - (v.y * tc) + w.y,
                  (u.z * sc) - (v.z * tc) + w.z) // = S1(sc) - S2(tc)

    dP.magnitude   // return the closest distance
  }

}
