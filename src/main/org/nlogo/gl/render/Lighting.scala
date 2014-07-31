// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import javax.media.opengl.{ GL, GL2 }
import javax.media.opengl.fixedfunc.GLLightingFunc
import javax.media.opengl.glu.GLU
import org.nlogo.api.World3D
import java.nio.FloatBuffer
import collection.mutable.ArrayBuffer
import java.lang.{Float => JFloat, Double => JDouble}

/**
 * This is a small library for managing and debugging lighting.  It includes
 * methods for showing the positions and directions of the lights in 3D space.
 *
 * It isn't normally enabled; to enable it, call the showLights() method.  If you ever wondered what
 * the default lighting scheme in NetLogo 3D looks like, here it is, there are two directional
 * lights pointing roughly at opposite corners.
 */

object LightManager {
  val lightNumbers = List(
    GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_LIGHT2, GLLightingFunc.GL_LIGHT3,
    GLLightingFunc.GL_LIGHT4, GLLightingFunc.GL_LIGHT5, GLLightingFunc.GL_LIGHT6, GLLightingFunc.GL_LIGHT7)
}

class LightManager {

  val lights = ArrayBuffer[Light]()

  private var glInstance: Option[GL2] = None
  implicit def getGL: GL2 = glInstance.getOrElse(
    sys.error("Handle to OpenGL interface is not set. Make sure init() got called."))

  def init(gl: GL2) {
    glInstance = Some(gl)
  }

  def addLight(light: Light) {
    require(lights.size < 8,
            "Failed to add light: Only a maximum of 8 lights is supported in OpenGL.")

    light.glLightNumber = getOpenGLLightNumber(lights.size)
    light.glInstance = glInstance

    light.turnOn()
    lights += light
  }

  /**
   * This method actually makes the lights effective by setting all of the appropriate OpenGL state variables.
   * This should be called on every frame (or just once, but be sure that no other parts of the application
   * change the lighting in that case).
   */
  def applyLighting() {
    lights.foreach(_.applyLight())
  }

  /**
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

  def getOpenGLLightNumber(lightIndex: Int) = {
    require(LightManager.lightNumbers.isDefinedAt(lightIndex),
            "Light index needs to be between 0 and 7.")
    LightManager.lightNumbers(lightIndex)
  }
}

abstract class Light {

  var ambient = RGBA(1, 1, 1, 1)
  var diffuse = RGBA(1, 1, 1, 1)
  var specular = RGBA(1, 1, 1, 1)

  var glLightNumber = -1

  var glInstance: Option[GL2] = None
  implicit def getGL: GL2 = glInstance.getOrElse(sys.error(
    "Handle to OpenGL interface must be set before attempting to use the light. "
    + "Use LightManager.addLight() and LightManager.removeLight() to add or remove lights, "
    + "and be sure LightManager.init() was called."))

  private var _isOn = true
  def isOn: Boolean = _isOn

  def turnOn() {
    assertValidLightNumber()
    _isOn = true
    val gl = getGL
    gl.glLightfv(glLightNumber, GLLightingFunc.GL_AMBIENT, FloatBuffer.wrap(ambient.toFloat4Array))
    gl.glLightfv(glLightNumber, GLLightingFunc.GL_DIFFUSE, FloatBuffer.wrap(diffuse.toFloat4Array))
    gl.glLightfv(glLightNumber, GLLightingFunc.GL_SPECULAR, FloatBuffer.wrap(specular.toFloat4Array))
    // Position/Direction gets set in the applyLight() method.
    gl.glEnable(glLightNumber)
  }

  def turnOff() {
    assertValidLightNumber()
    _isOn = false
    getGL.glDisable(glLightNumber)
  }

  def toggle() {
    if (isOn) turnOff() else turnOn()
  }

  /**
   * Makes the light effective by setting the appropriate OpenGL state variables. Be sure
   * that the light is on by calling turnOn(), but note that it is on by default.
   */
  def applyLight();

  /**
   * Shows the light's position in 3D space. This is intended as a debugging aid.
   */
  def showLight(glu: GLU, world: World3D, worldScale: Float, observerDistance: Double,
                  shapeRenderer: ShapeRenderer);

  /**
   * Helps visualize the light's position in 3D space by drawing some lines. This is intended as a debugging aid.
   */
  def renderPositionHintLines(x: JFloat, y: JFloat, z: JFloat,
                              minX: JFloat, minY: JFloat, minZ: JFloat,
                              maxX: JFloat, maxY: JFloat, maxZ: JFloat) {

    val gl = getGL

    // Render a line extending from the light position to the world bottom (helps visualize light's Z position)
    gl.glBegin(GL.GL_LINES)
    gl.glVertex3f(x, y, z)
    gl.glVertex3f(x, y, minZ)
    gl.glEnd()

    // Render a pair of crossing lines at the world bottom (helps visualize light's (X,Y) position)

    gl.glColor4fv(java.nio.FloatBuffer.wrap(Array(0.3f, 0.3f, 0.3f, 1.0f)))
    gl.glBegin(GL.GL_LINES)

    if (x <= maxX) {
      gl.glVertex3f(minX, y, minZ)
      gl.glVertex3f(x, y, minZ)
    }

    if (x >= minX) {
      gl.glVertex3f(x, y, minZ)
      gl.glVertex3f(maxX, y, minZ)
    }

    if (y <= maxY) {
      gl.glVertex3f(x, minY, minZ)
      gl.glVertex3f(x, y, minZ)
    }

    if (y >= minY) {
      gl.glVertex3f(x, y, minZ)
      gl.glVertex3f(x, maxY, minZ)
    }

    // If the light's (X,Y) position is outside of the world's boundaries, it may also be helpful to draw
    // a few lines extending from the world boundaries.

    if (x < minX || x > maxX) {
      if (y <= maxY) {
        gl.glVertex3f(x, minY, minZ)
        gl.glVertex3f(if (x < minX) minX else maxX, minY, minZ)
      }

      if (y >= minY) {
        gl.glVertex3f(x, maxY, minZ)
        gl.glVertex3f(if (x < minX) minX else maxX, maxY, minZ)
      }
    }

    if (y < minY || y > maxY) {
      if (x <= maxX) {
        gl.glVertex3f(minX, y, minZ)
        gl.glVertex3f(minX, if (y < minY) minY else maxY, minZ)
      }

      if (x >= minX) {
        gl.glVertex3f(maxX, y, minZ)
        gl.glVertex3f(maxX, if (y < minY) minY else maxY, minZ)
      }
    }

    gl.glEnd()
  }

  /**
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
    assert (glLightNumber >= GLLightingFunc.GL_LIGHT0 || glLightNumber <= GLLightingFunc.GL_LIGHT7,
      { sys.error("Invalid OpenGL light number: " + glLightNumber
          + ". Light number needs to be between " + GLLightingFunc.GL_LIGHT0 + " and " + GLLightingFunc.GL_LIGHT7
          + ". Use LightManager.addLight() and LightManager.removeLight() to add or remove lights.")
      })
  }

  def getLabel: String =
    "GL_LIGHT" + LightManager.lightNumbers.indexOf(glLightNumber) +
      " (" + typeLabel + ")" + (if (isOn) "" else " [off]")

  // Specifies whether this is a position or a directional light.
  def typeLabel: String
}


class DirectionalLight(val direction: Direction) extends Light {

  def typeLabel = "Directional"

  def applyLight() {
    val gl = getGL
    gl.glEnable(GLLightingFunc.GL_LIGHTING)
    if (isOn) {
      gl.glEnable(glLightNumber)
      gl.glLightfv(glLightNumber, GLLightingFunc.GL_POSITION, FloatBuffer.wrap(direction.toFloat4Array))
    } else {
      gl.glDisable(glLightNumber)
    }
  }

  def showLight(glu: GLU, world: World3D, worldScale: Float, observerDistance: Double,
                  shapeRenderer: ShapeRenderer) {
    val gl = getGL

    gl.glDisable(GLLightingFunc.GL_LIGHTING)

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
    shapeRenderer.renderLabel(getGL, getLabel, 47: JDouble, lightSourceX, lightSourceY, lightSourceZ,
        1.0f, 12, world.patchSize)

    gl.glEnable(GLLightingFunc.GL_LIGHTING)
  }
}


class PositionalLight(val position: Position) extends Light {

  def typeLabel = "Positional"

  def applyLight() {
    val gl = getGL
    gl.glEnable(GLLightingFunc.GL_LIGHTING)
    if (isOn) {
      gl.glEnable(glLightNumber)
      gl.glLightfv(glLightNumber, GLLightingFunc.GL_POSITION, FloatBuffer.wrap(position.toFloat4Array))
    } else {
      gl.glDisable(glLightNumber)
    }
  }

  def showLight(glu: GLU, world: World3D, worldScale: Float, observerDistance: Double,
                  shapeRenderer: ShapeRenderer) {
    val gl = getGL

    gl.glDisable(GLLightingFunc.GL_LIGHTING)

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
    shapeRenderer.renderLabel(getGL, getLabel, 47: JDouble, position.x, position.y, position.z,
        1.0f, 12, world.patchSize)

    gl.glEnable(GLLightingFunc.GL_LIGHTING)
  }
}

abstract class Vector3 {
  val x: JFloat; val y: JFloat; val z: JFloat
  def toFloat4Array: Array[Float];
}

case class Position(x: JFloat, y: JFloat, z: JFloat) extends Vector3 {
  override def toFloat4Array = Array(x, y, z, 1.0f)
}

case class Direction(x: JFloat, y: JFloat, z: JFloat) extends Vector3 {
  override def toFloat4Array = Array(x, y, z, 0.0f)
  def normalized: Direction = {
    val length = math.sqrt(x*x + y*y + z*z).toFloat
    Direction(x/length, y/length, z/length)
  }
}

case class RGBA(r: JFloat, g: JFloat, b: JFloat, a: JFloat) {
  def toFloat4Array = Array[Float](r, g, b, a)
}
