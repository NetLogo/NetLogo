// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object Shape {
  val Width = 300
  type RgbColor = org.nlogo.core.RgbColor
  object RgbColor {
    def apply(red: Int, green: Int, blue: Int, alpha: Int = 255) =
      org.nlogo.core.RgbColor(red, green, blue, alpha)
  }

  trait Element {
    def filled: Boolean
    def marked: Boolean
    def color: RgbColor
  }

  trait Circle extends Element {
    def color: RgbColor
    def filled: Boolean
    def marked: Boolean
    def x: Int
    def y: Int
    def diameter: Int
  }

  trait Line extends Element {
    def color: RgbColor
    def marked: Boolean
    def startPoint: (Int, Int)
    def endPoint: (Int, Int)
    def filled: Boolean
  }

  trait Rectangle extends Element {
    def color: RgbColor
    def filled: Boolean
    def marked: Boolean
    def upperLeftCorner: (Int, Int)
    def lowerRightCorner: (Int, Int)
    def getX: Int = upperLeftCorner._1
    def getY: Int = upperLeftCorner._2
    def getWidth: Int = lowerRightCorner._1 - upperLeftCorner._1
    def getHeight: Int = lowerRightCorner._2 - upperLeftCorner._2
  }

  trait Polygon extends Element {
    def color: RgbColor
    def filled: Boolean
    def marked: Boolean
    def points: Seq[(Int, Int)]
    def xCoords: Seq[Int] = points.map(_._1)
    def yCoords: Seq[Int] = points.map(_._2)
  }

  trait LinkLine {
    def xcor: Double
    def isVisible: Boolean
    def dashChoices: Seq[Float]
  }

  object LinkLine {
    val dashChoices = Set[Seq[Float]](
      Seq(0.0f, 1.0f),
      Seq(1.0f, 0.0f),
      Seq(2.0f, 2.0f),
      Seq(4.0f, 4.0f),
      Seq(4.0f, 4.0f, 2.0f, 2.0f)
    )
  }

  trait VectorShape extends Shape {
    def name: String
    def rotatable: Boolean
    def editableColorIndex: Int
    def elements: Seq[Element]
  }

  trait LinkShape extends Shape {
    def name: String
    def curviness: Double
    def linkLines: Seq[LinkLine]
    def indicator: VectorShape
  }
}

// so that org.nlogo.shape doesn't need to depend on org.nlogo.agent
sealed trait Shape {
  def name: String
  def name_=(s: String): Unit
}
