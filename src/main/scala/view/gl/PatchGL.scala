package view25d.view.gl

import java.util.ConcurrentModificationException

import com.jogamp.opengl.{ GL, GL2, GLAutoDrawable }
import com.jogamp.opengl.fixedfunc.GLMatrixFunc
import com.jogamp.opengl.glu.{ GLU, GLUquadric }

import view25d.view.{ MouseableGLWindow, PatchViewGUI }

class PatchGL(viewer: PatchViewGUI) extends MouseableGLWindow(viewer) {
  // handles for compiled GL shapes
  private var patchTileListHandle = 0
  private var patchThickTileListHandle = 0
  private var patchStickListHandle = 0
  private var sphereDotListHandle = 0
  private var altThickPatchHandle = 0
  private var axisHeadHandle = 0
  private var patchDiskTileHandle = 0
  private var patchSkyscraperHandle = 0
  private var pinHeadListHandle = 0

  // GLU quadric for use in making spheres and in setting up NLGLU helper class for turtle shapes
  protected var quadric: GLUquadric = null

  override def setupCompiledDisplayLists(gl: GL2): Unit = {
    patchTileListHandle = gl.glGenLists(1)
    gl.glNewList(patchTileListHandle, GL2.GL_COMPILE)
    Compilables.PatchTile(gl)
    gl.glEndList()

    patchStickListHandle = gl.glGenLists(1)
    gl.glNewList(patchStickListHandle, GL2.GL_COMPILE)
    Compilables.PatchStickTile(gl, 10 )
    gl.glEndList()

    patchThickTileListHandle = gl.glGenLists(1)
    gl.glNewList(patchThickTileListHandle, GL2.GL_COMPILE)
    Compilables.ThickPatchTile(gl, 0.4f, 0.2f)
    gl.glEndList()

    sphereDotListHandle = gl.glGenLists(1)

    val quadr = glu.gluNewQuadric()

    glu.gluQuadricDrawStyle(quadr, GLU.GLU_FILL)
    glu.gluQuadricNormals(quadr, GLU.GLU_SMOOTH)

    val radius = 0.4f
    val slices = 16

    gl.glNewList(sphereDotListHandle, GL2.GL_COMPILE)
    Compilables.PinHead(gl, glu, quadr, radius, slices)
    gl.glEndList()

    altThickPatchHandle = gl.glGenLists(1)
    gl.glNewList(altThickPatchHandle, GL2.GL_COMPILE)
    Compilables.ThickPatchTile(gl, 0.4f, 0.15f)
    Compilables.PinHead(gl, glu, quadr, 0.3f, slices)
    gl.glEndList()

    patchSkyscraperHandle = gl.glGenLists(1)
    gl.glNewList(patchSkyscraperHandle, GL2.GL_COMPILE)
    Compilables.box(gl, 0.4f, 1.0f)
    gl.glEndList()

    axisHeadHandle = gl.glGenLists(1)
    gl.glNewList(axisHeadHandle, GL2.GL_COMPILE)
    Compilables.AxisHead(gl, glu, quadr, 1.3, slices)
    gl.glEndList()

    patchDiskTileHandle = gl.glGenLists(1)
    gl.glNewList(patchDiskTileHandle, GL2.GL_COMPILE)
    Compilables.DiskPatchTile(gl, glu, quadr, 0.4, slices)
    gl.glEndList()

    pinHeadListHandle = gl.glGenLists(1)
    gl.glNewList(pinHeadListHandle, GL2.GL_COMPILE)
    Compilables.PinHead(gl, glu, quadr, 0.6f, slices)
    gl.glEndList()

    nlGLU.setQuadric(quadr)
    compileShapes(gl, compiledShapes, false)
    glu.gluDeleteQuadric(quadr)
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

    // gl.glColor3f(1.0, 0.6, 5.9)
    // gl.glColor3f(0.2, 0.2, 1.9)

    setColorAndStandardMaterial(gl, 0, 0.1f, 0.9f)
    gl.glPushMatrix()
    observer.applyPerspective(gl)

    if (viewer.viewOptions.tangents) {
      for (i <- 0 until viewer.worldWidth) {
        for (j <- 0 until viewer.worldHeight) {
          gl.glPushMatrix()
          gl.glTranslated(i + viewer.minPxcor, j + viewer.minPycor, -0.01)
          gl.glCallList(patchTileListHandle)
          gl.glPopMatrix()
        }
      }
    }

    for (i <- 0 until viewer.worldWidth) {
      for (j <- 0 until viewer.worldHeight) {
        gl.glPushMatrix()

        val pv =  viewer.reporterValueMatrix(i)(j)
        val v = viewer.zScale * pv.reporterValue
        val c = pv.color

        gl.glTranslated(i + viewer.minPxcor, j + viewer.minPycor, v)

        if (viewer.viewOptions.sticks) {
          // gl.glColor3f(2.5f, 2.5f, 2.5f)
          setColorAndStandardMaterial(gl, 0.6f, 0.6f, 0.6f)
          gl.glLineWidth(0.1f)
          gl.glBegin(GL.GL_LINES)
          gl.glVertex3i(0, 0, 0)
          gl.glVertex3d(0, 0, -v)
          gl.glEnd()
        }

        setColorAndStandardMaterial(gl, c.getRed / 255f, c.getGreen / 255f, c.getBlue / 255f)

        if (viewer.viewOptions.tangents) {
          if (j > 0 && j < viewer.worldHeight - 1) {
            val slopey = (viewer.reporterValueMatrix(i)(j + 1).reporterValue -
                          viewer.reporterValueMatrix(i)(j - 1).reporterValue) *
                          viewer.zScale / 2.0
            val beta = 180.0 * Math.atan(slopey) / Math.PI

            gl.glRotated(beta, 1, 0, 0)
          }

          if (i > 0 && i < viewer.worldWidth - 1) {
            val slopex = (viewer.reporterValueMatrix(i + 1)(j).reporterValue -
                          viewer.reporterValueMatrix(i - 1)(j).reporterValue) *
                          viewer.zScale / 2.0
            val alpha = 180.0 * Math.atan(slopex) / Math.PI

            gl.glRotated(-alpha, 0, 1, 0)
          }

          gl.glCallList(pinHeadListHandle)
          // gl.glCallList(patchDiskTileHandle)
        }

        else {
          // gl.glColor3f(1.0f, 3.9f, 0.6f)
          // gl.glColor3f(0.1f, 0.8f, 0.1f)
          gl.glTranslated(0, 0, -v)
          gl.glScaled(1, 1, v)
          gl.glCallList(patchSkyscraperHandle)
        }

        gl.glPopMatrix()
      }
    }

    if (viewer.viewOptions.structures) {
      for (tv <- viewer.turtleValues) {
        gl.glPushMatrix()

        try {
          setColorAndStandardMaterial(gl, tv.color.getRed / 255f, tv.color.getGreen / 255f, tv.color.getBlue / 255f)

          val zval = viewer.zScale * tv.reporterValue

          if (zval < 0)
            gl.glTranslated(tv.xcor , tv.ycor, zval - 0.045) // a tiny bit below the patch rep, take line thickness into account.
          else
            gl.glTranslated(tv.xcor , tv.ycor, zval + 0.045) // a tiny bit above the patch rep, take line thickness into account.

          // observer.applyNormal(gl) // DON'T turn the turtles to be 'sprites'
          gl.glScaled(tv.size, tv.size, tv.size)
          gl.glScaled(3.0, 3.0, 3.0)
          gl.glRotated(-tv.heading, 0, 0, 1) // DO reflect the turtles' headings
          gl.glCallList(compiledShapes(tv.shape))
        }

        catch {
          case cme: ConcurrentModificationException =>
            cme.printStackTrace()
            // proving that there are no cmes any more
            throw cme
          case npe: NullPointerException =>
            npe.printStackTrace()
            // proving that there are not npes any more.
            throw npe
        }

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
    }

    drawAxesIfDragging(gl, axisHeadHandle)

    gl.glPopMatrix()
  }
}
