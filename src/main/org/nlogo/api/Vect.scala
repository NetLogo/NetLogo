// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// This is an immutable zone. - ST 4/24/08
// Except when we use Array to interoperate with Java code. - ST 7/26/09

import java.lang.StrictMath._

object Vect {
  private val Infinitesimal = 3.2e-15
  private def bindWithinOne(num: Double) = num min 1 max -1
  def axisTransformation(a: Vect, u: Vect, v: Vect, w: Vect) =
    Vect(x = u.x * a.x + v.x * a.y + w.x * a.z,
         y = u.y * a.x + v.y * a.y + w.y * a.z,
         z = u.z * a.x + v.z * a.y + w.z * a.z)
      .normalize
  // a better return type would be (Vect,Vect) but at the moment the callers are mostly in
  // Java so it's easier for the callers if we return an Array - ST 7/26/09
  def toVectors(headingDegrees: Double, pitchDegrees: Double, rollDegrees: Double): Array[Vect] = {
    val heading = toRadians(headingDegrees)
    val pitch = toRadians(pitchDegrees)
    val roll = toRadians(rollDegrees)
    val tmp1 = Vect(sin(heading), cos(heading), tan(pitch)).normalize
    val forward = if (pitch > PI / 2 && pitch <= PI * 1.5) tmp1.invert.normalize else tmp1
    val right = Vect(sin(heading + PI / 2), cos(heading + PI / 2), 0).normalize
    val orthogonal = right.cross(forward)
    val tmp2 = Vect(1, 0, -tan(roll))
    val standardRight = if(roll > PI / 2 && roll <= PI * 1.5) tmp2.invert else tmp2
    Array(forward, Vect.axisTransformation(standardRight.normalize, right, forward, orthogonal))
  }
  // a better return type would be (Double,Double,Double) but at the moment the callers are mostly
  // in Java so it's easier for the callers if we return an Array - ST 7/26/09
  def toAngles(forward: Vect, right: Vect): Array[Double] = {
    val pitchCalc = asin(bindWithinOne(forward.z))
    val h = Vect(0, 1, 0).angle(Vect(forward.x, forward.y, 0))
    val headingCalc = if(forward.x < 0) 2 * PI - h else h
    val r = right.angle(Vect(sin(headingCalc + (PI / 2)), cos(headingCalc + (PI / 2)), 0))
    val rollCalc = if(right.z > 0) 2 * PI - r else r
    Array(toDegrees(headingCalc), toDegrees(pitchCalc), toDegrees(rollCalc))
  }
}

case class Vect(x: Double, y: Double, z: Double){
  import Vect._

  def magnitude = sqrt(x * x + y * y + z * z)
  def zeroify(d: Double) = if(abs(d) < Infinitesimal) 0.0 else d
  def invert = Vect(-x, -y, -z)
  def add(v: Vect) = Vect(x + v.x, y + v.y, z + v.z)
  def subtract(v: Vect) = Vect(x - v.x, y - v.y, z - v.z)
  def correct = Vect(zeroify(x), zeroify(y), zeroify(z))
  def normalize = {
    val mag = magnitude
    if(mag == 0) this else Vect(x / mag, y / mag, z / mag)
  }
  def rotateX(delta: Double) = {
    val deltaRadians = toRadians(delta)
    val deltaSin = sin(deltaRadians)
    val deltaCos = cos(deltaRadians)
    Vect(x = x, y = y * deltaCos - z * deltaSin, z = y * deltaSin + z * deltaCos).correct
  }
  def rotateY(delta: Double) = {
    val deltaRadians = toRadians(delta)
    val deltaSin = sin(deltaRadians)
    val deltaCos = cos(deltaRadians)
    Vect(x = z * deltaSin + x * deltaCos, y = y, z = z * deltaCos - x * deltaSin).correct
  }
  def rotateZ(delta: Double) = {
    val deltaRadians = toRadians(delta)
    val deltaSin = sin(deltaRadians)
    val deltaCos = cos(deltaRadians)
    Vect(x = x * deltaCos - y * deltaSin, y = x * deltaSin + y * deltaCos, z = z).correct
  }
  def transform(trans: Matrix3D) = {
    val vec = Array(x, y, z)
    trans.transform(vec, vec, 1)
    Vect(vec(0), vec(1), vec(2))
  }
  def dot(v: Vect) = x * v.x + y * v.y + z * v.z
  def cross(v: Vect) =
    Vect(x = y * v.z - z * v.y, y = z * v.x - x * v.z, z = x * v.y - y * v.x).normalize
  def angleTo(v: Vect) = {
    if(magnitude == 0 || v.magnitude == 0) 0.0
    else {
      val value = normalize.dot(v.normalize)
      val angle = acos(Vect.bindWithinOne(value))
      if(cross(v).z == -1) 2 * PI - angle else angle
    }
  }
  private def angle(v: Vect) = acos(Vect.bindWithinOne(normalize.dot(v.normalize)))
}
