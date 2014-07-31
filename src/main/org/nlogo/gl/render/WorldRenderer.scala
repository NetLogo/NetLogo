// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import javax.media.opengl.{ GL, GL2, GL2GL3 }
import javax.media.opengl.fixedfunc.GLLightingFunc
import org.nlogo.api.{ World, Agent, Patch, Patch3D, Turtle, Perspective, DrawingInterface, AgentException }

private class WorldRenderer(world: World, patchRenderer: PatchRenderer,
                            drawing: DrawingInterface, turtleRenderer: TurtleRenderer,
                            linkRenderer: LinkRenderer, settings: GLViewSettings) {

  val observer = world.observer
  val drawingRenderer = createDrawingRenderer(world, drawing, turtleRenderer, linkRenderer)
  var shapeManager: ShapeManager = null

  def createDrawingRenderer(world: World, drawing: DrawingInterface,
                            turtleRenderer: TurtleRenderer, linkRenderer: LinkRenderer): DrawingRendererInterface =
    new DrawingRenderer(world, drawing)

  def init(gl: GL2, shapeManager: ShapeManager) {
    this.shapeManager = shapeManager
    drawingRenderer.init(gl)
  }

  def observePerspective(gl: GL2) {
    var x = observer.oxcor - world.followOffsetX
    var y = observer.oycor - world.followOffsetY
    var z = observer.ozcor
    var heading = observer.heading
    var pitch = observer.pitch
    if(observer.perspective == Perspective.Follow || observer.perspective == Perspective.Ride) {
      var distance: Double = observer.followDistance
      // try and skip the area where you're way too close to the turtle to be interesting
      observer.targetAgent match {
        case t: Turtle if distance > 0 =>
          distance += t.size
        case _ =>
      }
      val (oldx, oldy, oldz) = (x, y, z)
      x -= distance * math.sin(math.toRadians(heading))
      y -= distance * math.cos(math.toRadians(heading))
      z = distance * 0.5
      pitch =
        try -world.protractor.towardsPitch(x, y, z, oldx, oldy, oldz, false)
        catch { case ex: AgentException => 0 }
    }
    gl.glRotated(90, -1.0, 0.0, 0.0)
    gl.glRotated(heading, 0.0, 0.0, 1.0)
    gl.glRotated(pitch,
                 math.cos(math.toRadians(heading)),
                 -math.sin(math.toRadians(heading)), 0.0)
    gl.glTranslated(-x * Renderer.WORLD_SCALE,
                    -y * Renderer.WORLD_SCALE,
                    -z * Renderer.WORLD_SCALE)
  }

  def getXYandZ(turtle: Turtle) =
    Array[Double](turtle.xcor, turtle.ycor, 0)

  def getOrientation(turtle: Turtle) =
    Array[Double](turtle.heading, 0, 0)

  /// crosshairs

  var showCrossHairs = false

  def getCrosshairCoords =
    Array[Double](observer.oxcor - world.followOffsetX,
                  observer.oycor - world.followOffsetY,
                  observer.ozcor)

  def renderCrossHairs(gl: GL2) {
    if(showCrossHairs) {
      val coords = getCrosshairCoords
      val perspective = observer.perspective
      if(perspective != Perspective.Follow && perspective != Perspective.Ride) {
        val dist = observer.dist
        coords(0) += dist * observer.dx
        coords(1) += dist * observer.dy
        coords(2) -= dist * observer.dz
      }
      coords(0) *= Renderer.WORLD_SCALE
      coords(1) *= Renderer.WORLD_SCALE
      coords(2) *= Renderer.WORLD_SCALE
      gl.glPushMatrix()
      gl.glTranslated(coords(0), coords(1), coords(2))
      gl.glCallList(shapeManager.getShape("@@@CROSSHAIRS@@@").displayListIndex)
      gl.glPopMatrix()
    }
  }

  def renderWorld(gl: GL2, fontSize: Int, patchSize: Double) {
    // we might get here before the world is set up
    if(world.patches != null) {
      patchRenderer.renderPatches(gl)
      if(settings.wireframeOn)
        renderWorldWireFrame(gl)
    }
  }

  def renderIndividualPatchShapes(gl: GL2, patch: Patch3D, outlineAgent: Agent,
                                  fontSize: Int, patchSize: Double) {
    outlineAgent match {
      case p: Patch =>
        patchRenderer.renderOutline(gl, p)
      case _ =>
    }
    patchRenderer.renderIndividualLabels(gl, patch, fontSize, patchSize)
  }

  def renderPatchShapes(gl: GL2, outlineAgent: Agent, fontSize: Int, patchSize: Double) {
    outlineAgent match {
      case p: Patch =>
        patchRenderer.renderOutline(gl, p)
      case _ =>
    }
    patchRenderer.renderLabels(gl, fontSize, patchSize)
  }

  def renderDrawing(gl: GL2) {
    drawingRenderer.renderDrawing(gl)
  }

  def renderWorldWireFrame(gl: GL2) {
    val coords = getWorldDimensions(world)
    // white lines only please
    gl.glPolygonMode(GL.GL_FRONT, GL2GL3.GL_LINE)
    gl.glDisable(GLLightingFunc.GL_LIGHTING)
    gl.glColor3f(1.0f, 1.0f, 1.0f)
    gl.glPushMatrix()
    gl.glScalef(coords(0), coords(1), coords(2))
    gl.glCallList(shapeManager.getShape("@@@WIREFRAME@@@").displayListIndex)
    gl.glPopMatrix()
    // restore state
    gl.glPolygonMode(GL.GL_FRONT, GL2GL3.GL_FILL)
    gl.glEnable(GLLightingFunc.GL_LIGHTING)
  }

  def getWorldDimensions(world: World) =
    // note SIZE not EDGE, 1 not 0
    Array[Float](world.worldWidth, world.worldHeight, 1)

  // when we switch views we want to make a new texture but we can't delete the textures here
  // because we need the gl.
  def cleanUp() {
    patchRenderer.deleteTexture()
    drawingRenderer.clear()
  }

}
