// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import javax.media.opengl.{ GL, GL2 }
import org.nlogo.api.World3D

private class ShapeRenderer3D(world: World3D) extends ShapeRenderer(world) {

  override def renderWrappedAgent(gl: GL2, shape3D: GLShape, size: Double, color: java.awt.Color,
                                  label: String, labelColor: AnyRef,
                                  x: Double, y: Double, z: Double, height: Float,
                                  patchSize: Double, fontSize: Int, outline: Boolean,
                                  lineScale: Double, orientation: Array[Double]) {
    val maxx = world.maxPxcor + 0.5
    val minx = world.minPxcor - 0.5
    val maxy = world.maxPycor + 0.5
    val miny = world.minPycor - 0.5
    val maxz = world.maxPzcor + 0.5
    val minz = world.minPzcor - 0.5
    val worldWidth = world.worldWidth
    val worldHeight = world.worldHeight
    val worldDepth = world.worldDepth
    var wrapXRight = false
    var wrapXLeft = false
    var wrapYTop = false
    var wrapYBottom = false
    val stroke = math.max(1, (patchSize * lineScale)).toFloat
    renderAgent(gl, shape3D, color, size, x, y, z,
      stroke, outline, orientation)
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
    if (y + size / 2 > maxy) {
      renderAgent(gl, shape3D, color, size, x, y - worldHeight, z,
        stroke, outline, orientation)
      if (wrapXRight) {
        renderAgent(gl, shape3D, color, size, x - worldWidth, y - worldHeight, z,
          stroke, outline, orientation)
      }
      if (wrapXLeft) {
        renderAgent(gl, shape3D, color, size, x + worldWidth, y - worldHeight, z,
          stroke, outline, orientation)
      }
      wrapYTop = true
    }
    if (y - size / 2 < miny) {
      renderAgent(gl, shape3D, color, size, x, y + worldHeight, z,
        stroke, outline, orientation)
      if (wrapXRight)
        renderAgent(gl, shape3D, color, size, x - worldWidth, y + worldHeight, z,
          stroke, outline, orientation)
      if (wrapXLeft)
        renderAgent(gl, shape3D, color, size, x + worldWidth, y + worldHeight, z,
          stroke, outline, orientation)
      wrapYBottom = false
    }
    // 3d is always a torus for now so don't worry about checking for wrapping.
    if (worldDepth > 1) {
      if (z + size / 2 > maxz) {
        renderAgent(gl, shape3D, color, size, x, y, z - worldDepth,
          stroke, outline, orientation)
        if (wrapXRight) {
          renderAgent(gl, shape3D, color, size, x - worldWidth, y, z - worldDepth,
            stroke, outline, orientation)
          if (wrapYTop)
            renderAgent(gl, shape3D, color, size, x - worldWidth, y - worldHeight, z - worldDepth,
              stroke, outline, orientation)
          if (wrapYBottom)
            renderAgent(gl, shape3D, color, size, x - worldWidth, y + worldHeight, z - worldDepth,
              stroke, outline, orientation)
        }
        if (wrapXLeft) {
          renderAgent(gl, shape3D, color, size, x + worldWidth, y, z - worldDepth,
            stroke, outline, orientation)
          if (wrapYTop)
            renderAgent(gl, shape3D, color, size, x + worldWidth, y - worldHeight, z - worldDepth,
              stroke, outline, orientation)
          if (wrapYBottom)
            renderAgent(gl, shape3D, color, size, x + worldWidth, y + worldHeight, z - worldDepth,
              stroke, outline, orientation)
        }
        if (wrapYTop)
          renderAgent(gl, shape3D, color, size, x, y - worldHeight, z - worldDepth,
            stroke, outline, orientation)
        if (wrapYBottom)
          renderAgent(gl, shape3D, color, size, x, y + worldHeight, z - worldDepth,
            stroke, outline, orientation)
      }
      if (z - size / 2 < minz) {
        renderAgent(gl, shape3D, color, size, x, y, z + worldDepth,
          stroke, outline, orientation)
        if (wrapXRight) {
          renderAgent(gl, shape3D, color, size, x - worldWidth, y, z + worldDepth,
            stroke, outline, orientation)
          if (wrapYTop)
            renderAgent(gl, shape3D, color, size, x - worldWidth, y - worldHeight, z + worldDepth,
              stroke, outline, orientation)
          if (wrapYBottom)
            renderAgent(gl, shape3D, color, size, x - worldWidth, y + worldHeight, z + worldDepth,
              stroke, outline, orientation)
        }
        if (wrapXLeft) {
          renderAgent(gl, shape3D, color, size, x + worldWidth, y, z + worldDepth,
            stroke, outline, orientation)
          if (wrapYTop)
            renderAgent(gl, shape3D, color, size, x + worldWidth, y - worldHeight, z + worldDepth,
              stroke, outline, orientation)
          if (wrapYBottom)
            renderAgent(gl, shape3D, color, size, x + worldWidth, y + worldHeight, z + worldDepth,
              stroke, outline, orientation)
        }
        if (wrapYTop)
          renderAgent(gl, shape3D, color, size, x, y - worldHeight, z + worldDepth,
            stroke, outline, orientation)
        if (wrapYBottom)
          renderAgent(gl, shape3D, color, size, x, y + worldHeight, z + worldDepth,
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

}
