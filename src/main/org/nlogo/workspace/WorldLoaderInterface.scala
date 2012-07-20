// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.api.{ UpdateMode, WorldDimensions }

trait WorldLoaderInterface {
  def patchSize(patchSize: Double): Unit
  def setDimensions(d: WorldDimensions, patchSize: Double): Unit
  def fontSize(fontSize: Int): Unit
  def setSize(x: Int, y: Int): Unit
  def changeTopology(wrapX: Boolean, wrapY: Boolean): Unit
  def clearTurtles(): Unit
  def updateMode(updateMode: UpdateMode): Unit
  def getMinimumWidth: Int
  def computePatchSize(width: Int, numPatches: Int): Double
  def calculateHeight(worldHeight: Int, patchSize: Double): Int
  def calculateWidth(worldWidth: Int, patchSize: Double): Int
  def insetWidth(): Int
  def tickCounterLabel(label: String): Unit
  def tickCounterLabel: String
  def showTickCounter(visible: Boolean)
  def showTickCounter: Boolean
  def frameRate: Double
  def frameRate(rate: Double): Unit
}
