// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object Shape {
  val Width = 300
  case class RgbColor(red: Int, green: Int, blue: Int, alpha: Int = 255) {
    override def equals(other: Any) = {
      other match {
        case color: RgbColor =>
          red == color.red && green == color.green && blue == color.blue && alpha == color.alpha
        
        case _ => false
      }
    }
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

    override def equals(other: Any) = {
      other match {
        case circle: Circle =>
          color == circle.color && filled == circle.filled && marked == circle.marked && x == circle.x &&
            y == circle.y && diameter == circle.diameter

        case _ => false
      }
    }
  }

  trait Line extends Element {
    def color: RgbColor
    def marked: Boolean
    def startPoint: (Int, Int)
    def endPoint: (Int, Int)
    def filled: Boolean

    override def equals(other: Any) = {
      other match {
        case line: Line =>
          color == line.color && marked == line.marked && startPoint == line.startPoint && endPoint == line.endPoint &&
            filled == line.filled
        
        case _ => false
      }
    }
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

    override def equals(other: Any) = {
      other match {
        case rectangle: Rectangle =>
          color == rectangle.color && filled == rectangle.filled && marked == rectangle.marked &&
            upperLeftCorner == rectangle.upperLeftCorner && lowerRightCorner == rectangle.lowerRightCorner
        
        case _ => false
      }
    }
  }

  trait Polygon extends Element {
    def color: RgbColor
    def filled: Boolean
    def marked: Boolean
    def points: Seq[(Int, Int)]
    def xCoords: Seq[Int] = points.map(_._1)
    def yCoords: Seq[Int] = points.map(_._2)

    override def equals(other: Any) = {
      other match {
        case polygon: Polygon =>
          color == polygon.color && filled == polygon.filled && marked == polygon.marked && points == polygon.points

        case _ => false
      }
    }
  }

  trait LinkLine {
    def xcor: Double
    def isVisible: Boolean
    def dashChoices: Seq[Float]

    override def equals(other: Any) = {
      other match {
        case line: LinkLine =>
          xcor == line.xcor && isVisible == line.isVisible && dashChoices == line.dashChoices
        
        case _ => false
      }
    }
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

    override def equals(other: Any) = {
      other match {
        case shape: VectorShape =>
          name == shape.name && rotatable == shape.rotatable && editableColorIndex == shape.editableColorIndex &&
            elements == shape.elements
        
        case _ => false
      }
    }
  }

  trait LinkShape extends Shape {
    def name: String
    def curviness: Double
    def linkLines: Seq[LinkLine]
    def indicator: VectorShape

    override def equals(other: Any) = {
      other match {
        case shape: LinkShape =>
          name == shape.name && curviness == shape.curviness && linkLines == shape.linkLines &&
            indicator == shape.indicator
        
        case _ => false
      }
    }
  }
}

// so that org.nlogo.shape doesn't need to depend on org.nlogo.agent
sealed trait Shape {
  def name: String
  def name_=(s: String): Unit
}
