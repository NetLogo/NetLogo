// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.util.{ Collection => JCollection, List => JList, Set => JSet }
import collection.JavaConverters._

object ShapeList {
  val DefaultShapeName = "default"
  def isDefaultShapeName(name: String) =
    name == DefaultShapeName
  def sortShapes(unsortedShapes: JList[Shape]): JList[Shape] =
    collection.mutable.ArrayBuffer(unsortedShapes.asScala: _*)
      .sortBy(_.getName)
      .asJava
}

class ShapeList(val kind: AgentKind, _shapes: Seq[Shape]) {

  def this(kind: AgentKind) = this(kind, Seq())

  private val shapes = collection.mutable.HashMap[String, Shape]()

  _shapes.foreach(add)

  import ShapeList._

  def shape(name: String): Shape =
    shapes.get(name).getOrElse(shapes(DefaultShapeName))

  /** Returns vector of the list of shapes available to the current model */
  def getShapes: JList[Shape] = {
    // leave out the default shape for now; we will add it later so that it is at the top of the list
    val currentShapes =
      shapes.values.toSeq.filterNot(s => isDefaultShapeName(s.getName))
    val sortedShapes = new java.util.ArrayList[Shape]
    sortedShapes.addAll(sortShapes(currentShapes.asJava))
    // make sure that the shape with the name DefaultShapeName is at the top of the list.
    sortedShapes.add(0, shapes(DefaultShapeName))
    sortedShapes
  }

  /** Returns a set of the names of all available shapes */
  def getNames: JSet[String] =
    shapes.keySet.asJava

  /** Returns true when a shape with the given name is already available to the current model */
  def exists(name: String) =
    shapes.contains(name)

  /** Clears the list of shapes currently available */
  def replaceShapes(newShapes: JCollection[Shape]) {
    shapes.clear()
    addAll(newShapes)
  }

  /** Adds a new shape to the ones currently available for use */
  def add(newShape: Shape): Shape = {
    val replaced = shapes.get(newShape.getName).orNull
    shapes(newShape.getName) = newShape
    replaced
  }

  /** Adds a collection of shapes to the ones currently available for use */
  def addAll(collection: JCollection[Shape]) {
    collection.asScala.foreach(add)
  }

  /** Removes a shape from those currently in use */
  def removeShape(shapeToRemove: Shape) = {
    val removed = shapes.get(shapeToRemove.getName).orNull
    shapes -= shapeToRemove.getName
    removed
  }

}
