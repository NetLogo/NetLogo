// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait Link3D extends Link {

  /** Returns the z-coordinate of end1 */
  def z1: Double

  /** Returns the z-coordinate of end2 this coordinate is "unwrapped" so it may actually be outside
    * the bounds of the world */
  def z2: Double

  /** Returns the pitch towards end2 from end1 */
  def pitch: Double

}
