// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.drawing

import scala.collection.mutable
import org.nlogo.api
import DrawingAction._
import org.nlogo.api.ActionBroker

class DrawingActionBroker(
  val trailDrawer: api.TrailDrawerInterface)
  extends ActionBroker[DrawingAction]
  with api.TrailDrawerInterface {

  override val runner = new DrawingActionRunner(trailDrawer)

  override def drawLine(
    x1: Double, y1: Double, x2: Double, y2: Double,
    color: AnyRef, size: Double, mode: String) {
    publish(DrawLine(x1, y1, x2, y2, color, size, mode))
  }

  override def setColors(colors: Array[Int]) { publish(SetColors(colors)) }
  override def sendPixels(dirty: Boolean) { publish(SendPixels(dirty)) }

  override def stamp(agent: api.Agent, erase: Boolean) {
    /*
     * The way TrailDrawer.stamp currently works, it is too dependent on
     * current world state to be easily modeled as an Action that can be
     * serialized, so we resort to running it and grabbing a bitmap that
     * can easily be stored in the Action. This is fugly, but will have to
     * do for now. NP 2013-02-04.
     */
    trailDrawer.stamp(agent, erase)
    val image = trailDrawer.getDrawing.asInstanceOf[java.awt.image.BufferedImage]
    // Actually running the Action would needlessly re-apply the bitmap.
    publishWithoutRunning(imageToAction(image))
  }

  override def clearDrawing() { publish(ClearDrawing()) }
  override def rescaleDrawing() { publish(RescaleDrawing()) }
  override def markClean() { publish(MarkClean()) }
  override def markDirty() { publish(MarkDirty()) }

  override def getAndCreateDrawing(dirty: Boolean): java.awt.image.BufferedImage = {
    publish(CreateDrawing(dirty))
    getDrawing.asInstanceOf[java.awt.image.BufferedImage]
  }

  override def importDrawing(file: org.nlogo.api.File): Unit =
    publish(ImportDrawing(file.getPath))

  override def importDrawing(is: java.io.InputStream): Unit =
    trailDrawer.importDrawing(is) // TODO: serialize image into action for both importDrawing methods

  override def readImage(is: java.io.InputStream): Unit =
    publish(imageToAction(javax.imageio.ImageIO.read(is)))

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
  override def exportDrawingToCSV(writer: java.io.PrintWriter) {
    trailDrawer.exportDrawingToCSV(writer)
  }

  /** Converts a java.awt.image.BufferedImage to a ReadImage drawing action. */
  private def imageToAction(image: java.awt.image.BufferedImage): ReadImage =
    ReadImage(imageToBytes(image))
}
