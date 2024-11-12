// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape

import java.awt.Color
import java.io.IOException

import org.nlogo.api.GraphicsInterface

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

class InvalidShapeDescriptionException extends IOException
