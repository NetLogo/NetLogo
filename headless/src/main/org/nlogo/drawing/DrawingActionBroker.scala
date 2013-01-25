package org.nlogo.drawing

import scala.collection.mutable
import org.nlogo.api
import DrawingActions._

class DrawingActionBroker(val trailDrawer: api.TrailDrawerInterface)
  extends api.TrailDrawerInterface
  with mutable.Publisher[DrawingAction] {

  val runner = new DrawingActionRunner(trailDrawer)

  override def publish(action: DrawingAction) {
    super.publish(action)
    println(action)
    runner.run(action)
  }

  override def drawLine(
    x1: Double, y1: Double, x2: Double, y2: Double,
    color: AnyRef, size: Double, mode: String) =
    publish(DrawLine(x1, y1, x2, y2, color, size, mode))

  override def setColors(colors: Array[Int]) { publish(SetColors(colors)) }
  override def sendPixels(dirty: Boolean) { publish(SendPixels(dirty)) }
  override def stamp(agent: api.Agent, erase: Boolean) {
    publish(Stamp(agent, erase))
  }
  override def clearDrawing() { publish(ClearDrawing()) }
  override def rescaleDrawing() { publish(RescaleDrawing()) }
  override def markClean() { publish(MarkClean()) }
  override def markDirty() { publish(MarkDirty()) }

  override def getAndCreateDrawing(dirty: Boolean): java.awt.image.BufferedImage = {
    publish(CreateDrawing(dirty))
    getDrawing.asInstanceOf[java.awt.image.BufferedImage]
  }

  // The following methods should be side-effect free. Thus, they
  // generate no actions and just delegate to the trailDrawer.
  override def sendPixels: Boolean = trailDrawer.sendPixels // just a getter, really
  override def getDrawing: AnyRef = trailDrawer.getDrawing
  override def colors: Array[Int] = trailDrawer.colors
  override def isDirty: Boolean = trailDrawer.isDirty
  override def getHeight: Int = trailDrawer.getHeight
  override def getWidth: Int = trailDrawer.getWidth
  override def isBlank: Boolean = trailDrawer.isBlank

  // this one does have side effects, but we don't want to record it
  override def exportDrawingToCSV(writer: java.io.PrintWriter) =
    trailDrawer.exportDrawingToCSV(writer)

  // I'm not sure about these two. Should we record them?
  override def readImage(is: java.io.InputStream) = trailDrawer.readImage(is)
  override def importDrawing(file: org.nlogo.api.File) = trailDrawer.importDrawing(file)
}
