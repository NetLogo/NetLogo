// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import java.awt.{ BorderLayout, Rectangle }
import java.awt.event.{ WindowAdapter, WindowEvent }

import org.nlogo.api.Version
import org.nlogo.gl.render.Renderer

class ObserverView(viewManager: ViewManager, renderer: Renderer, bounds: Rectangle)
extends View("3D View", viewManager, renderer) {

  def this(viewManager: ViewManager, renderer: Renderer) =
    this(viewManager, renderer, new Rectangle(600, 600))

  setBounds(bounds)
  val navBar = new ViewControlToolBar(this, inputHandler)
  add(navBar, BorderLayout.SOUTH)
  val controlStrip = new ViewControlStrip3D(viewManager.workspace, viewManager.tickCounterLabel)
  add(controlStrip, BorderLayout.NORTH)

  addWindowListener(new WindowAdapter {
    override def windowOpened(e: WindowEvent): Unit = {
      repaint()
    }

    override def windowClosing(e: WindowEvent): Unit = {
      if (!Version.is3D)
        ObserverView.this.viewManager.close()
    }
  })

  viewManager.addLinkComponent(this)

  override def updatePerspectiveLabel() {
    navBar.setStatus(viewManager.world.observer.perspective)
  }

  override def editFinished() = {
    super.editFinished()
    true
  }

  override def syncTheme(): Unit = {
    navBar.syncTheme()
    controlStrip.syncTheme()
  }
}
