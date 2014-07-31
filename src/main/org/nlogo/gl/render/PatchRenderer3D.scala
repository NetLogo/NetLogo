// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import javax.media.opengl.{GL, GL2, GL2GL3}
import org.nlogo.api.{ Patch, Patch3D, World, World3D, Color, DrawingInterface }

private class PatchRenderer3D(world: World3D, drawing: DrawingInterface, shapeRenderer: ShapeRenderer)
extends PatchRenderer(world, drawing, shapeRenderer) {

  override def renderPatchTexture(gl: GL2)
  {
    super.renderPatches(gl)
  }

  override def renderLabels(gl: GL2, fontSize: Int, patchSize: Double) {
    if(world.patchesAllBlack)
      super.renderLabels(gl, fontSize, patchSize)
  }

  override def renderIndividualLabels(gl: GL2, patch: Patch3D, fontSize: Int, patchSize: Double) {
    if(world.patchesAllBlack)
      super.renderIndividualLabels(gl, patch, fontSize, patchSize)
  }

  def renderIndividualPatch(gl: GL2, patch: Patch3D, fontSize: Int, patchSize: Double) {
    if(!world.patchesAllBlack) {
      var flag = true
      gl.glPushMatrix()
      val shape3D = shapeRenderer.getShape("@@@PATCH@@@")
      // If a patch is invisible, but has a label, we still need to render that
      // label. The label should get drawn a little lower for invisible patches
      // (See ShapeRenderer(3D).renderWrappedAgent() for label rendering for
      // visible patches).
      if(patch.alpha == 0.0 && patch.hasLabel) {  // TODO: Possibility of floating error here (and in Renderer.agentIsVisible() method).
        val scale = Renderer.WORLD_SCALE
        gl.glPushMatrix()
        val coords = getPatchCoords(patch)
        renderLabel(gl, scale * coords(0), scale * coords(1), scale * coords(2),
                    patch, fontSize, patchSize)
        gl.glPopMatrix()
        flag = false
      }
      if(flag)
        renderWrappedPatch(gl, patch, shape3D, fontSize, patchSize, false)
      gl.glPopMatrix()
    }
  }

  def renderBlackPatches(gl: GL2, patch: Patch3D, fontSize: Int, patchSize: Double) {
    if(!world.patchesAllBlack) {
      if(patch.hasLabel) {
        gl.glPushMatrix()
        val coords = getPatchCoords(patch)
        val scale = Renderer.WORLD_SCALE
        renderLabel(gl, scale * coords(0), scale * coords(1), scale * coords(2),
                    patch, fontSize, patchSize)
        gl.glPopMatrix()
      }
    }
  }

  override def renderPatches(gl: GL2, fontSize: Int, patchSize: Double) {
    if(!world.patchesAllBlack || world.patchesWithLabels != 0) {
      gl.glPushMatrix()
      val numPatches = world.patchColors.length
      val shape3D = shapeRenderer.getShape("@@@PATCH@@@")
      for(id <- 0 until numPatches) {
        val patch = world.getPatch(id).asInstanceOf[Patch3D]
        if(patch.alpha == 0.0) {
          // If a patch is invisible, but has a label, we still need to render that label. The label
          // should get drawn a little lower for invisible patches (See
          // ShapeRenderer(3D).renderWrappedAgent() for label rendering for visible patches).
          if(patch.hasLabel) {
            val scale = Renderer.WORLD_SCALE
            gl.glPushMatrix()
            val coords = getPatchCoords(patch)
            renderLabel(gl, scale * coords(0), scale * coords(1), scale * coords(2),
                        patch, fontSize, patchSize)
            gl.glPopMatrix()
          }
        }
        else
          renderWrappedPatch(gl, patch, shape3D, fontSize, patchSize, false)
      }
      gl.glPopMatrix()
    }
  }

  def renderWrappedPatch(gl: GL2, patch: Patch3D, shape: GLShape,
                        fontSize: Int, patchSize: Double, outline: Boolean) {
    shapeRenderer.renderWrappedAgent(
      gl, shapeRenderer.getShape("@@@PATCH@@@"),
      1.0, Color.getColor(patch.pcolor),
      patch.labelString, patch.labelColor,
      world.wrappedObserverX(patch.pxcor),
      world.wrappedObserverY(patch.pycor),
      world.wrappedObserverZ(patch.pzcor), 1,
      patchSize, fontSize, false, 0, Array[Double](0, 0, 0))
  }

  def getDimensions(world: World) = {
    val w = world.asInstanceOf[World3D]
    Array[Float](w.worldWidth, w.worldHeight, w.worldDepth)
  }

  override def getPatchCoords(patch: Patch) = {
    val p = patch.asInstanceOf[Patch3D]
    val coords = Array[Float](p.pxcor, p.pycor, p.pzcor)
    coords(0) = world.wrappedObserverX(coords(0)).toFloat
    coords(1) = world.wrappedObserverY(coords(1)).toFloat
    coords(2) = world.wrappedObserverZ(coords(2)).toFloat
    coords
  }

  override def renderOutline(gl: GL2, patch: Patch) {
    val shape = shapeRenderer.getShape("@@@PATCH@@@")
    val rgb = Color.getColor(patch.pcolor).getRGBColorComponents(null)
    gl.glPushMatrix()
    val coords = getPatchCoords(patch)
    gl.glTranslatef(coords(0) * Renderer.WORLD_SCALE,
                    coords(1) * Renderer.WORLD_SCALE,
                    coords(2) * Renderer.WORLD_SCALE)
    // render the thick-lined wireframe
    gl.glPushMatrix()
    gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE)
    gl.glColor3f(rgb(0), rgb(1), rgb(2))
    gl.glLineWidth(4)
    gl.glCallList(shape.displayListIndex)
    gl.glColor3f((rgb(0) + 0.5f) % 1.0f, (rgb(1) + 0.5f) % 1.0f,
                 (rgb(2) + 0.5f) % 1.0f)
    gl.glLineWidth(2)
    gl.glCallList(shape.displayListIndex)
    gl.glLineWidth(1)
    gl.glPopMatrix()
    gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL)
    gl.glPopMatrix()
  }

}
