// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.shape

import java.awt.Color
import java.util.Observable

import org.nlogo.{ core, api },
  core.Shape,
    Shape.{ VectorShape => BaseVectorShape },
  api.{ Color => ApiColor, GraphicsInterface }

import scala.collection.mutable.ArrayBuffer

@SerialVersionUID(0L)
object VectorShape {
  private object Recolorable extends Enumeration {
    type Recolorable = Value
    val UNKNOWN, TRUE, FALSE = Value
  }

  private[shape] def getString(v: Array[String], index: Int): String =
    if ((null != v) && (v.length > index)) v(index) else ""

  def getDefaultShape: VectorShape = {
    val result: VectorShape = new VectorShape
    result.name = org.nlogo.core.ShapeList.DefaultShapeName
    result.setRotatable(true)
    result.setEditableColorIndex(0)
    val xs = List(150, 40, 150, 260)
    val ys = List(5, 250, 205, 250)
    val defaultShape = new Polygon(xs, ys, new Color(141, 141, 141))
    defaultShape.filled = true
    defaultShape.marked = true
    result.addElement(defaultShape)
    result
  }

  val TURTLE_WIDTH: Int = 25
  val CLOSE_ENOUGH: Int = 10
  val NUM_GRID_LINES: Int = 20
}

@SerialVersionUID(0L)
class VectorShape extends Observable with BaseVectorShape with Cloneable with java.io.Serializable with DrawableShape {
  var name: String = ""

  var editableColorIndex: Int = 0
  var rotatable: Boolean = true

  private var _fgRecolorable: VectorShape.Recolorable.Recolorable =
    VectorShape.Recolorable.UNKNOWN

  protected var elementList = ArrayBuffer[Element]()

  override def elements: Seq[Shape.Element] =
    elementList.toSeq

  def setEditableColorIndex(editableColorIndex: Int) =
    this.editableColorIndex = editableColorIndex

  def getEditableColorIndex: Int =
    editableColorIndex

  override def clone: VectorShape =
    try {
      val newShape = super.clone.asInstanceOf[VectorShape]
      newShape.elementList = ArrayBuffer(elementList.map(_.clone.asInstanceOf[Element]): _*)
      newShape
    } catch {
      case ex: CloneNotSupportedException => throw new IllegalStateException(ex)
    }

  def setOutline() =
    elementList.foreach(_.filled = false)

  def getElements: java.util.List[Element] = {
    import scala.collection.JavaConverters._
    elementList.toList.asJava
  }

  def setRotatable(rotatable: Boolean) =
    this.rotatable = rotatable

  def isRotatable: Boolean = rotatable

  def fgRecolorable: Boolean = {
    if (_fgRecolorable eq VectorShape.Recolorable.UNKNOWN) {
      val editableColor: Color = new Color(ApiColor.getARGBByIndex(editableColorIndex))
      updateRecolorability(editableColor)
    }
    _fgRecolorable == VectorShape.Recolorable.TRUE
  }

  def markRecolorableElements(editableColor: Color, editableColorIndex: Int) = {
    this.editableColorIndex = editableColorIndex
    updateRecolorability(editableColor)
    elementList.foreach(el => el.marked = (el.awtColor == editableColor))
  }

  private def updateRecolorability(editableColor: Color) = {
    if(elementList.exists(_.awtColor == editableColor))
      _fgRecolorable = VectorShape.Recolorable.TRUE
    else
      _fgRecolorable = VectorShape.Recolorable.FALSE
  }

  def isTooSimpleToCache: Boolean =
    elementList.size match {
      case 0 => true
      case 1 => !element(0).isInstanceOf[Polygon]
      case _ => false
    }

  protected def element(i: Int): Element =
    elementList(i)

  def modifyingElements(newEls: => ArrayBuffer[Element]) = {
    val modifiedElements = newEls
    if (modifiedElements != elementList) {
      elementList = modifiedElements
      setChanged()
      notifyObservers()
    }
  }

  def changed(): Unit = {
    setChanged()
    notifyObservers()
  }

  def remove(element: Element) =
    modifyingElements { elementList - element }

  def removeLast() =
    modifyingElements { elementList.dropRight(1) }

  def removeAll() =
    modifyingElements { ArrayBuffer[Element]() }

  def add(element: Element) =
    modifyingElements {
      val newList = elementList :+ element
      element match {
        case rectangle: Rectangle => rectangle.setMaxsAndMins()
        case _ =>
      }
      newList
    }

  def addAtPosition(index: Int, element: Element) =
    modifyingElements {
      val (prefix, postfix) = elementList.splitAt(index)
      (prefix :+ element) ++ postfix
    }

  def rotateLeft() =
    elementList.foreach(_.rotateLeft())

  def rotateRight() =
    elementList.foreach(_.rotateRight())

  def flipHorizontal() =
    elementList.foreach(_.flipHorizontal())

  def flipVertical() =
    elementList.foreach(_.flipVertical())

  def paint(g: GraphicsInterface, turtleColor: Color, x: Double, y: Double, size: Double, cellSize: Double, angle: Int, lineThickness: Double) = {
    g.push()
    val scale: Double = size * cellSize
    try {
      if (isRotatable && angle != 0) {
        g.rotate(angle / 180.0 * StrictMath.PI, x, y, scale)
      }
      g.translate(x, y)
      g.scale(scale, scale, Shape.Width)
      g.setStrokeFromLineThickness(lineThickness, scale, cellSize, Shape.Width)
      elementList.foreach(_.draw(g, turtleColor, scale, if (isRotatable) angle else 0))
    } finally {
      g.pop()
    }
  }

  def paint(g: GraphicsInterface, turtleColor: Color, x: Int, y: Int, cellSize: Double, angle: Int) =
    paint(g, turtleColor, x, y, 1, cellSize, angle, 0.0f)

  def toReadableString: String =
    s"Shape $name:\n${elementList.map(_.toString)}"

  override def toString: String =
    (Seq(
      name,
      rotatable,
      editableColorIndex) ++
      elementList.filter(_.shouldSave).map(_.toString)).mkString("\n")

  def addElement(element: Element) =
    if (element != null)
      add(element)
}
