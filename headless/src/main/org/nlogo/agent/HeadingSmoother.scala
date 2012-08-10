// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

// This is the old AnimationRenderer class. It doesn't depend on opengl though and really all it does
// is upate the heading of the observer.  Makes more sense down here in agent I think.

class HeadingSmoother {

  private var thirdPersonOldHeading = 0d
  private var firstPersonOldHeading = 0d

  private var angleChange = 1d
  private var oldHeading = 0d

  def follow(agent: org.nlogo.api.Agent): Double = {
    thirdPersonUpdate(agent)
    thirdPersonOldHeading
  }

  def watch(agent: org.nlogo.api.Agent): Double = {
    firstPersonUpdate(agent)
    firstPersonOldHeading
  }

  private def thirdPersonUpdate(agent: org.nlogo.api.Agent) {
    var heading = agent match {
      case t: org.nlogo.api.Turtle => t.heading
      case _ => 0d
    }
    if (thirdPersonOldHeading + angleChange <= heading)
      if (heading - thirdPersonOldHeading > 180.0d)
        thirdPersonOldHeading -= angleChange
      else
        thirdPersonOldHeading += angleChange
    else if (thirdPersonOldHeading - angleChange >= heading)
      if (thirdPersonOldHeading - heading > 180.0d)
        thirdPersonOldHeading += angleChange
      else
        thirdPersonOldHeading -= angleChange
    else {
      thirdPersonOldHeading = heading
      angleChange = 1.0d
    }
    if (thirdPersonOldHeading >= 360.0d)
      thirdPersonOldHeading -= 360.0d
    else if (thirdPersonOldHeading < 0.0d)
      thirdPersonOldHeading += 360.0d
    if (heading == oldHeading)
      angleChange = angleChange * 1.5d
    else {
      angleChange = angleChange / 1.5d
      if (angleChange < 1.0d)
        angleChange = 1.0d
    }
    oldHeading = heading
  }

  private def firstPersonUpdate(agent: org.nlogo.api.Agent) = {
    var heading = agent match {
      case t: org.nlogo.api.Turtle => t.heading
      case _ => 0d
    }
    if (firstPersonOldHeading + angleChange <= heading)
      if (heading - firstPersonOldHeading > 180.0d)
        firstPersonOldHeading -= angleChange
      else
        firstPersonOldHeading += angleChange
    else if (firstPersonOldHeading - angleChange >= heading)
      if (firstPersonOldHeading - heading > 180.0d)
        firstPersonOldHeading += angleChange
      else
        firstPersonOldHeading -= angleChange
    else
      firstPersonOldHeading = heading
      angleChange = 6.0d
    if (firstPersonOldHeading >= 360.0d)
      firstPersonOldHeading -= 360.0d
    else if (firstPersonOldHeading < 0.0d)
      firstPersonOldHeading += 360.0d
    angleChange = angleChange * 1.75d
  }

}
