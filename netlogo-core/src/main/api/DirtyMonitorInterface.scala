// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait DirtyMonitorInterface {
  def setDirty(dirty: Boolean, path: Option[String] = None): Unit
}
