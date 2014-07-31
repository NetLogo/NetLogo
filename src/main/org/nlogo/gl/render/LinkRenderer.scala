// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import javax.media.opengl.GL
import javax.media.opengl.GL2
import javax.media.opengl.glu.GLU
import org.nlogo.api.{ Agent, Link, Perspective, World }

private class LinkRenderer(world: World, shapeRenderer: ShapeRenderer)
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

  def renderLinks(gl: GL2, glu: GLU, fontSize: Int, patchSize: Double, outlineAgent: Agent) {
    if(world.links == null)
      return
    val scale = lineScale
    import collection.JavaConverters._
    for(link <- world.links.agents.asScala.map(_.asInstanceOf[Link]))
      if(!link.hidden)
        renderWrappedLink(gl, link, fontSize, patchSize, outlineAgent == link, scale)
  }

  def getLinkCoords(link: Link) =
    Array[Float](link.x1.toFloat, link.y1.toFloat, 0, link.x2.toFloat, link.y2.toFloat, 0)

  def renderWrappedLink(gl: GL2, link: Link, fontSize: Int, patchSize: Double,
                        outline: Boolean, lineScale: Double) {
    val maxx = world.maxPxcor + 0.5
    val minx = world.minPxcor - 0.5
    val maxy = world.maxPycor + 0.5
    val miny = world.minPycor - 0.5
    val worldWidth = world.worldWidth
    val worldHeight = world.worldHeight
    var wrapXRight = false
    var wrapXLeft = false
    val coords = getLinkCoords(link)
    val size = link.size
    val shape = shapeRenderer.getLinkShape(link.shape)
    val color = org.nlogo.api.Color.getColor(link.color)
    renderLink(gl, shape, color, size,
               coords(0), coords(1), coords(2),
               coords(3), coords(4), coords(5),
               patchSize, link.lineThickness, link.isDirectedLink, link, outline)
    if(world.wrappingAllowedInX) {
      if(coords(3) + size / 2  > maxx) {
        renderLink(gl, shape, color, size,
                   coords(0) - worldWidth, coords(1), coords(2),
                   coords(3) - worldWidth, coords(4), coords(5),
                   patchSize, link.lineThickness, link.isDirectedLink, link, outline)
        wrapXRight = true
      }
      if(coords(0) - size / 2  < minx) {
        renderLink(gl, shape, color, size,
                   coords(0) + worldWidth, coords(1), coords(2),
                   coords(3) + worldWidth, coords(4), coords(5),
                   patchSize, link.lineThickness, link.isDirectedLink, link, outline)
        wrapXLeft = true
      }
    }
    if(world.wrappingAllowedInY) {
      if(coords(4) + size / 2  > maxy) {
        renderLink(gl, shape, color, size,
                   coords(0), coords(1) - worldHeight, coords(2),
                   coords(3), coords(4) - worldHeight, coords(5),
                   patchSize, link.lineThickness, link.isDirectedLink, link, outline)

        if(wrapXRight)
          renderLink(gl, shape, color, size,
                     coords(0) - worldWidth, coords(1) - worldHeight, coords(2),
                     coords(3) - worldWidth, coords(4) - worldHeight, coords(5),
                     patchSize, link.lineThickness, link.isDirectedLink, link, outline)
        if(wrapXLeft)
          renderLink(gl, shape, color, size,
                     coords(0) + worldWidth, coords(1) - worldHeight, coords(2),
                     coords(3) + worldWidth, coords(4) - worldHeight, coords(5),
                     patchSize, link.lineThickness, link.isDirectedLink, link, outline)
      }
      if(coords(1) - size / 2  < miny) {
        renderLink(gl, shape, color, size,
                   coords(0), coords(1) + worldHeight, coords(2),
                   coords(3), coords(4) + worldHeight, coords(5),
                   patchSize, link.lineThickness, link.isDirectedLink, link, outline)
        if(wrapXRight)
          renderLink(gl, shape, color, size,
                     coords(0) - worldWidth, coords(1) + worldHeight, coords(2),
                     coords(3) - worldWidth, coords(4) + worldHeight, coords(5),
                     patchSize, link.lineThickness, link.isDirectedLink, link, outline)
        if(wrapXLeft)
          renderLink(gl, shape, color, size,
                     coords(0) + worldWidth, coords(1) + worldHeight, coords(2),
                     coords(3) + worldWidth, coords(4) + worldHeight, coords(5),
                     patchSize, link.lineThickness, link.isDirectedLink, link, outline)
      }
    }
    if(link.hasLabel) {
      val labelCoords = getAgentCoords(link, 1)
      shapeRenderer.renderLabel(gl, link.labelString, link.labelColor,
                                (labelCoords(0) * Renderer.WORLD_SCALE).toFloat,
                                (labelCoords(1) * Renderer.WORLD_SCALE).toFloat,
                                (labelCoords(2) * Renderer.WORLD_SCALE).toFloat,
                                1, fontSize, patchSize)
    }
  }

  private def renderLink(gl: GL2, shape: GLLinkShape, color: java.awt.Color, size: Double,
                         x1: Float, y1: Float, z1: Float,
                         x2: Float, y2: Float, z2: Float,
                         patchSize: Double, lineThickness: Double, isDirected: Boolean,
                         link: Link, outline: Boolean) {
    gl.glPushMatrix()
    gl.glColor4fv(java.nio.FloatBuffer.wrap(color.getRGBColorComponents(null)))
    gl.glEnable(GL2.GL_LINE_STIPPLE)
    val stroke = (1.0 max (patchSize * lineThickness)).toFloat
    gl.glLineWidth(stroke)
    shape.render(gl, x1, y1, z1, x2, y2, z2, stroke,
                 isDirected, link, shapeRenderer, outline, color, world)
    gl.glLineWidth(1.0f)
    gl.glDisable(GL2.GL_LINE_STIPPLE)
    gl.glPopMatrix()
  }

  def renderIndividualLinks(gl: GL2, glu: GLU, link: Link, fontSize: Int,
                            patchSize: Double, outlineAgent: Agent)
  {
    if(world.links == null)
      return
    if(!link.hidden)
      renderWrappedLink(gl, link, fontSize, patchSize, (outlineAgent == link), lineScale)
  }

  def getOrientation(agent: Agent) =
    Array[Double](agent.asInstanceOf[Link].heading, 0, 0)

  private def getAgentCoords(agent: Agent, height: Double) = {
    val link = agent.asInstanceOf[Link]
    val coords = Array[Double](world.wrappedObserverX(link.midpointX),
                               world.wrappedObserverY(link.midpointY),
                               (height - 1) /  2)
    if(link.shape == "default")
      coords(2) /= 2
    coords
  }

}
