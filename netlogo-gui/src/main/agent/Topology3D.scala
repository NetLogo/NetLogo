// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.AgentException

trait Topology3D {
  def distanceWrap(dx: Double, dy: Double, dz: Double,
                   x1: Double, y1: Double, z1: Double,
                   x2: Double, y2: Double, z2: Double): Double
  def towardsPitchWrap(dx: Double, dy: Double, dz: Double): Double
  @throws(classOf[AgentException])
  def getPatchAt(xc: Double, yc: Double, zc: Double): Patch
  def getNeighbors3d(source: Patch3D): IndexedAgentSet
  def getNeighbors6(source: Patch3D): IndexedAgentSet
  def getPNU(source: Patch3D): Patch
  def getPEU(source: Patch3D): Patch
  def getPSU(source: Patch3D): Patch
  def getPWU(source: Patch3D): Patch
  def getPNEU(source: Patch3D): Patch
  def getPSEU(source: Patch3D): Patch
  def getPSWU(source: Patch3D): Patch
  def getPNWU(source: Patch3D): Patch
  def getPND(source: Patch3D): Patch
  def getPED(source: Patch3D): Patch
  def getPSD(source: Patch3D): Patch
  def getPWD(source: Patch3D): Patch
  def getPNED(source: Patch3D): Patch
  def getPSED(source: Patch3D): Patch
  def getPSWD(source: Patch3D): Patch
  def getPNWD(source: Patch3D): Patch
  def observerZ: Double
  def wrapZ(z: Double): Double
  def shortestPathZ(z1: Double, z2: Double): Double
}
