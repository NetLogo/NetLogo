// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import java.util.{ Map => JMap }
import javax.media.opengl.{ GL, GL2, GL2GL3 }
import javax.media.opengl.glu.{ GLU, GLUquadric }
import ShapeManager.SMOOTHNESS

private class Builtins(gl: GL2, glu: GLU, quadric: GLUquadric) {

  import gl._
  import glu._
  import GL._
  import GL2._
  import GL2GL3._
  import javax.media.opengl.fixedfunc.GLLightingFunc._

  def add(shapes: JMap[String, GLShape],
          shapeMap: JMap[String, String]): Int = {

    // note that this needs to be changed if a shape is added below
    var lastList = glGenLists(15) - 1

    def shape(name: String, rotatable: Boolean = true)(body: => Unit) {
      lastList += 1
      shapes.put(name, new GLShape(name, lastList, rotatable))
      glNewList(lastList, GL2.GL_COMPILE)
      body
      glEndList()
    }

    shape("default") { default() }
    shape("car") { car() }
    shape("plane") { plane() }
    shape("stoplight") { stoplight() }
    shape("cylinder") { cylinder() }
    shape("cone") { cone() }
    shape("cube") {
      Rectangle.renderRectangularPrism(gl, 0.14f, 0.14f, 0.14f, false)
    }
    shape("sphere") {
      gluSphere(quadric, 0.15f, SMOOTHNESS, SMOOTHNESS)
    }
    shape("dot") {
      gluSphere(quadric,
        // 2D dot shape, at least at present, is 40% the size of "circle"
        0.15f * 0.40f, SMOOTHNESS, SMOOTHNESS)
    }
    shape("@@@CROSSHAIRS@@@") { crosshairs() }
    shape("@@@HALO@@@", rotatable = false) { halo() }
    shape("@@@PATCH@@@") {
      Rectangle.renderRectangularPrism(gl, (Renderer.WORLD_SCALE / 2),
        (Renderer.WORLD_SCALE / 2), (Renderer.WORLD_SCALE / 2), false)
    }
    shape("@@@WIREFRAME@@@") {
      Rectangle.renderRectangularPrism(gl, (Renderer.WORLD_SCALE / 2),
        (Renderer.WORLD_SCALE / 2), (Renderer.WORLD_SCALE / 2), true)
    }

    shapeMap.put("square", "cube")
    shapeMap.put("triangle", "cone")
    shapeMap.put("circle", "sphere")

    lastList
  }

  private def default() {
    begin(GL_TRIANGLES) {
      glNormal3f(0f, 0f, -1f)
      glVertex3f(-0.10f, -0.125f, 0f)
      glVertex3f(0f, 0.125f, 0f)
      glVertex3f(0f, -0.075f, 0f)

      glNormal3f(0f, 0f, -1f)
      glVertex3f(0f, -0.075f, 0f)
      glVertex3f(0f, 0.125f, 0f)
      glVertex3f(0.10f, -0.125f, 0f)

      glNormal3f(-0.707f, 0f, 0.707f)
      glVertex3f(0f, 0.125f, 0f)
      glVertex3f(-0.10f, -0.125f, 0f)
      glVertex3f(0f, -0.075f, 0.08f)

      glNormal3f(0.707f, 0f, 0.707f)
      glVertex3f(0f, 0.125f, 0f)
      glVertex3f(0f, -0.075f, 0.08f)
      glVertex3f(0.10f, -0.125f, 0f)

      glNormal3f(-0.6f, -0.8f, 0f)
      glVertex3f(0f, -0.075f, 0.08f)
      glVertex3f(-0.10f, -0.125f, 0f)
      glVertex3f(0f, -0.075f, 0f)

      glNormal3f(-0.6f, 0.8f, 0f)
      glVertex3f(0f, -0.075f, 0.08f)
      glVertex3f(0f, -0.075f, 0f)
      glVertex3f(0.10f, -0.125f, 0f)
    }
  }

  private def car() {
    glTranslatef(0f, 0f, 0.14f * -0.6f)
    glRotatef(90f, 1f, 0f, 0f)
    glRotatef(90f, 0f, 1f, 0f)
    glScalef(0.5f, 0.5f, 0.5f)
    begin(GL_QUADS) {

      // Left (or is it right?) Face
      glNormal3f(0f, 0f, 1f)
      glVertex3f(.14f * -2f, .14f * -0.5f, .14f * 1f)
      glVertex3f(.14f * 2f, .14f * -0.5f, .14f * 1f)
      glVertex3f(.14f * 2f, .14f * 0.5f, .14f * 1f)
      glVertex3f(.14f * -2f, .14f * 0.5f, .14f * 1f)
      // Right (or is it left?) Face
      glNormal3f(0f, 0f, -1f)
      glVertex3f(.14f * -2f, .14f * -0.5f, .14f * -1f)
      glVertex3f(.14f * -2f, .14f * 0.5f, .14f * -1f)
      glVertex3f(.14f * 2f, .14f * 0.5f, .14f * -1f)
      glVertex3f(.14f * 2f, .14f * -0.5f, .14f * -1f)
      // Top Face
      glNormal3f(0f, 1f, 0f)
      glVertex3f(.14f * -2f, .14f * 0.5f, .14f * -1f)
      glVertex3f(.14f * -2f, .14f * 0.5f, .14f * 1f)
      glVertex3f(.14f * 2f, .14f * 0.5f, .14f * 1f)
      glVertex3f(.14f * 2f, .14f * 0.5f, .14f * -1f)
      // Bottom Face
      glNormal3f(0f, -1f, 0f)
      glVertex3f(.14f * -2f, .14f * -0.5f, .14f * -1f)
      glVertex3f(.14f * 2f, .14f * -0.5f, .14f * -1f)
      glVertex3f(.14f * 2f, .14f * -0.5f, .14f * 1f)
      glVertex3f(.14f * -2f, .14f * -0.5f, .14f * 1f)
      // From face
      glNormal3f(1f, 0f, 0f)
      glVertex3f(.14f * 2f, .14f * -0.5f, .14f * -1f)
      glVertex3f(.14f * 2f, .14f * 0.5f, .14f * -1f)
      glVertex3f(.14f * 2f, .14f * 0.5f, .14f * 1f)
      glVertex3f(.14f * 2f, .14f * -0.5f, .14f * 1f)
      // Back Face
      glNormal3f(-1f, 0f, 0f)
      glVertex3f(.14f * -2f, .14f * -0.5f, .14f * -1f)
      glVertex3f(.14f * -2f, .14f * -0.5f, .14f * 1f)
      glVertex3f(.14f * -2f, .14f * 1f, .14f * 1f)
      glVertex3f(.14f * -2f, .14f * 1f, .14f * -1f)

      // Cabin
      // Left Face
      glNormal3f(0f, 0f, 1f)
      glVertex3f(.14f * -2f, .14f * 0.5f, .14f * 1f)
      glVertex3f(.14f * 0.5f, .14f * 0.5f, .14f * 1f)
      glVertex3f(.14f * 0.5f, .14f * 1f, .14f * 1f)
      glVertex3f(.14f * -2f, .14f * 1f, .14f * 1f)

      // Right Face
      glNormal3f(0f, 0f, -1f)
      glVertex3f(.14f * -2f, .14f * 0.5f, .14f * -1f)
      glVertex3f(.14f * -2f, .14f * 1f, .14f * -1f)
      glVertex3f(.14f * 0.5f, .14f * 1f, .14f * -1f)
      glVertex3f(.14f * 0.5f, .14f * 0.5f, .14f * -1f)

      // Top Face
      glNormal3f(0f, 1f, 0f)
      glVertex3f(.14f * -2f, .14f * 1f, .14f * -1f)
      glVertex3f(.14f * -2f, .14f * 1f, .14f * 1f)
      glVertex3f(.14f * 0.5f, .14f * 1f, .14f * 1f)
      glVertex3f(.14f * 0.5f, .14f * 1f, .14f * -1f)

      // Front face
      glNormal3f(1f, 0f, 0f)
      glVertex3f(.14f * 0.5f, .14f * 0.5f, .14f * -1f)
      glVertex3f(.14f * 0.5f, .14f * 1f, .14f * -1f)
      glVertex3f(.14f * 0.5f, .14f * 1f, .14f * 1f)
      glVertex3f(.14f * 0.5f, .14f * 0.5f, .14f * 1f)
    }

    // wheels
    val p = gluNewQuadric()
    matrix {
      glTranslatef(0.14f * 1.25f,
        0.5f * -0.14f, // up and down
        0.14f * -1.01f)
      //glColor3fv(car_axle)
      glColor3f(0.1f, 0.1f, 0.1f)
      glRotatef(180f, 1f, 0f, 0f)

      gluDisk(p, 0.0, 0.14 * 0.4, 16, 1)

      glRotatef(180f, 1f, 0f, 0f)
      gluDisk(p, 0.0, 0.14 * 0.4, 16, 1)
      glRotatef(-180f, 1f, 0f, 0f)

      glTranslatef(0f, 0f, 0.14f * 2f * -1.01f)
      glRotatef(180f, 1f, 0f, 0f)
      glColor3f(0.1f, 0.1f, 0.1f)
      gluDisk(p, 0.0, 0.14 * 0.4, 16, 1)

      glRotatef(180f, 1f, 0f, 0f)
      gluDisk(p, 0.0, 0.14 * 0.4, 16, 1)
      glRotatef(-180f, 1f, 0f, 0f)
    }

    matrix {
      glTranslatef(
        -0.14f * 1.25f,
        0.5f * -0.14f, // up and down
        0.14f * -1.01f)
      glColor3f(0.1f, 0.1f, 0.1f)
      glRotatef(180f, 1f, 0f, 0f)
      gluDisk(p, 0.0, 0.14 * 0.4, 16, 1)
      glRotatef(180f, 1f, 0f, 0f)
      gluDisk(p, 0.0, 0.14 * 0.4, 16, 1)
      glRotatef(-180f, 1f, 0f, 0f)

      glTranslatef(0f, 0f, 0.14f * 2f * -1.01f)
      glRotatef(180f, 1f, 0f, 0f)
      glColor3f(0.1f, 0.1f, 0.1f)
      gluDisk(p, 0.0, 0.14 * 0.4, 16, 1)
      glRotatef(180f, 1f, 0f, 0f)
      gluDisk(p, 0.0, 0.14 * 0.4, 16, 1)
      glRotatef(-180f, 1f, 0f, 0f)
    }
  }

  private def plane() {
    begin(GL_QUADS) {
      glNormal3f(0f, 0f, 1f)
      glVertex3f(.14f, .14f, -.14f)
      glVertex3f(-.14f, .14f, -.14f)
      glVertex3f(-.14f, -.14f, -.14f)
      glVertex3f(.14f, -.14f, -.14f)

      glNormal3f(0f, 0f, -1f)
      glVertex3f(.14f, -.14f, -.14f)
      glVertex3f(-.14f, -.14f, -.14f)
      glVertex3f(-.14f, .14f, -.14f)
      glVertex3f(.14f, .14f, -.14f)
    }
  }

  private def crosshairs() {
    attrib(GL_ENABLE_BIT, GL_CURRENT_BIT) {
      glDisable(GL_LIGHTING)
      glDisable(GL_CULL_FACE)

      glColor3f(0f, 0f, 1f)
      gluDisk(quadric, 0.120f, 0.135f, SMOOTHNESS, 1)

      begin(GL_QUADS) {
        glVertex3f(-0.03f, 0.12f, 0f)
        glVertex3f(-0.03f, -0.12f, 0f)
        glVertex3f(0.03f, -0.12f, 0f)
        glVertex3f(0.03f, 0.12f, 0f)

        glVertex3f(-0.12f, 0.03f, 0f)
        glVertex3f(-0.12f, -0.03f, 0f)
        glVertex3f(0.12f, -0.03f, 0f)
        glVertex3f(0.12f, 0.03f, 0f)
      }

      begin(GL_TRIANGLES) {
        glVertex3f(-0.03f, 0.140f, 0f)
        glVertex3f(0.03f, 0.140f, 0f)
        glVertex3f(0.00f, 0.185f, 0f)
      }
    }
  }

  private def halo() {
    attrib(GL_ENABLE_BIT, GL_CURRENT_BIT) {

      glDisable(GL_LIGHTING)
      glDisable(GL_CULL_FACE)

      // inner-circle (used by stencil buffer)
      glColorMask(false, false, false, false)
      // use SMOOTHNESS * 2 for halo polygons because it looks better
      // and since there's only 1 halo, doesn't cost much - AZS 7/7/05
      gluDisk(quadric, 0f, 0.255f, SMOOTHNESS * 2, 1)
      glColorMask(true, true, true, true)

      glEnable(GL_BLEND)

      // inner-ring
      glColor4f(0.784f, 1f, 1f, 0.392f)
      gluDisk(quadric, 0.255f, 0.275f, SMOOTHNESS * 2, 1)

      // middle-ring
      glColor4f(0.784f, 1f, 1f, 0.196f)
      gluDisk(quadric, 0.275f, 0.285f, SMOOTHNESS * 2, 1)

      // dark outermost-ring
      glColor4f(0f, 0f, 0f, 0.196f)
      gluDisk(quadric, 0.285f, 0.295f, SMOOTHNESS * 2, 1)

      glDisable(GL_BLEND)
    }
  }

  private def stoplight() {
    glTranslatef(0f, 0f, (Renderer.WORLD_SCALE))
    gluSphere(quadric, (Renderer.WORLD_SCALE / 4),
      SMOOTHNESS, SMOOTHNESS)
    glColor3f(0.93f, 0.93f, 0.18f)
    Rectangle.renderRectangularPrism(
      gl, (0.14f / 6), (0.14f / 1.5f), (0.14f / 1.5f), false)
  }

  private def cylinder() {
    glTranslatef(0f, 0f, (-Renderer.WORLD_SCALE / 2) + 0.01f)
    gluCylinder(quadric, 0.12f, 0.12f,
      Renderer.WORLD_SCALE - 0.01, SMOOTHNESS, 1)
    glRotatef(180f, 1f, 0f, 0f)
    gluDisk(quadric, 0f, 0.12f, SMOOTHNESS, 1)
    glRotatef(180f, 1f, 0f, 0f)
    glTranslatef(0f, 0f, (Renderer.WORLD_SCALE - 0.01f))
    gluDisk(quadric, 0f, 0.12f, SMOOTHNESS, 1)
  }

  private def cone() {
    glRotatef(-90f, 1f, 0f, 0f)
    glTranslatef(0f, 0f, -(Renderer.WORLD_SCALE / 2))
    gluCylinder(quadric, Renderer.WORLD_SCALE / 2, 0f,
      Renderer.WORLD_SCALE, SMOOTHNESS, SMOOTHNESS)
    glRotatef(180f, 1f, 0f, 0f)
    gluDisk(quadric, 0f, Renderer.WORLD_SCALE / 2,
      SMOOTHNESS, 1)
  }

  // OpenGL helpers
  private def begin(n: Int)(body: => Unit) {
    glBegin(n)
    body
    glEnd()
  }
  private def attrib(ns: Int*)(body: => Unit) {
    ns.foreach(glPushAttrib)
    body
    ns.foreach(_ => glPopAttrib())
  }
  private def matrix(body: => Unit) {
    glPushMatrix()
    body
    glPopMatrix()
  }

}
