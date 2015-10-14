// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait World3D extends World {
  def worldDepth: Int
  def protractor3D: Protractor3D
  def minPzcor: Int
  def maxPzcor: Int
  def wrappedObserverZ(z: Double): Double
  def wrapZ(z: Double): Double  // World3D always wraps at present, so no AgentException - ST 3/3/09
  def followOffsetZ: Double
  @throws(classOf[AgentException])
  def getPatchAt(x: Double, y: Double, z: Double): Patch
}
