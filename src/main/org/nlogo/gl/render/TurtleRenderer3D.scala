// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import org.nlogo.api.{ Agent, Constants, Turtle3D, World, World3D }

private class TurtleRenderer3D(world: World, shapeRenderer: ShapeRenderer)
extends TurtleRenderer(world, shapeRenderer) {

  override def getOrientation(agent: Agent): Array[Double] = {
    val turtle = agent.asInstanceOf[Turtle3D]
    Array(turtle.heading, turtle.pitch, turtle.roll)
  }

  override def getXYandZComponents(agent: Agent, dist: Double): Array[Double] = {
    val t = agent.asInstanceOf[Turtle3D]
    val headingRadians = math.toRadians(t.heading)
    var cosHeading = math.cos(headingRadians)
    var sinHeading = math.sin(headingRadians)
    val pitchRadians = math.toRadians(t.pitch)
    var cosPitch = math.cos(pitchRadians)
    var sinPitch = math.sin(pitchRadians)
    if(math.abs(cosHeading) < Constants.Infinitesimal)
      cosHeading = 0
    if(math.abs(sinHeading) < Constants.Infinitesimal)
      sinHeading = 0
    if(math.abs(cosPitch) < Constants.Infinitesimal)
      cosPitch = 0
    if(math.abs(sinPitch) < Constants.Infinitesimal)
      sinPitch = 0
    Array(dist * sinHeading * cosPitch * Renderer.WORLD_SCALE,
          dist * cosHeading * cosPitch * Renderer.WORLD_SCALE,
          dist * sinPitch * Renderer.WORLD_SCALE)
  }

  override def getAgentCoords(agent: Agent, height: Double): Array[Double] = {
    val t = agent.asInstanceOf[Turtle3D]
    Array(world.wrappedObserverX(t.xcor),
          world.wrappedObserverY(t.ycor),
          world.asInstanceOf[World3D].wrappedObserverZ(t.zcor))
  }

}
