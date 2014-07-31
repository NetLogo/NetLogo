// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import javax.media.opengl.{ GL, GL2, GL2GL3 }
import java.nio.{ ByteBuffer, IntBuffer }

object TextureUtils {

  def makeTexture(gl: GL2, size: Int) {
    gl.glTexImage2D(
      GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, size, size,
      0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null)
  }

  def genTexture(gl: GL2): Int = {
    val tmp = Array(0)
    gl.glGenTextures(1, IntBuffer.wrap(tmp))
    tmp(0)
  }

  def calculateTextureSize(gl: GL2, width: Int, height: Int) = {
    // OpenGL only allows square textures whose dimension is at least 64...
    val glRendererName = gl.glGetString(GL.GL_RENDERER).toUpperCase
    val maxTextureSize =
      // ATI Rage Mobility lies, max texture size is 256
      // lists.apple.com/archives/mac-opengl/2003/Jul/msg00135.html
      // experiementally other Rage cards are reporting incorrect
      // numbers as well so let's limit them all to 256 to be safe. ev 6/3/05
      if (glRendererName.startsWith("ATI RAGE"))
        256
      // ATI Radeon 7500 lies too: the reported max texture size isn't
      // actually supported in fullscreen mode. We couldn't find any
      // documentation on this, but it looks like in fullscreen mode the
      // card only supports 512.
      //
      // This isn't the optimal solution since the smaller limit only
      // applies to fullscreen mode. Ideally, we'd only do this special
      // case in fullscreen mode, and use the full (reported) texture size
      // when not in fullscreen.
      // -- azs 6/13/05
      else if (glRendererName.startsWith("ATI RADEON 7500"))
        512
      // Same goes for the 9200
      else if (glRendererName.startsWith("ATI RADEON 9200"))
        512
      else
        GL.GL_MAX_TEXTURE_SIZE

    // ...and is a power of two
    Iterator.iterate(64)(_ * 2)
      .dropWhile(size => (width > size || height > size) && size < ((2 + maxTextureSize) / 2))
      .next
  }

  def createTileArray(width: Int, height: Int, textureSize: Int): Array[Array[Byte]] = {
    val numTiles =
      math.ceil(width.toDouble / textureSize).toInt *
      math.ceil(height.toDouble / textureSize).toInt
    Array.fill(numTiles)(Array[Byte]())
  }

  def setParameters(gl: GL2) {
    // We want our patches to have nice sharp edges, not blurry fuzzy edges (that would take longer
    // to render anyway). - ST 2/9/05
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST)
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST)
    // Theoretically, if we do the scaling right, it won't matter whether the texture is set to
    // clamp or repeat, because the texture will exactly fill its quad.  but just in case we're
    // slightly off, putting the texture in clamp mode seems like it might possibly 1) avoid
    // slightly weird appearance, and 2) slightly improve performance.  So let's do it. - ST 2/9/05
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP)
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP)
  }

  def reuseTexture(gl: GL2, tileWidth: Int, tileHeight: Int,
                   xOffset: Int, yOffset: Int, width: Int,
                   colors: Array[Int], _bytes: Array[Byte]): Array[Byte] = {

    var i = 0
    var size = (tileHeight * tileWidth) * 4

    val bytes = Option(_bytes)
                  .filter(_.size == size)
                  .getOrElse(new Array[Byte](size))

    // The colors array is ARGB but from what I understand from poking around on the web, there is
    // no GL_* constant corresponding to that type.  So we'll use GL_RGBA and do the conversion from
    // ARGB int[] to RGBA byte[] ourselves.  Not positive this is the best/fastest code for this
    // task. - ST 2/9/05, 8/8/05
    // Also, you'd think that since patches don't have alpha, we could save time by using GL_RGB in
    // that case instead of GL_RGBA.  But Esther and I tried it and we got weird results where the
    // patches with pxcor < pycor were OK, but the other half of the patches were the wrong color.
    // Neither of us could figure out why.  GL_RGB used to work fine, but that was before we were
    // using glTexSubImage2D (at the time we were just making a new texture every time with
    // glTexImage2D).  So we have given up and we just use RGBA all the time now.  - ST 8/8/05
    var x = xOffset
    while(x < tileWidth + xOffset) {
      var y = yOffset
      while(y < tileHeight + yOffset) {
        // I tried "optimizing" this by only shifting 8 bits at a time with "rgb >>= 8", but it
        // wasn't any faster - ST 8/8/05
        val rgb = colors(x + y * width)
        bytes(i) = (rgb >> 16).toByte // red
        i += 1
        bytes(i) = (rgb >> 8).toByte // green
        i += 1
        bytes(i) = rgb.toByte // blue
        i += 1
        bytes(i) = (rgb >> 24).toByte // alpha
        i += 1
        y += 1
      }
      x += 1
    }

    // because the texture coords are reversed swap height and width
    // ev 5/25/05
    gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, tileHeight, tileWidth,
                       GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap(bytes))

    bytes
  }

  def renderEmptyPlane(gl: GL2, _sideX: Float, _sideY: Float, _sideZ: Float) {
    val sideX = _sideX * (Renderer.WORLD_SCALE / 2)
    val sideY = _sideY * (Renderer.WORLD_SCALE / 2)
    val sideZ = _sideZ * (Renderer.WORLD_SCALE / 2)

    gl.glColor4f(0f, 0f, 0f, 0.5f)
    gl.glBegin(GL2GL3.GL_QUADS)
    gl.glNormal3f(0f, 0f, -1f)
    gl.glVertex3f(-sideX, +sideY, -sideZ)
    gl.glVertex3f(-sideX, -sideY, -sideZ)
    gl.glVertex3f(+sideX, -sideY, -sideZ)
    gl.glVertex3f(+sideX, +sideY, -sideZ)
    gl.glEnd()

    // both sides of the quad. patches are always opaque from both sides
    gl.glColor4f(0f, 0f, 0f, 0.5f)
    gl.glBegin(GL2GL3.GL_QUADS)
    gl.glNormal3f(0f, 0f, -1f)
    gl.glVertex3f(+sideX, +sideY, -sideZ)
    gl.glVertex3f(+sideX, -sideY, -sideZ)
    gl.glVertex3f(-sideX, -sideY, -sideZ)
    gl.glVertex3f(-sideX, +sideY, -sideZ)
    gl.glEnd()
  }

  def renderInPlane(gl: GL2, _sideX: Float, _sideY: Float, _sideZ: Float,
                    width: Float, height: Float, size: Float,
                    offsetX: Float, offsetY: Float) = {

    val sideX = _sideX * (Renderer.WORLD_SCALE / 2)
    val sideY = _sideY * (Renderer.WORLD_SCALE / 2)
    val sideZ = _sideZ * (Renderer.WORLD_SCALE / 2)

    // set color not quite sure I understand why this seems to matter, given the texture oh well -
    // ST 2/9/05
    gl.glColor4f(1f, 1f, 1f, 1f)

    // the texture is bigger than the world, because of the power-of-2 rule,
    // so some of the pixels in the texture are unused, so we need to magnify
    // the texture so the unused pixels disappear
    val offsetStretchX = offsetX / size
    val offsetStretchY = offsetY / size
    val textureStretchX = width / size + offsetStretchX
    val textureStretchY = height / size + offsetStretchY

    gl.glBegin(GL2GL3.GL_QUADS)
    gl.glNormal3f(0f, 0f, -1f)

    gl.glTexCoord2f(offsetStretchY, offsetStretchX)
    gl.glVertex3f(-sideX, sideY, -sideZ)

    gl.glTexCoord2f(textureStretchY, offsetStretchX)
    gl.glVertex3f(-sideX, -sideY, -sideZ)

    gl.glTexCoord2f(textureStretchY, textureStretchX)
    gl.glVertex3f(sideX, -sideY, -sideZ)

    gl.glTexCoord2f(offsetStretchY, textureStretchX)
    gl.glVertex3f(sideX, sideY, -sideZ)

    gl.glEnd()
  }

}
