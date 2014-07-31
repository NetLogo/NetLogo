// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import java.util.{ List => JList, ArrayList }
import javax.media.opengl.{ GL, GL2 }

object Tessellator {
  // see javax.media.opengl.glu.GLUtessellatorCallback
  class TessDataObject(val gl: GL2) {
    var tpe: Int = _
    val shapeData: JList[AnyRef] = new ArrayList[AnyRef]
  }
}

private class Tessellator extends javax.media.opengl.glu.GLUtessellatorCallbackAdapter {

  import Tessellator.TessDataObject

  def createTessDataObject(gl: GL2) =
    new TessDataObject(gl)

  override def beginData(tpe: Int, polygonData: AnyRef) {
    val data = polygonData.asInstanceOf[TessDataObject]
    data.gl.glBegin(tpe)
    data.tpe = tpe
    // for now assume we are talking about the "top" polygon
    data.gl.glNormal3f(0f, 0f, 1f)
  }

  override def combineData(coords: Array[Double], data: Array[AnyRef], weight: Array[Float],
                           outData: Array[AnyRef], polygonData: Object) {
    // not sure if this is right...
    outData(0) = Array[Double](coords(0), coords(1), coords(2) )
  }

  override def edgeFlagData(boundaryEdge: Boolean, polygonData: AnyRef) {
    val data = polygonData.asInstanceOf[TessDataObject]
    data.gl.glEdgeFlag(boundaryEdge)
    data.shapeData.add(boundaryEdge: java.lang.Boolean)
  }

  override def endData(polygonData: Object) {
    val data = polygonData.asInstanceOf[TessDataObject]
    data.gl.glEnd()
  }

  override def errorData(errnum: Int, polygonData: AnyRef) { }

  override def vertexData(vertexData: AnyRef, polygonData: AnyRef) {
    val data = polygonData.asInstanceOf[TessDataObject]
    data.gl.glVertex3dv(
      java.nio.DoubleBuffer.wrap(
        vertexData.asInstanceOf[Array[Double]]))
    data.shapeData.add(vertexData)
  }

}
