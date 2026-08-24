// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Component

trait Zoomable {
  def zoom(oldZoom: Float): Unit
}

// by default, zoom actions only affect components that are currently in the hierarchy. this helper class should be
// added to components that are removed and re-added to ensure that their zoom level is correct. parents of such
// components should call syncZoom() before displaying a potentially de-synced component. (Isaac B 8/27/26)
trait SyncZoom extends Component with Zoomable {
  private var lastZoom = 1f

  def syncZoom(): Unit = {
    if (lastZoom != Utils.getZoomFactor)
      Utils.zoomComponents(this, lastZoom)
  }

  override def zoom(oldZoom: Float): Unit = {
    lastZoom = Utils.getZoomFactor
  }
}
