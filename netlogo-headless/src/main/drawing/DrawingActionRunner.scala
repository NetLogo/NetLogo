// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.drawing

import java.io.ByteArrayInputStream
import java.util.Base64
import javax.imageio.ImageIO

import org.nlogo.api.{ ActionRunner, TrailDrawerInterface }

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
    case StampImage(imageBytes, _) =>
      trailDrawer.readImage(new ByteArrayInputStream(imageBytes))
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
      case _ => throw new Exception(s"Unexpected input: $base64")
    }
    val bytes = Base64.getDecoder.decode(byteString)
    (bytes, contentType)
  }

}
