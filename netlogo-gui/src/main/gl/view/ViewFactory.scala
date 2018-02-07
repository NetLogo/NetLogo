// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import java.awt.Rectangle
import java.awt.event.{ MouseEvent, MouseListener }
import org.nlogo.gl.render.Renderer

abstract class ViewFactory {
  def createRenderer(viewManager: ViewManager): Renderer
  def createRenderer(renderer: Renderer): Renderer

  def createRenderer(viewManager: ViewManager, renderer: Renderer): Renderer = {
    if (renderer == null) createRenderer(viewManager)
    else                  createRenderer(renderer)
  }

  class PopupListener(picker: Picker, view: View) extends MouseListener {
    def mouseEntered(evt: MouseEvent) { }
    def mouseExited(evt: MouseEvent) { }
    def mouseDragged(evt: MouseEvent) { }
    def mouseMoved(evt: MouseEvent) { }
    def mousePressed(evt: MouseEvent) {
      if (evt.isPopupTrigger) {
        view.renderer.queuePick(evt.getPoint, picker, view)
        evt.consume()
        view.renderer.showCrossHairs(true)
      }
    }
    def mouseReleased(evt: MouseEvent) {
      if (evt.isPopupTrigger) {
        view.renderer.queuePick(evt.getPoint, picker, view)
        evt.consume()
        view.signalViewUpdate()
      }
    }
    def mouseClicked(evt: MouseEvent) {}
  }

  def observer(viewManager: ViewManager, oldRenderer: Renderer): ObserverView = {
    val picker = new Picker(viewManager)
    val renderer = createRenderer(viewManager, oldRenderer)
    val view = new ObserverView(viewManager, renderer)
    val popupListener = new PopupListener(picker, view)
    view.canvas.addMouseListener(popupListener)
    view
  }

  def observer(viewManager: ViewManager, oldRenderer: Renderer, bounds: Rectangle): ObserverView = {
    val picker = new Picker(viewManager)
    val renderer = createRenderer(viewManager, oldRenderer)
    val view = new ObserverView(viewManager, renderer, bounds)
    val popupListener = new PopupListener(picker, view)
    view.canvas.addMouseListener(popupListener)
    view
  }

  private[view] def fullscreen(viewManager: ViewManager, oldRenderer: Renderer): FullscreenView = {
    // No PopupListener because showing popup menus on a fullscreen view causes a hard
    // Java crash. - RG 11/2/17
    val renderer = createRenderer(viewManager, oldRenderer)
    val view = new FullscreenView(viewManager, renderer)
    view
  }
}

class ThreeDGLViewFactory extends ViewFactory {
  def createRenderer(viewManager: ViewManager): Renderer = {
    new org.nlogo.gl.render.Renderer3D(viewManager.world, viewManager.graphicsSettings, viewManager.workspace, viewManager)
  }
  def createRenderer(renderer: Renderer): Renderer = {
    renderer.cleanUp()
    new org.nlogo.gl.render.Renderer3D(renderer)
  }
}

class TwoDGLViewFactory extends ViewFactory {
  def createRenderer(viewManager: ViewManager): Renderer = {
    new Renderer(viewManager.world, viewManager.graphicsSettings, viewManager.workspace, viewManager)
  }
  def createRenderer(renderer: Renderer): Renderer = {
    renderer.cleanUp()
    new org.nlogo.gl.render.Renderer(renderer)
  }
}
