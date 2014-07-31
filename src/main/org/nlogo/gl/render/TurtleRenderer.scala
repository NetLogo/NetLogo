// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import javax.media.opengl.{ GL, GL2 }
import javax.media.opengl.glu.GLU
import org.nlogo.api.{ Agent, Constants, Perspective, Turtle, World }

private class TurtleRenderer(world: World, shapeRenderer: ShapeRenderer)
extends AgentRenderer(world, shapeRenderer) {

  private def lineScale = {
    val distance =
      if(world.observer.perspective == Perspective.Follow ||
         world.observer.perspective == Perspective.Ride)
        world.observer.followDistance
      else
        world.observer.dist
    if(distance == 0)
      0d
    else
      (world.worldWidth max world.worldHeight) * 1.5 / distance
  }

  def renderTurtles(gl: GL2, glu: GLU, fontSize: Int, patchSize: Double, outlineAgent: Agent) {
    if (world.turtles == null)
      return
    import collection.JavaConverters._
    for(turtle <- world.turtles.agents.asScala.map(_.asInstanceOf[Turtle]))
      if ((world.observer.perspective != Perspective.Ride || world.observer.targetAgent != turtle)
          && !turtle.hidden)
        renderWrappedTurtle(gl, turtle, fontSize, patchSize, outlineAgent == turtle, lineScale)
  }

  def renderWrappedTurtle(gl: GL2, turtle: Turtle, fontSize: Int,
                          patchSize: Double, outline: Boolean, lineScale: Double) {
    val shape3D = shapeRenderer.getShape(turtle.shape)
    val height = shapeRenderer.getShapeHeight(turtle.shape, shape3D, turtle.size)
    val coords = getAgentCoords(turtle, height)
    shapeRenderer.renderWrappedAgent(
      gl, shape3D, turtle.size, org.nlogo.api.Color.getColor(turtle.color),
      turtle.labelString, turtle.labelColor, coords(0), coords(1), coords(2),
      height, patchSize, fontSize, outline, lineScale * turtle.lineThickness,
      getOrientation(turtle))
  }

  def renderHighlight(gl: GL2, agent: Turtle) {
    shapeRenderer.renderHighlight(
      gl, agent, shapeRenderer.shapeManager.getShape(agent.shape),
      getAgentCoords(agent, 1), getOrientation(agent))
  }

  def getXYandZComponents(agent: Agent, dist: Double): Array[Double] = {
    val turtle = agent.asInstanceOf[Turtle]
    val headingRadians = math.toRadians(turtle.heading)
    var cos = math.cos(headingRadians)
    var sin = math.sin(headingRadians)
    if(math.abs(cos) < Constants.Infinitesimal)
      cos = 0
    if(math.abs(sin) < Constants.Infinitesimal)
      sin = 0
    Array(dist * sin * Renderer.WORLD_SCALE,
          dist * cos * Renderer.WORLD_SCALE,
          0)
  }

  def getAgentCoords(agent: Agent, height: Double): Array[Double] = {
    val turtle = agent.asInstanceOf[Turtle]
    val coords = Array(world.wrappedObserverX(turtle.xcor),
                       world.wrappedObserverY(turtle.ycor),
                       (height - 1) /  2)
    if(turtle.shape == "default")
      coords(2) /= 2
    coords
  }

  def getOrientation(agent: Agent) =
    Array[Double](agent.asInstanceOf[Turtle].heading, 0, 0)

}
