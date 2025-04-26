// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package render

import java.awt.{ Composite, FontMetrics }
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import collection.mutable.ListBuffer
import org.nlogo.api.GraphicsInterface
import org.nlogo.util.MockSuite

object MockGraphics {
  trait Operation
  case class Location(x:Double,y:Double) extends Ordered[Location] {
    def compare(that:Location) = {
      if(this.x < that.x) -1 else if( that.x < this.x ) 1
      else if(this.y < that.y) -1 else if( that.y < this.y ) 1 else 0
    }
  }
  case class Size(x:Double,y:Double)
  case class Line(p1: Location, p2:Location) extends Operation
  case class Label(loc:Location) extends Operation with Ordered[Label]{
    def compare(that:Label) = this.loc.compare(that.loc)
  }
  case class LabelSize(width: Int, height:Int)
  case class Shape(name:String, loc:Location, filled:Boolean) extends Operation
  case class Circle(loc:Location, size:Size, filled:Boolean) extends Operation
  case class Rect(loc:Location, size:Size, filled:Boolean) extends Operation
  case class Polygon(locs:List[Location], filled:Boolean) extends Operation
  case class PolyLine(locs:List[Location]) extends Operation
  case class Image(loc:Location, size:Size) extends Operation
}

class MockGraphics(mockTest:MockSuite) extends GraphicsInterface {

  import MockGraphics._

  protected var composite: String = "src"
  protected var transforms = List.empty[AffineTransform]
  protected var antialiasing: Boolean = false
  protected var operations = new ListBuffer[Operation]
  protected var stroke = 1.0
  protected var color = java.awt.Color.black
  protected var transform = new AffineTransform

  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit = {
    operations += Line(loc(x1, y1),loc(x2, y2))
  }
  def drawLabel(s: String, x: Double, y: Double, patchSize: Double): Unit ={
    operations += Label(loc(x, y))
  }
  def draw(shape: java.awt.Shape): Unit = {
    operations += Shape(shape.getClass.getName, loc(0, 0), false)
  }
  def drawCircle(x: Double, y: Double, xDiameter: Double, yDiameter: Double, scale: Double, angle: Double): Unit ={
    operations += Circle(loc(x, y), size(xDiameter, yDiameter), false)
  }
  def fillRect(x: Double, y: Double, width: Double, height: Double, scale: Double, angle: Double): Unit ={
    operations += Rect(loc(x, y), size(width, height), filled=true)
  }
  def drawImage(image: java.awt.Image, x: Int, y: Int, width: Int, height: Int): Unit ={
    operations += Image(loc(x, y),size(width, height))
  }
  def drawImage(image: BufferedImage): Unit = {
    operations += Image(loc(0, 0), size(image.getWidth, image.getHeight))
  }
  def fillPolygon(xcors: Array[Int], ycors: Array[Int], length: Int): Unit ={
    operations += Polygon(poly(xcors, ycors), filled=true)
  }
  def fill(shape: java.awt.Shape): Unit ={
    operations += Shape(shape.getClass.getName,loc(0, 0), filled=true)
  }
  def drawPolyline(xcors: Array[Int], ycors: Array[Int], length: Int): Unit ={
    operations += PolyLine(poly(xcors, ycors))
  }
  def drawRect(x: Double, y: Double, width: Double, height: Double, scale: Double, angle: Double): Unit ={
    operations += Rect(loc(x, y),size(width, height), filled=false)
  }
  def fillCircle(x: Double, y: Double, xDiameter: Double, yDiameter: Double, scale: Double, angle: Double): Unit ={
    operations += Circle(loc(x, y),size(xDiameter, yDiameter), filled=true)
  }
  def fillRect(x: Int, y: Int, width: Int, height: Int): Unit ={
    operations += Rect(loc(x, y),size(width, height), filled=true)
  }
  def drawPolygon(xcors: Array[Int], ycors: Array[Int], length: Int): Unit ={
    operations += Polygon(poly(xcors, ycors), filled=false)
  }

  def labels: List[Label] = operations.collect{case l:Label => l}.toList

  override def toString = operations.mkString("\n")

  def loc(x: Double, y: Double) = {
     Location((transform.getTranslateX + (x * transform.getScaleX)),(transform.getTranslateY + (y * transform.getScaleY)))
  }
  private def poly(xcors: Array[Int], ycors: Array[Int]): List[Location] = {
    xcors.zip(ycors).map(t => loc(t._1, t._2)).toList
  }
  private def size(width: Double, height: Double) = Size(transform.getScaleX * width, transform.getScaleY * height)

  def pop(): Unit = { transform = transforms.head; transforms = transforms.tail }
  def dispose(): Unit ={}
  def setStroke(width: Float, dashes: Array[Float]): Unit ={ stroke = width }
  def setStroke(width: Double): Unit ={ stroke = width }
  def setStrokeFromLineThickness(lineThickness: Double, scale: Double, cellSize: Double, shapeWidth: Double): Unit ={
    stroke = ((shapeWidth / scale) *
            (if (lineThickness == 0) 1 else StrictMath.max(1, lineThickness * cellSize))).asInstanceOf[Float]
  }
  def setColor(c: java.awt.Color): Unit ={ color = c }
  def antiAliasing(on: Boolean): Unit ={ antialiasing = on }
  def setStrokeControl(): Unit ={}
  def rotate(theta: Double): Unit ={ transform.rotate(theta) }
  def rotate(theta: Double, x: Double, y: Double): Unit ={ transform.rotate(theta, x, y) }
  def rotate(theta: Double, x: Double, y: Double, offset: Double): Unit ={
    transform.rotate(theta, x + offset / 2, y + offset / 2)
  }
  def scale(x: Double, y: Double): Unit ={ transform.scale(x, y) }
  def scale(x: Double, y: Double, shapeWidth: Double): Unit ={ transform.scale(x / shapeWidth, y / shapeWidth) }
  def translate(x: Double, y: Double): Unit ={ transform.translate(x, y) }
  def setInterpolation(): Unit ={}
  def push(): Unit = { transforms = transform.clone.asInstanceOf[AffineTransform]::transforms }

  def setComposite(comp: Composite): Unit ={
    composite = if (comp == java.awt.AlphaComposite.Src) "src"
    else if (comp == java.awt.AlphaComposite.Clear) "clear"
    else throw new IllegalStateException
  }
  def location(x:Double,y:Double) =
    "(" + (transform.getTranslateX() + (x * transform.getScaleX() ) ) + " , " +
            (transform.getTranslateY() + (y * transform.getScaleY() ) ) + ")"

  private var fm: Option[FontMetrics] = None
  private def setFontMetrics(fm: FontMetrics): Unit ={ this.fm = Option(fm) }
  def getFontMetrics: FontMetrics = fm.get
  def allowingLabels(labelSize:LabelSize): Unit ={
    import mockTest._
    val mockFontMetrics = mock[FontMetrics]
    setFontMetrics(mockFontMetrics)
    expecting {
      allowing(mockFontMetrics).stringWidth("123"); willReturn(labelSize.width)
      allowing(mockFontMetrics).getHeight(); willReturn(labelSize.height)
    }
  }

  def clear() = operations.clear()
}
