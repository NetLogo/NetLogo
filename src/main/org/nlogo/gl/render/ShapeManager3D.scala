package org.nlogo.gl.render

import java.util.{ List => JList, Map => JMap }
import javax.media.opengl.GL
import javax.media.opengl.glu.GLU
import org.nlogo.api.ShapeList
import org.nlogo.shape.{ LinkShape, VectorShape }
import ShapeManager.SMOOTHNESS

private class ShapeManager3D(gl: GL, glu: GLU, turtleShapeList: ShapeList, linkShapeList: ShapeList,
                             customShapes: JMap[String, JList[String]])
extends ShapeManager(gl, glu, turtleShapeList, linkShapeList, customShapes) {

  override def addLinkShape(gl: GL, glu: GLU, shape: LinkShape, index: Int) {
    val vShape = shape.getDirectionIndicator.asInstanceOf[VectorShape]
    linkShapes.put(shape.getName,
                   new GLLinkShape3D(
                     shape, new GLShape(vShape.getName, index, vShape.isRotatable)))
    compileShape(gl, glu, vShape, index, vShape.isRotatable)
  }
  
  override def renderCircle(gl: GL, glu: GLU, innerRadius: Float, outerRadius: Float, zDepth: Float, rotatable: Boolean) {
    glu.gluDisk(quadric, innerRadius, outerRadius, SMOOTHNESS, 1)
    if(rotatable) {
      gl.glRotatef(180f, 1f, 0f, 0f)
      gl.glTranslatef(0f, 0f, zDepth * 2)
      glu.gluDisk(quadric, innerRadius, outerRadius, SMOOTHNESS, 1)
    }
  }

  override def renderRectangle(gl: GL, x0: Float, x1: Float,
                               y0: Float, y1: Float,
                               z0: Float, z1: Float,
                               filled: Boolean, rotatable: Boolean) {
    Rectangle.renderRectangularPrism(gl, x0, x1, y0, y1, 
                                     z0, z1, filled, rotatable, rotatable)
  }
  
}
