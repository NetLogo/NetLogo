// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

import org.nlogo.api.World

import scala.util.Try

sealed trait ModelUpdate {
  def tag: String
}

object JobDone {
  def unapply(jd: JobDone): Option[String] = Some(jd.tag)
  def apply(tag: String): JobDone = JobFinished(tag)
}

sealed trait JobDone extends ModelUpdate

//TODO: These values don't take errors into account at the moment
case class JobFinished(tag: String) extends JobDone // finished can mean stopped or completed
case class JobHalted(tag: String) extends JobDone
case class JobErrored(tag: String, error: Exception) extends ModelUpdate
case class MonitorsUpdate(values: Map[String, Try[AnyRef]], time: Long) extends ModelUpdate {
  def tag = "~monitors-update~"
}
case class WorldUpdate(world: World, time: Long) extends ModelUpdate {
  def tag = "~world-update~"
}
