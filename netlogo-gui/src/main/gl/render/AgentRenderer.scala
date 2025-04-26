// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import com.jogamp.opengl.GL2
import com.jogamp.opengl.util.gl2.GLUT
import com.jogamp.opengl.fixedfunc.{ GLLightingFunc => GLL }
import org.nlogo.api.World

abstract private class AgentRenderer(val world: World, val shapeRenderer: ShapeRenderer)

private[render] object AgentRenderer {

  val glut = new GLUT()

  // This number is chosen to keep labels in 2D and 3D view at similar sizes.
  private val FontScale = 6500f

  def renderString(gl: GL2, world: World, str: String, color: AnyRef, fontSize: Int, patchSize: Double) = {
    val lines = str.split("\n")
    val rgb: Array[Float] =
      org.nlogo.api.Color.getColor(color).getRGBColorComponents(null)
    val scale: Float = (fontSize * 12 / patchSize.toFloat) / FontScale
    gl.glColor3fv(java.nio.FloatBuffer.wrap(rgb))
    gl.glDisable(GLL.GL_LIGHTING)
    gl.glScalef(scale, scale, 3.0f)
    val strwidth: Float = (glut.glutStrokeLength(GLUT.STROKE_ROMAN, lines(0))).toFloat
    gl.glTranslatef(-strwidth / -2.0f, 0.0f, 0.0f)
    lines.foreach { l =>
      glut.glutStrokeString(GLUT.STROKE_ROMAN, l)
      // Why -120.0f and not, like, 1.0f? TBH I don't know. The glScalef takes care of factoring out font and patch
      // size, so it makes sense this would be a constant. But beyond that, this just seemed to work...
      gl.glTranslatef((-glut.glutStrokeLength(GLUT.STROKE_ROMAN, l)).toFloat, -120.0f, 0.0f)
    }
    gl.glEnable(GLL.GL_LIGHTING)
  }

}
