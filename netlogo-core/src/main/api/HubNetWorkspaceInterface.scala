// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait HubNetWorkspaceInterface {
  def getPropertiesInterface: WorldPropertiesInterface
  def hubNetRunning: Boolean
  def hubNetRunning_=(running: Boolean): Unit
  def modelNameForDisplay: String
}
