// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait HubNetWorkspaceInterface extends CompilerServices with DisplayModelName {
  def getPropertiesInterface: WorldPropertiesInterface
  def hubNetRunning_=(running: Boolean): Unit
}
