// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import java.lang.{ Double => JDouble }
import
  org.nlogo.{ core, api },
    core.{ I18N, LogoList },
    api.{ AgentException, Exceptions }

trait AgentColors extends Agent {
  @throws(classOf[AgentException])
  def validRGBList(rgb: LogoList, allowAlpha: Boolean): Unit = {
    if (rgb.size == 3 || (allowAlpha && rgb.size == 4)) {
      try {
        var i = 0
        while (i < rgb.size) {
          validRGB(rgb.get(i).asInstanceOf[JDouble].intValue)
          i += 1
        }
        return
      } catch {
        case ex: ClassCastException =>
          // just fall through and throw the error below
          Exceptions.ignore(ex)
      }
    }
    val key =
      if (allowAlpha) "org.nlogo.agent.Agent.rgbListSizeError.3or4"
      else            "org.nlogo.agent.Agent.rgbListSizeError.3"
    throw new AgentException(I18N.errors.get(key))
  }

  @throws(classOf[AgentException])
  private def validRGB(c: Int): Unit = {
    if (c < 0 || c > 255) {
      throw new AgentException(I18N.errors.get("org.nlogo.agent.Agent.rgbValueError"))
    }
  }
}
