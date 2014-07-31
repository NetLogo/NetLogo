// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import javax.media.opengl.{ GL, GL2, GLEventListener, GLAutoDrawable }
import com.jogamp.common.nio.Buffers

trait ExportRenderer extends Renderer with GLEventListener {

  var pixelInts: Array[Int] = null

  override def display(gLDrawable: GLAutoDrawable) {
    init(gLDrawable)
    val gl = gLDrawable.getGL().asInstanceOf[GL2]
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT)
    shapeManager.checkQueue(gl, glu)
    render(gl)
    gl.glFinish()
    exportGraphics(gLDrawable)
  }

  private def exportGraphics(drawable: GLAutoDrawable) {
    width = drawable.getWidth
    height = drawable.getHeight
    val pixelsRGB = Buffers.newDirectByteBuffer(width * height * 3)
    val gl = drawable.getGL.asInstanceOf[GL2]
    gl.glReadBuffer(GL.GL_BACK)
    gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1)
    gl.glReadPixels(
      0,     // GLint x
      0,     // GLint y
      width,      // GLsizei width
      height,    // GLsizei height
      GL.GL_RGB,    // GLenum format
      GL.GL_UNSIGNED_BYTE,   // GLenum type
      pixelsRGB
    ) // GLvoid *pixels

    pixelInts = Array.fill(width * height)(0)

    // Convert RGB bytes to ARGB ints with no transparency. Flip image vertically by reading the
    // rows of pixels in the byte buffer in reverse - (0,0) is at bottom left in OpenGL.

    var p = width * height * 3 // Points to first byte (red) in each row.
    var i = 0   // Index into target int[]
    var w3 = width * 3    // Number of bytes in each row
    for(row <- 0 until height) {
      p -= w3
      var q = p  // Index into ByteBuffer
      for(col <- 0 until width) {
        val iR = pixelsRGB.get(q); q += 1
        val iG = pixelsRGB.get(q); q += 1
        val iB = pixelsRGB.get(q); q += 1
        pixelInts(i) =
          0xFF000000 |
          ((iR & 0x000000FF) << 16) |
          ((iG & 0x000000FF) << 8) |
          (iB & 0x000000FF)
        i += 1
      }
    }
  }

  override def dispose(glDrawable: GLAutoDrawable): Unit = {
  }

}

private class ExportRenderer2D(renderer: Renderer) extends Renderer(renderer) with ExportRenderer

private class ExportRenderer3D(renderer: Renderer) extends Renderer3D(renderer) with ExportRenderer
