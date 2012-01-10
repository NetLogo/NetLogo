// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import javax.media.opengl.GLCanvas

import org.nlogo.gl.render.Renderer
import java.awt.image.BufferedImage
import java.awt.event.{ KeyEvent, KeyAdapter, MouseEvent }

abstract class View(title: String, val viewManager: ViewManager, var renderer: Renderer)
extends java.awt.Frame(title)
with org.nlogo.window.Event.LinkChild {

  var canvas: javax.media.opengl.GLCanvas = null
  val picker = new Picker(this)

  if (org.nlogo.api.Version.is3D)
    if (renderer == null)
      renderer = new org.nlogo.gl.render.Renderer3D(
        viewManager.world, viewManager.graphicsSettings,
        viewManager.workspace, viewManager)
    else {
      renderer.cleanUp()
      renderer = new org.nlogo.gl.render.Renderer3D(renderer)
    }
  else if (renderer == null) {
    renderer = new Renderer(
      viewManager.world, viewManager.graphicsSettings,
      viewManager.workspace, viewManager)
  }
  else {
    renderer.cleanUp()
    renderer = new org.nlogo.gl.render.Renderer(renderer)
  }

  val inputHandler = new MouseMotionHandler(this)
  createCanvas(viewManager.antiAliasingOn)
  setLayout(new java.awt.BorderLayout)
  add(canvas, java.awt.BorderLayout.CENTER)
  canvas.setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR))

  def updatePerspectiveLabel() { }

  def createCanvas(antiAliasing: Boolean) {
    val capabilities = new javax.media.opengl.GLCapabilities
    capabilities.setSampleBuffers(antiAliasing)
    capabilities.setNumSamples(4)
    capabilities.setStencilBits(1)
    canvas = new GLCanvas(capabilities)
    canvas.addGLEventListener(renderer)
    canvas.addMouseListener(inputHandler)
    canvas.addMouseMotionListener(inputHandler)
    canvas.addMouseWheelListener(inputHandler)
    canvas.addKeyListener(new KeyInputHandler)
  }

  class KeyInputHandler extends KeyAdapter {
    override def keyPressed(e: KeyEvent) {
      if (e.getKeyCode == KeyEvent.VK_ESCAPE) {
        viewManager.setFullscreen(false)
      }
    }
  }

  override def getLinkParent = viewManager

  def updateRenderer() {
    renderer.update()
  }

  def setVisible() {
    super.setVisible(true)
    toFront()
    canvas.requestFocus()
  }

  def display() {
    canvas.display()
  }

  def invalidateTurtleShape(shape: String) {
    renderer.invalidateTurtleShape(shape)
  }

  def invalidateLinkShape(shape: String) {
    renderer.invalidateLinkShape(shape)
  }

  def signalViewUpdate() {
    canvas.repaint()
  }

  def resetPerspective() {
    viewManager.world.observer.resetPerspective()
    signalViewUpdate()
    updatePerspectiveLabel()
  }

  def exportView: BufferedImage = {
    val exporter = renderer.createExportRenderer()
    canvas.addGLEventListener(exporter)
    canvas.display()
    canvas.removeGLEventListener(exporter)
    val bufferedImage = new BufferedImage(
      canvas.getWidth, canvas.getHeight, BufferedImage.TYPE_INT_ARGB)
    bufferedImage.setRGB(0, 0, canvas.getWidth, canvas.getHeight,
                         exporter.pixelInts, 0, canvas.getWidth)
    bufferedImage
  }

  /// properties

  def editFinished(): Boolean = {
    renderer.cleanUp()
    display()
    true
  }

  def doPopup(e: MouseEvent) {
    renderer.queuePick(e.getPoint, picker)
    e.consume()
  }

}
