package org.nlogo.api

trait HubNetWorkspaceInterface extends CompilerServices {
  def getPropertiesInterface: WorldPropertiesInterface
  def hubNetRunning(running: Boolean)
  def modelNameForDisplay: String
}
