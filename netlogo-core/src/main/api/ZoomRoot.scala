// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait ZoomRoot {
  def zoomIn(): Unit
  def zoomOut(): Unit
  def resetZoom(): Unit
}
