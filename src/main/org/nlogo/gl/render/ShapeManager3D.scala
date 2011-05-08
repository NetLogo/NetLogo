package org.nlogo.gl.render

import java.util.{ List => JList, Map => JMap }
import javax.media.opengl.GL
import javax.media.opengl.glu.GLU
import org.nlogo.api.ShapeList
import org.nlogo.shape.{ LinkShape, VectorShape }
import org.nlogo.util.JCL._
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

  override def renderPolygon(gl: GL, data: Tessellator.TessDataObject, glu: GLU,
                             xcoords: JList[java.lang.Integer],
                             ycoords: JList[java.lang.Integer],
                             zDepth: Float, rotatable: Boolean) {
    glu.gluTessBeginPolygon(tess, data)
    glu.gluTessBeginContour(tess)
    // render top
    for((x, y) <- xcoords zip ycoords) {
      val coords = Array[Double](
        x.intValue * 0.001 - 0.15,
        (300 - y.intValue) * .001 - 0.15,
        zDepth)
      glu.gluTessVertex(tess, coords, 0, coords)
    }
    glu.gluTessEndContour(tess)
    glu.gluTessEndPolygon(tess)     
    if(rotatable) {
      gl.glBegin(data.tyype)
      gl.glNormal3f(0f, 0f, -1f)
      for(element <- data.shapeData)
        element match {
          case coords: Array[Double] =>
            data.gl.glVertex3d(coords(0), coords(1), -zDepth)
          case b: java.lang.Boolean =>
            gl.glEdgeFlag(b.booleanValue)
        }
      gl.glEnd()
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
