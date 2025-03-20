// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import java.awt.{ Font, Graphics2D, Graphics }
import java.awt.event.MouseEvent
import java.io.{ ByteArrayInputStream, DataInputStream }
import javax.swing.border.LineBorder

import org.nlogo.api.{ Perspective, Graphics2DWrapper, ViewSettings }
import org.nlogo.core.{ View => CoreView }
import org.nlogo.hubnet.mirroring._
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.{ ViewMouseHandler, ViewWidgetInterface, Widget }

// The view widget in the client.
class ClientView(clientPanel: ClientPanel) extends Widget with ViewWidgetInterface with ViewSettings {

  type WidgetModel = CoreView

  val world = new ClientWorld()
  val renderer = new ClientRenderer(world)
  def isHeadless = false
  private var _displayOn = false
  def setDisplayOn(on: Boolean) { _displayOn = on; repaint() }

  setBackground(InterfaceColors.Transparent)

  locally {
    world.setTrailDrawer(renderer.trailDrawer())
    val mouser = new ViewMouseHandler(this, world, this) {
      override def mousePressed(e: MouseEvent) {
        super.mousePressed(e)
        if (_displayOn && mouseInside) clientPanel.sendMouseMessage(mouseXCor, mouseYCor, true)
      }
      override def mouseReleased(e: MouseEvent) {
        super.mouseReleased(e)
        if (_displayOn && mouseInside) clientPanel.sendMouseMessage(mouseXCor, mouseYCor, false)
      }
    }
    addMouseListener(mouser)
    addMouseMotionListener(mouser)
  }

  // PAINTING
  override def paintComponent(g: Graphics) {
    this.synchronized {
      setFontSize(g)
      if (!_displayOn || world == null) {
        g.setColor(InterfaceColors.viewBackground)
        g.fillRect(0, 0, getWidth, getHeight)
      }
      else {
        g.setClip(0, 0, getWidth, getHeight)
        world.applyOverrides()
        renderer.paint(new Graphics2DWrapper(g.asInstanceOf[Graphics2D]), this)
        world.rollbackOverrides()
      }
    }
  }

  private def setFontSize(g: Graphics) {
    val font = g.getFont()
    g.setFont(new Font(font.getName(), font.getStyle(), world.fontSize()))
  }

  //Updates the world and draws it.
  def updateDisplay(worldData: Array[Byte]) {
    this.synchronized {
      if (world != null) {
        try {
          world.updateFrom(new java.io.DataInputStream(new java.io.ByteArrayInputStream(worldData)))
          renderer.changeTopology(world.wrappingAllowedInX(), world.wrappingAllowedInY())
          renderer.resetCache(patchSize)
          _displayOn=true
        }
        catch {case e: java.io.IOException => org.nlogo.api.Exceptions.handle(e)}
        repaint()
      }
    }
  }

  def handleOverrideList(list: OverrideList, clear: Boolean) {
    if (clear) world.updateOverrides(list.asInstanceOf[ClearOverride])
    else world.updateOverrides(list.asInstanceOf[SendOverride])
    if (_displayOn) repaint()
  }

  def clearOverrides() {
    world.clearOverrides()
    if (_displayOn) repaint()
  }

  def handleAgentPerspective(data: Array[Byte]) {
    world.updateClientPerspective(new AgentPerspective(new DataInputStream(new ByteArrayInputStream(data))))
    if (_displayOn) repaint()
  }

  override def syncTheme(): Unit = {
    setBorder(new LineBorder(InterfaceColors.viewBorder, 2))
  }

  /// satisfy ViewWidgetInterface

  override def load(view: WidgetModel): AnyRef = {
    setBounds(view.x, view.y, view.width, view.height)
    world.viewWidth(getWidth)
    world.viewHeight(getHeight)
    world.setWorldSize(
      view.dimensions.minPxcor, view.dimensions.maxPxcor,
      view.dimensions.minPycor, view.dimensions.maxPycor)
    if (getWidth > getHeight) world.patchSize(getWidth.toDouble / world.worldWidth)
    else world.patchSize(getHeight.toDouble / world.worldHeight)
    this
  }

  override def model: WidgetModel = {
    val b = getUnzoomedBounds
    CoreView(
      x = b.x, y = b.y, width = b.width, height = b.height,
      dimensions = world.getDimensions)
  }

  def fontSize = world.fontSize
  def patchSize = world.patchSize
  def viewWidth = world.viewWidth
  def viewHeight = world.viewHeight
  def perspective = world.perspective
  def viewOffsetX = world.followOffsetX
  def viewOffsetY = world.followOffsetY
  def renderPerspective = true
  def drawSpotlight = world.serverMode() || world.perspective.isInstanceOf[Perspective.Follow]
  def getAdditionalHeight = 0
}
