// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object ShapeList {
  val DefaultShapeName = "default"
  def isDefaultShapeName(name: String) =
    name == DefaultShapeName
  def sortShapes(unsortedShapes: Seq[Shape]): Seq[Shape] =
    collection.mutable.ArrayBuffer(unsortedShapes: _*)
      .sortBy(_.name)
}

class ShapeList(val kind: AgentKind, _shapes: Seq[Shape]) {

  def this(kind: AgentKind) = this(kind, Seq())

  private val shapeMap = collection.mutable.HashMap[String, Shape]()

  _shapes.foreach(add)

  import ShapeList._

  def shape(name: String): Shape =
    shapeMap.get(name).getOrElse(shapeMap(DefaultShapeName))

  /** Returns vector of the list of shapes available to the current model */
  def shapes: Seq[Shape] =
    // make sure that the shape with the name DefaultShapeName is at the top of the list.
    shapeMap(DefaultShapeName) +:
      shapeMap.values.toSeq.filterNot(s => isDefaultShapeName(s.name)).sortBy(_.name)

  /** Returns a set of the names of all available shapes */
  def names: Set[String] =
    shapeMap.keySet.toSet

  /** Returns true when a shape with the given name is already available to the current model */
  def exists(name: String) =
    shapeMap.contains(name)

  /** Clears the list of shapes currently available */
  def replaceShapes(newShapes: Iterable[Shape]) {
    shapeMap.clear()
    addAll(newShapes)
  }

  /** Adds a new shape to the ones currently available for use */
  def add(newShape: Shape): Shape = {
    val replaced = shapeMap.get(newShape.name).orNull
    shapeMap(newShape.name) = newShape
    replaced
  }

  /** Adds a collection of shapes to the ones currently available for use */
  def addAll(collection: Iterable[Shape]) =
    collection.foreach(add)

  /** Removes a shape from those currently in use */
  def removeShape(shapeToRemove: Shape) =
    shapeMap.remove(shapeToRemove.name).orNull
}
