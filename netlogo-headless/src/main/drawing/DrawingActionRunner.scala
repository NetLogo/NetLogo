// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.drawing

import org.nlogo.api
import DrawingAction._

class DrawingActionRunner(
  val trailDrawer: api.TrailDrawerInterface)
  extends api.ActionRunner[DrawingAction] {

  override def run(action: DrawingAction) = action match {
    case DrawLine(x1, y1, x2, y2, penColor, penSize, penMode) =>
      trailDrawer.drawLine(x1, y1, x2, y2, penColor, penSize, penMode)
    case SetColors(colors) =>
      trailDrawer.setColors(colors)
    case SendPixels(dirty) =>
      trailDrawer.sendPixels(dirty)
    case ReadImage(imageBytes) =>
      readBytes(imageBytes)
    case StampImage(imageBytes, _) =>
      readBytes(imageBytes)
    case EraseStampImage(imageBytes, _) =>
      readBytes(imageBytes)
    case CreateDrawing(dirty: Boolean) =>
      trailDrawer.getAndCreateDrawing(dirty)
    case ImportDrawing(filePath: String) =>
      trailDrawer.importDrawing(new api.LocalFile(filePath))
    case ClearDrawing =>
      trailDrawer.clearDrawing()
    case RescaleDrawing =>
      trailDrawer.rescaleDrawing()
    case MarkClean =>
      trailDrawer.markClean()
    case MarkDirty =>
      trailDrawer.markDirty()
  }

  private def readBytes(imageBytes: Array[Byte]): Unit = {
    val inputStream = new java.io.ByteArrayInputStream(imageBytes)
    trailDrawer.readImage(inputStream)
  }

}
