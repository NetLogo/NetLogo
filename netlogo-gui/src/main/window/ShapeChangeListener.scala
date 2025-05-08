// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.World
import org.nlogo.core.{ ShapeEvent, ShapeAdded, ShapeRemoved }

object ShapeChangeListener {
  def listen(workspace: GUIWorkspace, world: World): Unit = {
    def handleShapeEvent(event: ShapeEvent): Unit = {
      event match {
        case ShapeAdded(newShape, oldShapeOption, _) =>
          oldShapeOption.foreach(workspace.shapeChanged)
          workspace.shapeChanged(newShape)
        case ShapeRemoved(removedShape, _) => workspace.shapeChanged(removedShape)
        case _ =>
          // note that the other cases aren't handled here as they happen only when a view refresh would happen anyway
      }
    }

    world.turtleShapes.subscribe(handleShapeEvent)
    world.linkShapes.subscribe(handleShapeEvent)
  }
}
