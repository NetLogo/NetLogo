// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api

trait Camera extends api.Camera { self: Observer =>

  private var _heading = 0.0
  def heading = _heading
  def heading_=(heading: Double) {
    _heading = ((heading % 360) + 360) % 360
  }

  private var _pitch = 0.0
  def pitch = _pitch
  def pitch_=(pitch: Double) {
    _pitch = ((pitch % 360) + 360) % 360
  }

  private var _roll = 0.0
  def roll = _roll
  def roll_=(roll: Double) {
    _roll = ((roll % 360) + 360) % 360
  }

  private var _rotationPoint: api.Vect = null
  def rotationPoint = _rotationPoint

  def setRotationPoint(v: api.Vect) {
    _rotationPoint = v
  }

  def setRotationPoint(x: Double, y: Double, z: Double) {
    _rotationPoint = new api.Vect(x, y, z)
  }

  def setRotationPoint(agent: api.Agent) {
    agent match {
      case t: api.Turtle =>
        setRotationPoint(t.xcor, t.ycor, 0)
      case l: api.Link =>
        setRotationPoint(l.midpointX, l.midpointY, 0)
      case p: api.Patch =>
        setRotationPoint(p.pxcor, p.pycor, 0)
    }
  }

  def dist: Double =
    StrictMath.sqrt(
      (rotationPoint.x - oxcor) * (rotationPoint.x - oxcor) +
        (rotationPoint.y - oycor) * (rotationPoint.y - oycor) +
        ((rotationPoint.z - ozcor) * (rotationPoint.z - ozcor)))

  val headingSmoother = new HeadingSmoother

  var followDistance = 5

  def setOrientation(heading: Double, pitch: Double, roll: Double) {
    this.heading = heading
    this.pitch = pitch
    this.roll = roll
  }

  def dx: Double = {
    val value =
      StrictMath.cos(StrictMath.toRadians(pitch)) *
        StrictMath.sin(StrictMath.toRadians(heading))
    if (StrictMath.abs(value) < api.Constants.Infinitesimal)
      0
    else
      value
  }

  def dy: Double = {
    val value =
      StrictMath.cos(StrictMath.toRadians(pitch)) *
        StrictMath.cos(StrictMath.toRadians(heading))
    if (StrictMath.abs(value) < api.Constants.Infinitesimal)
      0
    else
      value
  }

  def dz: Double = {
    val value = StrictMath.sin(StrictMath.toRadians(pitch))
    if (StrictMath.abs(value) < api.Constants.Infinitesimal)
      0
    else
      value
  }

  def face(agent: api.Agent) {
    try heading = world.protractor.towards(this, agent, false)
    catch { case _: api.AgentException => heading = 0 }
    try pitch = -world.protractor.towardsPitch(this, agent, false)
    catch { case _: api.AgentException => pitch = 0 }
    setRotationPoint(agent)
  }

  def face(x: Double, y: Double) {
    try heading = world.protractor.towards(this, x, y, false)
    catch { case _: api.AgentException => heading = 0 }
    try pitch = -world.protractor.towardsPitch(this, x, y, 0, false)
    catch { case _: api.AgentException => pitch = 0 }
    setRotationPoint(x, y, 0)
  }

  @throws(classOf[api.AgentException])
  def moveTo(otherAgent: Agent) {
    otherAgent match {
      case t: api.Turtle =>
        oxyandzcor(t.xcor, t.ycor, 0)
      case p: api.Patch =>
        oxyandzcor(p.pxcor, p.pycor, 0)
      case _: api.Link =>
        throw new api.AgentException("you can't move-to a link")
    }
    face(rotationPoint.x, rotationPoint.y)
  }

  def updatePosition(): Boolean = {
    var changed = false
    perspective match {
      case api.Perspective.Observe =>
        return false
      case api.Perspective.Watch =>
        if (targetAgent.id == -1) {
          resetPerspective()
          return true
        }
        setRotationPoint(targetAgent)
        face(targetAgent)
      case _ => // follow and ride are the same save initial conditions.
        if (targetAgent.id == -1) { // it's dead!
          resetPerspective()
          return true
        }
        val turtle = targetAgent.asInstanceOf[Turtle]
        oxyandzcor(turtle.xcor, turtle.ycor, 0)
        val newHeading = headingSmoother.follow(targetAgent)
        if (perspective == api.Perspective.Follow) {
          changed = heading != newHeading
          heading = newHeading
        }
        else
          heading = turtle.heading
        pitch = 0
        roll = 0
    }
    changed
  }

  // This is a hack for now, there is prob. a better way of doing this - jrn 6/9/05
  def atHome3D: Boolean =
    (perspective == api.Perspective.Observe) && (oxcor == 0) && (oycor == 0) &&
        (ozcor == StrictMath.max(world.worldWidth, world.worldHeight) * 1.5) &&
        (heading == 0) && (pitch == 90) && (roll == 0) &&
        (rotationPoint.x == 0 && rotationPoint.y == 0 && rotationPoint.z == 0)

  def orbitRight(delta: Double) {
    val newHeading = heading - delta
    val dxy = dist * StrictMath.cos(StrictMath.toRadians(pitch))
    val x = -dxy * StrictMath.sin(StrictMath.toRadians(newHeading))
    val y = -dxy * StrictMath.cos(StrictMath.toRadians(newHeading))
    oxyandzcor(x + rotationPoint.x, y + rotationPoint.y, ozcor)
    heading = newHeading
  }

  def orbitUp(delta: Double) {
    val newPitch = pitch + delta
    val z = dist * StrictMath.sin(StrictMath.toRadians(newPitch))
    val dxy = dist * StrictMath.cos(StrictMath.toRadians(newPitch))
    val x = -dxy * StrictMath.sin(StrictMath.toRadians(heading))
    val y = -dxy * StrictMath.cos(StrictMath.toRadians(heading))
    // don't let observer go under patch-plane or be upside-down
    if (z + rotationPoint.z > 0 && newPitch < 90) {
      oxyandzcor(x + rotationPoint.x, y + rotationPoint.y, z + rotationPoint.z)
      pitch = newPitch
    }
  }

  def translate(thetaX: Double, thetaY: Double) {
    val headingR = StrictMath.toRadians(heading)
    val sinH = StrictMath.sin(headingR)
    val cosH = StrictMath.cos(headingR)
    oxcor = oxcor - (cosH * thetaX + sinH * thetaY) * 0.1
    oycor = oycor + (sinH * thetaX - cosH * thetaY) * 0.1
    setRotationPoint(new api.Vect(rotationPoint.x - ((cosH * thetaX + sinH * thetaY) * 0.1),
        rotationPoint.y + ((sinH * thetaX - cosH * thetaY) * 0.1),
        rotationPoint.z))
  }

  def distance(agent: api.Agent): Double =
    agent match {
      case t: Turtle =>
        distance(t.xcor, t.ycor)
      case p: Patch =>
        distance(p.pxcor, p.pycor)
      case l: Link =>
        world.protractor.distance(agent, oxcor, oycor, true)
    }

  def distance(t: api.Turtle): Double =
    distance(t.xcor, t.ycor)

  def distance(x: Double, y: Double): Double =
    StrictMath.sqrt(
      (x - oxcor) * (x - oxcor) +
        (y - oycor) * (y - oycor) +
        ozcor * ozcor)

  def home() {
    oxcor = world.minPxcor + (world.maxPxcor - world.minPxcor) / 2.0
    oycor = world.minPycor + (world.maxPycor - world.minPycor) / 2.0
    ozcor = StrictMath.max(world.worldWidth, world.worldHeight) * 1.5
    heading = 0
    pitch = 90
    roll = 0
    setRotationPoint(oxcor, oycor, 0)
  }

  var ozcor: Double = 0

  def oxyandzcor(_oxcor: Double, _oycor: Double, _ozcor: Double) {
    oxcor = _oxcor
    oycor = _oycor
    ozcor = _ozcor
  }

}
