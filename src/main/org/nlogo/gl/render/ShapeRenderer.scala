// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import javax.media.opengl.{ GL, GL2, GL2GL3 }

import org.nlogo.api.World
import org.nlogo.api.Perspective
import org.nlogo.api.Agent

private class ShapeRenderer(world: World) {

  // We use a stencil buffer to highlight turtles. Some graphics cards, including the NVIDIA GeForce
  // FX 5200 Ultra, don't seem to support a stencil buffer, so we keep track of whether this card
  // supports the buffer and implement a workaround if not. - AZS 6/22/05
  var stencilSupport = false

  var shapeManager: ShapeManager = _

  def renderWrappedAgent(gl: GL2, shape3D: GLShape, size: Double, color: java.awt.Color,
                         label: String, labelColor: AnyRef,
                         x: Double, y: Double, z: Double, height: Float,
                         patchSize: Double, fontSize: Int, outline: Boolean,
                         lineThickness: Double, orientation: Array[Double]) {
    val maxx = world.maxPxcor + 0.5
    val minx = world.minPxcor - 0.5
    val maxy = world.maxPycor + 0.5
    val miny = world.minPycor - 0.5
    val worldWidth = world.worldWidth
    val worldHeight = world.worldHeight
    var wrapXRight = false
    var wrapXLeft = false
    val stroke = math.max(1, (patchSize * lineThickness)).toFloat
    renderAgent(gl, shape3D, color, size, x, y, z,
      stroke, outline, orientation)
    if (world.wrappingAllowedInX) {
      if (x + size / 2 > maxx) {
        renderAgent(gl, shape3D, color, size, x - worldWidth, y, z,
          stroke, outline, orientation)
        wrapXRight = true
      }
      if (x - size / 2 < minx) {
        renderAgent(gl, shape3D, color, size, x + worldWidth, y, z,
          stroke, outline, orientation)
        wrapXLeft = true
      }
    }
    if (world.wrappingAllowedInY) {
      if (y + size / 2 > maxy) {
        renderAgent(gl, shape3D, color, size, x, y - worldHeight, z,
          stroke, outline, orientation)
        if (wrapXRight)
          renderAgent(gl, shape3D, color, size,
            x - worldWidth, y - worldHeight, z,
            stroke, outline, orientation)

        if (wrapXLeft)
          renderAgent(gl, shape3D, color, size,
            x + worldWidth, y - worldHeight, z,
            stroke, outline, orientation)
      }
      if (y - size / 2 < miny) {
        renderAgent(gl, shape3D, color, size, x, y + worldHeight, z,
          stroke, outline, orientation)
        if (wrapXRight)
          renderAgent(gl, shape3D, color, size,
            x - worldWidth, y + worldHeight, z,
            stroke, outline, orientation)
        if (wrapXLeft)
          renderAgent(gl, shape3D, color, size,
            x + worldWidth, y + worldHeight, z,
            stroke, outline, orientation)
      }
    }
    if (label.length != 0)
      renderLabel(gl, label, labelColor,
        (x * Renderer.WORLD_SCALE).toFloat,
        (y * Renderer.WORLD_SCALE).toFloat,
        (z * Renderer.WORLD_SCALE).toFloat,
        height, fontSize, patchSize)
  }

  def renderAgent(gl: GL2, shape3D: GLShape, color: java.awt.Color, size: Double,
                  xcor: Double, ycor: Double, zcor: Double,
                  stroke: Float, outline: Boolean, orientation: Array[Double]) {
    gl.glPushMatrix()
    alignAgent(gl, size,
      xcor * Renderer.WORLD_SCALE,
      ycor * Renderer.WORLD_SCALE,
      zcor * Renderer.WORLD_SCALE,
      shape3D, false, orientation)
    gl.glColor4fv(java.nio.FloatBuffer.wrap(color.getRGBComponents(null)))
    if (outline)
      doOutline(gl, shape3D, color.getComponents(null))
    else {
      gl.glLineWidth(stroke)
      gl.glCallList(shape3D.displayListIndex)
      gl.glLineWidth(1f)
    }
    gl.glPopMatrix()
  }

  def alignAgent(gl: GL2, size: Double, xcor: Double, ycor: Double, zcor: Double,
                 shape3D: GLShape, highlight: Boolean, orientation: Array[Double]) {
    val Array(heading, pitch, roll) = orientation
    gl.glTranslated(xcor, ycor, zcor)
    // non-rotatable shapes always face the viewpoint
    if (highlight && world.observer.perspective == Perspective.Follow) {
      gl.glRotated(-world.observer.heading, 0.0, 0.0, 1.0)
      gl.glRotated(90, 1.0, 0.0, 0.0)
      gl.glRotated(-world.observer.pitch, -1.0, 0.0, 0.0)
    }
    else if (shape3D.rotatable && !highlight) {
      gl.glRotated((0 - heading), 0.0d, 0.0d, 1.0d)
      gl.glRotated(pitch, 1.0d, 0.0d, 0.0d)
      gl.glRotated(roll, 0.0d, 1.0d, 0.0d)
    }
    else {
      gl.glRotated(-world.observer.heading, 0.0, 0.0, 1.0)
      gl.glRotated(90, 1.0, 0.0, 0.0)
      if (world.observer.perspective == Perspective.Follow || world.observer.perspective == Perspective.Ride) {
        gl.glRotated(-world.observer.pitch, -1.0, 0.0, 0.0)
        gl.glRotated(-world.observer.roll, 0.0, 0.0, 1.0)
      }
      else {
        gl.glRotated(world.observer.pitch, -1.0, 0.0, 0.0)
        gl.glRotated(world.observer.roll, 0.0, 0.0, 1.0)
      }
    }
    gl.glScaled(size, size, size)
  }

  def doOutline(gl: GL2, shape3D: GLShape, rgb: Array[Float]) {
    if (stencilSupport) {
      // This highlighting code was borrowed from
      // http://www.flipcode.com/articles/article_objectoutline.shtml
      gl.glClearStencil(0)
      gl.glClear(GL.GL_STENCIL_BUFFER_BIT)
      // Render the object into the stencil buffer.
      gl.glEnable(GL.GL_STENCIL_TEST)
      gl.glStencilFunc(GL.GL_ALWAYS, 1, 0xFFFF)
      gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE)
      // save the old transformation for rendering the highlight wireframe
      gl.glPushMatrix()
      gl.glCallList(shape3D.displayListIndex)
      gl.glPopMatrix()
      // Render the thick wireframe version.
      gl.glStencilFunc(GL.GL_NOTEQUAL, 1, 0xFFFF)
      gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE)
      gl.glLineWidth(2)
      gl.glPolygonMode(GL.GL_FRONT, GL2GL3.GL_LINE)
      gl.glColor4f((rgb(0) + 0.5f) % 1f, (rgb(1) + 0.5f) % 1f,
        (rgb(2) + 0.5f) % 1f, 0.5f)
      gl.glCallList(shape3D.displayListIndex)
      gl.glLineWidth(4)
      gl.glPolygonMode(GL.GL_FRONT, GL2GL3.GL_LINE)
      gl.glColor4f(rgb(0), rgb(1), rgb(2), 0.5f)
      gl.glCallList(shape3D.displayListIndex)
      gl.glLineWidth(1)
      gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL)
      gl.glDisable(GL.GL_STENCIL_TEST)
    }
    else {
      // render the thick-lined wireframe
      gl.glPushMatrix()
      gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE)
      gl.glColor4f(rgb(0), rgb(1), rgb(2), 0.5f)
      gl.glLineWidth(4)
      gl.glCallList(shape3D.displayListIndex)
      gl.glColor4f((rgb(0) + 0.5f) % 1f,
                   (rgb(1) + 0.5f) % 1f,
                   (rgb(2) + 0.5f) % 1f, 0.5f)
      gl.glLineWidth(2)
      gl.glCallList(shape3D.displayListIndex)
      gl.glLineWidth(1)
      gl.glPopMatrix()
      // push the filled object towards the screen
      gl.glEnable(GL.GL_POLYGON_OFFSET_FILL)
      gl.glPolygonOffset(-3f, -3f)
      gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL)
      gl.glColor4f(rgb(0), rgb(1), rgb(2), 0.5f)
      gl.glCallList(shape3D.displayListIndex)
      gl.glDisable(GL.GL_POLYGON_OFFSET_FILL)
    }
  }

  def getShape(name: String): GLShape =
    shapeManager.getShape(name)

  def getLinkShape(name: String): GLLinkShape =
    shapeManager.getLinkShape(name)

  def getShapeHeight(name: String, shape: GLShape, size: Double): Float =
    if (shapeManager.modelLibraryShape(name) && shape.rotatable)
      1f
    else
      size.toFloat

  def renderHighlight(gl: GL2, agent: Agent, shape: GLShape, coords: Array[Double], orientation: Array[Double]) {
    gl.glPushMatrix()
    alignAgent(gl, agent.size,
      coords(0) * Renderer.WORLD_SCALE,
      coords(1) * Renderer.WORLD_SCALE,
      coords(2) * Renderer.WORLD_SCALE,
      shape, true, orientation)
    renderHalo(gl, true, agent.size * 3.3333 * 0.285 * 2.0)
    gl.glPopMatrix()
  }

  def renderLabel(gl: GL2, label: String, labelColor: AnyRef,
                  xcor: Float, ycor: Float, zcor: Float,
                  height: Float, fontSize: Int, patchSize: Double) {
    val observer = world.observer
    gl.glPushMatrix()
    gl.glTranslated(xcor, ycor,
      zcor + ((Renderer.WORLD_SCALE / 2) * height))
    gl.glRotated(-observer.heading, 0.0, 0.0, 1.0)
    gl.glRotated(90, 1.0, 0.0, 0.0)
    if (observer.perspective == Perspective.Follow || observer.perspective == Perspective.Ride) {
      gl.glRotated(-observer.pitch, -1.0, 0.0, 0.0)
      gl.glRotated(-observer.roll, 0.0, 0.0, 1.0)
    } else {
      gl.glRotated(observer.pitch, -1.0, 0.0, 0.0)
      gl.glRotated(observer.roll, 0.0, 0.0, 1.0)
    }
    AgentRenderer.renderString(gl, world, label, labelColor, fontSize, patchSize)
    gl.glPopMatrix()
  }

  def renderHalo(gl: GL2, isTurtle: Boolean, diameter: Double) {
    val haloShape = shapeManager.getShape("@@@HALO@@@")
    val width = world.worldWidth
    val height = world.worldHeight
    // this is adopted from the 2D spotlight stretch algorithm
    val stretch =
      // Don't let the spotlight be smaller than 10% of total view height / width
      if (diameter < math.max(width / 10.0, height / 10.0))
        math.max(width / 10.0, height / 10.0) / diameter
      else 1.0
    // force these things to be rendered so they don't get cutoff by the world
    gl.glDepthFunc(GL.GL_ALWAYS)
    if (stencilSupport) {
      gl.glClearStencil(0)
      gl.glClear(GL.GL_STENCIL_BUFFER_BIT)
      gl.glEnable(GL.GL_STENCIL_TEST)
      gl.glStencilFunc(GL.GL_ALWAYS, 1, 0xFFFF)
      gl.glStencilOp(GL.GL_KEEP, GL.GL_REPLACE, GL.GL_REPLACE)
    }
    gl.glPushMatrix()
    gl.glScaled(stretch, stretch, 0.0)
    gl.glCallList(haloShape.displayListIndex)
    gl.glPopMatrix()
    if (world.observer.perspective == Perspective.Watch && stencilSupport) {
      if (!isTurtle) {
        gl.glRotated(-world.observer.heading, 0.0, 0.0, 1.0)
        gl.glRotated(90, 1.0, 0.0, 0.0)
        gl.glRotated(world.observer.pitch, -1.0, 0.0, 0.0)
      }
      gl.glStencilFunc(GL.GL_NOTEQUAL, 1, 0xFFFF)
      gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP)

      // draw dark screen over everything except the halo
      gl.glEnable(GL.GL_BLEND)
      gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE)
      val darkOverlay = Array(0f, 0f, 0.196f, 0.392f)
      gl.glColor4fv(java.nio.FloatBuffer.wrap(darkOverlay))
      gl.glBegin(GL2GL3.GL_QUADS)
      gl.glNormal3f(0f, 0f, 1f)
      gl.glVertex3f(-100f, 100f, 0f)
      gl.glVertex3f(-100f, -100f, 0f)
      gl.glVertex3f(100f, -100f, 0f)
      gl.glVertex3f(100f, 100f, 0f)
      gl.glEnd()
      gl.glDisable(GL.GL_BLEND)
      gl.glDisable(GL.GL_STENCIL_TEST)
    }
    gl.glDepthFunc(GL.GL_LEQUAL)
  }

}
