// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.drawing

import java.io.ByteArrayInputStream
import java.util.Base64
import javax.imageio.ImageIO

import org.nlogo.api.{ ActionRunner, AgentSet, Link, Patch, TrailDrawerInterface, Turtle, World }
import org.nlogo.core.AgentKind

import DrawingAction._

class DrawingActionRunner(val trailDrawer: TrailDrawerInterface) extends ActionRunner[DrawingAction] {

  override def run(action: DrawingAction) = action match {
    case DrawLine(x1, y1, x2, y2, penColor, penSize, penMode) =>
      trailDrawer.drawLine(x1, y1, x2, y2, penColor, penSize, penMode)
    case SetColors(base64) =>
      val (bytes, _) = base64ToBytes(base64)
      val image      = ImageIO.read(new ByteArrayInputStream(bytes))
      val width      = image.getWidth
      val height     = image.getHeight
      trailDrawer.setColors(bytes.map(_.toInt), width, height)
    case SendPixels(dirty) =>
      trailDrawer.sendPixels(dirty)
    case ReadImage(imageBytes) =>
      trailDrawer.readImage(new ByteArrayInputStream(imageBytes))
    case StampImage(stamp) =>
      stamp match {
        case ts: TurtleStamp =>
          trailDrawer.stamp(new DummyTurtle(ts), ts.erase)

        case ls: LinkStamp =>
          trailDrawer.stamp(new DummyLink(ls), ls.erase)
      }
    case CreateDrawing(dirty: Boolean) =>
      trailDrawer.getAndCreateDrawing(dirty)
    case ImportDrawing(base64) =>
      val (bytes, contentType) = base64ToBytes(base64)
      trailDrawer.importDrawing(new ByteArrayInputStream(bytes), Option(contentType))
    case ClearDrawing =>
      trailDrawer.clearDrawing()
    case RescaleDrawing =>
      trailDrawer.rescaleDrawing()
    case MarkClean =>
      trailDrawer.markClean()
    case MarkDirty =>
      trailDrawer.markDirty()
  }

  private def base64ToBytes(base64: String): (Array[Byte], String) = {
    val MimeRegex = "data:(.*);base64".r
    val (contentType, byteString) = base64.split(",") match {
      case Array(MimeRegex(c), b) => (c, b)
      case _ => throw new IllegalStateException
    }
    val bytes = Base64.getDecoder.decode(byteString)
    (bytes, contentType)
  }

  private class DummyTurtle(stamp: TurtleStamp) extends Turtle {
    override def xcor: Double = stamp.x
    override def ycor: Double = stamp.y
    override def size: Double = stamp.size
    override def heading: Double = stamp.heading
    override def color: AnyRef = stamp.color
    override def shape: String = stamp.shapeName
    override def lineThickness: Double = stamp.thickness

    override def alpha: Int = ???
    override def classDisplayName: String = ???
    override def getVariable(vn: Int): AnyRef = ???
    override def id: Long = ???
    override def kind: AgentKind = ???
    override def setVariable(vn: Int, value: AnyRef): Unit = ???
    override def variables: Array[AnyRef] = ???
    override def world: World = ???
    override def getBreed: AgentSet = ???
    override def getBreedIndex: Int = ???
    override def getPatchHere: Patch = ???
    override def hasLabel: Boolean = ???
    override def heading(d: Double): Unit = ???
    override def hidden: Boolean = ???
    override def jump(distance: Double): Unit = ???
    override def labelColor: AnyRef = ???
    override def labelString: String = ???
  }

  private class DummyLink(stamp: LinkStamp) extends Link {
    override def x1: Double = stamp.x1
    override def x2: Double = stamp.x2
    override def y1: Double = stamp.y1
    override def y2: Double = stamp.y2
    override def midpointX: Double = stamp.midpointX
    override def midpointY: Double = stamp.midpointY
    override def heading: Double = stamp.heading
    override def color: AnyRef = stamp.color
    override def shape: String = stamp.shapeName
    override def lineThickness: Double = stamp.thickness
    override def isDirectedLink: Boolean = stamp.isDirected
    override def size: Double = stamp.size
    override def hidden: Boolean = stamp.isHidden
    override def hasLabel: Boolean = stamp.hasLabel
    override def labelString: String = stamp.labelString
    override def labelColor: AnyRef = stamp.labelColor
    override def linkDestinationSize: Double = stamp.destinationSize

    override def alpha: Int = ???
    override def classDisplayName: String = ???
    override def getVariable(vn: Int): AnyRef = ???
    override def id: Long = ???
    override def kind: org.nlogo.core.AgentKind = ???
    override def setVariable(vn: Int, value: AnyRef): Unit = ???
    override def variables: Array[AnyRef] = ???
    override def world: org.nlogo.api.World = ???
    override def end1: org.nlogo.api.Turtle = ???
    override def end2: org.nlogo.api.Turtle = ???
    override def getBreed: org.nlogo.api.AgentSet = ???
    override def getBreedIndex: Int = ???
  }

}
