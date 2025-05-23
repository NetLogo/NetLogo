// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import com.jogamp.opengl.{ GL, GL2, GL2GL3, GL2ES3 }
import com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING
import org.nlogo.api.{ AgentFollowingPerspective, World, WorldRenderable, Patch, Patch3D, DrawingInterface}

private class PatchRenderer(world: World & WorldRenderable, drawing: DrawingInterface, shapeRenderer: ShapeRenderer)
extends TextureRenderer(world) {

  def getPatchCoords(patch: Patch): Array[Float] = {
    val coords = Array[Float](patch.pxcor.toFloat, patch.pycor.toFloat, 0)
    coords(0) = world.wrappedObserverX(coords(0)).toFloat
    coords(1) = world.wrappedObserverY(coords(1)).toFloat
    coords
  }

  def renderIndividualLabels(gl: GL2, patch: Patch3D, fontSize: Int, patchSize: Double): Unit = {
    if(world.patchesWithLabels > 0) {
      val scale = Renderer.WORLD_SCALE
      if(patch.hasLabel) {
        gl.glPushMatrix()
        val coords = getPatchCoords(patch)
        renderLabel(gl, scale * coords(0), scale * coords(1), scale * coords(2),
                    patch, fontSize, patchSize)
        gl.glPopMatrix()
      }
    }
  }

  def renderLabels(gl: GL2, fontSize: Int, patchSize: Double): Unit = {
    if(world.patchesWithLabels > 0) {
      val numPatches = world.patchColors.length
      val scale = Renderer.WORLD_SCALE
      for(id <- 0 until numPatches) {
        val patch = world.getPatch(id)
        if(patch.hasLabel) {
          gl.glPushMatrix()
          val coords = getPatchCoords(patch)
          renderLabel(gl, scale * coords(0), scale * coords(1), scale * coords(2),
                      patch, fontSize, patchSize)
          gl.glPopMatrix()
        }
      }
    }
  }

  def renderLabel(gl: GL2, col: Float, row: Float, dep: Float,
                  patch: Patch, fontSize: Int, patchSize: Double): Unit = {
    val observer = world.observer
    val orientation = observer.orientation.get
    gl.glTranslated(col, row, dep)
    gl.glRotated(-orientation.heading, 0.0, 0.0, 1.0)
    gl.glRotated(90, 1.0, 0.0, 0.0)
    observer.perspective match {
      case (_: AgentFollowingPerspective) =>
        gl.glRotated(-orientation.pitch, -1.0, 0.0, 0.0)
        gl.glRotated(-orientation.roll, 0.0, 0.0, 1.0)
      case _ =>
        gl.glRotated(orientation.pitch, -1.0, 0.0, 0.0)
        gl.glRotated(orientation.roll, 0.0, 0.0, 1.0)
    }
    AgentRenderer.renderString(
      gl, world, patch.labelString, patch.labelColor, fontSize, patchSize)
  }

  def renderOutline(gl: GL2, patch: Patch): Unit = {
    gl.glPushMatrix()
    val scale = Renderer.WORLD_SCALE
    val rgb = org.nlogo.api.Color.getColor(patch.pcolor).getRGBColorComponents(null)
    gl.glColor3f((rgb(0) + 0.5f) % 1f,
                 (rgb(1) + 0.5f) % 1f,
                 (rgb(2) + 0.5f) % 1f)
    gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE)
    gl.glEnable(GL2GL3.GL_POLYGON_OFFSET_LINE)
    gl.glPolygonOffset(-1f, -1f)
    gl.glScaled(scale, scale, scale)
    val coords = getPatchCoords(patch)
    gl.glTranslated(coords(0), coords(1), coords(2))
    gl.glBegin(GL2ES3.GL_QUADS)
    gl.glVertex3f(-0.5f, 0.5f, -0.5f)
    gl.glVertex3f(-0.5f, -0.5f, -0.5f)
    gl.glVertex3f(0.5f, -0.5f, -0.5f)
    gl.glVertex3f(0.5f, 0.5f, -0.5f)
    gl.glEnd()
    gl.glDisable(GL2GL3.GL_POLYGON_OFFSET_LINE)
    gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL)
    gl.glPopMatrix()
  }

  def renderHightlight(gl: GL2, patch: Patch): Unit = {
    gl.glPushMatrix()
    val scale = Renderer.WORLD_SCALE
    gl.glScaled(scale, scale, scale)
    val coords = getPatchCoords(patch)
    gl.glTranslated(coords(0), coords(1), -0.5)
    gl.glScaled(3.3333, 3.3333, 3.3333)
    shapeRenderer.renderHalo(gl, false, (3.3333 * 0.285 * 2.0))
    gl.glPopMatrix()
  }

  def renderPatchTexture(gl: GL2): Unit = {
    renderPatches(gl)
  }

  /// textures

  def renderPatches(gl: GL2): Unit = {
    calculateTextureSize(gl, world.patchesAllBlack)
    renderTexture(gl)
  }

  def renderPatches(gl: GL2, fontSize: Int, patchSize: Double): Unit = { }

  private def renderTexture(gl: GL2): Unit = {
    gl.glEnable(GL.GL_TEXTURE_2D)
    gl.glDisable(GL_LIGHTING)
    // we disable writing to the depth buffer because we technically want to guarantee the drawing
    // will be written, which has exactly the same depth as the patches, and hence can cause
    // "stitching" - jrn 6/2/05
    if(!drawing.isBlank)
      gl.glDepthMask(false)
    if(!world.patchesAllBlack) {
      // based on earlier calculations the texture is not the right size so delete old ones and make
      // new ones ev 5/25/05
      if(newTexture) {
        if(texture != 0)
          gl.glDeleteTextures(1, java.nio.IntBuffer.wrap(Array[Int](texture)))
        texture = TextureUtils.genTexture(gl)
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture)
        TextureUtils.makeTexture(gl, textureSize)
        world.markPatchColorsDirty()
        newTexture = false
      }
      else
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture)
    }
    gl.glPushMatrix()
    // now scale up to fill the world
    if(world.patchesAllBlack) {
      gl.glScalef(world.worldWidth.toFloat, world.worldHeight.toFloat, 1)
      TextureUtils.renderEmptyPlane(gl, 1f, 1f, 1f)
    }
    else {
      TextureUtils.setParameters(gl)
      renderTextureTiles(gl, world.worldWidth, world.worldHeight, textureSize,
                         world.patchColors, world.patchColorsDirty)
      world.markPatchColorsClean()
    }
    // done, clean up
    if(!drawing.isBlank)
      gl.glDepthMask(true)
    gl.glEnable(GL_LIGHTING)
    gl.glPopMatrix()
    gl.glBindTexture(GL.GL_TEXTURE_2D, 0)
  }

  private def calculateTextureSize(gl: GL2, patchesBlank: Boolean): Unit = {
    // generate new textures
    if(!patchesBlank || textureSize == 0) {
      val newSize = TextureUtils.calculateTextureSize(
        gl, world.worldWidth, world.worldHeight)
      if(textureSize != newSize) {
        newTexture = true
        textureSize = newSize
        tiles = TextureUtils.createTileArray(world.worldWidth, world.worldHeight, textureSize)
      }
    }
  }

}
