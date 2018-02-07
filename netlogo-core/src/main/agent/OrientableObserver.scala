// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api,
  api.{ AgentException, AgentFollowingPerspective, Numbers, ObserverOrientation, Perspective, Vect }

trait OrientatableObserver { this: Observer =>
  var oxcor: Double = 0
  var oycor: Double = 0
  var ozcor: Double = 0

  def oxcor(d: Double): Unit = { oxcor = d }
  def oycor(d: Double): Unit = { oycor = d }
  def ozcor(d: Double): Unit = { ozcor = d }

  var perspective: api.Perspective = api.Perspective.Observe

  protected val _orientation = new Orientation(this)
  override val orientation: Option[ObserverOrientation] = Some(_orientation)

  private val headingSmoother = new HeadingSmoother();

  resetPerspective()

  def setPerspective(perspective: Perspective): Unit = {
    this.perspective = perspective
    updatePosition()
  }

  def resetPerspective(): Unit = {
    setPerspective(api.Perspective.Observe)
    home()
  }

  def orbitRight(delta: Double): Unit =
    _orientation.orbitRight(delta)

  def orbitUp(delta: Double): Unit =
    _orientation.orbitUp(delta)

  def setRotationPoint(v: Vect): Unit =
    _orientation.rotationPoint = v

  def heading(heading: Double): Unit =
    _orientation.heading = ((heading % 360) + 360) % 360

  def pitch(pitch: Double): Unit =
    _orientation.pitch = ((pitch % 360) + 360) % 360

  def roll(roll: Double): Unit =
    _orientation.roll = ((roll % 360) + 360) % 360

  def setRotationPoint(x: Double, y: Double, z: Double): Unit =
    _orientation.rotationPoint = new Vect(x, y, z);

  def oxyandzcor(oxcor: Double, oycor: Double, ozcor: Double): Unit = {
    this.oxcor = oxcor
    this.oycor = oycor
    this.ozcor = ozcor
  }

  def distance(t: org.nlogo.api.Turtle): Double =
    distance(t.xcor, t.ycor)

  def distance(x: Double, y: Double): Double =
    StrictMath.sqrt((x - oxcor) * (x - oxcor)
      + (y - oycor) * (y - oycor)
      + ozcor * ozcor)

  def atHome2D: Boolean =
    (perspective == Perspective.Observe) && (oxcor == 0) && (oycor == 0)

  // This is a hack for now, there is prob. a better way of doing this - jrn 6/9/05
  def atHome3D: Boolean =
    (perspective == Perspective.Observe) && (oxcor == 0) && (oycor == 0) &&
        (ozcor == StrictMath.max(world.worldWidth, world.worldHeight) * 1.5) &&
        _orientation.atHome3D

  def home(): Unit = {
    oxcor = world.minPxcor + ((world.maxPxcor - world.minPxcor) / 2.0);
    oycor = world.minPycor + ((world.maxPycor - world.minPycor) / 2.0);
    ozcor = StrictMath.max(world.worldWidth, world.worldHeight) * 1.5;
    _orientation.heading = 0
    _orientation.pitch = 90
    _orientation.roll = 0
    setRotationPoint(oxcor, oycor, 0)
  }

  def face(agent: org.nlogo.api.Agent): Unit = {
    try {
      heading(world.protractor.towards(this, agent, false))
    } catch {
      case ex: AgentException => heading(0.0)
    }
    try {
      pitch(-world.protractor.towardsPitch(this, agent, false))
    } catch {
      case ex: AgentException => pitch(0.0)
    }

    setRotationPoint(agent)
  }

  def face(x: Double, y: Double): Unit = {
    try {
      heading(world.protractor.towards(this, x, y, false))
    } catch {
      case ex: AgentException => heading(0.0)
    }
    try {
      pitch(-world.protractor.towardsPitch(this, x, y, 0, false));
    } catch {
      case ex: AgentException => pitch(0.0)
    }

    setRotationPoint(x, y, 0)
  }

  def setRotationPoint(agent: org.nlogo.api.Agent): Unit = {
    agent match {
      case t: org.nlogo.api.Turtle => setRotationPoint(t.xcor, t.ycor, 0)
      case l: org.nlogo.api.Link => setRotationPoint(l.midpointX, l.midpointY, 0)
      case p: org.nlogo.api.Patch => setRotationPoint(p.pxcor, p.pycor, 0)
      case _ =>
    }
  }

  def rotationPoint: Vect = _orientation.rotationPoint

  @throws(classOf[AgentException])
  def moveto(otherAgent: Agent): Unit = {
    otherAgent match {
      case t: Turtle => oxyandzcor(t.xcor, t.ycor, 0)
      case p: Patch  => oxyandzcor(p.pxcor, p.pycor, 0)
      case _         => throw new AgentException("you can't move-to a link");
    }
    face(_orientation.rotationPoint.x, _orientation.rotationPoint.y)
  }

  def updatePosition(): Boolean = {
    var changed = false

    perspective match {
      case Perspective.Observe => false
      case w: Perspective.Watch if w.targetAgent != null =>
        if (targetAgent.id == -1) {
          resetPerspective()
          true
        } else {
          setRotationPoint(targetAgent)
          face(targetAgent)
          false
        }
      case rideOrFollow: AgentFollowingPerspective =>
        if (targetAgent != null) {
          if (targetAgent.id == -1) { // he's dead!
            resetPerspective()
            true
          } else {
            val turtle = targetAgent.asInstanceOf[Turtle]
            oxyandzcor(turtle.xcor, turtle.ycor, 0)
            val newHeading = headingSmoother.follow(targetAgent);
            perspective match {
              case f: Perspective.Follow =>
                changed = (_orientation.heading != newHeading)
                heading(newHeading)
              case _ =>
                heading(turtle.heading)
            }

            pitch(0)
            roll(0)
            false
          }
        }
        else {
          false
        }
    }

  }

  def translate(thetaX: Double, thetaY: Double): Unit = {
    val headingR = StrictMath.toRadians(_orientation.heading);
    val sinH = StrictMath.sin(headingR);
    val cosH = StrictMath.cos(headingR);

    oxcor -= ((cosH * thetaX + sinH * thetaY) * 0.1);
    oycor += ((sinH * thetaX - cosH * thetaY) * 0.1);

    _orientation.rotationPoint =
      new Vect(_orientation.rotationPoint.x - ((cosH * thetaX + sinH * thetaY) * 0.1),
        _orientation.rotationPoint.y + ((sinH * thetaX - cosH * thetaY) * 0.1),
        _orientation.rotationPoint.z)
  }

  class Orientation(observer: Observer) extends ObserverOrientation {
    var pitch: Double = 0.0
    var heading: Double = 0.0
    var roll: Double = 0.0
    var rotationPoint: Vect = null

    private val infinitesimal = Numbers.Infinitesimal

    final private def truncateTiny(d: Double): Double = {
      if (StrictMath.abs(d) < infinitesimal) 0
      else                                   d
    }

    def dist: Double =
      StrictMath.sqrt((rotationPoint.x - observer.oxcor) * (rotationPoint.x - observer.oxcor)
          + (rotationPoint.y - observer.oycor) * (rotationPoint.y - observer.oycor)
          + ((rotationPoint.z - observer.ozcor) * (rotationPoint.z - observer.ozcor)))

    def dx: Double =
      truncateTiny(
        StrictMath.cos(StrictMath.toRadians(_orientation.pitch)) *
          StrictMath.sin(StrictMath.toRadians(_orientation.heading)))

    def dy: Double =
      truncateTiny(
        StrictMath.cos(StrictMath.toRadians(_orientation.pitch)) *
          StrictMath.cos(StrictMath.toRadians(_orientation.heading)))

    def dz: Double =
      truncateTiny(StrictMath.sin(StrictMath.toRadians(_orientation.pitch)))

    def atHome3D: Boolean =
      (heading == 0) && (pitch == 90) && (roll == 0) &&
        (rotationPoint.x == 0 && rotationPoint.y == 0 && rotationPoint.z == 0)

    def normalizeDegrees(d: Double): Double =
      ((d % 360) + 360) % 360

    def orbitRight(_delta: Double): Unit = {
      val delta = -_delta;

      val newHeading = heading + delta
      val dxy = dist * StrictMath.cos(StrictMath.toRadians(pitch))
      val x = -dxy * StrictMath.sin(StrictMath.toRadians(newHeading));
      val y = -dxy * StrictMath.cos(StrictMath.toRadians(newHeading));

      observer.oxyandzcor(x + rotationPoint.x, y + rotationPoint.y, ozcor)
      heading = normalizeDegrees(newHeading)
    }

    def orbitUp(_delta: Double): Unit = {
      val delta = -_delta;

      val newPitch = pitch - delta
      val z = dist * StrictMath.sin(StrictMath.toRadians(newPitch))
      val dxy = dist * StrictMath.cos(StrictMath.toRadians(newPitch))
      val x = -dxy * StrictMath.sin(StrictMath.toRadians(heading))
      val y = -dxy * StrictMath.cos(StrictMath.toRadians(heading))

      // don't let observer go under patch-plane or be upside-down
      if (z + _orientation.rotationPoint.z > 0 && newPitch < 90) {
        observer.oxyandzcor(x + rotationPoint.x, y + rotationPoint.y, z + rotationPoint.z)
        pitch = normalizeDegrees(newPitch)
      }
    }
  }
}
