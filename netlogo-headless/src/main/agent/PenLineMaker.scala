// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.Numbers

case class Trail(x1: Double, y1: Double, x2: Double, y2: Double, dist: Double)

private[agent] object PenLineMaker {

  private case class HelperContext(makeTrailsBy: (Double, Double, Double) => Seq[Trail],
                                   lazyWrapX:    (Double) => Double,
                                   lazyWrapY:    (Double) => Double)

  def apply(x: Double, y: Double, heading: Double, jumpDist: Double, minX: Double, maxX: Double, minY: Double, maxY: Double): Array[Trail] =
    if (jumpDist <= 0)
      Array.empty
    else {
      val makeTrailsBy = makeTrails(heading, minX, maxX, minY, maxY) _
      val lazyWrapX    = lazyWrapValue(minX, maxX) _
      val lazyWrapY    = lazyWrapValue(minY, maxY) _
      helper(x, y, jumpDist, HelperContext(makeTrailsBy, lazyWrapX, lazyWrapY)).toArray
    }

  private def helper(x: Double, y: Double, jumpDist: Double, context: HelperContext, acc: Seq[Trail] = Seq.empty): Seq[Trail] = {

    val HelperContext(makeTrailsBy, lazyWrapX, lazyWrapY) = context

    val trails       = makeTrailsBy(x, y, jumpDist)
    val trail        = trails.minBy(_.dist)
    val newAcc       = acc :+ trail
    val nextJumpDist = jumpDist - trail.dist

    if (nextJumpDist <= 0)
      newAcc
    else {
      val newX = lazyWrapX(trail.x2)
      val newY = lazyWrapY(trail.y2)
      helper(newX, newY, nextJumpDist, context, newAcc)
    }

  }

  private def lazyWrapValue(min: Double, max: Double)(value: Double): Double =
    if (value <= min)
      max
    else if (value >= max)
      min
    else
      value

  private def distanceFromLegs(l1: Double, l2: Double): Double = {
    val square = (x: Double) => StrictMath.pow(x, 2)
    StrictMath.sqrt(square(l1) + square(l2))
  }

  private def makeTrails(heading: Double, minX: Double, maxX: Double, minY: Double, maxY: Double)
                        (x: Double, y: Double, jumpDist: Double): Seq[Trail] = {

    val squash = (x: Double) => if (StrictMath.abs(x) < Numbers.Infinitesimal) 0 else x

    val xcomp = (StrictMath.toRadians _ andThen StrictMath.sin andThen squash)(heading)
    val ycomp = (StrictMath.toRadians _ andThen StrictMath.cos andThen squash)(heading)
    val tan   = (StrictMath.toRadians _ andThen StrictMath.tan andThen squash)(heading)

    val rawX = x + xcomp * jumpDist
    val rawY = y + ycomp * jumpDist

    val baseTrails = Option(new Trail(x, y, rawX, rawY, jumpDist))

    val makeTrailComponent =
      (endX: Double, endY: Double, dx: Double, dy: Double) =>
        Option(new Trail(x, y, endX, endY, distanceFromLegs(dx, dy)))

    val yInterceptTrails =
      if (rawX > maxX) {
        val dx         = maxX - x
        val dy         = dx / tan
        val interceptY = y + dy
        makeTrailComponent(maxX, interceptY, dx, dy)
      }
      else if (rawX < minX) {
        val dx         = x - minX
        val dy         = dx / tan
        val interceptY = y - dy
        makeTrailComponent(minX, interceptY, dx, dy)
      }
    else
      None

    val xInterceptTrails =
      if (rawY > maxY) {
        val dy         = maxY - y
        val dx         = dy * tan
        val interceptX = x + dx
        makeTrailComponent(interceptX, maxY, dx, dy)
      }
      else if (rawY < minY) {
        val dy         = y - minY
        val dx         = dy * tan
        val interceptX = x - dx
        makeTrailComponent(interceptX, minY, dx, dy)
      }
    else
      None

    (baseTrails ++ xInterceptTrails ++ yInterceptTrails).toSeq

  }

}
