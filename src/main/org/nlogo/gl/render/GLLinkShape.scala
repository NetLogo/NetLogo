// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import javax.media.opengl.{ GL, GL2 }

import org.nlogo.api.{ Link, World }
import org.nlogo.shape.LinkShape

private class GLLinkShape(shape: LinkShape, val directionIndicator: GLShape) {

  private val curviness = shape.curviness
  private val lines: Array[LinkLine] = {
    val a = Array.fill[LinkLine](shape.numLines)(null)
    var i, j = 0
    while(i < a.size) {
      val line = shape.getLine(j)
      if(line.isVisible) {
        a(i) = new LinkLine(line.xcor, line.dashIndex)
        i += 1
      }
      j += 1
    }
    a
  }

  def render(gl: GL2,
             x1: Float, y1: Float, z1: Float,
             x2: Float, y2: Float, z2: Float,
             stroke: Float, isDirected: Boolean, link: Link,
             shapeRenderer: ShapeRenderer, outline: Boolean,
             color: java.awt.Color, world: World) {
    for(line <- lines) {
      gl.glColor4fv(java.nio.FloatBuffer.wrap(color.getRGBComponents(null)))
      line.render(gl,
                  x1 * Renderer.WORLD_SCALE,
                  y1 * Renderer.WORLD_SCALE,
                  z1 * Renderer.WORLD_SCALE,
                  x2 * Renderer.WORLD_SCALE,
                  y2 * Renderer.WORLD_SCALE,
                  z2 * Renderer.WORLD_SCALE,
                  // I pulled 100 out of the air. ok to tweak - ev 12/6/07
                  curviness, stroke, (100 / world.observer.dist).toInt)
    }
    if(isDirected) {
      gl.glLineStipple(4, LinkLine.dashChoices(1))
      renderDirectionIndicator(
        gl, shapeRenderer, x1, y1, z1, x2, y2, z2,
        link, color, outline, stroke, world)
    }
  }

  def renderDirectionIndicator(gl: GL2, shapeRenderer: ShapeRenderer,
                               x1: Double, y1: Double, z1: Double,
                               x2: Double, y2: Double, z2: Double,
                               link: Link, color: java.awt.Color, outline: Boolean,
                               stroke: Float, world: World) {
    val size = link.size
    val indicatorSize = 1.0 max (stroke / 2)
    val trans = shape.getDirectionIndicatorTransform(
      x1, y1, x2, y2, size, link.linkDestinationSize + 2, link, 1, indicatorSize)
    shapeRenderer.renderAgent(
      gl, directionIndicator, color, indicatorSize,
      trans(1), trans(2), 0, stroke, outline,
      Array[Double](trans(0), 0, 0))
  }

}
