// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait HubNetWorkspaceInterface {
  def getPropertiesInterface: WorldPropertiesInterface
  def hubNetRunning_=(running: Boolean)
  def modelNameForDisplay: String
  def compilerServices: CompilerServices
}
