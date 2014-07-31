// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import javax.media.opengl.GL2

private trait DrawingRendererInterface {
  def renderDrawing(gl: GL2): Unit
  def clear(): Unit
  def init(gl: GL2): Unit
}
