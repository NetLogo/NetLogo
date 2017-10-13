// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import org.nlogo.gl.render.Renderer

class ObserverView(viewManager: ViewManager, renderer: Renderer, bounds: java.awt.Rectangle)
extends View("3D View", viewManager, renderer) {

  def this(viewManager: ViewManager, renderer: Renderer) =
    this(viewManager, renderer, new java.awt.Rectangle(600, 600))

  setBounds(bounds)
  val navBar = new ViewControlToolBar(this, inputHandler)
  add(navBar, java.awt.BorderLayout.SOUTH)
  val controlStrip = new ViewControlStrip3D(viewManager.workspace, viewManager.tickCounterLabel)
  add(controlStrip, java.awt.BorderLayout.NORTH)

  viewManager.addLinkComponent(this)

  override def updatePerspectiveLabel() {
    navBar.setStatus(viewManager.world.observer.perspective)
  }

  override def editFinished() = {
    super.editFinished()
    true
  }
}
