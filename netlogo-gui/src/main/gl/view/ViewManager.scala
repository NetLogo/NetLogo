// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view


import org.nlogo.core.{ I18N, Shape }
import org.nlogo.shape.{ VectorShape, LinkShape }
import org.nlogo.gl.render.{ GLViewSettings, JOGLException, JOGLLoader }
import org.nlogo.window.{ GUIWorkspace, JOGLLoadingException, JOGLVersionMismatchException, TickCounterLabel }
import javax.swing.JFrame
import java.awt.event.KeyListener

class ViewManager(val workspace: GUIWorkspace,
                  appWindow: JFrame,
                  keyListener: KeyListener)
    extends org.nlogo.window.GLViewManagerInterface
    with org.nlogo.window.Event.LinkChild
    with org.nlogo.window.Event.LinkParent
    with org.nlogo.window.Events.PeriodicUpdateEvent.Handler
    with org.nlogo.window.LinkRoot
    with GLViewSettings {

  val world = workspace.world
  var currentView: View = null
  var observerView: ObserverView = null
  val tickCounterLabel = new TickCounterLabel(workspace.world)
  addLinkComponent(tickCounterLabel)
  private var fullscreenView: FullscreenView = null
  var turtleView: View = null
  private var fullscreen = false

  var paintingImmediately = false
  private var _framesSkipped = false
  override def framesSkipped() { _framesSkipped = true }

  override def getLinkParent = appWindow

  @throws(classOf[JOGLLoadingException])
  def open() {
    if (observerView != null) {
      observerView.toFront()
      observerView.updatePerspectiveLabel()
    } else
      try init()
      catch {
        case vex: JOGLVersionMismatchException =>
          org.nlogo.swing.Utils.alert(
            vex.getMessage,
            I18N.gui.get("common.buttons.continue"))
        case ex: JOGLLoadingException =>
          if (observerView != null) {
            observerView.dispose()
            observerView = null
          }
          throw ex
      }
  }

  def init() {
    var jvmex: JOGLException = null

    // if we have a frame already, dispose of it
    Option(observerView).foreach(_.dispose())

    try {
      observerView = new ObserverView(this, null)
      observerView.canvas.addKeyListener(keyListener)
      currentView = observerView
      org.nlogo.awt.Positioning.moveNextTo(observerView, appWindow)
      currentView.updatePerspectiveLabel()
      observerView.setVisible(true)
    } catch {
      case e: java.lang.UnsatisfiedLinkError =>
        throw new JOGLLoadingException(
          "NetLogo could not load the JOGL native libraries on your computer.\n\n" +
            "Write bugs@ccl.northwestern.edu for assistance.", e)
    }
    if (jvmex != null)
      throw new JOGLVersionMismatchException(jvmex.getMessage)
  }

  def setFullscreen(fullscreen: Boolean) {
    if (fullscreen != isFullscreen) {
      val gd = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment.getDefaultScreenDevice
      // this is necessary in order to force PatchRenderer to make a new texture, since the old one
      // won't survive the transition to fullscreen - ST 2/9/05
      world.markPatchColorsDirty()
      if (fullscreen) {
        if (!gd.isFullScreenSupported)
          throw new UnsupportedOperationException(
            "This graphics environment does not support full screen mode")
        currentView.setVisible(true)
        appWindow.setVisible(false)
        fullscreenView = new FullscreenView(this, currentView.renderer)
        fullscreenView.canvas.addKeyListener(keyListener)
        fullscreenView.init()
        observerView.setVisible(false)
        currentView = fullscreenView
        this.fullscreen = true
      } else {
        appWindow.setVisible(true)
        gd.setFullScreenWindow(null)
        observerView.setVisible(true)
        observerView.updateRenderer()
        this.fullscreen = false
        currentView = observerView
        fullscreenView.dispose()
      }
    }
  }

  def isFullscreen = fullscreen

  def editFinished() {
    if (currentView != null)
      currentView.editFinished()
  }

  def is3D = currentView != null

  def isDead = false

  def close() {
    if(currentView != null) {
      workspace.set2DViewEnabled(true)
      currentView.dispose()
      observerView = null
      currentView = null
    }
  }

  private val paintRunnable =
    new Runnable() {
      override def run() {
        incrementalUpdateFromEventThread()
      }
    }

  def incrementalUpdateFromJobThread() {
    try org.nlogo.awt.EventQueue.invokeAndWait(paintRunnable)
    catch {
      case ex: InterruptedException =>
        repaint()
    }
  }

  def incrementalUpdateFromEventThread() {
    // in case we get called before init() - ST 2/18/05
    if (currentView != null) {
      workspace.updateManager.beginPainting()
      currentView.display()
      workspace.updateManager.donePainting()
      currentView.updatePerspectiveLabel()
    }
  }

  def repaint() {
    // in case we get called before init() - ST 2/18/05
    if (currentView != null) {
      workspace.updateManager.beginPainting()
      currentView.signalViewUpdate()
      workspace.updateManager.donePainting()
      currentView.updatePerspectiveLabel()
      _framesSkipped = false
    }
  }

  private var antiAliasing = true

  def antiAliasingOn(antiAliasing: Boolean) {
    this.antiAliasing = antiAliasing
    if (currentView != null) {
      world.markPatchColorsDirty()
      observerView = new ObserverView(this, currentView.renderer, currentView.getBounds)
      currentView.dispose()
      currentView = observerView
      currentView.setVisible(true)
    }
  }

  def antiAliasingOn = antiAliasing

  var wireframeOn = true

  def paintImmediately(force: Boolean) {
    if (viewIsVisible && (_framesSkipped || force)) {
      paintingImmediately = true
      repaint()
      paintingImmediately = false
    }
  }

  def viewIsVisible = currentView.isShowing

  def getExportWindowFrame: java.awt.Component =
    currentView

  def exportView = currentView.exportView

  override def addLinkComponent(c: AnyRef) {
    linkComponents.clear()
    super.addLinkComponent(c)
  }

  def handle(e: org.nlogo.window.Events.PeriodicUpdateEvent) {
    if (observerView != null)
      observerView.controlStrip.updateTicks()
  }

  def displayOn = workspace.displaySwitchOn

  def displayOn(displayOn: Boolean) {
    workspace.displaySwitchOn(displayOn)
  }

  def graphicsSettings: org.nlogo.api.ViewSettings =
    workspace.view

  def mouseXCor =
    Option(currentView).map(_.renderer.mouseXCor).getOrElse(0f)

  def mouseYCor =
    Option(currentView).map(_.renderer.mouseYCor).getOrElse(0f)

  def resetMouseCors() {
    if (currentView != null)
      currentView.renderer.resetMouseCors()
  }

  def mouseDown =
    Option(currentView).map(_.renderer.mouseDown).getOrElse(false)

  def mouseInside = currentView.renderer.mouseInside

  def shapeChanged(shape: Shape) {
    if (currentView != null) {
      shape match {
        case _: Shape.VectorShape =>
          currentView.invalidateTurtleShape(shape.name)
        case _: Shape.LinkShape =>
          currentView.invalidateLinkShape(shape.name)
      }
      repaint()
    }
  }

  @throws(classOf[java.io.IOException])
  @throws(classOf[org.nlogo.shape.InvalidShapeDescriptionException])
  def addCustomShapes(filename: String) {
    currentView.renderer.addCustomShapes(filename)
  }

  def displaySwitch(on: Boolean) {
    observerView.controlStrip.displaySwitch.setOn(on)
  }

  def displaySwitch =
    observerView.controlStrip.displaySwitch.isSelected

  // I think the 3D renderer grabs it's font size directly from the main view so we don't need to
  // keep track of it here.
  def applyNewFontSize(fontSize: Int, zoom: Int) {}

  var warned = false

}
