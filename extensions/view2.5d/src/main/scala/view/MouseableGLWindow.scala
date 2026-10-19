package view25d.view

import java.awt.event.{ MouseAdapter, MouseEvent, MouseWheelEvent }
import java.nio.{ FloatBuffer, IntBuffer }

import com.jogamp.opengl.{ GL, GL2, GL2ES1, GLAutoDrawable, GLEventListener }
import com.jogamp.opengl.awt.GLJPanel
import com.jogamp.opengl.fixedfunc.{ GLLightingFunc, GLMatrixFunc }
import com.jogamp.opengl.glu.GLU

import org.nlogo.app.App
import org.nlogo.core.ShapeList
import org.nlogo.gl.render.{ Polygons, Tessellator }
import org.nlogo.shape.{ Circle, Curve, Line, Polygon, Rectangle, VectorShape }
import org.nlogo.swing.Utils

import scala.collection.mutable.Map

import view25d.view.gl.NetLogoGLU

abstract class MouseableGLWindow(viewer: VarviewWindow) extends MouseAdapter with GLEventListener {
  // initial values are overwritten immediately - this is the correct initial value for the default model
  val observer = new Observer(0, 0, 49.5)

  def updateObserverPerspectiveAnglesWithDeltas(thetax: Double, thetay: Double): Unit = {
    observer.updatePerspectiveAngles(thetax, thetay)
    canvas.repaint()
  }

  def getObserverPerspectiveAngles: Array[Double] =
    Array(observer.heading, observer.pitch)

  def shiftObserverFocusPoint(deltax: Double, deltay: Double): Unit = {
    observer.objectiveShift(deltax, deltay)
    canvas.repaint()
  }

  def getObserverPerspectiveFocusPoint: Array[Double] =
    Array(observer.rotx, observer.roty)

  def zoomToDistance(dist: Double): Unit = {
    observer.zoomToDistance(dist)
    canvas.repaint()
  }

  def getObserverDistance: Double =
    observer.dist

  protected var canvas: GLJPanel = null

  protected var oldx = 0
  protected var oldy = 0

  var dragging = false

  def setCanvas(glCanvas: GLJPanel): Unit = {
    canvas = glCanvas
  }

  def repaintCanvas(): Unit = {
    canvas.repaint()
  }

  protected def setupCompiledDisplayLists(gl: GL2): Unit

  protected def setupLightingAndViewPort(gl: GL2, glu: GLU): Unit = {
    gl.glShadeModel(GLLightingFunc.GL_SMOOTH) // Enable Smooth Shading
    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)   // Black Background
    gl.glClearDepth(1.0f)                     // Depth Buffer Setup
    gl.glEnable(GL.GL_DEPTH_TEST)            // Enables Depth Testing
    gl.glDepthFunc(GL.GL_LEQUAL)             // The Type Of Depth Testing To Do

    gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_FASTEST)

    // Lighting
    gl.glEnable(GLLightingFunc.GL_LIGHTING)

    // First two lights are non-specular, ambient & diffuse lights.
    // this is the standard lighting from NetLogo's 3d view.
    // But it's too dim and lacks necessary direction for real color 3d shapes.
    gl.glLightfv(1, GLLightingFunc.GL_POSITION, FloatBuffer.wrap(Array(-1.0f, -0.3f, 0.4f, 0.0f)))
    gl.glLightfv(1, GLLightingFunc.GL_AMBIENT, FloatBuffer.wrap(Array(0.25f, 0.25f, 0.25f, 1.0f)))
    gl.glLightfv(1, GLLightingFunc.GL_DIFFUSE, FloatBuffer.wrap(Array(0.35f, 0.35f, 0.35f, 1.0f)))
    gl.glLightfv(1, GLLightingFunc.GL_SPECULAR, FloatBuffer.wrap(Array(0.0f, 0.0f, 0.0f, 0.0f)))
    gl.glEnable(1)

    gl.glLightfv(2, GLLightingFunc.GL_POSITION, FloatBuffer.wrap(Array(1.0f, 0.6f, -0.5f, 0.0f)))
    gl.glLightfv(2, GLLightingFunc.GL_AMBIENT, FloatBuffer.wrap(Array(0.25f, 0.25f, 0.25f, 1.0f)))
    gl.glLightfv(2, GLLightingFunc.GL_DIFFUSE, FloatBuffer.wrap(Array(0.35f, 0.35f, 0.35f, 1.0f)))
    gl.glLightfv(2, GLLightingFunc.GL_SPECULAR, FloatBuffer.wrap(Array(0.0f, 0.0f, 0.0f, 0.0f)))
    gl.glEnable(2)

    // add an additional light to give shadows, needed for 3d shapes to show better.
    // Set light parameters.
    gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_POSITION,
                 Array(1.2f * viewer.maxPxcor, 1.2f * viewer.maxPycor, 70.0f, 1), 0)
    gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_AMBIENT, Array(0.4f, 0.4f, 0.4f, 1f), 0) // .2fs
    gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_SPECULAR, Array(0.7f, 0.7f, 0.7f, 1f), 0)
    gl.glEnable(GLLightingFunc.GL_LIGHT1)

    // This is necessary for properly rendering scaled objects. Without this, small objects
    // may look too bright, and large objects will look flat.
    gl.glEnable(GLLightingFunc.GL_NORMALIZE)

    // Coloring
    gl.glColorMaterial(GL.GL_FRONT, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE)
    gl.glEnable(GLLightingFunc.GL_COLOR_MATERIAL)

    // Remove back-face rendering -- temporarily kept in.
    // gl.glCullFace(GL2.GL_BACK)
    // gl.glEnable(GL2.GL_CULL_FACE)
    gl.glGetIntegerv(GL.GL_STENCIL_BITS, IntBuffer.wrap(Array(1)))

    mainViewport(gl, glu)

    observer.goHome(viewer)
  }

  private def mainViewport(gl: GL2, glu: GLU): Unit = {
    gl.glViewport(0, 0, viewer.viewWidth, viewer.viewHeight)

    gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
    gl.glLoadIdentity()

    // It's nice to be able to zoom way, so large zClip
    val zClip = viewer.worldWidth.max(viewer.worldHeight) * 40

    // rectangular viewport with a ratio of width / height prevents the view from stretching while still allowing
    // the graphics inside the view to extend into the rectangular margins (Isaac B 6/26/25)
    glu.gluPerspective(45.0f, viewer.viewWidth.toFloat / viewer.viewHeight, 0.1, zClip)
    gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
    gl.glLoadIdentity()
  }

  protected def setColorAndStandardMaterial(gl: GL2, red: Float, green: Float, blue: Float): Unit = {
    val rgb = Array(red, green, blue)

    gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_AMBIENT, rgb, 0)
    gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_SPECULAR, rgb, 0)
    gl.glMaterialf(GL.GL_FRONT, GLLightingFunc.GL_SHININESS, 0.5f)
    gl.glColor3f(red, green, blue)
  }

  protected def drawAxesIfDragging(gl: GL2, axisHeadHandle: Int): Unit = {
    if (dragging) {
      val zmax = 10.0 * viewer.zScale + 1

      setColorAndStandardMaterial(gl, 0.9f, 0, 0)

      gl.glLineWidth(2.4f)
      gl.glBegin(GL.GL_LINES)
      gl.glVertex3i(0, 0, 0)
      gl.glVertex3i(viewer.maxPxcor + 1, 0, 0)
      gl.glVertex3i(0, 0, 0)
      gl.glVertex3d(0, viewer.maxPycor + 1, 0)
      gl.glVertex3i(0, 0, 0)
      gl.glVertex3d(0, 0, zmax)
      gl.glVertex3i(0, 0, 0)
      gl.glEnd()

      gl.glPushMatrix()
      gl.glTranslated(viewer.maxPxcor + 1, 0, 0)
      gl.glRotated(90.0, 0, 1, 0)
      gl.glCallList(axisHeadHandle)
      gl.glPopMatrix()

      gl.glPushMatrix()
      gl.glTranslated(0, viewer.maxPycor + 1, 0)
      gl.glRotated(-90.0, 1, 0, 0)
      gl.glCallList(axisHeadHandle)
      gl.glPopMatrix()

      gl.glPushMatrix()
      gl.glTranslated(0, 0, zmax)
      gl.glCallList(axisHeadHandle)
      gl.glPopMatrix()
    }
  }

  override def mouseDragged(e: MouseEvent): Unit = {
    val nx = e.getX
    val ny = e.getY

    viewer.getMode match {
      case VarviewWindow.ORBIT =>
        observer.updatePerspectiveAngles((nx - oldx) / 2.0, (oldy - ny) / 2.0)
      case VarviewWindow.MOVE =>
        observer.shift((nx - oldx) / 2.0, (oldy - ny) / 2.0)
      case VarviewWindow.ZOOM =>
        observer.zoomby(oldy - ny)
      case VarviewWindow.SCALEZ =>
        viewer.zoomZby((oldy - ny) / 30.0)
      case _ =>
    }

    oldx = nx
    oldy = ny

    canvas.repaint()
  }

  override def mouseWheelMoved(e: MouseWheelEvent): Unit = {
    observer.zoomby(-e.getPreciseWheelRotation)

    canvas.repaint()
  }

  override def mousePressed(e: MouseEvent): Unit = {
    oldx = e.getX
    oldy = e.getY

    dragging = true

    canvas.repaint()
  }

  override def mouseReleased(e: MouseEvent): Unit = {
    dragging = false

    canvas.repaint()
  }

  /// These are variables and methods shared by PatchGL and TurtleGL

  // for turtle shapes
  protected val compiledShapes = Map[String, Int]()
  protected val nlGLU = new NetLogoGLU
  protected var glu: GLU = null

  var areShapesStale = false

  def compileShape(nlGLU: NetLogoGLU, gl: GL2, glu: GLU, shape: VectorShape, index: Int, rotatable: Boolean): Unit = {
    val tessellator = new Tessellator
    val tess = GLU.gluNewTess()

    GLU.gluTessCallback(tess, GLU.GLU_TESS_BEGIN_DATA, tessellator)
    GLU.gluTessCallback(tess, GLU.GLU_TESS_EDGE_FLAG_DATA, tessellator)
    GLU.gluTessCallback(tess, GLU.GLU_TESS_VERTEX_DATA, tessellator)
    GLU.gluTessCallback(tess, GLU.GLU_TESS_END_DATA, tessellator)
    GLU.gluTessCallback(tess, GLU.GLU_TESS_COMBINE_DATA, tessellator)
    GLU.gluTessCallback(tess, GLU.GLU_TESS_ERROR_DATA, tessellator)
    GLU.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD)

    gl.glNewList(index, GL2.GL_COMPILE)

    if (!rotatable)
      gl.glDisable(GLLightingFunc.GL_LIGHTING)

    // render each element in this shape
    val elements = shape.getElementsJ

    for (i <- 0 until elements.size) {
      elements.get(i) match {
        case rect: Rectangle =>
          nlGLU.renderRectangle(gl, i, rect, rotatable)
        case polygon: Polygon =>
          Polygons.renderPolygon(gl, glu, tessellator, tess, i, polygon, rotatable, false) // is3D = false
        case circle: Circle =>
          nlGLU.renderCircle(gl, glu, i, circle, rotatable)
        case line: Line =>
          nlGLU.renderLine(gl, i, line)
        case curve: Curve =>
          throw new IllegalStateException
        case _ =>
      }
    }

    if (!rotatable)
      gl.glEnable(GLLightingFunc.GL_LIGHTING)

    gl.glDisable(GL.GL_CULL_FACE)
    gl.glEndList()
  }

  // Compile all the turtle shapes
  def compileShapes(gl: GL2, compiledShapes: Map[String, Int], rotatable: Boolean): Unit = {
    App.app.workspace.world.turtleShapeList.names.foreach(name => {
      val handle = gl.glGenLists(1)
      val shape = App.app.workspace.world.turtleShapeList.shape(name).asInstanceOf[VectorShape]

      compileShape(nlGLU, gl, glu, shape, handle, false)
      compiledShapes += ((name, handle))
    })
  }

  // Replace deleted shapes in compiled shape map with the compiled default shape
  def setDeletedShapesToDefaultShape(compiledShapes: Map[String, Int]): Unit = {
    // If a shape was deleted its name will still be a key for the compiled shapes,
    // but it will not have a name among the turtle shapes.
    // Hence it gets the default shape. Fortunately the default shape cannot be deleted or edited.
    // aab 05/06/2021
    (compiledShapes.keysIterator.toSet -- App.app.workspace.world.turtleShapeList.names).foreach(name => {
      compiledShapes += ((name, compiledShapes(ShapeList.DefaultShapeName)))
    })
  }

  def updateTurtleDisplayList(): Unit = {
    areShapesStale = true
  }

  // required by Interface GLEventListener
  override def init(drawable: GLAutoDrawable): Unit = {
    compiledShapes.clear()

    val gl = drawable.getGL.asInstanceOf[GL2]

    glu = new GLU()

    setupCompiledDisplayLists(gl)
    setupLightingAndViewPort(gl, glu)
  }

  // required by Interface GLEventListener
  override def dispose(drawable: GLAutoDrawable): Unit = {}

  // required by Interface GLEventListener
  override def reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int): Unit = {
    viewer.viewWidth = width.toInt
    viewer.viewHeight = height.toInt

    mainViewport(drawable.getGL.asInstanceOf[GL2], glu)
  }
}
