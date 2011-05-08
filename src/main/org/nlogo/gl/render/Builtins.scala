package org.nlogo.gl.render

import javax.media.opengl.GL
import javax.media.opengl.glu.{ GLU, GLUquadric }
import ShapeManager.SMOOTHNESS

private object Builtins {

  def add(gl: GL, glu: GLU,
          shapes: java.util.Map[String, GLShape],
          shapeMap: java.util.Map[String, String],
          quadric: GLUquadric): Int = {

    // note that this needs to be changed if a shape is added below
    var lastList = gl.glGenLists(15)

    // Compile default shape
    shapes.put("default", new GLShape("default", lastList))
    gl.glNewList(lastList, GL.GL_COMPILE)
    gl.glBegin(GL.GL_TRIANGLES)
    gl.glNormal3f(0f, 0f, -1f)
    gl.glVertex3f(-0.10f, -0.125f, 0f)
    gl.glVertex3f(0f, 0.125f, 0f)
    gl.glVertex3f(0f, -0.075f, 0f)

    gl.glNormal3f(0f, 0f, -1f)
    gl.glVertex3f(0f, -0.075f, 0f)
    gl.glVertex3f(0f, 0.125f, 0f)
    gl.glVertex3f(0.10f, -0.125f, 0f)

    gl.glNormal3f(-0.707f, 0f, 0.707f)
    gl.glVertex3f(0f, 0.125f, 0f)
    gl.glVertex3f(-0.10f, -0.125f, 0f)
    gl.glVertex3f(0f, -0.075f, 0.08f)

    gl.glNormal3f(0.707f, 0f, 0.707f)
    gl.glVertex3f(0f, 0.125f, 0f)
    gl.glVertex3f(0f, -0.075f, 0.08f)
    gl.glVertex3f(0.10f, -0.125f, 0f)

    gl.glNormal3f(-0.6f, -0.8f, 0f)
    gl.glVertex3f(0f, -0.075f, 0.08f)
    gl.glVertex3f(-0.10f, -0.125f, 0f)
    gl.glVertex3f(0f, -0.075f, 0f)

    gl.glNormal3f(-0.6f, 0.8f, 0f)
    gl.glVertex3f(0f, -0.075f, 0.08f)
    gl.glVertex3f(0f, -0.075f, 0f)
    gl.glVertex3f(0.10f, -0.125f, 0f)
    gl.glEnd()
    gl.glEndList()

    // Compile car shape
    lastList += 1
    shapes.put("car", new GLShape("car", lastList))
    gl.glNewList(lastList, GL.GL_COMPILE)
    gl.glTranslatef(0f, 0f, 0.14f * -0.6f)
    gl.glRotatef(90f, 1f, 0f, 0f)
    gl.glRotatef(90f, 0f, 1f, 0f)
    gl.glScalef(0.5f, 0.5f, 0.5f)
    gl.glBegin(GL.GL_QUADS)

    // Left (or is it right?) Face
    gl.glNormal3f(0f, 0f, 1f)
    gl.glVertex3f(.14f * -2f, .14f * -0.5f, .14f * 1f)
    gl.glVertex3f(.14f * 2f, .14f * -0.5f, .14f * 1f)
    gl.glVertex3f(.14f * 2f, .14f * 0.5f, .14f * 1f)
    gl.glVertex3f(.14f * -2f, .14f * 0.5f, .14f * 1f)
    // Right (or is it left?) Face
    gl.glNormal3f(0f, 0f, -1f)
    gl.glVertex3f(.14f * -2f, .14f * -0.5f, .14f * -1f)
    gl.glVertex3f(.14f * -2f, .14f * 0.5f, .14f * -1f)
    gl.glVertex3f(.14f * 2f, .14f * 0.5f, .14f * -1f)
    gl.glVertex3f(.14f * 2f, .14f * -0.5f, .14f * -1f)
    // Top Face
    gl.glNormal3f(0f, 1f, 0f)
    gl.glVertex3f(.14f * -2f, .14f * 0.5f, .14f * -1f)
    gl.glVertex3f(.14f * -2f, .14f * 0.5f, .14f * 1f)
    gl.glVertex3f(.14f * 2f, .14f * 0.5f, .14f * 1f)
    gl.glVertex3f(.14f * 2f, .14f * 0.5f, .14f * -1f)
    // Bottom Face
    gl.glNormal3f(0f, -1f, 0f)
    gl.glVertex3f(.14f * -2f, .14f * -0.5f, .14f * -1f)
    gl.glVertex3f(.14f * 2f, .14f * -0.5f, .14f * -1f)
    gl.glVertex3f(.14f * 2f, .14f * -0.5f, .14f * 1f)
    gl.glVertex3f(.14f * -2f, .14f * -0.5f, .14f * 1f)
    // From face
    gl.glNormal3f(1f, 0f, 0f)
    gl.glVertex3f(.14f * 2f, .14f * -0.5f, .14f * -1f)
    gl.glVertex3f(.14f * 2f, .14f * 0.5f, .14f * -1f)
    gl.glVertex3f(.14f * 2f, .14f * 0.5f, .14f * 1f)
    gl.glVertex3f(.14f * 2f, .14f * -0.5f, .14f * 1f)
    // Back Face
    gl.glNormal3f(-1f, 0f, 0f)
    gl.glVertex3f(.14f * -2f, .14f * -0.5f, .14f * -1f)
    gl.glVertex3f(.14f * -2f, .14f * -0.5f, .14f * 1f)
    gl.glVertex3f(.14f * -2f, .14f * 1f, .14f * 1f)
    gl.glVertex3f(.14f * -2f, .14f * 1f, .14f * -1f)

    // Cabin
    // Left Face
    gl.glNormal3f(0f, 0f, 1f)
    gl.glVertex3f(.14f * -2f, .14f * 0.5f, .14f * 1f)
    gl.glVertex3f(.14f * 0.5f, .14f * 0.5f, .14f * 1f)
    gl.glVertex3f(.14f * 0.5f, .14f * 1f, .14f * 1f)
    gl.glVertex3f(.14f * -2f, .14f * 1f, .14f * 1f)

    // Right Face
    gl.glNormal3f(0f, 0f, -1f)
    gl.glVertex3f(.14f * -2f, .14f * 0.5f, .14f * -1f)
    gl.glVertex3f(.14f * -2f, .14f * 1f, .14f * -1f)
    gl.glVertex3f(.14f * 0.5f, .14f * 1f, .14f * -1f)
    gl.glVertex3f(.14f * 0.5f, .14f * 0.5f, .14f * -1f)

    // Top Face
    gl.glNormal3f(0f, 1f, 0f)
    gl.glVertex3f(.14f * -2f, .14f * 1f, .14f * -1f)
    gl.glVertex3f(.14f * -2f, .14f * 1f, .14f * 1f)
    gl.glVertex3f(.14f * 0.5f, .14f * 1f, .14f * 1f)
    gl.glVertex3f(.14f * 0.5f, .14f * 1f, .14f * -1f)

    // Front face
    gl.glNormal3f(1f, 0f, 0f)
    gl.glVertex3f(.14f * 0.5f, .14f * 0.5f, .14f * -1f)
    gl.glVertex3f(.14f * 0.5f, .14f * 1f, .14f * -1f)
    gl.glVertex3f(.14f * 0.5f, .14f * 1f, .14f * 1f)
    gl.glVertex3f(.14f * 0.5f, .14f * 0.5f, .14f * 1f)

    gl.glEnd() // quads

    // wheels
    val p = glu.gluNewQuadric()
    gl.glPushMatrix()
    gl.glTranslatef(0.14f * 1.25f,
      0.5f * -0.14f, // up and down
      0.14f * -1.01f)
    //gl.glColor3fv(car_axle)
    gl.glColor3f(0.1f, 0.1f, 0.1f)
    gl.glRotatef(180f, 1f, 0f, 0f)

    glu.gluDisk(p, 0.0, 0.14 * 0.4, 16, 1)

    gl.glRotatef(180f, 1f, 0f, 0f)
    glu.gluDisk(p, 0.0, 0.14 * 0.4, 16, 1)
    gl.glRotatef(-180f, 1f, 0f, 0f)

    gl.glTranslatef(0f, 0f, 0.14f * 2f * -1.01f)
    gl.glRotatef(180f, 1f, 0f, 0f)
    gl.glColor3f(0.1f, 0.1f, 0.1f)
    glu.gluDisk(p, 0.0, 0.14 * 0.4, 16, 1)

    gl.glRotatef(180f, 1f, 0f, 0f)
    glu.gluDisk(p, 0.0, 0.14 * 0.4, 16, 1)
    gl.glRotatef(-180f, 1f, 0f, 0f)
    gl.glPopMatrix()

    gl.glPushMatrix()
    gl.glTranslatef(
      -0.14f * 1.25f,
      0.5f * -0.14f, // up and down
      0.14f * -1.01f)
    gl.glColor3f(0.1f, 0.1f, 0.1f)
    gl.glRotatef(180f, 1f, 0f, 0f)
    glu.gluDisk(p, 0.0, 0.14 * 0.4, 16, 1)
    gl.glRotatef(180f, 1f, 0f, 0f)
    glu.gluDisk(p, 0.0, 0.14 * 0.4, 16, 1)
    gl.glRotatef(-180f, 1f, 0f, 0f)

    gl.glTranslatef(0f, 0f, 0.14f * 2f * -1.01f)
    gl.glRotatef(180f, 1f, 0f, 0f)
    gl.glColor3f(0.1f, 0.1f, 0.1f)
    glu.gluDisk(p, 0.0, 0.14 * 0.4, 16, 1)
    gl.glRotatef(180f, 1f, 0f, 0f)
    glu.gluDisk(p, 0.0, 0.14 * 0.4, 16, 1)
    gl.glRotatef(-180f, 1f, 0f, 0f)

    gl.glPopMatrix()

    //---
    gl.glEndList()

    // Compile cube shape
    lastList += 1
    shapes.put("cube", new GLShape("cube", lastList))
    gl.glNewList(lastList, GL.GL_COMPILE)
    Rectangle.renderRectangularPrism(gl, 0.14f, 0.14f, 0.14f, false)
    gl.glEndList()

    // Compile plane shape
    lastList += 1
    shapes.put("plane", new GLShape("plane", lastList))
    gl.glNewList(lastList, GL.GL_COMPILE)
    gl.glBegin(GL.GL_QUADS)
    gl.glNormal3f(0f, 0f, 1f)
    gl.glVertex3f(.14f, .14f, -.14f)
    gl.glVertex3f(-.14f, .14f, -.14f)
    gl.glVertex3f(-.14f, -.14f, -.14f)
    gl.glVertex3f(.14f, -.14f, -.14f)

    gl.glNormal3f(0f, 0f, -1f)
    gl.glVertex3f(.14f, -.14f, -.14f)
    gl.glVertex3f(-.14f, -.14f, -.14f)
    gl.glVertex3f(-.14f, .14f, -.14f)
    gl.glVertex3f(.14f, .14f, -.14f)
    gl.glEnd()
    gl.glEndList()

    // Compile cube shape
    lastList += 1
    shapes.put("@@@PATCH@@@",
      new GLShape("@@@PATCH@@@", lastList))
    gl.glNewList(lastList, GL.GL_COMPILE)
    Rectangle.renderRectangularPrism(gl, (Renderer.WORLD_SCALE / 2),
      (Renderer.WORLD_SCALE / 2),
      (Renderer.WORLD_SCALE / 2),
      false)
    gl.glEndList()

    // Compile wire frame shape
    lastList += 1
    shapes.put("@@@WIREFRAME@@@",
      new GLShape("@@@WIREFRAME@@@", lastList))
    gl.glNewList(lastList, GL.GL_COMPILE)
    Rectangle.renderRectangularPrism(gl, (Renderer.WORLD_SCALE / 2),
      (Renderer.WORLD_SCALE / 2),
      (Renderer.WORLD_SCALE / 2),
      true)
    gl.glEndList()

    // Compile cross-hairs shape
    lastList += 1
    shapes.put("@@@CROSSHAIRS@@@",
      new GLShape("@@@CROSSHAIRS@@@", lastList))
    gl.glNewList(lastList, GL.GL_COMPILE)
    gl.glPushAttrib(GL.GL_ENABLE_BIT)
    gl.glPushAttrib(GL.GL_CURRENT_BIT)

    gl.glDisable(GL.GL_LIGHTING)
    gl.glDisable(GL.GL_CULL_FACE)

    gl.glColor3f(0f, 0f, 1f)

    glu.gluDisk(quadric, 0.120f, 0.135f, SMOOTHNESS, 1)

    gl.glBegin(GL.GL_QUADS)
    gl.glVertex3f(-0.03f, 0.12f, 0f)
    gl.glVertex3f(-0.03f, -0.12f, 0f)
    gl.glVertex3f(0.03f, -0.12f, 0f)
    gl.glVertex3f(0.03f, 0.12f, 0f)

    gl.glVertex3f(-0.12f, 0.03f, 0f)
    gl.glVertex3f(-0.12f, -0.03f, 0f)
    gl.glVertex3f(0.12f, -0.03f, 0f)
    gl.glVertex3f(0.12f, 0.03f, 0f)
    gl.glEnd()

    gl.glBegin(GL.GL_TRIANGLES)
    gl.glVertex3f(-0.03f, 0.140f, 0f)
    gl.glVertex3f(0.03f, 0.140f, 0f)
    gl.glVertex3f(0.00f, 0.185f, 0f)
    gl.glEnd()

    gl.glPopAttrib()
    gl.glPopAttrib()
    gl.glEndList()

    // Compile halo shape
    lastList += 1
    shapes.put("@@@HALO@@@", new GLShape("@@@HALO@@@", lastList, false))
    gl.glNewList(lastList, GL.GL_COMPILE)
    gl.glPushAttrib(GL.GL_ENABLE_BIT)
    gl.glPushAttrib(GL.GL_CURRENT_BIT)

    gl.glDisable(GL.GL_LIGHTING)
    gl.glDisable(GL.GL_CULL_FACE)

    // inner-circle (used by stencil buffer)
    gl.glColorMask(false, false, false, false)
    // use SMOOTHNESS * 2 for halo polygons because it looks better 
    // and since there's only 1 halo, doesn't cost much - AZS 7/7/05
    glu.gluDisk(quadric, 0f, 0.255f, SMOOTHNESS * 2, 1)
    gl.glColorMask(true, true, true, true)

    gl.glEnable(GL.GL_BLEND)

    // inner-ring
    gl.glColor4f(0.784f, 1f, 1f, 0.392f)
    glu.gluDisk(quadric, 0.255f, 0.275f, SMOOTHNESS * 2, 1)

    // middle-ring
    gl.glColor4f(0.784f, 1f, 1f, 0.196f)
    glu.gluDisk(quadric, 0.275f, 0.285f, SMOOTHNESS * 2, 1)

    // dark outermost-ring
    gl.glColor4f(0f, 0f, 0f, 0.196f)
    glu.gluDisk(quadric, 0.285f, 0.295f, SMOOTHNESS * 2, 1)

    gl.glDisable(GL.GL_BLEND)

    gl.glPopAttrib()
    gl.glPopAttrib()
    gl.glEndList()

    // Compile sphere shape
    lastList += 1
    shapes.put("sphere", new GLShape("sphere", lastList))
    gl.glNewList(lastList, GL.GL_COMPILE)
    glu.gluSphere(quadric, 0.15f, SMOOTHNESS, SMOOTHNESS)
    gl.glEndList()

    // Compile dot shape
    lastList += 1
    shapes.put("dot", new GLShape("dot", lastList))
    gl.glNewList(lastList, GL.GL_COMPILE)
    glu.gluSphere(quadric,
      // 2D dot shape, at least at present, is 40% the size
      // of "circle", so use 40% here too
      0.15f * 0.40f,
      SMOOTHNESS, SMOOTHNESS)
    gl.glEndList()

    // Compile stoplight shape
    lastList += 1
    shapes.put("stoplight", new GLShape("stoplight", lastList))
    gl.glNewList(lastList, GL.GL_COMPILE)
    gl.glTranslatef(0f, 0f, (Renderer.WORLD_SCALE))
    glu.gluSphere(quadric, (Renderer.WORLD_SCALE / 4),
      SMOOTHNESS, SMOOTHNESS)
    gl.glColor3f(0.93f, 0.93f, 0.18f)
    Rectangle.renderRectangularPrism(gl, (0.14f / 6),
      (0.14f / 1.5f),
      (0.14f / 1.5f),
      false)
    gl.glEndList()

    // Compile cylinder shape
    lastList += 1
    shapes.put("cylinder", new GLShape("cylinder", lastList))
    gl.glNewList(lastList, GL.GL_COMPILE)
    gl.glTranslatef(0f, 0f, (-Renderer.WORLD_SCALE / 2) + 0.01f)
    glu.gluCylinder(quadric, 0.12f, 0.12f,
      Renderer.WORLD_SCALE - 0.01, SMOOTHNESS, 1)
    gl.glRotatef(180f, 1f, 0f, 0f)
    glu.gluDisk(quadric, 0f, 0.12f, SMOOTHNESS, 1)
    gl.glRotatef(180f, 1f, 0f, 0f)
    gl.glTranslatef(0f, 0f, (Renderer.WORLD_SCALE - 0.01f))
    glu.gluDisk(quadric, 0f, 0.12f, SMOOTHNESS, 1)
    gl.glEndList()

    // Compile cone shape
    lastList += 1
    shapes.put("cone", new GLShape("cone", lastList))
    gl.glNewList(lastList, GL.GL_COMPILE)
    gl.glRotatef(-90f, 1f, 0f, 0f)
    gl.glTranslatef(0f, 0f, -(Renderer.WORLD_SCALE / 2))
    glu.gluCylinder(quadric, Renderer.WORLD_SCALE / 2, 0f,
      Renderer.WORLD_SCALE, SMOOTHNESS, SMOOTHNESS)
    gl.glRotatef(180f, 1f, 0f, 0f)
    glu.gluDisk(quadric, 0f, Renderer.WORLD_SCALE / 2,
      SMOOTHNESS, 1)
    gl.glEndList()

    shapeMap.put("square", "cube")
    shapeMap.put("triangle", "cone")
    shapeMap.put("circle", "sphere")

    lastList
  }

}
