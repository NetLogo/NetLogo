// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import java.awt.Color

trait GLViewSettings {
  def wireframeOn: Boolean
  def getBgColor: Color
  def getWireframeColor: Color
}
