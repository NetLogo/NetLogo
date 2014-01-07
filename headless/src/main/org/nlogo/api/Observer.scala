// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait Observer extends Agent with Camera {

  /** Returns the currently watched or followed agent (or nobody) */
  def targetAgent: Agent

  /** Returns the current perspective */
  def perspective: Perspective
  def setPerspective(p: Perspective, a: Agent)
  
  def followOffsetX: Double
  def followOffsetY: Double

  /** Returns the current distance behind the followed turtle the 3D view is displaying */
  def followDistance: Int

  def oxcor: Double
  def oycor: Double

}
