// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import javax.media.opengl.{ GL, GL2 }
import org.nlogo.api.{ Turtle, Turtle3D, Patch, Patch3D, World, World3D,
                       Agent, AgentException, Perspective, DrawingInterface }

private class WorldRenderer3D(world: World3D, patchRenderer: PatchRenderer3D,
                              drawing: DrawingInterface, turtleRenderer: TurtleRenderer3D,
                              linkRenderer: LinkRenderer3D, settings: GLViewSettings)
extends WorldRenderer(world, patchRenderer, drawing, turtleRenderer, linkRenderer, settings) {

  override def createDrawingRenderer(world: World, drawing: DrawingInterface,
                                     renderer: TurtleRenderer, linkRenderer: LinkRenderer): DrawingRendererInterface =
    new TrailRenderer3D(world.asInstanceOf[World3D],
                        renderer.asInstanceOf[TurtleRenderer3D],
                        linkRenderer.asInstanceOf[LinkRenderer3D])

  override def observePerspective(gl: GL2) {
    var x = observer.oxcor - world.followOffsetX
    var y = observer.oycor - world.followOffsetY
    var z = observer.ozcor - world.followOffsetZ
    var heading = observer.heading
    var pitch = observer.pitch
    var roll = observer.roll
    var dx = observer.dx
    var dy = observer.dy
    var dz = observer.dz
    if(observer.perspective == Perspective.Follow || observer.perspective == Perspective.Ride) {
      val turtle = observer.targetAgent.asInstanceOf[Turtle3D]
      var distance: Double = observer.followDistance
      // try and skip the area where you're way too close to the turtle to be interesting
      if(distance > 0)
        distance += turtle.size
      if(world.worldDepth > 1) {
        x = x - turtle.dx * distance
        y = y - turtle.dy * distance
        z = z - turtle.dz * distance
        pitch = - pitch
        dx = - dx
        dy = - dy
      }
      else {
        val oldx = x
        val oldy = y
        val oldz = z
        x = x - distance * math.sin(math.toRadians(heading))
        y = y - distance * math.cos(math.toRadians(heading))
        z = distance * 0.5
        pitch =
          try -world.protractor.towardsPitch(x, y, z, oldx, oldy, oldz, false)
          catch { case ex: AgentException => 0 }
        roll = 0
      }
    }
    gl.glRotated(90, -1.0, 0.0, 0.0)
    gl.glRotated(heading, 0.0, 0.0, 1.0)
    gl.glRotated(pitch,
                 math.cos(math.toRadians(heading)),
                 -math.sin(math.toRadians(heading)), 0.0)
    gl.glRotated(-roll, -dx, -dy, dz)
    gl.glTranslated(-(x * Renderer.WORLD_SCALE),
                    -(y * Renderer.WORLD_SCALE),
                    -(z * Renderer.WORLD_SCALE))
  }

  override def renderPatchShapes(gl: GL2, outlineAgent: Agent,
                                 fontSize: Int, patchSize: Double) {
    // we might get here before the world is set up
    if(world.patches != null) {
      if (world.worldDepth > 1)
        patchRenderer.renderPatches(gl, fontSize, patchSize)
      outlineAgent match {
        case p: Patch =>
          patchRenderer.renderOutline(gl, p)
        case _ =>
      }
    }
  }

  override def renderIndividualPatchShapes(gl: GL2, patch: Patch3D, outlineAgent: Agent,
                                           fontSize: Int, patchSize: Double) {
    if(world.patches != null && world.worldDepth > 1)
      patchRenderer.renderIndividualPatch(gl, patch, fontSize, patchSize)
  }

  override def renderWorld(gl: GL2, fontSize: Int, patchSize: Double) {
    // we might get here before the world is set up
    if(world.patches != null) {
      if (world.worldDepth == 1)
        // I might be wrong, but the only way we can get here is if we're in 3D, and we manually set
        // the world depth to be 1 (by changing min-pzcor and max-pzcor).
        //
        // If this was meant to determine whether we're in the 3D view in 2D, this wouldn't work
        // because we would be in WorldRenderer.renderWorld(), instead of
        // WorldRenderer3D.renderWorld().
        //
        // However, I'm going to leave this here anyway to avoid breaking something else I'm not
        // aware of, or if I'm just plain wrong about this.
        //
        // Note: If we did manually change the world depth to be 1, this might result in patches
        // being rendered twice, because PatchRenderer3D.renderPatchTexture() calls renderPatches().
        patchRenderer.renderPatchTexture(gl)
      if(settings.wireframeOn)
        renderWorldWireFrame(gl)
    }
  }

  def renderTrails(gl: GL2) {
    drawingRenderer match {
      case r: TrailRenderer3D =>
        r.renderTrails(gl)
      case _ =>
    }
  }

  override def getCrosshairCoords =
    Array[Double](observer.oxcor - world.followOffsetX,
                  observer.oycor - world.followOffsetY,
                  observer.ozcor - world.followOffsetZ)

  override def getWorldDimensions(world: World) = {
    val w = world.asInstanceOf[World3D]
    Array[Float](w.worldWidth, w.worldHeight, w.worldDepth)
  }

  override def getXYandZ(turtle: Turtle) = {
    val t = turtle.asInstanceOf[Turtle3D]
    Array[Double](t.xcor, t.ycor, t.zcor)
  }

  override def getOrientation(turtle: Turtle) = {
    val t = turtle.asInstanceOf[Turtle3D]
    Array[Double](t.heading, t.pitch, t.roll)
  }

}
