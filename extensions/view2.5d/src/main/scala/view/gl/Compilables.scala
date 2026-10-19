package view25d.view.gl

import com.jogamp.opengl.{ GL, GL2 }
import com.jogamp.opengl.glu.{ GLU, GLUquadric }

object Compilables {
  def PatchTile(gl: GL2): Unit = {
    gl.glBegin(GL2.GL_POLYGON)
    gl.glVertex2d(-0.4, 0.4)
    gl.glVertex2d(0.4, 0.4)
    gl.glVertex2d(0.4, -0.4)
    gl.glVertex2d(-0.4, -0.4)
    gl.glVertex2d(-0.4, 0.4)
    gl.glEnd()
  }

  def PatchStickTile(gl: GL2, stickHeight: Int): Unit = {
    gl.glColor3f(1.0f, 0.6f, 5.9f)
    gl.glBegin(GL2.GL_POLYGON)
    gl.glVertex2d(-0.4, 0.4)
    gl.glVertex2d(0.4, 0.4)
    gl.glVertex2d(0.4, -0.4)
    gl.glVertex2d(-0.4, -0.4)
    gl.glVertex2d(-0.4, 0.4)
    gl.glEnd()

    gl.glColor3f(2.5f, 2.5f, 2.5f)
    gl.glLineWidth(0.1f)
    gl.glBegin(GL.GL_LINES)
    gl.glVertex3i(0, 0, 0)
    gl.glVertex3i(0, 0, stickHeight)
    gl.glEnd()
  }

  // sz should be greater than 0 and less than or equal to 0.5
  // depth controls the z-size of the tile
  def ThickPatchTile(gl: GL2, sz: Float, depth: Float): Unit = {
    gl.glBegin(GL2.GL_POLYGON) // f1: front
    gl.glNormal3f(0, sz, 0)
    gl.glVertex3f(-sz, sz, depth)
    gl.glVertex3f(sz, sz, depth)
    gl.glVertex3f(sz, sz, -depth)
    gl.glVertex3f(-sz, sz, -depth)
    gl.glEnd()
    gl.glBegin(GL2.GL_POLYGON) // f2: bottom
    gl.glNormal3f(0, 0, -depth)
    gl.glVertex3f(-sz, sz, -depth)
    gl.glVertex3f(sz, sz, -depth)
    gl.glVertex3f(sz, -sz, -depth)
    gl.glVertex3f(-sz, -sz, -depth)
    gl.glEnd()
    gl.glBegin(GL2.GL_POLYGON) // f3: back
    gl.glNormal3f(0, -sz, 0)
    gl.glVertex3f(-sz, -sz, depth)
    gl.glVertex3f(sz, -sz, depth)
    gl.glVertex3f(sz, -sz, -depth)
    gl.glVertex3f(-sz, -sz, -depth)
    gl.glEnd()
    gl.glBegin(GL2.GL_POLYGON) // f4: top
    gl.glNormal3f(0, 0, depth)
    gl.glVertex3f(-sz, sz, depth)
    gl.glVertex3f(sz, sz, depth)
    gl.glVertex3f(sz, -sz, depth)
    gl.glVertex3f(-sz, -sz, depth)
    gl.glEnd()
    gl.glBegin(GL2.GL_POLYGON) // f5: left
    gl.glNormal3f(-sz, 0, 0)
    gl.glVertex3f(-sz, -sz, depth)
    gl.glVertex3f(-sz, sz, depth)
    gl.glVertex3f(-sz, sz, -depth)
    gl.glVertex3f(-sz, -sz, -depth)
    gl.glEnd()
    gl.glBegin(GL2.GL_POLYGON) // f6: right
    gl.glNormal3f(sz, 0, 0)
    gl.glVertex3f(sz, -sz, depth)
    gl.glVertex3f(sz, sz, depth)
    gl.glVertex3f(sz, sz, -depth)
    gl.glVertex3f(sz, -sz, -depth)
    gl.glEnd()
  }

  def box(gl: GL2, baseHalf: Float, height: Float): Unit = {
    gl.glBegin(GL2.GL_POLYGON) // f1: front
    gl.glNormal3f(0, 1, 0)
    gl.glVertex3f(-baseHalf, baseHalf, height)
    gl.glVertex3f(baseHalf, baseHalf, height)
    gl.glVertex3f(baseHalf, baseHalf, 0)
    gl.glVertex3f(-baseHalf, baseHalf, 0)
    gl.glEnd()
    gl.glBegin(GL2.GL_POLYGON) // f2: bottom
    gl.glNormal3f(0, 0, -1)
    gl.glVertex3f(-baseHalf, baseHalf, 0)
    gl.glVertex3f(baseHalf, baseHalf, 0)
    gl.glVertex3f(baseHalf, -baseHalf, 0)
    gl.glVertex3f(-baseHalf, -baseHalf, 0)
    gl.glEnd()
    gl.glBegin(GL2.GL_POLYGON) // f3:back
    gl.glNormal3f(0, -1, 0)
    gl.glVertex3f(-baseHalf, -baseHalf, height)
    gl.glVertex3f(baseHalf, -baseHalf, height)
    gl.glVertex3f(baseHalf, -baseHalf, 0)
    gl.glVertex3f(-baseHalf, -baseHalf, 0)
    gl.glEnd()
    gl.glBegin(GL2.GL_POLYGON) // f4: top
    gl.glNormal3f(0, 0, 1)
    gl.glVertex3f(-baseHalf, baseHalf, height)
    gl.glVertex3f(baseHalf, baseHalf, height)
    gl.glVertex3f(baseHalf, -baseHalf, height)
    gl.glVertex3f(-baseHalf, -baseHalf, height)
    gl.glEnd()
    gl.glBegin(GL2.GL_POLYGON) // f5: left
    gl.glNormal3f(-1, 0, 0)
    gl.glVertex3f(-baseHalf, -baseHalf, height)
    gl.glVertex3f(-baseHalf, baseHalf, height)
    gl.glVertex3f(-baseHalf, baseHalf, 0)
    gl.glVertex3f(-baseHalf, -baseHalf, 0)
    gl.glEnd()
    gl.glBegin(GL2.GL_POLYGON) // f6: right
    gl.glNormal3f(1, 0, 0)
    gl.glVertex3f(baseHalf, -baseHalf ,height)
    gl.glVertex3f(baseHalf, baseHalf, height)
    gl.glVertex3f(baseHalf, baseHalf, 0)
    gl.glVertex3f(baseHalf, -baseHalf, 0)
    gl.glEnd()
  }

  def PinHead(gl: GL2, glu: GLU, quadr: GLUquadric, radius: Float, slices: Int): Unit = {
    // glu.gluSphere(quadr, radius, slices, stacks)
    gl.glTranslated(0, 0, -radius / 4)
    glu.gluCylinder(quadr, radius, radius, radius / 2, slices, slices)
    // gl.glRotatef(180f, 1f, 0f, 0f)
    glu.gluDisk(quadr, 0f, radius, slices, 1)
    gl.glTranslated(0, 0, radius / 2)
    glu.gluDisk(quadr, 0f, radius, slices, 1)
  }

  def DiskPatchTile(gl: GL2, glu: GLU, quadr: GLUquadric, sz: Double, smoothness: Int): Unit = {
    glu.gluDisk(quadr, 0f, sz, smoothness, 1)
  }

  def AxisHead(gl: GL2, glu: GLU, quadr: GLUquadric, sz: Double, smoothness: Int): Unit = {
    glu.gluCylinder(quadr, sz / 2.0, 0, sz, smoothness, smoothness)
    gl.glRotatef(180f, 1f, 0f, 0f)
    glu.gluDisk(quadr, 0f, sz / 2, smoothness, 1)
  }

  def ThickStem(gl: GL2, glu: GLU, quadr: GLUquadric, sz: Double, smoothness: Int): Unit = {
    glu.gluCylinder(quadr, sz / 2.0, sz / 2.0, sz, smoothness, smoothness)
    // gl.glTranslated(0, 0, 0.8 * sz)
    // glu.gluCylinder(quadr, sz / 2.0, 0.0, 0.2 * sz, smoothness, smoothness)
  }
}
