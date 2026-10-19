package view25d.view.gl

import com.jogamp.opengl.{ GL, GL2, GLAutoDrawable }
import com.jogamp.opengl.fixedfunc.GLMatrixFunc
import com.jogamp.opengl.glu.{ GLU, GLUquadric }

import org.nlogo.api.Color

import view25d.view.{ MouseableGLWindow, TurtleViewGUI }

class TurtleGL(viewer: TurtleViewGUI) extends MouseableGLWindow(viewer) {
  // handles for compiled GL shapes
  var patchTileListHandle = 0
  var stemSkyscraperHandle = 0
  var pinHeadListHandle = 0
  var axisHeadHandle = 0

  // GLU quadric for use in making spheres and in setting up NLGLU helper class for turtle shapes
  protected var quadric: GLUquadric = null

  override def setupCompiledDisplayLists(gl: GL2): Unit = {
    patchTileListHandle = gl.glGenLists(1)
    gl.glNewList(patchTileListHandle, GL2.GL_COMPILE)
    Compilables.PatchTile(gl)
    gl.glEndList()

    quadric = glu.gluNewQuadric()
    glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL)
    glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH)
    nlGLU.setQuadric(quadric)

    stemSkyscraperHandle = gl.glGenLists(1)
    gl.glNewList(stemSkyscraperHandle, GL2.GL_COMPILE)
    // Compilables.box(gl, .4f, 1.0f)
    Compilables.ThickStem(gl, glu, quadric, 1.0, 6)
    gl.glEndList()

    pinHeadListHandle = gl.glGenLists(1)

    val slices = 16

    gl.glNewList(pinHeadListHandle, GL2.GL_COMPILE)
    Compilables.PinHead(gl, glu, quadric, 0.4f, slices)
    gl.glEndList()

    compileShapes(gl, compiledShapes, false)

    axisHeadHandle = gl.glGenLists(1)
    gl.glNewList(axisHeadHandle, GL2.GL_COMPILE)
    Compilables.AxisHead(gl, glu, quadric, 1.3, slices)
    gl.glEndList()
  }

  override def display(drawable: GLAutoDrawable): Unit = {
    val gl = drawable.getGL.asInstanceOf[GL2]

    if (areShapesStale) {
      compileShapes(gl, compiledShapes, false)
      setDeletedShapesToDefaultShape(compiledShapes)
      areShapesStale = false
    }

    gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
    gl.glLoadIdentity()

    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT)
    gl.glLineWidth(1.0f)

    gl.glPushMatrix()
    observer.applyPerspective(gl)

    if (viewer.viewOptions.usePColor) {
      for (i <- 0 until viewer.worldWidth) {
        for (j <- 0 until viewer.worldHeight) {
          gl.glPushMatrix()
          gl.glTranslated(i + viewer.minPxcor, j + viewer.minPycor, 0)

          val c = viewer.patchColorMatrix(i)(j)

          setColorAndStandardMaterial(gl, c.getRed / 255f, c.getGreen / 255f, c.getBlue / 255f)

          gl.glCallList(patchTileListHandle)
          gl.glPopMatrix()
        }
      }
    }

    else {
      setColorAndStandardMaterial(gl, 0.1f, 0.1f, 1)

      for (i <- 0 until viewer.worldWidth) {
        for (j <- 0 until viewer.worldHeight) {
          gl.glPushMatrix()
          gl.glTranslated(i + viewer.minPxcor, j + viewer.minPycor, 0)
          gl.glCallList(patchTileListHandle)
          gl.glPopMatrix()
        }
      }
    }

    // Draw Turtle Stems
    val stemThickness = viewer.viewOptions.getStemThickness

    for (tv <- viewer.turtleReporterValues) {
      gl.glPushMatrix()

      val zval = viewer.zScale * tv.reporterValue

      gl.glTranslated(tv.xcor, tv.ycor, zval)

      val c = Color.getColor(Double.box(tv.stemColor))

      setColorAndStandardMaterial(gl, c.getRed / 255f, c.getGreen / 255f, c.getBlue / 255f)

      if (stemThickness == 0) {
        gl.glLineWidth(0.1f)
        gl.glBegin(GL.GL_LINES)
        gl.glVertex3i(0, 0, 0)
        gl.glVertex3d(0, 0, -zval)
        gl.glEnd()
      }

      else {
        gl.glPushMatrix()
        gl.glTranslated(0, 0, -zval)
        gl.glScaled(stemThickness, stemThickness, zval)
        gl.glCallList(stemSkyscraperHandle)
        gl.glPopMatrix()
      }

      if (viewer.viewOptions.showSize)
        gl.glScaled(tv.size, tv.size, tv.size)

      if (viewer.viewOptions.showColor)
        setColorAndStandardMaterial(gl, tv.color.getRed / 255f, tv.color.getGreen / 255f, tv.color.getBlue / 255f)
      else
        setColorAndStandardMaterial(gl, 0.8f, 0.8f, 0.45f)

      if (viewer.viewOptions.showShape) {
        observer.applyNormal(gl)

        if (stemThickness > 0)
          gl.glTranslated(0, 0, stemThickness / 2)

        gl.glScaled(3.0, 3.0, 3.0)
        gl.glCallList(compiledShapes(tv.shape))
      }

      else
        gl.glCallList(pinHeadListHandle)

      gl.glPopMatrix()
    }

    // Draw links as lines
    for (lv <- viewer.linkValues) {
      var zval1 = 0.0
      var zval2 = 0.0

      if (viewer.viewOptions.linksAreThreeD) {
        zval1 = viewer.zScale * lv.zcor1
        zval2 = viewer.zScale * lv.zcor2
      }

      val c = lv.color

      setColorAndStandardMaterial(gl, c.getRed / 255f, c.getGreen / 255f, c.getBlue / 255f)

      // lines for links need a minimum thickness to be visible
      gl.glLineWidth(lv.thickness.max(0.1).toFloat)
      gl.glBegin(GL.GL_LINES)
      gl.glVertex3d(lv.xcor1, lv.ycor1, zval1)
      gl.glVertex3d(lv.xcor2, lv.ycor2, zval2)
      gl.glEnd()
    }

    drawAxesIfDragging(gl, axisHeadHandle)
    gl.glPopMatrix()
  }
}
