// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import java.awt.image.BufferedImage

import com.jogamp.opengl.awt.GLCanvas

trait ViewInterface {
  def exportView: BufferedImage
  def resetPerspective(): Unit
  def canvas: GLCanvas
  def renderer: Renderer
  def signalViewUpdate(): Unit
}
