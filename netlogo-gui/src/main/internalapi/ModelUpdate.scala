// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

import org.nlogo.api.World

import scala.util.Try

sealed trait ModelUpdate {
  def tag: String
}

//TODO: These values don't take errors into account at the moment
case class JobDone(tag: String) extends ModelUpdate // done could mean halted, stopped, or completed
case class JobErrored(tag: String, error: Exception) extends ModelUpdate
case class MonitorsUpdate(values: Map[String, Try[AnyRef]], time: Long) extends ModelUpdate {
  def tag = "~monitors-update~"
}
case class WorldUpdate(world: World, time: Long) extends ModelUpdate {
  def tag = "~world-update~"
}
