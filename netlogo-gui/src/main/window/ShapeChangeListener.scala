// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.World
import org.nlogo.core.{ ShapeEvent, ShapeAdded, ShapeRemoved }

class ShapeChangeListener(workspace: GUIWorkspace, world: World) {
  private val turtleShapeTracker = world.turtleShapes
  private val linkShapeTracker = world.linkShapes
  private val turtleListener = new turtleShapeTracker.Sub {
    def notify(pub: turtleShapeTracker.Pub, event: ShapeEvent): Unit = {
      handleShapeEvent(event)
    }
  }

  private val linkListener = new linkShapeTracker.Sub {
    def notify(pub: linkShapeTracker.Pub, event: ShapeEvent): Unit = {
      handleShapeEvent(event)
    }
  }

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
  turtleShapeTracker.subscribe(turtleListener)
  linkShapeTracker.subscribe(linkListener)
}
