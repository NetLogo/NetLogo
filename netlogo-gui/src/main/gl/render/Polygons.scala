// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import org.nlogo.shape.Polygon
import com.jogamp.opengl.{ GL, GL2, GL2GL3, GL2ES3 }
import com.jogamp.opengl.glu.{ GLU, GLUtessellator }

object Polygons {

  def renderPolygon(gl: GL2, glu: GLU, tessellator: Tessellator, tess: GLUtessellator,
                    offset: Int, poly: Polygon, rotatable: Boolean, is3D: Boolean): Unit = {
    val zDepth = 0.01f + offset * 0.0001f
    // this is more complex than it looks, primarily because OpenGL cannot render concave polygons
    // directly but must "tessellate" the polygon into simple convex triangles first - jrn 6/13/05
    if (!poly.marked) {
      val rgb = poly.awtColor.getRGBColorComponents(null)
      gl.glPushAttrib(GL2.GL_CURRENT_BIT)
      gl.glColor3fv(java.nio.FloatBuffer.wrap(rgb))
    }
    if (!poly.filled)
      gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE)
    // It appears that polys go in arbitrary winding.  I don't know what is a better approach
    // disabling back-facked culling or redrawing the object in reverse winding (or some better
    // alternative) ... too lazy right now - jrn 6/13/05
    gl.glDisable(GL.GL_CULL_FACE)
    val data = tessellator.createTessDataObject(gl)
    val xcoords = poly.xCoords
    val ycoords = poly.yCoords
    if(is3D)
      renderPolygon3D(gl, glu, tess, data, xcoords, ycoords, zDepth, rotatable)
    else
      renderPolygon2D(gl, glu, tess, data, xcoords, ycoords, zDepth, rotatable)
    if (!poly.filled)
      gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL)
    // no need to "pancake" if it is always facing the user
    if (rotatable) {
      // render sides
      for(i <- 0 until xcoords.size) {
        val coords = Array[Float](
          xcoords(i) * 0.001f - 0.15f,
          (300 - ycoords(i)) * .001f - 0.15f,
          xcoords((i + 1) % xcoords.size) * .001f - 0.15f,
          (300 - ycoords((i + 1) % xcoords.size)) * .001f - 0.15f
        )
        gl.glBegin(GL2ES3.GL_QUADS)
        val (normalX, normalY, normalZ) =
          findNormal(coords(0), coords(1), -zDepth,
                     coords(0), coords(1), zDepth,
                     coords(2), coords(3), zDepth)
        gl.glNormal3d(normalX, normalY, normalZ)
        gl.glVertex3f(coords(0), coords(1), -zDepth)
        gl.glVertex3f(coords(0), coords(1), zDepth)
        gl.glVertex3f(coords(2), coords(3), zDepth)
        gl.glVertex3f(coords(2), coords(3), -zDepth)
        gl.glEnd()
      }
    }
    gl.glEnable(GL.GL_CULL_FACE)
    if (!poly.marked)
      gl.glPopAttrib()
  }

  private def renderPolygon2D(gl: GL2, glu: GLU, tess: GLUtessellator,
                              data: Tessellator.TessDataObject,
                              xcoords: Seq[Int],
                              ycoords: Seq[Int],
                              zDepth: Float, rotatable: Boolean): Unit = {
    GLU.gluTessBeginPolygon(tess, data)
    GLU.gluTessBeginContour(tess)
    for(i <- 0 until xcoords.size) {
      val coords = Array[Double](
        xcoords(i) * .001 - 0.15,
        (300 - ycoords(i)) * .001 - 0.15,
        zDepth)
      GLU.gluTessVertex(tess, coords, 0, coords)
    }
    GLU.gluTessEndContour(tess)
    GLU.gluTessEndPolygon(tess)
  }

  private def renderPolygon3D(gl: GL2, glu: GLU, tess: GLUtessellator,
                              data: Tessellator.TessDataObject,
                              xcoords: Seq[Int],
                              ycoords: Seq[Int],
                              zDepth: Float, rotatable: Boolean): Unit = {
    GLU.gluTessBeginPolygon(tess, data)
    GLU.gluTessBeginContour(tess)
    // render top
    for((x, y) <- xcoords zip ycoords) {
      val coords = Array[Double](
        x * 0.001 - 0.15,
        (300 - y) * .001 - 0.15,
        zDepth)
      GLU.gluTessVertex(tess, coords, 0, coords)
    }
    GLU.gluTessEndContour(tess)
    GLU.gluTessEndPolygon(tess)
    if(rotatable) {
      import scala.jdk.CollectionConverters.ListHasAsScala

      gl.glBegin(data.tpe)
      gl.glNormal3f(0f, 0f, -1f)
      for(element <- data.shapeData.asScala)
        element match {
          case coords: Array[Double] =>
            data.gl.glVertex3d(coords(0), coords(1), -zDepth)
          case b: java.lang.Boolean =>
            gl.glEdgeFlag(b.booleanValue)
          case _ =>
            throw new IllegalStateException
        }
      gl.glEnd()
    }
  }

  private def findNormal(x1: Float, y1: Float, z1: Float,
                         x2: Float, y2: Float, z2: Float,
                         x3: Float, y3: Float, z3: Float): (Double, Double, Double) = {
    val vectX1 = x1 - x2
    val vectY1 = y1 - y2
    val vectZ1 = z1 - z2
    val vectX2 = x3 - x2
    val vectY2 = y3 - y2
    val vectZ2 = z3 - z2
    val normX = vectY1 * vectZ2 - vectY2 * vectZ1
    val normY = vectX2 * vectZ1 - vectX1 * vectZ2
    val normZ = vectX1 * vectY2 - vectX2 * vectY1
    val leng = math.sqrt(normX * normX + normY * normY + normZ * normZ)
    (-normX / leng, -normY / leng, -normZ / leng)
  }

}
