package org.nlogo.gl.render

import javax.media.opengl.GL
import javax.media.opengl.glu.GLU
import org.nlogo.api.World3D
import java.nio.FloatBuffer
import collection.mutable.ListBuffer
import java.lang.{IndexOutOfBoundsException, IllegalStateException, Float => JavaFloat, Double => JavaDouble}

class LightManager {

  val lights = new ListBuffer[Light]()

  // Keep track of how many lights we're using (OpenGL supports up to 8)
  var LightCounter = 0

  private var glInstance: Option[GL] = None
  implicit def getGL: GL = glInstance.getOrElse(throw new IllegalStateException("Handle to OpenGL interface is not set. "
    + "Make sure init() got called."))

  def init(gl: GL) {
    this.glInstance = Some(gl)
  }

  def AddLight(light: Light) {
    if (LightCounter >= 8) {
      throw new IllegalStateException("Failed to add light: Only a maximum of 8 lights is supported in OpenGL.")
    }
    light.glLightNumber = GetOpenGLLightNumber(LightCounter)
    light.glInstance = glInstance

    light.turnOn()
    lights ++= List(light)
    LightCounter += 1
  }

  /*
   * This method actually makes the lights effective by setting all of the appropriate OpenGL state variables.
   * This should be called on every frame (or just once, but be sure that no other parts of the application
   * change the lighting in that case).
   */
  def applyLighting() {
    lights.foreach(_.applyLight())
  }

  /*
   * Shows the positions of all the lights present in the world.  Positional lights are rendered as spheres,
   * and directional lights are rendered as arrows. This is intended as a debugging aid.
   *
   * Note that directional lights don't actually have a position (they are infinitely far away). However,
   * this method will approximate the source of the light by rendering it outside of the world.
   */
  def showLights(glu: GLU, world: World3D, worldScale: Float, observerDistance: Double,
                   shapeRenderer: ShapeRenderer) {
    lights.foreach(_.showLight(glu, world, worldScale, observerDistance, shapeRenderer))
  }

  def GetOpenGLLightNumber(lightIndex: Int) = lightIndex match {
    case 0 => GL.GL_LIGHT0
    case 1 => GL.GL_LIGHT1
    case 2 => GL.GL_LIGHT2
    case 3 => GL.GL_LIGHT3
    case 4 => GL.GL_LIGHT4
    case 5 => GL.GL_LIGHT5
    case 6 => GL.GL_LIGHT6
    case 7 => GL.GL_LIGHT7
    case _ => throw new IndexOutOfBoundsException("Light index needs to be between 0 and 7.")
  }
}


abstract class Light {
  
  var ambient = new RGBA(1, 1, 1, 1)
  var diffuse = new RGBA(1, 1, 1, 1)
  var specular = new RGBA(1, 1, 1, 1)

  var glLightNumber = -1
  
  var glInstance: Option[GL] = None
  implicit def getGL: GL = glInstance.getOrElse(throw new IllegalStateException(
    "Handle to OpenGL interface must be set before attempting to use the light. "
    + "Use LightManager.AddLight() and LightManager.RemoveLight() to add or remove lights, "
    + "and be sure LightManager.init() was called."))

  protected var isOn = true
  def IsOn: Boolean = isOn

  def turnOn() {
    assertValidLightNumber()
    isOn = true
    val gl = getGL
    gl.glLightfv(glLightNumber, GL.GL_AMBIENT, FloatBuffer.wrap(ambient.toFloat4Array))
    gl.glLightfv(glLightNumber, GL.GL_DIFFUSE, FloatBuffer.wrap(diffuse.toFloat4Array))
    gl.glLightfv(glLightNumber, GL.GL_SPECULAR, FloatBuffer.wrap(specular.toFloat4Array))
    // Position/Direction gets set in the applyLight() method.
    gl.glEnable(glLightNumber)
  }

  def turnOff() {
    assertValidLightNumber()
    isOn = false
    getGL.glDisable(glLightNumber)
  }

  def toggle() {
    if (isOn) turnOff() else turnOn()
  }

  /*
   * Makes the light effective by setting the appropriate OpenGL state variables. Be sure
   * that the light is on by calling turnOn(), but note that it is on by default.
   */
  def applyLight();

  /*
   * Shows the light's position in 3D space. This is intended as a debugging aid.
   */
  def showLight(glu: GLU, world: World3D, worldScale: Float, observerDistance: Double,
                  shapeRenderer: ShapeRenderer);

  /*
   * Helps visualize the light's position in 3D space by drawing some lines. This is intended as a debugging aid.
   */
  def renderPositionHintLines(x: JavaFloat, y: JavaFloat, z: JavaFloat,
                              MinX: JavaFloat, MinY: JavaFloat, MinZ: JavaFloat,
                              MaxX: JavaFloat, MaxY: JavaFloat, MaxZ: JavaFloat) {
    
    val gl = getGL

    // Render a line extending from the light position to the world bottom (helps visualize light's Z position)
    gl.glBegin(GL.GL_LINES)
    gl.glVertex3f(x, y, z)
    gl.glVertex3f(x, y, MinZ)
    gl.glEnd()

    // Render a pair of crossing lines at the world bottom (helps visualize light's (X,Y) position)

    gl.glColor4fv(java.nio.FloatBuffer.wrap(Array(0.3f, 0.3f, 0.3f, 1.0f)))
    gl.glBegin(GL.GL_LINES)

    if (x <= MaxX) {
      gl.glVertex3f(MinX, y, MinZ)
      gl.glVertex3f(x, y, MinZ)
    }

    if (x >= MinX) {
      gl.glVertex3f(x, y, MinZ)
      gl.glVertex3f(MaxX, y, MinZ)
    }

    if (y <= MaxY) {
      gl.glVertex3f(x, MinY, MinZ)
      gl.glVertex3f(x, y, MinZ)
    }

    if (y >= MinY) {
      gl.glVertex3f(x, y, MinZ)
      gl.glVertex3f(x, MaxY, MinZ)
    }
    
    // If the light's (X,Y) position is outside of the world's boundaries, it may also be helpful to draw
    // a few lines extending from the world boundaries.

    if (x < MinX || x > MaxX) {
      if (y <= MaxY) {
        gl.glVertex3f(x, MinY, MinZ)
        gl.glVertex3f(if (x < MinX) MinX else MaxX, MinY, MinZ)
      }

      if (y >= MinY) {
        gl.glVertex3f(x, MaxY, MinZ)
        gl.glVertex3f(if (x < MinX) MinX else MaxX, MaxY, MinZ)
      }
    }

    if (y < MinY || y > MaxY) {
      if (x <= MaxX) {
        gl.glVertex3f(MinX, y, MinZ)
        gl.glVertex3f(MinX, if (y < MinY) MinY else MaxY, MinZ)
      }

      if (x >= MinX) {
        gl.glVertex3f(MaxX, y, MinZ)
        gl.glVertex3f(MaxX, if (y < MinY) MinY else MaxY, MinZ)
      }
    }

    gl.glEnd()
  }

  /*
   * Draws a 3D arrow (comprised of a cylinder and a cone) at the current position specified by the OpenGL
   * modelview matrix, pointing in the direction specified by the vector (xdir, ydir, zdir). This is used
   * for rendering directional lights.
   */
  def render3DArrow(glu: GLU, xdir: Double, ydir: Double, zdir: Double) {
    val gl = getGL
    
    val RADDEG = 57.29578

    val azimuth = math.atan2(ydir, xdir) * RADDEG
    val elevation = math.acos(zdir) * RADDEG

    gl.glDisable(GL.GL_CULL_FACE)
    gl.glPushMatrix()

    // Rotate in the specified direction
    gl.glRotated(azimuth, 0, 0, 1)
    gl.glRotated(elevation, 0, 1, 0)

    // Draw arrow tail
    glu.gluCylinder(glu.gluNewQuadric(), 0.1, 0.1, 1.0, 12, 12)

    // Draw arrow head
    gl.glTranslated(0.0, 0.0, 1.0)
    glu.gluCylinder(glu.gluNewQuadric(), 0.3, 0.0, 0.5, 12, 12)

    gl.glPopMatrix()
    gl.glEnable(GL.GL_CULL_FACE)
  }

  def assertValidLightNumber() {
    assert (glLightNumber >= GL.GL_LIGHT0 || glLightNumber <= GL.GL_LIGHT7,
      { throw new IllegalStateException("Invalid OpenGL light number: " + glLightNumber
          + ". Light number needs to be between " + GL.GL_LIGHT0 + " and " + GL.GL_LIGHT7
          + ". Use LightManager.AddLight() and LightManager.RemoveLight() to add or remove lights.")
      })
  }

  def getLabel: String = {
    val numberLabel = glLightNumber match {
      case GL.GL_LIGHT0 => "GL_LIGHT0"
      case GL.GL_LIGHT1 => "GL_LIGHT1"
      case GL.GL_LIGHT2 => "GL_LIGHT2"
      case GL.GL_LIGHT3 => "GL_LIGHT3"
      case GL.GL_LIGHT4 => "GL_LIGHT4"
      case GL.GL_LIGHT5 => "GL_LIGHT5"
      case GL.GL_LIGHT6 => "GL_LIGHT6"
      case GL.GL_LIGHT7 => "GL_LIGHT7"
    }
    numberLabel + " (" + typeLabel + ")" + (if (isOn) "" else " [off]")
  }

  // Specifies whether this is a position or a directional light.
  def typeLabel: String
}


class DirectionalLight(val direction: Direction) extends Light {

  def typeLabel = "Directional"

  def applyLight() {
    val gl = getGL
    gl.glEnable(GL.GL_LIGHTING)
    if (isOn) {
      gl.glEnable(glLightNumber)
      gl.glLightfv(glLightNumber, GL.GL_POSITION, FloatBuffer.wrap(direction.toFloat4Array))
    } else {
      gl.glDisable(glLightNumber)
    }
  }

  def showLight(glu: GLU, world: World3D, worldScale: Float, observerDistance: Double,
                  shapeRenderer: ShapeRenderer) {
    val gl = getGL
    
    gl.glDisable(GL.GL_LIGHTING)

    gl.glPushMatrix()

    // Normalize the direction
    val normalizedDirection = direction.normalized

    // Translate in the opposite direction that the light is pointing at, going a bit outside the world's
    // boundaries. This position approximates the source location of the directional light.
    val worldMaxExtent = math.max(world.maxPxcor, math.max(world.maxPycor, world.maxPzcor))
    val offset = (-2.0 * worldMaxExtent).toFloat
    val lightSourceX = offset * normalizedDirection.x * worldScale
    val lightSourceY = offset * normalizedDirection.y * worldScale
    val lightSourceZ = offset * normalizedDirection.z * worldScale
    gl.glTranslatef(lightSourceX, lightSourceY, lightSourceZ)

    // Draw an arrow that points in the direction of the light.
    gl.glColor4fv(java.nio.FloatBuffer.wrap(Array(0.98f, 0.98f, 0.82f, 1.0f)))
    render3DArrow(glu, normalizedDirection.x.toDouble, normalizedDirection.y.toDouble, normalizedDirection.z.toDouble)

    gl.glPopMatrix()

    renderPositionHintLines(lightSourceX, lightSourceY, lightSourceZ,
                            worldScale * world.minPxcor, worldScale * world.minPycor, worldScale * world.minPzcor,
                            worldScale * world.maxPxcor, worldScale * world.maxPycor, worldScale * world.maxPzcor)

    // Render a label for the light
    shapeRenderer.renderLabel(getGL, getLabel, new JavaDouble(47.0), lightSourceX, lightSourceY, lightSourceZ,
        1.0f, 12, world.patchSize)

    gl.glEnable(GL.GL_LIGHTING)
  }
}


class PositionalLight(val position: Position) extends Light {

  def typeLabel = "Positional"

  def applyLight() {
    val gl = getGL
    gl.glEnable(GL.GL_LIGHTING)
    if (isOn) {
      gl.glEnable(glLightNumber)
      gl.glLightfv(glLightNumber, GL.GL_POSITION, FloatBuffer.wrap(position.toFloat4Array))
    } else {
      gl.glDisable(glLightNumber)
    }
  }

  def showLight(glu: GLU, world: World3D, worldScale: Float, observerDistance: Double,
                  shapeRenderer: ShapeRenderer) {
    val gl = getGL
    
    gl.glDisable(GL.GL_LIGHTING)

    // Render a sphere at the light's position
    gl.glPushMatrix()
    gl.glTranslatef(position.x, position.y, position.z)
    gl.glColor4fv(java.nio.FloatBuffer.wrap(Array(0.98f, 0.98f, 0.82f, 1.0f)))
    glu.gluSphere(glu.gluNewQuadric(), 0.002 * observerDistance, 16, 16)
    gl.glPopMatrix()

    renderPositionHintLines(position.x, position.y, position.z,
                            worldScale * world.minPxcor, worldScale * world.minPycor, worldScale * world.minPzcor,
                            worldScale * world.maxPxcor, worldScale * world.maxPycor, worldScale * world.maxPzcor)

    // Render a label for the light
    shapeRenderer.renderLabel(getGL, getLabel, new JavaDouble(47.0), position.x, position.y, position.z,
        1.0f, 12, world.patchSize)

    gl.glEnable(GL.GL_LIGHTING)
  }
}

abstract class Vector3(val x: JavaFloat, val y: JavaFloat, val z: JavaFloat) {
  def toFloat4Array: Array[Float];
}

class Position(x: JavaFloat, y: JavaFloat, z: JavaFloat) extends Vector3(x, y, z) {
  def toFloat4Array: Array[Float] = Array(x, y, z, 1.0f)
}

class Direction(x: JavaFloat, y: JavaFloat, z: JavaFloat) extends Vector3(x, y, z) {
  def toFloat4Array: Array[Float] = Array(x, y, z, 0.0f)
  def normalized: Direction = {
    val length = math.sqrt(x*x + y*y + z*z).toFloat
    new Direction(x/length, y/length, z/length)
  }
}

class RGBA(val r: JavaFloat, val g: JavaFloat, val b: JavaFloat, val a: JavaFloat) {
  def toFloat4Array: Array[Float] = Array(r, g, b, a)
}