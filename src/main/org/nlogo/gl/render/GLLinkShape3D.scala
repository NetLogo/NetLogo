// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import javax.media.opengl.{ GL, GL2 }
import org.nlogo.api.{ Link, Link3D, World, World3D }
import org.nlogo.shape.LinkShape

private class GLLinkShape3D(shape: LinkShape, directionIndicator: GLShape)
extends GLLinkShape(shape, directionIndicator) {

  override def renderDirectionIndicator(gl: GL2, shapeRenderer: ShapeRenderer,
                                        x1: Double, y1: Double, z1: Double,
                                        x2: Double, y2: Double, z2: Double,
                                        link: Link, color: java.awt.Color, outline: Boolean,
                                        stroke: Float, world: World) {
    val size = link.size
    val indicatorSize = 1.0 max (stroke / 2)
    val trans = getDirectionIndicatorTransform(
      x1, y1, z1, x2, y2, z2, size, link.linkDestinationSize + 2, link, 1,
      indicatorSize, world.asInstanceOf[World3D])
    shapeRenderer.renderAgent(
      gl, directionIndicator, color, indicatorSize,
      trans(3), trans(4), trans(5), stroke, outline,
      Array[Double](trans(0), trans(1), trans(2)))
  }

  def getDirectionIndicatorTransform(x1: Double, y1: Double, z1: Double,
                                     x2: Double, y2: Double, z2: Double,
                                     linkLength: Double, destSize: Double, link: Link,
                                     cellSize: Double, size: Double, world: World3D) =
    Array[Double](
      link.heading,
      link.asInstanceOf[Link3D].pitch,
      0,
      x2 + ((x1 - x2) / linkLength * destSize * 2 / 3),
      y2 - ((y2 - y1) / linkLength * destSize * 2 / 3),
      z2 + ((z1 - z2) / linkLength * destSize * 2 / 3))

}
