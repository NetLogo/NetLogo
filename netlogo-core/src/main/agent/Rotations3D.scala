// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.Matrix3D

// This class exists because there are problems with floating point arithmetic.
// You can convince yourself of this by opening a scala session and entering the following:
//
// StrictMath.sin(StrictMath.PI / 2) => Double = 6.123233995736766E-17
//
// So the problem with using real-valued transformations on Matrices is that
// they come up with weird numbers. At the moment, we only use this when working
// with Ties in NetLogo 3D, but we may run into this eventually in other places
// as well. RG 5/26/17

object Rotations3D {
  private def normalize(deg: Double): Double = {
    val modded = deg % 360.0
    if (modded < 0)
      modded + 360.0
    else
      modded
  }
  def zrot(deg: Double): Matrix3D = {
    normalize(deg) match {
      case 90.0 =>
        new Matrix3D(
          Array(0, -1, 0),
          Array(1, 0, 0),
          Array(0, 0, 1))
      case 180.0 =>
        new Matrix3D(
          Array(-1, 0, 0),
          Array(0, -1, 0),
          Array(0, 0, 1))
      case 270.0 =>
        new Matrix3D(
          Array(0, 1, 0),
          Array(-1, 0, 0),
          Array(0, 0, 1))
      case _ =>
        val m = new Matrix3D()
        m.zrot(deg)
        m
    }
  }
}
