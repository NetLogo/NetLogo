package org.nlogo.agent

import org.nlogo.api
import collection.JavaConverters._

private object Alpha {

  /**
   * Returns true if there is at least one partially transparent turtle, patch, link, or 3D stamp
   * present. This determines whether it is necessary to sort the objects by their distance to the
   * observer before rendering, which is necessary for transparency to work in OpenGL.
   *
   * @return True if the scene has at least one partially transparent item
   */
  def hasPartiallyTransparentObjects(world: api.World) =
    world.turtles.agents.asScala.exists(_.isPartiallyTransparent) ||
    world.patches.agents.asScala.exists(_.isPartiallyTransparent) ||
    world.links.agents.asScala.exists(_.isPartiallyTransparent) ||
    // In the 3D view in 2D, stamps are rasterized on the drawing layer,
    // so they're not agents and we don't need to check for them here.
    (world.getDrawing match {
       case d: Drawing3D =>
         (d.turtleStamps.asScala.exists(_.isPartiallyTransparent) ||
          d.linkStamps.asScala.exists(_.isPartiallyTransparent))
       case _ =>
         false
    })

}
