// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait Turtle3D extends Turtle {

  /** Returns the value of the <code>pitch</code> variable */
  def pitch: Double

  /** Returns the value of the <code>roll</code> variable */
  def roll: Double

  /** Returns the value of the <code>zcor</code> variable */
  def zcor: Double

  /** Returns the x component of the forward vector */
  def dx: Double

  /** Returns the y component of the forward vector */
  def dy: Double

  /** Returns the z component of the forward vector */
  def dz: Double

}
