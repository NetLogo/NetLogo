// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import javax.media.opengl.{ GL, GL2, GL2GL3 }

private object Rectangle {

  // symmetric over axes
  def renderRectangularPrism(gl: GL2, edgeX: Float, edgeY: Float, edgeZ: Float, invert: Boolean) {
    if (invert)
      renderRectangularPrism(gl, edgeX, -edgeX, edgeY, -edgeY, edgeZ, -edgeZ, false, true, true)
    else
      renderRectangularPrism(gl, -edgeX, edgeX, -edgeY, edgeY, -edgeZ, edgeZ, false, true, true)
  }

  // utility function to render a 3D-rectangle-prism
  def renderRectangularPrism(gl: GL2, left: Float, right: Float,
                             back: Float, front: Float,
                             bottom: Float, top: Float, hollow: Boolean,
                             hasBottom: Boolean, hasSides: Boolean) {

    if (hollow)
      gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE)

    gl.glBegin(GL2GL3.GL_QUADS)

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

      gl.glBegin(GL2GL3.GL_QUADS)

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

}
