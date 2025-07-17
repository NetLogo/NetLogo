// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import java.awt.event.KeyListener
import java.lang.UnsatisfiedLinkError
import javax.swing.JFrame

import org.nlogo.awt.Positioning
import org.nlogo.core.{ I18N, Shape }
import org.nlogo.gl.render.GLViewSettings
import org.nlogo.swing.Utils
import org.nlogo.window.{ GUIWorkspace, JOGLLoadingException, JOGLVersionMismatchException, TickCounterLabel }

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
  var currentView: Option[View] = None
  var observerView: Option[ObserverView] = None
  val tickCounterLabel = new TickCounterLabel(workspace.world)
  addLinkComponent(tickCounterLabel)
  private var fullscreenView: Option[FullscreenView] = None
  private var fullscreen = false

  var paintingImmediately = false
  private var _framesSkipped = false
  override def framesSkipped(): Unit = { _framesSkipped = true }

  override def getLinkParent = appWindow

  @throws(classOf[JOGLLoadingException])
  def open(): Unit = {
    observerView match {
      case Some(view) =>
        view.toFront()
        view.updatePerspectiveLabel()

      case _ =>
        try {
          init()
        } catch {
          case vex: JOGLVersionMismatchException =>
            Utils.alert(vex.getMessage, I18N.gui.get("common.buttons.continue"))

          case ex: JOGLLoadingException =>
            observerView.foreach(_.dispose())
            observerView = None

            throw ex
        }
    }

    syncTheme()
  }

  def init(): Unit = {
    // if we have a frame already, dispose of it
    observerView.foreach(_.dispose())

    try {
      val view = new ObserverView(this, null)
      observerView = Option(view)
      view.canvas.addKeyListener(keyListener)
      currentView = observerView
      Positioning.moveNextTo(view, appWindow)
      currentView.foreach(_.updatePerspectiveLabel())
      view.setVisible(true)
    } catch {
      case e: UnsatisfiedLinkError =>
        throw new JOGLLoadingException(
          "NetLogo could not load the JOGL native libraries on your computer.\n\n" +
            "Write bugs@ccl.northwestern.edu for assistance.", e)
    }
  }

  def setFullscreen(fullscreen: Boolean): Unit = {
    if (fullscreen != isFullscreen) {
      val gd = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment.getDefaultScreenDevice
      // this is necessary in order to force PatchRenderer to make a new texture, since the old one
      // won't survive the transition to fullscreen - ST 2/9/05
      world.markPatchColorsDirty()
      if (fullscreen) {
        if (!gd.isFullScreenSupported)
          throw new UnsupportedOperationException(
            "This graphics environment does not support full screen mode")
        currentView.foreach(_.setVisible(true))
        appWindow.setVisible(false)
        val view = new FullscreenView(this, currentView.map(_.renderer).orNull)
        fullscreenView = Some(view)
        view.canvas.addKeyListener(keyListener)
        view.init()
        observerView.foreach(_.setVisible(false))
        currentView = fullscreenView
        this.fullscreen = true
      } else {
        appWindow.setVisible(true)
        gd.setFullScreenWindow(null)
        observerView.foreach(_.setVisible(true))
        observerView.foreach(_.display())
        observerView.foreach(_.updateRenderer())
        this.fullscreen = false
        currentView = observerView
        fullscreenView.foreach(_.dispose())
        fullscreenView = None
      }
    }
  }

  def isFullscreen = fullscreen

  def editFinished(): Unit = {
    currentView.foreach(_.editFinished())
  }

  def is3D = currentView.isDefined

  def isDead = false

  def close(): Unit = {
    currentView.foreach { view =>
      workspace.set2DViewEnabled(true)
      view.dispose()
      observerView = None
      currentView = None
    }
  }

  private val paintRunnable =
    new Runnable() {
      override def run(): Unit = {
        incrementalUpdateFromEventThread()
      }
    }

  def incrementalUpdateFromJobThread(): Unit = {
    try org.nlogo.awt.EventQueue.invokeAndWait(paintRunnable)
    catch {
      case ex: InterruptedException =>
        repaint()
    }
  }

  def incrementalUpdateFromEventThread(): Unit = {
    currentView.foreach { view =>
      workspace.updateManager.beginPainting()
      view.display()
      workspace.updateManager.donePainting()
      view.updatePerspectiveLabel()
    }
  }

  def repaint(): Unit = {
    currentView.foreach { view =>
      workspace.updateManager.beginPainting()
      view.display()
      workspace.updateManager.donePainting()
      view.updatePerspectiveLabel()
      _framesSkipped = false
    }
  }

  private var antiAliasing = true

  def antiAliasingOn(antiAliasing: Boolean): Unit = {
    this.antiAliasing = antiAliasing
    currentView.foreach { view =>
      world.markPatchColorsDirty()
      observerView = Option(new ObserverView(this, view.renderer, view.getBounds))
      view.dispose()
      currentView = observerView
      currentView.foreach(_.setVisible(true))
    }
  }

  def antiAliasingOn = antiAliasing

  var wireframeOn = true

  def setWireframeOn(on: Boolean): Unit = {
    wireframeOn = on
  }

  def paintImmediately(force: Boolean): Unit = {
    if (viewIsVisible && (_framesSkipped || force)) {
      paintingImmediately = true
      repaint()
      paintingImmediately = false
    }
  }

  def viewIsVisible = currentView.exists(_.isShowing)

  def getExportWindowFrame: java.awt.Component =
    currentView.orNull

  def exportView = currentView.map(_.exportView).orNull

  override def addLinkComponent(c: AnyRef): Unit = {
    linkComponents.clear()
    super.addLinkComponent(c)
  }

  def handle(e: org.nlogo.window.Events.PeriodicUpdateEvent): Unit = {
    observerView.foreach(_.controlStrip.updateTicks())
  }

  def displayOn = workspace.displaySwitchOn

  def displayOn(displayOn: Boolean): Unit = {
    workspace.displaySwitchOn(displayOn)
  }

  def graphicsSettings: org.nlogo.api.ViewSettings =
    workspace.view

  def mouseXCor =
    currentView.map(_.renderer.mouseXCor).getOrElse(0f)

  def mouseYCor =
    currentView.map(_.renderer.mouseYCor).getOrElse(0f)

  def resetMouseCors(): Unit = {
    currentView.foreach(_.renderer.resetMouseCors())
  }

  def mouseDown =
    currentView.exists(_.renderer.mouseDown)

  def mouseInside = currentView.exists(_.renderer.mouseInside)

  def shapeChanged(shape: Shape): Unit = {
    currentView.foreach { view =>
      shape match {
        case _: Shape.VectorShape =>
          view.invalidateTurtleShape(shape.name)
        case _: Shape.LinkShape =>
          view.invalidateLinkShape(shape.name)
      }
      repaint()
    }
  }

  @throws(classOf[java.io.IOException])
  @throws(classOf[org.nlogo.shape.InvalidShapeDescriptionException])
  def addCustomShapes(filename: String): Unit = {
    currentView.foreach(_.renderer.addCustomShapes(filename))
  }

  def displaySwitch(on: Boolean): Unit = {
    observerView.foreach(_.controlStrip.displaySwitch.setOn(on))
  }

  def displaySwitch =
    observerView.exists(_.controlStrip.displaySwitch.isSelected)

  // I think the 3D renderer grabs it's font size directly from the main view so we don't need to
  // keep track of it here.
  def applyNewFontSize(fontSize: Int, zoom: Int): Unit = {}

  var warned = false

  override def syncTheme(): Unit = {
    observerView.foreach(_.syncTheme())
    fullscreenView.foreach(_.syncTheme())
  }
}
