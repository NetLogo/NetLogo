// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.drawing

import org.nlogo.api.Action

sealed trait DrawingAction extends Action

object DrawingAction {

  case class DrawLine(
    x1: Double, y1: Double, x2: Double, y2: Double,
    penColor: AnyRef, penSize: Double, penMode: String)
    extends DrawingAction

  // Previously, `ReadImage` was all that was used for stamping, and that was fine for Model Runs.
  // However, NetLogo Web needs to be generating updates that match (or are comparable to) updates
  // from Model Runs.  Passing around image bytes is (more or less) impossible in NetLogo Web, so
  // I've added some fields that match up to with how NetLogo Web will handle these stamping events.
  // --JAB (3/18/15)
  case class StampImage(imageBytes: Array[Byte], stamp: AgentStamp)      extends DrawingAction
  case class EraseStampImage(imageBytes: Array[Byte], stamp: AgentStamp) extends DrawingAction
  case class SetColors(colors: Array[Int])                               extends DrawingAction
  case class SendPixels(dirty: Boolean)                                  extends DrawingAction
  case class ReadImage(imageBytes: Array[Byte])                          extends DrawingAction
  case class CreateDrawing(dirty: Boolean)                               extends DrawingAction
  case class ImportDrawing(filePath: String)                             extends DrawingAction // TODO: store actual image
  case object ClearDrawing                                               extends DrawingAction
  case object RescaleDrawing                                             extends DrawingAction
  case object MarkClean                                                  extends DrawingAction
  case object MarkDirty                                                  extends DrawingAction

}

sealed trait AgentStamp

case class TurtleStamp(
  x: Double, y: Double, size: Double, heading: Double,
  color: AnyRef, shapeName: String, stampMode: String) extends AgentStamp

case class LinkStamp(
  x1: Double, y1: Double, x2: Double, y2: Double, midpointX: Double, midpointY: Double,
  heading: Double, color: AnyRef, shapeName: String, thickness: Double,
  isDirected: Boolean, size: Double, isHidden: Boolean, stampMode: String) extends AgentStamp
