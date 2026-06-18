// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.bsapp

import com.jogamp.opengl.{ GLCapabilities, GLProfile }
import com.jogamp.opengl.awt.GLJPanel

import java.awt.{ BorderLayout, Cursor, Dimension, Frame }
import java.awt.event.{ KeyAdapter, KeyEvent, MouseEvent }

import org.nlogo.agent.{ World, World3D }
import org.nlogo.api.{ DrawingInterface, WorldRenderable }
import org.nlogo.awt.Positioning
import org.nlogo.core.I18N
import org.nlogo.gl.render.{ GLViewSettings, LinkRenderer3D, PatchRenderer3D, Renderer3D, ShapeRenderer3D,
                             TurtleRenderer3D, WorldRenderer3D }
import org.nlogo.gl.view.{ GLViewInterface, MouseMotionHandler, ViewControlToolBar }
import org.nlogo.render.Renderer
import org.nlogo.swing.NetLogoIcon
import org.nlogo.theme.ThemeSync

class View3D(workspace: SemiHeadlessWorkspace) extends Frame(I18N.gui.get("menu.tools.3DView")) with GLViewInterface
                                               with GLViewSettings with NetLogoIcon with ActiveView with ThemeSync {

  private val world3D: World3D & WorldRenderable = world.asInstanceOf[World3D & WorldRenderable]
  private val drawing: DrawingInterface = new Renderer(world).trailDrawer

  private val shapeRenderer = new ShapeRenderer3D(world3D)
  private val turtleRenderer = new TurtleRenderer3D(world3D, shapeRenderer)
  private val linkRenderer = new LinkRenderer3D(world3D, shapeRenderer)
  private val patchRenderer = new PatchRenderer3D(world3D, drawing, shapeRenderer)
  private val worldRenderer = new WorldRenderer3D(world3D, patchRenderer, drawing, turtleRenderer, linkRenderer, this)

  override val renderer = new Renderer3D(world, workspace, drawing, this, shapeRenderer, turtleRenderer, linkRenderer,
                                         patchRenderer, worldRenderer)

  private val inputHandler = new MouseMotionHandler(this)

  private val capabilities = new GLCapabilities(GLProfile.get(GLProfile.GL2)) {
    setSampleBuffers(true)
    setNumSamples(4)
    setStencilBits(1)
  }

  private val canvas = new GLJPanel(capabilities) {
    setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR))

    addGLEventListener(renderer)
    addMouseListener(inputHandler)
    addMouseMotionListener(inputHandler)
    addMouseWheelListener(inputHandler)

    addKeyListener(new KeyAdapter {
      override def keyPressed(e: KeyEvent): Unit = {
        if (e.getKeyCode == KeyEvent.VK_ESCAPE)
          setFullscreen(false)
      }
    })
  }

  private val toolBar = new ViewControlToolBar(this, inputHandler)

  setLayout(new BorderLayout)

  add(canvas, BorderLayout.CENTER)
  add(toolBar, BorderLayout.SOUTH)

  setSize(getPreferredSize)
  syncTheme()

  override def setVisible(visible: Boolean): Unit = {
    super.setVisible(visible)

    if (visible)
      Positioning.moveNextTo(this, workspace.getFrame)
  }

  override def display(): Unit = {
    canvas.display()
  }

  override def paintView(): Unit = {
    if (!isVisible)
      setVisible(true)

    display()
  }

  override def disable(): Unit = {
    setVisible(false)
  }

  override def world: World =
    workspace.world

  override def doPopup(e: MouseEvent): Unit = {}

  override def wireframeOn: Boolean =
    true

  override def resetPerspective(): Unit = {
    world.observer.resetPerspective()
    display()
  }

  override def setFullscreen(fullscreen: Boolean): Unit = {
    if (fullscreen && !isUndecorated) {
      dispose()
      setExtendedState(Frame.MAXIMIZED_BOTH)
      setUndecorated(true)
      setVisible(true)
    } else if (!fullscreen && isUndecorated) {
      dispose()
      setExtendedState(Frame.NORMAL)
      setUndecorated(false)
      setSize(getPreferredSize)
      setVisible(true)
    }
  }

  override def getPreferredSize: Dimension =
    new Dimension(600, 600)

  override def syncTheme(): Unit = {
    toolBar.syncTheme()
  }
}
