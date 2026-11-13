// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import java.lang.{ Double => JDouble }
import
  org.nlogo.{ core, api },
    core.{ LocaleContext, LogoList },
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
    throw new AgentException(if (allowAlpha) {
      loc"An rgb list must contain 3 or 4 numbers 0-255"
    } else {
      loc"An rgb list must contain 3 numbers 0-255"
    })
  }

  @throws(classOf[AgentException])
  private def validRGB(c: Int): Unit = {
    if (c < 0 || c > 255) {
      throw new AgentException(loc"RGB values must be 0-255")
    }
  }
}
