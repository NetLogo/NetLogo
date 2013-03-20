// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.drawing

import org.nlogo.api.Action

sealed trait DrawingAction extends Action

object DrawingAction {

  case class DrawLine(
    x1: Double, y1: Double, x2: Double, y2: Double,
    penColor: AnyRef, penSize: Double, penMode: String)
    extends DrawingAction
  case class SetColors(colors: Array[Int]) extends DrawingAction
  case class SendPixels(dirty: Boolean) extends DrawingAction
  case class ReadImage(imageBytes: Array[Byte]) extends DrawingAction
  case class CreateDrawing(dirty: Boolean) extends DrawingAction
  case class ImportDrawing(filePath: String) extends DrawingAction // TODO: store actual image
  case class ClearDrawing() extends DrawingAction
  case class RescaleDrawing() extends DrawingAction
  case class MarkClean() extends DrawingAction
  case class MarkDirty() extends DrawingAction
}
