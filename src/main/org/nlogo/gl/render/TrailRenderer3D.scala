// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import javax.media.opengl.{ GL, GL2 }
import org.nlogo.api.{ Drawing3D, DrawingLine3D, World3D, Perspective }
import collection.JavaConverters._

private class TrailRenderer3D(world: World3D, renderer: TurtleRenderer3D, linkRenderer: LinkRenderer3D)
extends DrawingRendererInterface {

  var lineIndex = 0

  // make a calllist for the line shape we use for drawing.  we don't want to have to depend on the
  // shapeManager so just add it here.
  def init(gl: GL2) {
    lineIndex = gl.glGenLists(1)
    gl.glNewList(lineIndex, GL2.GL_COMPILE)
    gl.glBegin(GL.GL_LINES)
    gl.glNormal3f(0f, 0f, -1f)
    gl.glVertex3f(0, 0 , 0)
    gl.glVertex3f(0 , Renderer.WORLD_SCALE, 0)
    gl.glEnd()
    gl.glEndList()
  }

  private def drawing = world.getDrawing.asInstanceOf[Drawing3D]

  def renderDrawing(gl: GL2) {
    var defaultDist = 1.5 * (world.worldWidth max world.worldHeight max world.worldDepth)
    // Link stamps
    for(stamp <- drawing.linkStamps.asScala) {
      val distance =
        if (world.observer.perspective == Perspective.Follow || world.observer.perspective == Perspective.Ride)
          world.observer.followDistance
        else world.observer.dist
      var lineScale: Double = 0
      if(distance != 0)
        lineScale = (math.max(world.worldWidth, world.worldHeight)) * 1.5 / distance
      linkRenderer.renderWrappedLink(gl, stamp, 0, world.patchSize, false, lineScale)
    }
    // Turtle stamps
    for(stamp <- drawing.turtleStamps.asScala)
      renderer.renderWrappedTurtle(gl, stamp, 0, world.patchSize, false, defaultDist)
    // Turtle trails (pen-down command)
    renderTrails(gl)
    gl.glLineWidth(1f)
  }

  def renderTrails(gl: GL2) {
    for(line <- drawing.lines.asScala)
      renderWrappedLine(gl, line)
  }

  private def renderWrappedLine(gl: GL2, l: DrawingLine3D) {
    val x = world.wrappedObserverX(l.x0)
    val y = world.wrappedObserverY(l.y0)
    val z = world.wrappedObserverZ(l.z0)
    val worldWidth = world.worldWidth
    val worldHeight = world.worldHeight
    val worldDepth = world.worldDepth
    var wrapXRight = false
    var wrapXLeft = false
    var wrapYTop = false
    var wrapYBottom = false
    renderLine(gl, l, x, y, z)
    // note that we pre-wrap the lines when they are drawn in a standard world.  so we only need to
    // do this wrapping if the view is off-center ev 5/24/06
    if(x != l.x0) {
      val endX = x + l.x1 - l.x0
      val maxx = world.maxPxcor + 0.5
      val minx = world.minPxcor - 0.5
      if(endX  > maxx) {
        renderLine(gl, l, x - worldWidth, y, z)
        wrapXRight = true
      }
      if(endX  < minx) {
        renderLine(gl, l, x + worldWidth, y, z)
        wrapXLeft = true
      }
    }
    if(y != l.y0) {
      val endY = y + l.y1 - l.y0
      val maxy = world.maxPycor + 0.5
      val miny = world.minPycor - 0.5
      if(endY  > maxy) {
        renderLine(gl, l, x, y - worldHeight, z)
        if(wrapXRight)
          renderLine(gl, l, x - worldWidth, y - worldHeight, z)
        if(wrapXLeft)
          renderLine(gl, l, x + worldWidth, y - worldHeight, z)
        wrapYTop = true
      }
      if(endY  < miny) {
        renderLine(gl, l, x, y + worldHeight, z)
        if(wrapXRight)
          renderLine(gl, l, x - worldWidth, y + worldHeight, z)
        if(wrapXLeft)
          renderLine(gl, l, x + worldWidth, y + worldHeight, z)
        wrapYBottom = true
      }
    }
    if(z != l.z0) {
      val endZ = z + l.z1 - l.z0
      val maxz = world.maxPzcor + 0.5
      val minz = world.minPzcor - 0.5
      if(endZ  > maxz) {
        renderLine(gl, l, x, y, z - worldDepth)
        if(wrapXRight) {
          renderLine(gl, l, x - worldWidth, y, z - worldDepth)
          if (wrapYTop)
            renderLine(gl, l, x - worldWidth, y - worldHeight, z - worldDepth)
          if (wrapYBottom)
            renderLine(gl, l, x - worldWidth, y + worldHeight, z - worldDepth)
        }
        if(wrapXLeft) {
          renderLine(gl, l, x + worldWidth, y, z - worldDepth)
          if (wrapYTop)
            renderLine(gl, l, x + worldWidth, y - worldHeight, z - worldDepth)
          if (wrapYBottom)
            renderLine(gl, l, x + worldWidth, y + worldHeight, z - worldDepth)
        }
        if(wrapYTop)
          renderLine(gl, l, x, y - worldHeight, z - worldDepth)
        if(wrapYBottom)
          renderLine(gl, l, x, y + worldHeight, z - worldDepth)
      }
      if(endZ  < minz) {
        renderLine(gl, l, x, y, z + worldDepth)
        if(wrapXRight) {
          renderLine(gl, l, x - worldWidth, y, z + worldDepth)
          if (wrapYTop)
            renderLine(gl, l, x - worldWidth, y - worldHeight, z + worldDepth)
          if (wrapYBottom)
            renderLine(gl, l, x - worldWidth, y + worldHeight, z + worldDepth)
        }
        if(wrapXLeft) {
          renderLine(gl, l, x + worldWidth, y, z + worldDepth)
          if (wrapYTop)
            renderLine(gl, l, x + worldWidth, y - worldHeight, z + worldDepth)
          if (wrapYBottom)
            renderLine(gl, l, x + worldWidth, y + worldHeight, z + worldDepth)
        }
        if(wrapYTop)
          renderLine(gl, l, x, y - worldHeight, z + worldDepth)
        if(wrapYBottom)
          renderLine(gl, l, x, y + worldHeight, z + worldDepth)
      }
    }
  }

  private def renderLine(gl: GL2, line: DrawingLine3D, x: Double, y: Double, z: Double) {
    val color = org.nlogo.api.Color.getColor(line.color)
    gl.glPushMatrix()
    alignLine(gl, line, x, y, z)
    gl.glColor3fv(java.nio.FloatBuffer.wrap(color.getRGBColorComponents(null)))
    gl.glLineWidth((1.0 max line.width).toFloat)
    gl.glCallList(lineIndex)
    gl.glPopMatrix()
  }

  private def alignLine(gl: GL2, line: DrawingLine3D, x: Double, y: Double, z: Double) {
    val length = line.length
    gl.glTranslated(x * Renderer.WORLD_SCALE,
                    y * Renderer.WORLD_SCALE,
                    z * Renderer.WORLD_SCALE)
    gl.glRotated(-line.heading, 0.0d, 0.0d, 1.0d)
    gl.glRotated(line.pitch, 1.0d, 0.0d, 0.0d)
    gl.glScaled(length, length, length)
  }

  def clear() { }

}
