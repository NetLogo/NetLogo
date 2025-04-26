// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.drawing

import java.awt.image.BufferedImage
import java.io.{ ByteArrayOutputStream, InputStream, PrintWriter }
import java.nio.file.Paths
import java.util.Base64
import javax.imageio.ImageIO

import org.nlogo.api
import DrawingAction._

import
  org.nlogo.api.{ ActionBroker, Link, Turtle }

import org.nlogo.core.File

class DrawingActionBroker(
  val trailDrawer: api.TrailDrawerInterface)
  extends ActionBroker[DrawingAction]
  with api.TrailDrawerInterface {

  override val runner = new DrawingActionRunner(trailDrawer)

  override def drawLine(
    x1: Double, y1: Double, x2: Double, y2: Double,
    color: AnyRef, size: Double, mode: String): Unit = {
    publish(DrawLine(x1, y1, x2, y2, color, size, mode))
  }

  override def setColors(colors: Array[Int], width: Int, height: Int): Unit = {

    val image  = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    image.setRGB(0, 0, width, height, colors, 0, width)

    val baos = new ByteArrayOutputStream
    ImageIO.write(image, "png", baos)
    baos.flush()
    val bytes = baos.toByteArray
    baos.close()

    val base64 = bytesToBase64(bytes, "image/png")
    publish(SetColors(base64))

  }

  override def sendPixels(dirty: Boolean): Unit = { publish(SendPixels(dirty)) }

  override def stamp(agent: api.Agent, erase: Boolean): Unit = {

    /*
     * The way TrailDrawer.stamp currently works, it is too dependent on
     * current world state to be easily modeled as an Action that can be
     * serialized, so we resort to running it and grabbing a bitmap that
     * can easily be stored in the Action. This is fugly, but will have to
     * do for now. NP 2013-02-04.
     */
    trailDrawer.stamp(agent, erase)
    val image = trailDrawer.getDrawing.asInstanceOf[BufferedImage]
    val bytes = imageToBytes(image)

    val stamp =
      agent match {
        case l: Link   =>
          import l._
          LinkStamp(x1, y1, end2.xcor, end2.ycor,
                    midpointX, midpointY, heading, color, shape,
                    lineThickness, isDirectedLink, size, hidden,
                    if (erase) "erase" else "normal")
        case t: Turtle =>
          import t._
          TurtleStamp(xcor, ycor, size, heading, color, shape,
                      if (erase) "erase" else "normal")
        case _ =>
          throw new Exception(s"Unexpected agent: $agent")
      }

    // Actually running the Action would needlessly re-apply the bitmap.
    publishWithoutRunning(StampImage(bytes, stamp))

  }

  override def clearDrawing(): Unit = { publish(ClearDrawing) }
  override def rescaleDrawing(): Unit = { publish(RescaleDrawing) }
  override def markClean(): Unit = { publish(MarkClean) }
  override def markDirty(): Unit = { publish(MarkDirty) }

  override def getAndCreateDrawing(dirty: Boolean): BufferedImage = {
    publish(CreateDrawing(dirty))
    getDrawing.asInstanceOf[BufferedImage]
  }

  override def importDrawing(file: File): Unit = {
    val mimeType = Paths.get(file.getAbsolutePath).toUri.toURL.openConnection().getContentType
    importDrawing(file.getInputStream, Option(mimeType))
  }

  override def importDrawing(is: InputStream, mimeTypeOpt: Option[String] = None): Unit = {

    val buffer = new ByteArrayOutputStream
    val data   = new Array[Byte](1024)

    var justRead = is.read(data, 0, data.length)
    while (justRead != -1) {
      buffer.write(data, 0, justRead)
      justRead = is.read(data, 0, data.length)
    }

    buffer.flush()

    val mimeType = mimeTypeOpt.getOrElse("unknown")
    val base64   = bytesToBase64(buffer.toByteArray, mimeType)

    importDrawingBase64(base64)

  }

  override def importDrawingBase64(base64: String): Unit =
    publish(ImportDrawing(base64))

  override def readImage(inputStream: InputStream): Unit =
    readImage(javax.imageio.ImageIO.read(inputStream))

  override def readImage(image: BufferedImage): Unit =
    publish(imageToAction(image))

  // The following methods should be side-effect free. Thus, they
  // generate no actions and just delegate to the trailDrawer.
  override def sendPixels: Boolean = trailDrawer.sendPixels // just a getter, really
  override def getDrawing: AnyRef = trailDrawer.getDrawing
  override def colors: Array[Int] = trailDrawer.colors
  override def isDirty: Boolean = trailDrawer.isDirty
  override def getHeight: Int = trailDrawer.getHeight
  override def getWidth: Int = trailDrawer.getWidth
  override def isBlank: Boolean = trailDrawer.isBlank

  // This one does have side effects, but we don't want to record it. Or do we?
  override def exportDrawingToCSV(writer: PrintWriter): Unit = {
    trailDrawer.exportDrawingToCSV(writer)
  }

  /** Converts a BufferedImage to a ReadImage drawing action. */
  private def imageToAction(image: BufferedImage): ReadImage =
    ReadImage(imageToBytes(image))

  private def bytesToBase64(bytes: Array[Byte], contentType: String): String =
    s"data:$contentType;base64,${Base64.getEncoder().encodeToString(bytes)}"

}
