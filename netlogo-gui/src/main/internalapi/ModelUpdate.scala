// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

import org.nlogo.api.World

sealed trait ModelUpdate {
  def tag: String
}

case class JobDone(tag: String) extends ModelUpdate // done could mean halted, stopped, or completed
case class JobErrored(tag: String, error: Exception) extends ModelUpdate
case class WorldUpdate(world: World) extends ModelUpdate {
  def tag = "~world-update~"
}
