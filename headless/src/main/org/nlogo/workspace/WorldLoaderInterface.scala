// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.api.{ UpdateMode, WorldDimensions }

trait WorldLoaderInterface {
  def patchSize_=(patchSize: Double): Unit
  def setDimensions(d: WorldDimensions, patchSize: Double): Unit
  def fontSize_=(fontSize: Int): Unit
  def setSize(x: Int, y: Int): Unit
  def changeTopology(wrapX: Boolean, wrapY: Boolean): Unit
  def clearTurtles(): Unit
  def updateMode_=(updateMode: UpdateMode): Unit
  def getMinimumWidth: Int
  def computePatchSize(width: Int, numPatches: Int): Double
  def calculateHeight(worldHeight: Int, patchSize: Double): Int
  def calculateWidth(worldWidth: Int, patchSize: Double): Int
  def insetWidth(): Int
  def tickCounterLabel_=(label: String): Unit
  def tickCounterLabel: String
  def showTickCounter_=(visible: Boolean)
  def showTickCounter: Boolean
  def frameRate: Double
  def frameRate_=(rate: Double): Unit
}
