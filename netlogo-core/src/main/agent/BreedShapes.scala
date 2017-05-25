// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

// this class uses a concurrent map because it will be accessed
// from both the Job and the event thread. RG 4/21/16
import scala.collection.immutable.ListMap
import scala.collection.JavaConverters._

import org.nlogo.core.{ AllShapesReplaced, Breed, Shape, ShapeList, ShapeEvent, ShapeRemoved, ShapeListTracker }

import java.util.{ Map => JMap, HashMap => JHashMap }
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

  def setUpBreedShapes(clear: Boolean, breeds: Map[String, Breed]): Unit = {
    if (clear) {
      shapes.clear()
    }

    for {
      (_, breed) <- breeds
    } {
      shapes.put(breed.name, shapes.getOrDefault(breed.name, "__default"))
    }
    shapes.put(genericBreedName, shapes.getOrDefault(genericBreedName, "default"))
  }

  def setUpBreedShapes(clear: Boolean, breedsOrNull: JMap[String, _ <: AgentSet]): Unit = {
    if (clear) {
      shapes.clear()
    }

    for {
      breeds <- Option(breedsOrNull)
      (_, breed) <- breeds.asScala
    } {
      shapes.put(breed.printName, shapes.getOrDefault(breed.printName, "__default"))
    }
    shapes.put(genericBreedName, shapes.getOrDefault(genericBreedName, "default"))
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
