// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import com.jogamp.opengl.{ GL, GL2 }
import org.nlogo.api.{ Turtle, Turtle3D, Patch, Patch3D, World, World3D,
                       Agent, AgentFollowingPerspective, AgentException, Perspective, DrawingInterface }

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
    val orientation = observer.orientation.get
    var heading = orientation.heading
    var pitch   = orientation.pitch
    var roll    = orientation.roll
    var dx      = orientation.dx
    var dy      = orientation.dy
    var dz      = orientation.dz
    val turtleAndDistance = observer.perspective match {
      case afp: AgentFollowingPerspective => Some((afp.targetAgent, afp.followDistance))
      case _ => None
    }

    turtleAndDistance.foreach {
      case (turtle: Turtle3D, followDist: Int) =>
        var distance = followDist.toDouble
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
      case _ =>
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
        case p: Patch => patchRenderer.renderOutline(gl, p)
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
    if (world.patches != null) {
      // We can get here two ways:
      // - we open a 3D model and set the world depth to be 1 (by changing min/max-pzcor).
      // - we open a 2D model in 3D (has world depth of 1 by default)
      //
      // This *won't* render the 3D view in 2D - that code is in WorldRenderer.renderWorld().
      //
      // Note that we disable textures explicitly in the else case since they are enabled by
      // renderPatchTexture. Leaving them enabled screws up rendering if we're not rendering
      // the patches as textures anymore.
      if (world.worldDepth == 1)
        patchRenderer.renderPatchTexture(gl)
      else
        gl.glDisable(GL.GL_TEXTURE_2D)

      if (settings.wireframeOn)
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
