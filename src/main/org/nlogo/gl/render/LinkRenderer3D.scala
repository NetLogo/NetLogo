package org.nlogo.gl.render

import org.nlogo.api.{ Agent, Link, Link3D, World }

private class LinkRenderer3D(world: World, shapeRenderer: ShapeRenderer)
extends LinkRenderer(world, shapeRenderer) {

  def getXYandZComponents(agent: Agent, dist: Double): Array[Double] = {
    val link = agent.asInstanceOf[Link3D]
    val size = link.size
    Array((link.x2 - link.x1) / size * dist,
          (link.y2 - link.y1) / size * dist, 
          (link.z2 - link.z1) / size * dist)
  }
  
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
