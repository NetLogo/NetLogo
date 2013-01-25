package org.nlogo.drawing

import org.nlogo.api.TrailDrawerInterface
import DrawingAction._
import org.nlogo.api.ActionRunner

class DrawingActionRunner(val trailDrawer: TrailDrawerInterface)
  extends ActionRunner[DrawingAction] {

  override def run(action: DrawingAction) = action match {
    case DrawLine(x1, y1, x2, y2, penColor, penSize, penMode) =>
      trailDrawer.drawLine(x1, y1, x2, y2, penColor, penSize, penMode)
    case SetColors(colors) =>
      trailDrawer.setColors(colors)
    case SendPixels(dirty) =>
      trailDrawer.sendPixels(dirty)
    case Stamp(agent, erase) =>
      trailDrawer.stamp(agent, erase)
    case CreateDrawing(dirty: Boolean) =>
      trailDrawer.getAndCreateDrawing(dirty)
    case ClearDrawing() =>
      trailDrawer.clearDrawing()
    case RescaleDrawing() =>
      trailDrawer.rescaleDrawing()
    case MarkClean() =>
      trailDrawer.markClean()
    case MarkDirty() =>
      trailDrawer.markDirty()
  }

}
