// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait Observer extends Agent {

  /** Returns the currently watched or followed agent (or nobody) */
  def targetAgent: Agent

  /** Returns the current perspective */
  def perspective: Perspective
  def setPerspective(p: Perspective)

  def oxcor: Double
  def oycor: Double
  def ozcor: Double

  def orientation: Option[ObserverOrientation]
}

trait ObserverOrientation {
  /** Returns the current distance behind the followed turtle the 3D view is displaying */
  def dist: Double
  def heading: Double
  def pitch: Double
  def roll: Double
  def dx: Double
  def dy: Double
  def dz: Double
}
