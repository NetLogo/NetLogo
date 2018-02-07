// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.util.function.{ Function => JFunction }
import org.nlogo.agent.World
import org.nlogo.core.{ Model, Shape => CoreShape, ShapeAdded, ShapeEvent, ShapeListTracker, ShapeRemoved },
  CoreShape.{ LinkShape => CoreLinkShape, VectorShape => CoreVectorShape }
import Events.UpdateModelEvent

import scala.reflect.ClassTag

class ShapeChangeListener(workspace: GUIWorkspace, world: World) extends Event.LinkChild {

  def getLinkParent = workspace

  private val turtleShapeTracker = world.turtleShapes
  private val linkShapeTracker = world.linkShapes

  private def createShapeListener[A <: CoreShape](tracker: ShapeListTracker, update: Seq[A] => JFunction[Model, Model])
  (implicit ct: ClassTag[A]): tracker.Sub = {
    new tracker.Sub {
      def notify(pub: tracker.Pub, event: ShapeEvent): Unit = {
        handleShapeEvent(event)
        val allShapes = event.newShapeList.shapes.collect {
          case v: A => v
        }
        new UpdateModelEvent(update(allShapes)).raise(ShapeChangeListener.this)
      }
    }
  }

  private val turtleListener: turtleShapeTracker.Sub =
    createShapeListener[CoreVectorShape](turtleShapeTracker, updateModelTurtleShapes _)
  turtleShapeTracker.subscribe(turtleListener)
  private val linkListener: linkShapeTracker.Sub =
    createShapeListener[CoreLinkShape](linkShapeTracker, updateModelLinkShapes _)
  linkShapeTracker.subscribe(linkListener)

  def handleShapeEvent(event: ShapeEvent): Unit = {
    event match {
      case ShapeAdded(_, oldShapeOption, _) => oldShapeOption.foreach(workspace.shapeChanged)
      case ShapeRemoved(removedShape, _) => workspace.shapeChanged(removedShape)
      case _ =>
        // note that the other cases aren't handled here as they happen only when a view refresh would happen anyway
    }
  }

  def updateModelTurtleShapes(s: Seq[CoreVectorShape])(m: Model): Model =
    m.copy(turtleShapes = s)

  def updateModelLinkShapes(s: Seq[CoreLinkShape])(m: Model): Model =
    m.copy(linkShapes = s)
}
