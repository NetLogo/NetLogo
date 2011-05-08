package org.nlogo.gl.render 

import javax.media.opengl.GL
import javax.media.opengl.glu.GLU
import org.nlogo.api.{ Agent, Perspective, Turtle, World }
import org.nlogo.util.JCL._

private class TurtleRenderer(world: World, shapeRenderer: ShapeRenderer)
extends AgentRenderer(world, shapeRenderer) {

  private def lineScale = {
    val distance =
      if(world.observer.perspective == Perspective.FOLLOW || 
         world.observer.perspective == Perspective.RIDE)
        world.observer.followDistance
      else
        world.observer.dist
    if(distance == 0)
      0d
    else
      (world.worldWidth max world.worldHeight) * 1.5 / distance
  }

  def renderTurtles(gl: GL, glu: GLU, fontSize: Int, patchSize: Double, outlineAgent: Agent) {
    if (world.turtles == null)
      return
    for(turtle <- world.turtles.agents.map(_.asInstanceOf[Turtle]))
      if (world.observer.perspective != Perspective.RIDE || 
          (world.observer.targetAgent != turtle && !turtle.hidden))
        renderWrappedTurtle(gl, turtle, fontSize, patchSize, outlineAgent == turtle, lineScale)
  }

  def renderWrappedTurtle(gl: GL, turtle: Turtle, fontSize: Int, 
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

  def renderHighlight(gl: GL, agent: Turtle) {
    shapeRenderer.renderHighlight(
      gl, agent, getAgentCoords(agent, 1), getOrientation(agent))
  }

  def getXYandZComponents(agent: Agent, dist: Double): Array[Double] = {
    val turtle = agent.asInstanceOf[Turtle]
    val headingRadians = StrictMath.toRadians(turtle.heading)
    var cos = StrictMath.cos(headingRadians)
    var sin = StrictMath.sin(headingRadians)
    if(StrictMath.abs(cos) < org.nlogo.api.World.INFINITESIMAL)
      cos = 0
    if(StrictMath.abs(sin) < org.nlogo.api.World.INFINITESIMAL)
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
