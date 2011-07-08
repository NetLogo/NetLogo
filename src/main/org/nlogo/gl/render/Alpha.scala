package org.nlogo.gl.render

import org.nlogo.api.{ Agent, Color, Drawing3D, Link, Patch3D, Patch, Turtle, World, World3D }
import collection.JavaConverters._

private object Alpha {

  def agentIsPartiallyTransparent(agent: Agent) =
    getAlpha(agent) < 255

  def getAlpha(agent: Agent) = {
    val color = agent match {
      case t: Turtle => t.color
      case p: Patch => p.pcolor
      case l: Link => l.color
      case _ => 255: java.lang.Double
    }
    // special case black, non-RGB 3D patches to be invisible.  kinda janky to have a special case
    // like that but until we have an alpha variable I guess it's the least bad design. - ST 4/20/11
    if (agent.isInstanceOf[Patch3D] && color == Color.BoxedBlack)
      0d
    else
      Color.getColor(color).getAlpha
  }

  /**
   * If there is at least one partially transparent turtle, patch, or link
   * present in the scene, this function will return true. This is used to
   * determine whether it is necessary to sort the objects by their distance
   * to the observer before rendering, which is necessary for transparency
   * to work in OpenGL.
   *
   * @return True if the scene has at least one partially transparent object.
   */
  def sceneHasPartiallyTransparentObjects(world: World) =
    world.turtles.agents.asScala.exists(agentIsPartiallyTransparent) ||
    world.patches.agents.asScala.exists(agentIsPartiallyTransparent) ||
    world.links.agents.asScala.exists(agentIsPartiallyTransparent) ||
    // In the 3D view in 2D, stamps are rasterized on the drawing layer,
    // so they're not agents and we don't need to check for them here.
    (world.getDrawing match {
       case d: Drawing3D =>
         (d.turtleStamps.asScala.exists(agentIsPartiallyTransparent) ||
          d.linkStamps.asScala.exists(agentIsPartiallyTransparent))
       case _ =>
         false
    })

}
