// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.World
import org.nlogo.core.{ ShapeEvent, ShapeAdded, ShapeRemoved }

object ShapeChangeListener {
  def listen(workspace: GUIWorkspace, world: World) {
    val turtleShapeTracker = world.turtleShapes
    val linkShapeTracker = world.linkShapes

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

    val turtleListener = new turtleShapeTracker.Sub {
      def notify(pub: turtleShapeTracker.Pub, event: ShapeEvent): Unit = {
        handleShapeEvent(event)
      }
    }

    val linkListener = new linkShapeTracker.Sub {
      def notify(pub: linkShapeTracker.Pub, event: ShapeEvent): Unit = {
        handleShapeEvent(event)
      }
    }

    turtleShapeTracker.subscribe(turtleListener)
    linkShapeTracker.subscribe(linkListener)
  }
}
