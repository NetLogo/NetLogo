// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import org.nlogo.api.{ Agent, Link, Link3D, World }

private class LinkRenderer3D(world: World, shapeRenderer: ShapeRenderer)
extends LinkRenderer(world, shapeRenderer) {

  override def getOrientation(agent: Agent): Array[Double] = {
    val link = agent.asInstanceOf[Link3D]
    Array(link.heading, link.pitch, 0d)
  }

  override def getLinkCoords(agent: Link): Array[Float] = {
    val link = agent.asInstanceOf[Link3D]
    Array(link.x1.toFloat, link.y1.toFloat, link.z1.toFloat,
          link.x2.toFloat, link.y2.toFloat, link.z2.toFloat)
  }

}
