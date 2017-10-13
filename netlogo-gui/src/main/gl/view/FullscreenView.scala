// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import org.nlogo.gl.render.Renderer
import java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment

private class FullscreenView(viewManager: ViewManager, renderer: Renderer)
extends View("", viewManager, renderer) {

  setUndecorated(true)
  setVisible(true)

  def init() {
    val gd = getLocalGraphicsEnvironment.getDefaultScreenDevice
    gd.setFullScreenWindow(this)
    canvas.requestFocus()
  }
}
