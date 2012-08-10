// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape

import org.nlogo.api.{ GraphicsInterface, Shape }
import java.awt.Color
import java.io.IOException

trait ShapesManagerInterface {
  def reset(): Unit
  def init(name: String): Unit
}

trait TurtleShapesManagerInterface extends ShapesManagerInterface

trait LinkShapesManagerInterface extends ShapesManagerInterface

trait DrawableShape {
  def isRotatable: Boolean
  def paint(g: GraphicsInterface, color: Color, x: Int, y: Int, cellSize: Double, angle: Int)
  def getEditableColorIndex: Int
}

trait ModelSectionReader {
  @throws(classOf[IOException])
  def read(path: String): Array[String]
  @throws(classOf[IOException])
  def getVersion(path: String): String
}

trait ShapeChangeListener {
  def shapeChanged(shape: Shape): Unit
  def shapeRemoved(shape: Shape): Unit
}

class InvalidShapeDescriptionException extends Exception
