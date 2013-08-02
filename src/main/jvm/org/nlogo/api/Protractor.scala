// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait Protractor {

  @throws(classOf[AgentException])
  def towards(fromX: Double, fromY: Double,
              toX: Double, toY: Double,
              wrap: Boolean): Double

}
