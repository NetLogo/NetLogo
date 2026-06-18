// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.view

import java.awt.Frame
import java.awt.event.MouseEvent

import org.nlogo.agent.World
import org.nlogo.gl.render.Renderer

trait GLViewInterface extends Frame {
  var warned = false

  def world: World
  def renderer: Renderer
  def display(): Unit
  def doPopup(e: MouseEvent): Unit
  def resetPerspective(): Unit
  def setFullscreen(fullscreen: Boolean): Unit
}
