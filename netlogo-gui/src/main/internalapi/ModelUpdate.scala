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
case class UpdateSuccess(update: UpdateVariable) extends ModelUpdate {
  def tag = "~world.variable.updated.success"
}
case class UpdateFailure(update: UpdateVariable, actualValue: AnyRef) extends ModelUpdate {
  def tag = "~world.variable.updated.failure"
}
case class UpdateError(update: UpdateVariable, error: AnyRef) extends ModelUpdate {
  def tag = "~world.variable.updated.error"
}
case class MonitorsUpdate(values: Map[String, Try[AnyRef]], time: Long) extends ModelUpdate {
  def tag = "~monitors.update"
}
case object TicksStarted extends ModelUpdate {
  def tag = "~world.ticks.started"
}
case object TicksCleared extends ModelUpdate {
  def tag = "~world.ticks.cleared"
}
case class WorldUpdate(world: World, time: Long) extends ModelUpdate {
  def tag = "~world.update"
}
case class RequestUIAction(request: UIRequest) extends ModelUpdate {
  def tag = "~ui.request"
}
