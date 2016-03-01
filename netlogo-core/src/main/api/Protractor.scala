// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait Protractor {

  @throws(classOf[AgentException])
  def towards(fromX: Double, fromY: Double,
              toX: Double, toY: Double,
              wrap: Boolean): Double

  @throws(classOf[AgentException])
  def towardsPitch(fromX: Double, fromY: Double, fromZ: Double,
                   toX: Double, toY: Double, toZ: Double,
                   wrap: Boolean): Double

}
