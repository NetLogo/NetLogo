// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import java.awt.event.MouseEvent
import java.awt.{Font, Graphics2D, Graphics}
import java.io.{ByteArrayInputStream, DataInputStream}
import org.nlogo.api.{Perspective, Graphics2DWrapper, ViewSettings}
import org.nlogo.hubnet.mirroring._
import org.nlogo.window.{InterfaceColors, ViewMouseHandler, ViewWidgetInterface, Widget}

// The view widget in the client.
class ClientView(clientPanel: ClientPanel) extends Widget with ViewWidgetInterface with ViewSettings {
  val world = new ClientWorld()
  val renderer = new ClientRenderer(world)
  def isHeadless = false
  private var _displayOn = false
  def setDisplayOn(on: Boolean) { _displayOn = on; repaint() }

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
        g.setColor(InterfaceColors.GRAPHICS_BACKGROUND)
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
        catch {case e: java.io.IOException => org.nlogo.util.Exceptions.handle(e)}
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

  /// satisfy ViewWidgetInterface

  //Loads a view specification from file data.
  override def load(strings: Array[String], helper: Widget.LoadHelper) = {
    val Array(x1, y1, x2, y2) = strings.drop(1).take(4).map(_.toInt)
    setSize(x2 - x1, y2 - y1)
    world.viewWidth(getWidth)
    world.viewHeight(getHeight)
    world.setWorldSize(strings(17).toInt, strings(18).toInt, strings(19).toInt, strings(20).toInt)
    if (getWidth > getHeight) world.patchSize(getWidth / world.worldWidth)
    else world.patchSize(getHeight / world.worldHeight)
    this
  }

  def fontSize = world.fontSize
  def patchSize = world.patchSize
  def viewWidth = world.viewWidth
  def viewHeight = world.viewHeight
  def perspective = world.perspective
  def viewOffsetX = world.followOffsetX
  def viewOffsetY = world.followOffsetY
  def renderPerspective = true
  def drawSpotlight = world.serverMode() || (world.perspective != Perspective.Follow)
  override def save: String = throw new UnsupportedOperationException()
  def asWidget: Widget = this
  def getAdditionalHeight = 0
}
