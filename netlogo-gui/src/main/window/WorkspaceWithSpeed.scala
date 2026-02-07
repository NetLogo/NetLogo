// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.Workspace

trait WorkspaceWithSpeed extends Workspace {
  def updateManager: UpdateManagerInterface
  def speedSliderPosition(): Double
  def speedSliderPosition(speed: Double): Unit
}
