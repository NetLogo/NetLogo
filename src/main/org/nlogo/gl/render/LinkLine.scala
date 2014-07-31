// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import java.nio.FloatBuffer
import javax.media.opengl.{ GL, GL2, GL2GL3 }

private object LinkLine {
  val dashChoices =
    Array[Short](0x0000.toShort, 0xffff.toShort,
                 0xf0f0.toShort, 0xcccc.toShort,
                 0xcaca.toShort)
}

private class LinkLine(xcor: Double, dashIndex: Int) {

  private val lineStipple: Short = LinkLine.dashChoices(dashIndex)

  def render(gl:GL2, x1: Float, y1: Float, z1: Float,
             x2: Float, y2: Float, z2: Float, curviness: Double, stroke: Float, distance: Int) {
    gl.glLineStipple(distance, lineStipple)
    if(curviness == 0)
      renderLine(gl, x1, y1, z1, x2, y2, z2, stroke)
    else
      renderCurve(gl, x1, y1, z1, x2, y2, z2, curviness, stroke)
  }

  private def renderCurve(gl: GL2,
                          _x1: Float, _y1: Float, _z1: Float,
                          _x2: Float, _y2: Float, _z2: Float,
                          curviness: Double, stroke: Float) {
    var (x1, y1, z1) = (_x1, _y1, _z1)
    var (x2, y2, z2) = (_x2, _y2, _z2)
    val xd = x1 - x2
    val yd = y2 - y1
    val zd = z1 - z2
    val size = math.sqrt(xd * xd + yd * yd + zd * zd)
    val ycomp = xd / size
    val xcomp = yd / size
    val zcomp = zd / size
    if(xcor != 0) {
      x1 += (xcomp * xcor * stroke * Renderer.WORLD_SCALE).toFloat
      x2 += (xcomp * xcor * stroke * Renderer.WORLD_SCALE).toFloat
      y1 += (ycomp * xcor * stroke * Renderer.WORLD_SCALE).toFloat
      y2 += (ycomp * xcor * stroke * Renderer.WORLD_SCALE).toFloat
      z1 += (zcomp * xcor * stroke * Renderer.WORLD_SCALE).toFloat
      z2 += (zcomp * xcor * stroke * Renderer.WORLD_SCALE).toFloat
    }
    val midX = ((x1 + x2) / 2) + (-curviness * xcomp / 2).toFloat
    val midY = ((y1 + y2) / 2) + (-curviness * ycomp / 2).toFloat
    val midZ = ((z1 + z2) / 2) + (-curviness * zcomp / 2).toFloat

    val controlPoints = FloatBuffer.wrap(Array[Float](x1, y1, z1, midX, midY, midZ, x2, y2, z2))
    gl.glMap1f(GL2.GL_MAP1_VERTEX_3, 0.0f, 1.0f, 3, 3, controlPoints)
    gl.glEnable(GL2.GL_MAP1_VERTEX_3)
    gl.glBegin(GL.GL_LINE_STRIP)
    for (i <- 0 to 30)
      gl.glEvalCoord1f(i / 30.0f)
    gl.glEnd()
  }

  private def renderLine(gl: GL2,
                         _x1: Float, _y1: Float, _z1: Float,
                         _x2: Float, _y2: Float, _z2: Float,
                         stroke: Float) {
    var (x1, y1, z1) = (_x1, _y1, _z1)
    var (x2, y2, z2) = (_x2, _y2, _z2)
    if(xcor != 0) {
      val xd = x1 - x2
      val yd = y2 - y1
      val zd = z1 - z2
      val size = math.sqrt(xd * xd + yd * yd + zd * zd)
      val ycomp = (xd / size) * xcor * stroke * Renderer.WORLD_SCALE
      val xcomp = (yd / size) * xcor * stroke * Renderer.WORLD_SCALE
      val zcomp = (zd / size) * xcor * stroke * Renderer.WORLD_SCALE
      x1 += xcomp.toFloat
      x2 += xcomp.toFloat
      y1 += ycomp.toFloat
      y2 += ycomp.toFloat
      z1 += zcomp.toFloat
      z2 += zcomp.toFloat
    }
    gl.glBegin(GL.GL_LINES)
    gl.glNormal3f(0.0f, 0.0f, -1.0f)
    gl.glVertex3f(x1, y1, z1)
    gl.glVertex3f(x2, y2, z2)
    gl.glEnd()
  }

}
