// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import java.util.{ List => JList }
import org.nlogo.api.Agent

trait PickListener {
  def pick(mousePt: java.awt.Point, agents: JList[Agent]): Unit
}
