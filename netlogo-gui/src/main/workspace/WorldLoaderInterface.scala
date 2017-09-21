// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.core.{ UpdateMode, View => CoreView, WorldDimensions }
import org.nlogo.api.{ RichWorldDimensions, WorldResizer },
  RichWorldDimensions._

trait WorldLoaderInterface {
  def adjustDimensions(d: WorldDimensions): WorldDimensions
  def calculateViewSize(d: WorldDimensions, v: CoreView): (Int, Int)
  def changeTopology(wrapX: Boolean, wrapY: Boolean): Unit
  def clearTurtles(): Unit
  def fontSize(fontSize: Int): Unit
  def frameRate: Double
  def frameRate(rate: Double): Unit
  def patchSize(patchSize: Double): Unit
  def setDimensions(d: WorldDimensions, showProgress: Boolean, toStop: WorldResizer.JobStop): Unit
  def setSize(x: Int, y: Int): Unit
  def showTickCounter(visible: Boolean)
  def showTickCounter: Boolean
  def tickCounterLabel(label: String): Unit
  def tickCounterLabel: String
  def updateMode(updateMode: UpdateMode): Unit
}

trait DefaultWorldLoader extends WorldLoaderInterface {
  // this adjusts the patchSize if the view dimensions are not suitable
  def adjustDimensions(d: WorldDimensions): WorldDimensions = {
    val widgetWidth = calculateWidth(d)
    val minWidth = getMinimumWidth
    if (widgetWidth < minWidth)
      d.copyPreservingArity(patchSize = computePatchSize(minWidth - insetWidth, d.width))
    else
      d
  }

  def calculateHeight(d: WorldDimensions): Int
  def calculateWidth(d: WorldDimensions): Int
  def calculateViewSize(d: WorldDimensions, v: CoreView): (Int, Int) = {
    val widgetWidth = calculateWidth(d)
    val minWidth = getMinimumWidth
    val width = widgetWidth max minWidth
    val height = calculateHeight(d)
    (width, height)
  }

  def computePatchSize(width: Int, numPatches: Int): Double
  def getMinimumWidth: Int
  def insetWidth: Int
}
