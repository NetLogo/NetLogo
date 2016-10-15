// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

// this class uses a concurrent map because it will be accessed
// from both the Job and the event thread. RG 4/21/16

import org.nlogo.core.{ AllShapesReplaced, Shape, ShapeList, ShapeEvent, ShapeRemoved, ShapeListTracker }
import scala.collection.mutable.Subscriber

import java.util.{ Map => JMap }
import java.util.concurrent.{ ConcurrentMap, ConcurrentHashMap }

class BreedShapes(val genericBreedName: String, tracker: ShapeListTracker){
  private val shapes = new ConcurrentHashMap[String, String]()

  private val subscriber = new tracker.Sub {
    override def notify(pub: tracker.Pub, evt: ShapeEvent): Unit = {
      evt match {
        case ShapeRemoved(shape: Shape, newShapeList: ShapeList) =>
          removeFromBreedShapes(shape.name)
        case AllShapesReplaced(oldShapeList: ShapeList, newShapeList: ShapeList) =>
          (oldShapeList.names -- newShapeList.names).foreach(removeFromBreedShapes)
        case _ =>
      }
    }
  }

  tracker.subscribe(subscriber)

  def setUpBreedShapes(clear: Boolean, breedsOrNull: JMap[String, AgentSet]): Unit = {
    if (clear) {
      shapes.clear()
    }

    Option(breedsOrNull).map { breeds =>
      val iter = breeds.values.iterator
      while (iter.hasNext) {
        val breedName = iter.next().printName
        val oldShape = Option(shapes.get(breedName))
        shapes.put(breedName, oldShape.getOrElse("__default"))
      }
      val oldShape = Option(shapes.get(genericBreedName))
      shapes.put(genericBreedName, oldShape.getOrElse("default"))
    }
  }

  def removeFromBreedShapes(shapeName: String): Unit = {
    if (shapes.containsValue(shapeName)) {
      val entries = shapes.entrySet.iterator
      while (entries.hasNext) {
        val entry = entries.next()
        if (entry.getValue == shapeName)
          entry.setValue("__default")
      }
    }
  }

  def breedShape(breed: AgentSet): String = {
    Option(shapes.get(breed.printName)).map(res =>
      if (res == "__default") shapes.get(genericBreedName) else res
    ).getOrElse("default")
  }

  def breedHasShape(breed: AgentSet): Boolean = {
    Option(shapes.get(breed.printName)).map(res =>
      if (res == "__default") false else true
    ).getOrElse(false)
  }

  def setBreedShape(breed: AgentSet, shapeName: String): Unit = {
    shapes.put(breed.printName, shapeName)
  }
}
