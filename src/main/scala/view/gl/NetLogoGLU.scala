package view25d.view.gl

import com.jogamp.opengl.{ GL, GL2, GL2ES3, GL2GL3 }
import com.jogamp.opengl.glu.{ GLU, GLUquadric }

import java.nio.FloatBuffer

import org.nlogo.shape.{ Circle, Line, Rectangle }

class NetLogoGLU {
  private val smoothness = 16
  private val is3D = false

  private var quadric: GLUquadric = null

  def setQuadric(quad: GLUquadric): Unit = {
    quadric = quad
  }

  def renderRectangle(gl: GL2, offset: Int, rect: Rectangle, rotatable: Boolean): Unit = {
    val zDepth = 0.01f + offset * 0.0001f

    if (!rect.marked) {
      gl.glPushAttrib(GL2.GL_CURRENT_BIT)
      gl.glColor3fv(FloatBuffer.wrap(rect.displayColor(null).getRGBColorComponents(null)))
    }

    val corners = rect.getCorners

    // no need to "pancake" if it is always facing the user
    renderRectangle(gl, corners(0).x * 0.001f - 0.15f, corners(1).x * 0.001f - 0.15f,
                    (300 - corners(1).y) * 0.001f - 0.15f, (300 - corners(0).y) * 0.001f - 0.15f, -zDepth, zDepth,
                    !rect.filled, rotatable)

    if (!rect.marked)
      gl.glPopAttrib()
  }

  def renderRectangle(gl: GL2, x0: Float, x1: Float, y0: Float, y1: Float, z0: Float, z1: Float, filled: Boolean,
                      rotatable: Boolean): Unit = {
    renderRectangularPrism(gl, x0, x1, y0, y1, z0, z1, filled, false, rotatable)
  }

  def renderRectangularPrism(gl: GL2, left: Float, right: Float, back: Float, front: Float, bottom: Float, top: Float,
                             hollow: Boolean, hasBottom: Boolean, hasSides: Boolean): Unit = {
    if (hollow)
      gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE)

    gl.glBegin(GL2ES3.GL_QUADS)

    // top
    gl.glNormal3f(0f, 0f, 1f)
    gl.glVertex3f(left, front, top)
    gl.glVertex3f(left, back, top)
    gl.glVertex3f(right, back, top)
    gl.glVertex3f(right, front, top)

    // bottom
    if (hasBottom) {
      gl.glNormal3f(0f, 0f, -1f)
      gl.glVertex3f(left, front, bottom)
      gl.glVertex3f(right, front, bottom)
      gl.glVertex3f(right, back, bottom)
      gl.glVertex3f(left, back, bottom)
    }

    gl.glEnd()

    if (hollow) {
      gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL)
      gl.glDisable(GL.GL_CULL_FACE)
    }

    if (hasSides) {
      gl.glBegin(GL2ES3.GL_QUADS)

      // left
      gl.glNormal3f(-1f, 0f, 0f)
      gl.glVertex3f(left, front, top)
      gl.glVertex3f(left, front, bottom)
      gl.glVertex3f(left, back, bottom)
      gl.glVertex3f(left, back, top)

      // front
      gl.glNormal3f(0f, 1f, 0f)
      gl.glVertex3f(right, front, top)
      gl.glVertex3f(right, front, bottom)
      gl.glVertex3f(left, front, bottom)
      gl.glVertex3f(left, front, top)

      // right
      gl.glNormal3f(1f, 0f, 0f)
      gl.glVertex3f(right, back, top)
      gl.glVertex3f(right, back, bottom)
      gl.glVertex3f(right, front, bottom)
      gl.glVertex3f(right, front, top)

      // back
      gl.glNormal3f(0f, -1f, 0f)
      gl.glVertex3f(left, back, top)
      gl.glVertex3f(left, back, bottom)
      gl.glVertex3f(right, back, bottom)
      gl.glVertex3f(right, back, top)

      gl.glEnd()
    }

    if (hollow)
      gl.glEnable(GL.GL_CULL_FACE)
  }

  def renderCircle(gl: GL2, glu: GLU, offset: Int, circle: Circle, rotatable: Boolean): Unit = {
    val zDepth = 0.01f + offset * 0.0001f

    if (!circle.marked) {
      gl.glPushAttrib(GL2.GL_CURRENT_BIT)
      gl.glColor3fv(FloatBuffer.wrap(circle.displayColor(null).getRGBColorComponents(null)))
    }

    // for now assume it is a circle
    val radius = (circle.bounds.getWidth * 0.0005f).toFloat

    val origin = Array[Float](circle.origin.getX.toFloat * 0.001f - 0.15f,
                              (300 - circle.origin.getY.toFloat) * 0.001f - 0.15f)

    gl.glPushMatrix()

    if (!circle.filled)
      glu.gluQuadricDrawStyle(quadric, GLU.GLU_SILHOUETTE)

    // no need to "pancake" if it is always facing the user
    if (rotatable) {
      if (!circle.filled) {
        glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL)
        gl.glDisable(GL.GL_CULL_FACE)
      }

      gl.glTranslatef(origin(0), origin(1), -zDepth)
      glu.gluCylinder(quadric, radius, radius, 2 * zDepth, smoothness, 1)

      if (!circle.filled) {
        gl.glEnable(GL.GL_CULL_FACE)
        glu.gluQuadricDrawStyle(quadric, GLU.GLU_SILHOUETTE)
      }

      gl.glTranslatef(0.0f, 0.0f, 2 * zDepth)
    }

    else
      gl.glTranslatef(origin(0), origin(1), zDepth)

    renderCircle(gl, glu, 0.0f, radius, zDepth, rotatable)

    if (!circle.filled)
      glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL)

    gl.glPopMatrix()

    if (!circle.marked)
      gl.glPopAttrib()
  }

  def renderCircle(gl: GL2, glu: GLU, innerRadius: Float, outerRadius: Float, zDepth: Float, rotatable: Boolean): Unit = {
    glu.gluDisk(quadric, innerRadius, outerRadius, smoothness, 1)

    if (is3D && rotatable) {
      gl.glRotatef(180f, 1f, 0f, 0f)
      gl.glTranslatef(0f, 0f, zDepth * 2)
      glu.gluDisk(quadric, innerRadius, outerRadius, smoothness, 1)
    }
  }

  def renderLine(gl: GL2, offset: Int, line: Line): Unit = {
    val zDepth = offset * 0.0001f

    if (!line.marked) {
      gl.glPushAttrib(GL2.GL_CURRENT_BIT)
      gl.glColor3fv(FloatBuffer.wrap(line.displayColor(null).getRGBColorComponents(null)))
    }

    val start = line.getStart
    val end = line.getEnd

    val coords = Array(start.x * 0.001f - 0.15f,
                       (300 - start.y) * 0.001f - 0.15f,
                       end.x * 0.001f - 0.15f,
                       (300 - end.y) * 0.001f - 0.15f)

    gl.glBegin(GL.GL_LINES)
    gl.glNormal3f(0.0f, 0.0f, -1.0f)

    // top line
    gl.glVertex3f(coords(0), coords(1), zDepth)
    gl.glVertex3f(coords(2), coords(3), zDepth)
    gl.glEnd()

    // we don't "pancake" line because we don't really need to,
    // by default the line tilts to always show the user
    // a flat face, and this way we can scale all lines
    // by line thickness and it will look good and it will be fast. ev 4/5/06

    if (!line.marked)
      gl.glPopAttrib()
  }
}
