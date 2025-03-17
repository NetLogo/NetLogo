// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.awt.GLCanvas

import java.awt.Frame
import java.awt.event.{ KeyEvent, KeyAdapter, MouseEvent }
import java.awt.image.BufferedImage

import org.nlogo.api.Version
import org.nlogo.gl.render.Renderer
import org.nlogo.swing.{ NetLogoIcon, Utils }
import org.nlogo.theme.ThemeSync
import org.nlogo.window.Event.LinkChild

abstract class View(title: String, val viewManager: ViewManager, var renderer: Renderer)
  extends Frame(title) with LinkChild with ThemeSync with NetLogoIcon {

  var canvas: GLCanvas = null
  val picker = new Picker(this)

  if (Version.is3D) {
    if (renderer == null) {
      renderer = new org.nlogo.gl.render.Renderer3D(
        viewManager.world, viewManager.graphicsSettings,
        viewManager.workspace, viewManager, Utils.getUIScale)
    } else {
      renderer.cleanUp()
      renderer = new org.nlogo.gl.render.Renderer3D(renderer)
    }
  }

  else {
    if (renderer == null) {
      renderer = new Renderer(
        viewManager.world, viewManager.graphicsSettings,
        viewManager.workspace, viewManager, Utils.getUIScale)
    }
    else {
      renderer.cleanUp()
      renderer = new org.nlogo.gl.render.Renderer(renderer)
    }
  }

  val inputHandler = new MouseMotionHandler(this)
  createCanvas(viewManager.antiAliasingOn)
  setLayout(new java.awt.BorderLayout)
  add(canvas, java.awt.BorderLayout.CENTER)
  canvas.setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR))

  def updatePerspectiveLabel() { }

  def createCanvas(antiAliasing: Boolean) {
    val capabilities = new com.jogamp.opengl.GLCapabilities(GLProfile.get(GLProfile.GL2))
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

  def resetPerspective() {
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

  def doPopup(e: MouseEvent) {
    renderer.queuePick(e.getPoint, picker)
    e.consume()
  }

  override def dispose(): Unit = {
    renderer.cleanUp()
    super.dispose()
  }
}
