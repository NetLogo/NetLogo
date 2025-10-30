// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import com.jogamp.opengl.{ GLCapabilities, GLProfile }
import com.jogamp.opengl.awt.GLJPanel

import java.awt.Frame
import java.awt.event.{ KeyEvent, KeyAdapter, MouseEvent }
import java.awt.image.BufferedImage

import org.nlogo.analytics.Analytics
import org.nlogo.api.{ DrawingInterface, Version, World3D, WorldRenderable, WorldWithWorldRenderable }
import org.nlogo.gl.render.{ LinkRenderer, LinkRenderer3D, PatchRenderer, PatchRenderer3D, Renderer, Renderer3D,
                             ShapeRenderer, ShapeRenderer3D, TurtleRenderer, TurtleRenderer3D, WorldRenderer,
                             WorldRenderer3D }
import org.nlogo.swing.{ AutomateWindow, NetLogoIcon }
import org.nlogo.theme.ThemeSync
import org.nlogo.window.Event.LinkChild

abstract class View(title: String, val viewManager: ViewManager, var renderer: Renderer)
  extends Frame(title) with LinkChild with ThemeSync with NetLogoIcon with AutomateWindow {

  var canvas: GLJPanel = null
  val picker = new Picker(this)

  if (Version.is3D) {
    if (renderer == null) {
      val world: World3D & WorldRenderable = viewManager.world.asInstanceOf[World3D & WorldRenderable]
      val drawing: DrawingInterface = viewManager.workspace

      val shapeRenderer = new ShapeRenderer3D(world)
      val turtleRenderer = new TurtleRenderer3D(world, shapeRenderer)
      val linkRenderer = new LinkRenderer3D(world, shapeRenderer)
      val patchRenderer = new PatchRenderer3D(world, drawing, shapeRenderer)
      val worldRenderer = new WorldRenderer3D(world, patchRenderer, drawing, turtleRenderer, linkRenderer, viewManager)

      renderer = new Renderer3D(viewManager.world, viewManager.graphicsSettings, drawing, viewManager, shapeRenderer,
                                turtleRenderer, linkRenderer, patchRenderer, worldRenderer)
    } else {
      renderer.cleanUp()
      renderer = new Renderer3D(renderer)
    }
  }

  else {
    if (renderer == null) {
      val world: WorldWithWorldRenderable = viewManager.world
      val drawing: DrawingInterface = viewManager.workspace

      val shapeRenderer = new ShapeRenderer(world)
      val turtleRenderer = new TurtleRenderer(world, shapeRenderer)
      val linkRenderer = new LinkRenderer(world, shapeRenderer)
      val patchRenderer = new PatchRenderer(world, drawing, shapeRenderer)
      val worldRenderer = new WorldRenderer(world, patchRenderer, drawing, turtleRenderer, linkRenderer, viewManager)

      renderer = new Renderer(world, viewManager.graphicsSettings, drawing, viewManager, shapeRenderer, turtleRenderer,
                              linkRenderer, patchRenderer, worldRenderer)
    }
    else {
      renderer.cleanUp()
      renderer = new Renderer(renderer)
    }
  }

  val inputHandler = new MouseMotionHandler(this)
  createCanvas(viewManager.antiAliasingOn)
  setLayout(new java.awt.BorderLayout)
  add(canvas, java.awt.BorderLayout.CENTER)
  canvas.setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR))

  def updatePerspectiveLabel(): Unit = { }

  def createCanvas(antiAliasing: Boolean): Unit = {
    val capabilities = new GLCapabilities(GLProfile.get(GLProfile.GL2))
    capabilities.setSampleBuffers(antiAliasing)
    capabilities.setNumSamples(4)
    capabilities.setStencilBits(1)
    canvas = new GLJPanel(capabilities)
    canvas.addGLEventListener(renderer)
    canvas.addMouseListener(inputHandler)
    canvas.addMouseMotionListener(inputHandler)
    canvas.addMouseWheelListener(inputHandler)
    canvas.addKeyListener(new KeyInputHandler)
  }

  class KeyInputHandler extends KeyAdapter {
    override def keyPressed(e: KeyEvent): Unit = {
      if (e.getKeyCode == KeyEvent.VK_ESCAPE) {
        viewManager.setFullscreen(false)
      }
    }
  }

  override def getLinkParent = viewManager

  def updateRenderer(): Unit = {
    renderer.update()
  }

  def setVisible(): Unit = {
    setVisible(true)
    toFront()
    canvas.requestFocus()
  }

  override def setVisible(visible: Boolean): Unit = {
    if (visible)
      Analytics.threedViewOpen()

    super.setVisible(visible)
  }

  def display(): Unit = {
    canvas.display()
  }

  def invalidateTurtleShape(shape: String): Unit = {
    renderer.invalidateTurtleShape(shape)
  }

  def invalidateLinkShape(shape: String): Unit = {
    renderer.invalidateLinkShape(shape)
  }

  def resetPerspective(): Unit = {
    viewManager.world.observer.resetPerspective()
    display()
    updatePerspectiveLabel()
  }

  def exportView: BufferedImage = {
    val exporter = renderer.createExportRenderer()
    canvas.addGLEventListener(exporter)
    canvas.display()
    canvas.removeGLEventListener(exporter)
    val bufferedImage = new BufferedImage(
      exporter.getWidth, exporter.getHeight, BufferedImage.TYPE_INT_ARGB)
    bufferedImage.setRGB(0, 0, exporter.getWidth, exporter.getHeight,
                         exporter.pixelInts, 0, exporter.getWidth)
    bufferedImage
  }

  /// properties

  def editFinished(): Boolean = {
    renderer.cleanUp()
    display()
    true
  }

  def doPopup(e: MouseEvent): Unit = {
    renderer.queuePick(e.getPoint, picker)
    e.consume()
  }

  override def dispose(): Unit = {
    renderer.cleanUp()
    super.dispose()
  }
}
