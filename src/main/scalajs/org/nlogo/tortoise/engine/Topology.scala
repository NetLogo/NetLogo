package org.nlogo.tortoise.engine

trait Topology {
  def world: World
  def getNeighbors(pxcor: XCor, pycor: YCor): Seq[Patch]
  def wrap(pos: Double, min: Double, max: Double): Double
}

class Torus(override val world: World, minPxcor: XCor, maxPxcor: XCor, minPycor: YCor, maxPycor: YCor) extends Topology {

  override def wrap(pos: Double, min: Double, max: Double): Double =
    if (pos >= max)
      (min + ((pos - max) % (max - min)))
    else if (pos < min) {
      val result = max - ((min - pos) % (max - min))
      if (result < max)
        result
      else
        min
    }
    else
      pos

  override def getNeighbors(pxcor: XCor, pycor: YCor): Seq[Patch] = {

    implicit val (xcor, ycor) = (pxcor, pycor)

    val (minX, maxX, minY, maxY) = (pxcor == minPxcor, pxcor == maxPxcor, pycor == minPycor, pycor == maxPycor)
    val coordFuncs = (minX, maxX, minY, maxY) match {
      case (true, true, true, true) => Seq()
      case (true, true, _, _)       => Seq(getPatchNorth, getPatchSouth)
      case (_, _, true, true)       => Seq(getPatchEast,  getPatchWest)
      case _                        => Seq(getPatchNorth,     getPatchEast,      getPatchSouth,     getPatchWest,
                                           getPatchNorthEast, getPatchSouthEast, getPatchSouthWest, getPatchNorthWest)
    }

    coordFuncs map { case (x, y) => (world.getPatchAt(x, y)) }

  }


  private def getPatchNorth    (implicit pxcor: XCor, pycor: YCor): (XCor, YCor) = (noHoriz,   goingNorth)
  private def getPatchSouth    (implicit pxcor: XCor, pycor: YCor): (XCor, YCor) = (noHoriz,   goingSouth)
  private def getPatchEast     (implicit pxcor: XCor, pycor: YCor): (XCor, YCor) = (goingEast, noVert)
  private def getPatchWest     (implicit pxcor: XCor, pycor: YCor): (XCor, YCor) = (goingWest, noVert)
  private def getPatchNorthWest(implicit pxcor: XCor, pycor: YCor): (XCor, YCor) = (goingWest, goingNorth)
  private def getPatchSouthWest(implicit pxcor: XCor, pycor: YCor): (XCor, YCor) = (goingWest, goingSouth)
  private def getPatchSouthEast(implicit pxcor: XCor, pycor: YCor): (XCor, YCor) = (goingEast, goingSouth)
  private def getPatchNorthEast(implicit pxcor: XCor, pycor: YCor): (XCor, YCor) = (goingEast, goingNorth)


  private def noHoriz(implicit pxcor: XCor): XCor =
    pxcor

  private def noVert (implicit pycor: YCor): YCor =
    pycor

  private def goingNorth(implicit pycor: YCor): YCor =
    if (pycor == maxPycor) minPycor else YCor(pycor.value + 1)

  private def goingEast(implicit pxcor: XCor): XCor =
    if (pxcor == maxPxcor) minPxcor else XCor(pxcor.value + 1)

  private def goingSouth(implicit pycor: YCor): YCor =
    if (pycor == minPycor) maxPycor else YCor(pycor.value - 1)

  private def goingWest(implicit pxcor: XCor): XCor =
    if (pxcor == minPxcor) maxPxcor else XCor(pxcor.value - 1)

}
