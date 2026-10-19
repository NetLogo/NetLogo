package view25d.view

import com.jogamp.opengl.GL2

import org.nlogo.api.Constants

class Observer(xs: Double, ys: Double, zs: Double) {
  private var x = xs
  private var y = ys
  private var z = zs

  var heading = 0.0
  var pitch = 90.0

  var rotx = 0.0
  var roty = 0.0
  var rotz = 0.0

  def goHome(world: VarviewWindow): Unit = {
    x = world.minPxcor + (world.maxPxcor - world.minPxcor) / 2.0
    y = world.minPycor + (world.maxPycor - world.minPycor) / 2.0
    z = world.worldWidth.max(world.worldHeight) * 1.5

    heading = 0
    pitch = 90

    rotx = x
    roty = y
    rotz = 0
  }

  def applyPerspective(gl: GL2): Unit = {
    gl.glRotated(90, -1.0, 0.0, 0.0)
    gl.glRotated(heading, 0.0, 0.0, 1.0)
    gl.glRotated(pitch, Math.cos(Math.toRadians(heading)), -Math.sin(Math.toRadians(heading)), 0.0)
    gl.glTranslated(-x, -y, -z)
  }

  def applyNormal(gl: GL2): Unit = {
    // cross product of x, y, z and 0, 0, 1
    gl.glRotated(-heading, x, y, z)
    gl.glRotated(pitch - 90, y, -x, 0)
  }

  def updatePerspectiveAngles(delthetax: Double, delthetay: Double): Unit = {
    orbitRight(delthetax)
    orbitUp(delthetay)
  }

  private def orbitRight(delta: Double): Unit = {
    heading = heading + delta

    if (heading > 360)
      heading = heading - 360
    else if (heading < 0)
      heading = heading + 360

    val dxy = dist * Math.cos(Math.toRadians(pitch))

    x = rotx - dxy * Math.sin(Math.toRadians(heading))
    y = roty - dxy * Math.cos(Math.toRadians(heading))
  }

  private def orbitUp(delta: Double): Unit = {
    val newPitch = pitch - delta

    val dxy = dist * Math.cos(Math.toRadians(newPitch))
    val zn = dist * Math.sin(Math.toRadians(newPitch))

    // Don't let observer go (too far) under patch-plane or be upside-down
    // for stopping at the xy axis, replace "dist / 4.0" with 0
    if (zn + rotz > -dist / 4.0 && newPitch < 90) {
      x = rotx - dxy * Math.sin(Math.toRadians(heading))
      y = roty - dxy * Math.cos(Math.toRadians(heading))
      z = rotz + zn
      pitch = newPitch
    }
  }

  def shift(deltax: Double, deltay: Double): Unit = {
    val sinH = Math.sin(Math.toRadians(heading))
    val cosH = Math.cos(Math.toRadians(heading))

    objectiveShift(-(cosH * deltax + sinH * deltay) * 0.1, (sinH * deltax - cosH * deltay) * 0.1)
  }

  def objectiveShift(delx: Double, dely: Double): Unit = {
    x += delx
    y += dely

    rotx += delx
    roty += dely
  }

  def zoomToDistance(distance: Double): Unit = {
    val ratio = distance / dist

    x = rotx + ratio * (x - rotx)
    y = roty + ratio * (y - roty)
    z = rotz + ratio * (z - rotz)
  }

  def zoomby(deltavert: Double): Unit = {
    if (deltavert < dist) {
      x += deltavert * dx
      y += deltavert * dy
      z -= deltavert * dz
    }
  }

  def dist: Double =
    Math.sqrt((rotx - x) * (rotx - x) + (roty - y) * (roty - y) + (rotz - z) * (rotz - z))

  private def dx: Double = {
    val value = Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(heading))

    if (Math.abs(value) < Constants.Infinitesimal)
      0
    else
      value
  }

  private def dy: Double = {
    val value = Math.cos(Math.toRadians(pitch)) * Math.cos(Math.toRadians(heading))

    if (Math.abs(value) < Constants.Infinitesimal)
      0
    else
      value
  }

  private def dz: Double = {
    val value = Math.sin(Math.toRadians(pitch))

    if (Math.abs(value) < Constants.Infinitesimal)
      0
    else
      value
  }
}
