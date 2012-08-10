// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
 * Provides access to NetLogo observer
 */

trait Observer extends Agent {

  /** Returns the currently watched or followed agent (or nobody) */
  def targetAgent: Agent

  /** Returns the current perspective */
  def perspective: Perspective

  def heading: Double
  def pitch: Double
  def roll: Double
  def oxcor: Double
  def oycor: Double
  def ozcor: Double
  def dx: Double
  def dy: Double
  def dz: Double
  def setPerspective(p: Perspective, a: Agent)

}
