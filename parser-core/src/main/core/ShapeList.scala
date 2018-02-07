// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import java.util.{ Collection => JCollection }
import scala.collection.mutable.Publisher
import scala.collection.JavaConverters._

object ShapeList {
  val DefaultShapeName = "default"
  def isDefaultShapeName(name: String) =
    name == DefaultShapeName
  def sortShapes(unsortedShapes: Seq[Shape]): Seq[Shape] =
    collection.mutable.ArrayBuffer(unsortedShapes: _*)
      .sortBy(_.name)
  def shapesToMap(collection: Iterable[Shape]): Map[String, Shape] =
    collection.map(s => (s.name -> s)).toMap
}

import ShapeList._

case class ShapeList(kind: AgentKind, shapeMap: Map[String, Shape]) {
  def get(name: String): Option[Shape] = shapeMap.get(name)
  def remove(shape: Shape): ShapeList = copy(shapeMap = shapeMap.filterNot(_._2 == shape))

  def isEmpty: Boolean = shapeMap.isEmpty

  def shape(name: String): Shape =
    shapeMap.get(name).getOrElse(shapeMap(DefaultShapeName))

  /** Returns vector of the list of shapes available to the current model */
  def shapes: Seq[Shape] =
    // make sure that the shape with the name DefaultShapeName is at the top of the list.
    shapeMap.get(DefaultShapeName).toSeq ++
      shapeMap.values.toSeq.filterNot(s => isDefaultShapeName(s.name)).sortBy(_.name)

  /** Returns a set of the names of all available shapes */
  def names: Set[String] = shapeMap.keySet.toSet

  /** Returns true when a shape with the given name is already available to the current model */
  def exists(name: String): Boolean = shapeMap.contains(name)
}

sealed trait ShapeEvent {
  def newShapeList: ShapeList
}

case class ShapeAdded(newShape: Shape, oldValue: Option[Shape], newShapeList: ShapeList) extends ShapeEvent
case class ShapesAdded(addedShapes: Map[String, Shape], newShapeList: ShapeList) extends ShapeEvent
case class AllShapesReplaced(oldShapeList: ShapeList, newShapeList: ShapeList) extends ShapeEvent
case class ShapeRemoved(removedShape: Shape, newShapeList: ShapeList) extends ShapeEvent

class ShapeListTracker(private var _shapeList: ShapeList) extends Publisher[ShapeEvent] {
  def this(kind: AgentKind, map: Map[String, Shape]) = this(ShapeList(kind, map))
  def this(kind: AgentKind) = this(ShapeList(kind, Map()))

  def shapeList: ShapeList = _shapeList

  def add(newShape: Shape): Unit = {
    val removed = _shapeList.get(newShape.name)
    _shapeList = _shapeList.copy(shapeMap = _shapeList.shapeMap.updated(newShape.name, newShape))
    publish(ShapeAdded(newShape, removed, _shapeList))
  }

  def addAll(collection: Iterable[Shape]): Unit = {
    val newMap = shapesToMap(collection)
    _shapeList = _shapeList.copy(shapeMap = _shapeList.shapeMap ++ newMap)
    publish(ShapesAdded(newMap, _shapeList))
  }

  def replaceShapes(newShapes: Iterable[Shape]): Unit = {
    val newMap = shapesToMap(newShapes)
    val oldShapeList = _shapeList.copy()
    _shapeList = _shapeList.copy(shapeMap = newMap)
    publish(AllShapesReplaced(oldShapeList, _shapeList))
  }

  def replaceShapes(newShapes: JCollection[_ <: Shape]): Unit = {
    replaceShapes(newShapes.asScala)
  }

  def removeShape(shapeToRemove: Shape): Unit = {
    _shapeList = _shapeList.remove(shapeToRemove)
    publish(ShapeRemoved(shapeToRemove, _shapeList))
  }
}
