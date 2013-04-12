// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait Observer extends Agent {

  /** Returns the currently watched or followed agent (or nobody) */
  def targetAgent: Agent

  /** Returns the current perspective */
  def perspective: Perspective
  def setPerspective(p: Perspective, a: Agent)

  def oxcor: Double
  def oycor: Double

}
