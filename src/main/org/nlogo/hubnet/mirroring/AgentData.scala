package org.nlogo.hubnet.mirroring

abstract class AgentData extends Overridable {
  def xcor: Double
  def ycor: Double
  def spotlightSize: Double
  def wrapSpotlight: Boolean
}
