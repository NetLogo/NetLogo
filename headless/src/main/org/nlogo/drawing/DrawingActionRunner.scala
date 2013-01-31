// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.drawing

import org.nlogo.api
import DrawingAction._
import scala.collection.JavaConverters._
class DrawingActionRunner(
  val trailDrawer: api.TrailDrawerInterface,
  val world: api.World)
  extends api.ActionRunner[DrawingAction] {

  override def run(action: DrawingAction) = action match {
    case DrawLine(x1, y1, x2, y2, penColor, penSize, penMode) =>
      trailDrawer.drawLine(x1, y1, x2, y2, penColor, penSize, penMode)
    case SetColors(colors) =>
      trailDrawer.setColors(colors)
    case SendPixels(dirty) =>
      trailDrawer.sendPixels(dirty)
    case Stamp(agentKind, agentId, erase) => {
      val agentSet = agentKind match {
        case "Turtle" => world.turtles
        case "Link"   => world.links
      }
      for (agent <- agentSet.agents.asScala.find(_.id == agentId))
        trailDrawer.stamp(agent, erase)
    }
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
