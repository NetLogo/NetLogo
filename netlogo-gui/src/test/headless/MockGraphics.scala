// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.awt.{List=>AWTList, _}
import geom.AffineTransform
import image.BufferedImage
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

  private var composite: String = "src"
  private var transforms = List.empty[AffineTransform]
  private var antialiasing: Boolean = false
  private var operations = new ListBuffer[Operation]
  private var stroke = 1.0
  private var color = java.awt.Color.black
  private var transform = new AffineTransform

  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
    operations += Line(loc(x1, y1),loc(x2, y2))
  }
  def drawLabel(s: String, x: Double, y: Double, patchSize: Double){
    operations += Label(loc(x, y))
  }
  def draw(shape: java.awt.Shape) {
    operations += Shape(shape.getClass.getName, loc(0, 0), false)
  }
  def drawCircle(x: Double, y: Double, xDiameter: Double, yDiameter: Double, scale: Double, angle: Double){
    operations += Circle(loc(x, y), size(xDiameter, yDiameter), false)
  }
  def fillRect(x: Double, y: Double, width: Double, height: Double, scale: Double, angle: Double){
    operations += Rect(loc(x, y), size(width, height), filled=true)
  }
  def drawImage(image: java.awt.Image, x: Int, y: Int, width: Int, height: Int){
    operations += Image(loc(x, y),size(width, height))
  }
  def drawImage(image: BufferedImage) {
    operations += Image(loc(0, 0), size(image.getWidth, image.getHeight))
  }
  def fillPolygon(xcors: Array[Int], ycors: Array[Int], length: Int){
    operations += Polygon(poly(xcors, ycors), filled=true)
  }
  def fill(shape: java.awt.Shape){
    operations += Shape(shape.getClass.getName,loc(0, 0), filled=true)
  }
  def drawPolyline(xcors: Array[Int], ycors: Array[Int], length: Int){
    operations += PolyLine(poly(xcors, ycors))
  }
  def drawRect(x: Double, y: Double, width: Double, height: Double, scale: Double, angle: Double){
    operations += Rect(loc(x, y),size(width, height), filled=false)
  }
  def fillCircle(x: Double, y: Double, xDiameter: Double, yDiameter: Double, scale: Double, angle: Double){
    operations += Circle(loc(x, y),size(xDiameter, yDiameter), filled=true)
  }
  def fillRect(x: Int, y: Int, width: Int, height: Int){
    operations += Rect(loc(x, y),size(width, height), filled=true)
  }
  def drawPolygon(xcors: Array[Int], ycors: Array[Int], length: Int){
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

  def pop{ transform = transforms.head; transforms = transforms.tail }
  def dispose{}
  def setStroke(width: Float, dashes: Array[Float]){ stroke = width }
  def setStroke(width: Double){ stroke = width }
  def setStrokeFromLineThickness(lineThickness: Double, scale: Double, cellSize: Double, shapeWidth: Double){
    stroke = ((shapeWidth / scale) *
            (if (lineThickness == 0) 1 else StrictMath.max(1, lineThickness * cellSize))).asInstanceOf[Float]
  }
  def setColor(c: java.awt.Color){ color = c }
  def antiAliasing(on: Boolean){ antialiasing = on }
  def setStrokeControl{}
  def rotate(theta: Double){ transform.rotate(theta) }
  def rotate(theta: Double, x: Double, y: Double){ transform.rotate(theta, x, y) }
  def rotate(theta: Double, x: Double, y: Double, offset: Double){
    transform.rotate(theta, x + offset / 2, y + offset / 2)
  }
  def scale(x: Double, y: Double){ transform.scale(x, y) }
  def scale(x: Double, y: Double, shapeWidth: Double){ transform.scale(x / shapeWidth, y / shapeWidth) }
  def translate(x: Double, y: Double){ transform.translate(x, y) }
  def setInterpolation{}
  def push{ transforms = transform.clone.asInstanceOf[AffineTransform]::transforms }

  def setComposite(comp: Composite){
    composite = if (comp == java.awt.AlphaComposite.Src) "src"
    else if (comp == java.awt.AlphaComposite.Clear) "clear"
    else throw new IllegalStateException
  }
  def location(x:Double,y:Double) =
    "(" + (transform.getTranslateX() + (x * transform.getScaleX() ) ) + " , " +
            (transform.getTranslateY() + (y * transform.getScaleY() ) ) + ")"

  private var fm: Option[FontMetrics] = None
  private def setFontMetrics(fm: FontMetrics){ this.fm = Option(fm) }
  def getFontMetrics: FontMetrics = fm.get
  def allowingLabels(labelSize:LabelSize){
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
