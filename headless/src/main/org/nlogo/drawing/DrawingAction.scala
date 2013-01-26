package org.nlogo.drawing

import org.nlogo.api.Action

sealed trait DrawingAction extends Action

object DrawingAction {

  // TODO: these will need to be serialized, and I should take
  // take into account wrt the case class members
  // That means replacing api.Agent with an AgentKey,
  // and penColor needs to be addressed too

  case class DrawLine(
    x1: Double, y1: Double, x2: Double, y2: Double,
    penColor: AnyRef, penSize: Double, penMode: String)
    extends DrawingAction
  case class SetColors(colors: Array[Int]) extends DrawingAction
  case class SendPixels(dirty: Boolean) extends DrawingAction
  case class Stamp(agentKind: String, agentId: Long, erase: Boolean) extends DrawingAction
  case class CreateDrawing(dirty: Boolean) extends DrawingAction
  case class ClearDrawing() extends DrawingAction
  case class RescaleDrawing() extends DrawingAction
  case class MarkClean() extends DrawingAction
  case class MarkDirty() extends DrawingAction
}
