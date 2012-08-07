// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.AgentException
import collection.mutable.ArrayBuffer
import collection.JavaConverters._

@annotation.strictfp
class Drawing3D(world: World3D) extends org.nlogo.api.Drawing3D {

  private val _lines = ArrayBuffer[org.nlogo.api.DrawingLine3D]()
  def lines = _lines.asJava
  private val _turtleStamps = ArrayBuffer[org.nlogo.api.TurtleStamp3D]()
  def turtleStamps = _turtleStamps.asJava
  private val _linkStamps = ArrayBuffer[org.nlogo.api.LinkStamp3D]()
  def linkStamps = _linkStamps.asJava

  def clear() {
    lines.clear()
    turtleStamps.clear()
    linkStamps.clear()
  }

  private def heading(x0: Double, y0: Double, x1: Double, y1: Double) =
    try world.protractor.towards(x0, y0, x1, y1, true)
    catch { case _: AgentException => 0.0 }

  private def pitch(x0: Double, y0: Double, z0: Double, x1: Double, y1: Double, z1: Double) =
    world.protractor.towardsPitch(x0, y0, z0, x1, y1, z1, true)

  def stamp(agent: Agent) {
    agent match {
      case t: Turtle3D =>
        turtleStamps.add(new TurtleStamp3D(t))
      case l: Link3D =>
        linkStamps.add(new LinkStamp3D(l))
    }
  }

  def drawLine(x0: Double, y0: Double, z0: Double,
               x1: Double, y1: Double, z1: Double,
               width: Double, color: AnyRef) {
    wrap(DrawingLine3D(
      x0, y0, z0, x1, y1, z1,
      heading(x0, y0, x1, y1),
      pitch(x0, y0, z0, x1, y1, z1),
      width, color))
  }

  def addLine(x0: Double, y0: Double, z0: Double,
              x1: Double, y1: Double, z1: Double,
              width: Double, color: AnyRef) {
    lines.add(DrawingLine3D(
      x0, y0, z0, x1, y1, z1,
      heading(x0, y0, x1, y1),
      pitch(x0, y0, z0, x1, y1, z1),
      width, color))
  }

  def addStamp(shape: String, xcor: Double, ycor: Double, zcor: Double, size: Double,
               heading: Double, pitch: Double, roll: Double, color: AnyRef, lineThickness: Double) {
    turtleStamps.add(
      new TurtleStamp3D(shape, xcor, ycor, zcor, size, heading, pitch, roll, color, lineThickness))
  }

  def addStamp(shape: String, x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double,
               color: AnyRef, lineThickness: Double, directedLink: Boolean, destSize: Double,
               heading: Double, pitch: Double) {
     linkStamps.add(new LinkStamp3D(shape, x1, y1, z1, x2, y2, z2, color,
                                    lineThickness, directedLink, destSize, heading, pitch))
  }

  private def wrap(l: DrawingLine3D) {
    var startX = l.x0
    var startY = l.y0
    var endX = l.x0
    var endY = l.y0
    var startZ = l.z0
    var endZ = l.z0
    if (endX < startX) {
      val temp = endX
      endX = startX
      startX = temp
    }
    if (endY < startY) {
      val temp = endY
      endY = startY
      startY = temp
    }
    if (endZ < startZ) {
      val temp = endZ
      endZ = startZ
      startZ = temp
    }
    val xdiff = l.x1 - l.x0
    val ydiff = l.y1 - l.y0
    val zdiff = l.z1 - l.z0
    var distX = l.x1 - l.x0
    var distY = l.y1 - l.y0
    var distZ = l.z1 - l.z0
    var newStartX = 0d
    var newStartY = 0d
    var newStartZ = 0d
    val maxy = world.maxPycor + 0.4999999
    val maxx = world.maxPxcor + 0.4999999
    val maxz = world.maxPzcor + 0.4999999
    val miny = world.minPycor - 0.5
    val minx = world.minPxcor - 0.5
    val minz = world.minPzcor - 0.5
    val pixelSize = 1 / world.patchSize

    do {
      endX = startX + distX
      endY = startY + distY
      endZ = startZ + distZ
      if (endY < miny) {
        endX = (miny - startY) * xdiff / ydiff + startX
        endY = miny
        endZ = (miny - startY) * zdiff / ydiff + startZ
        newStartY = maxy
        newStartX = endX
        newStartZ = endZ
        if (newStartX == minx)
          newStartX = maxx
        else if (newStartX == maxx)
          newStartX = minx
        if (newStartZ == maxz)
          newStartZ = minz
        else if (newStartZ == minz)
          newStartZ = maxz
      }
      if (endY > maxy) {
        endX = startX + ((maxy - startY) * xdiff / ydiff)
        endY = maxy
        endZ = startZ + ((maxy - startY) * zdiff / ydiff)
        newStartX = endX
        newStartY = miny
        newStartZ = endZ
        if (newStartX == minx)
          newStartX = maxx
        else if (newStartX == maxx)
          newStartX = minx
        if (newStartZ == maxz)
          newStartZ = minz
        else if (newStartZ == minz)
          newStartZ = maxz
      }
      if (endX < minx) {
        endX = minx
        endY = (ydiff * (endX - startX)) / xdiff + startY
        endZ = (zdiff * (endX - startX)) / xdiff + startZ
        newStartX = maxx
        newStartY = endY
        newStartZ = endZ
        if (newStartY == miny)
          newStartY = maxy
        else if (newStartY == maxy)
          newStartY = miny
        if (newStartZ == maxz)
          newStartZ = minz
        else if (newStartZ == minz)
          newStartZ = maxz
      }
      if (endX > maxx) {
        endX = maxx
        endY = (ydiff * (endX - startX)) / xdiff + startY
        endZ = (zdiff * (endX - startX)) / xdiff + startZ
        newStartX = minx
        newStartY = endY
        newStartZ = endZ
        if (newStartY == miny)
          newStartY = maxy
        else if (newStartY == maxy)
          newStartY = miny
        if (newStartZ == maxz)
          newStartZ = minz
        else if (newStartZ == minz)
          newStartZ = maxz
      }
      if (endZ < minz) {
        endZ = minz
        endY = (ydiff * (endZ - startZ)) / zdiff + startY
        endX = (xdiff * (endZ - startZ)) / zdiff + startX
        newStartZ = maxz
        newStartY = endY
        newStartX = endX
        if (newStartY == miny)
          newStartY = maxy
        else if (newStartY == maxy)
          newStartY = miny
        if (newStartX == minx)
          newStartX = maxx
        else if (newStartX == maxx)
          newStartX = minx
      }
      if (endZ > maxz) {
        endZ = maxz
        endY = (ydiff * (endZ - startZ)) / zdiff + startY
        endX = (xdiff * (endZ - startZ)) / zdiff + startX
        newStartZ = minz
        newStartY = endY
        newStartX = endX
        if (newStartY == miny)
          newStartY = maxy
        else if (newStartY == maxy)
          newStartY = miny
        if (newStartX == minx)
          newStartX = maxx
        else if (newStartX == maxx)
          newStartX = minx
      }

      lines.add(DrawingLine3D(startX, startY, startZ,
                              endX, endY, endZ,
                              heading(startX, startY, endX, endY),
                              pitch(startX, startY, startZ, endX, endY, endZ),
                              l.width, l.color))

      distX -= (endX - startX)
      distY -= (endY - startY)
      distZ -= (endZ - startZ)

      startX = newStartX
      startY = newStartY
      startZ = newStartZ
    } while (StrictMath.abs(distY) >= pixelSize
             || StrictMath.abs(distX) >= pixelSize
             || StrictMath.abs(distZ) >= pixelSize)
  }

}
