// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.core.{ WorldDimensions }
import org.nlogo.api.{ Version, WorldDimensions3D }
import org.nlogo.workspace.{ AbstractWorkspace, WorldLoaderInterface, Controllable }

trait HeadlessWorkspaceBase extends AbstractWorkspace with WorldLoaderInterface with Controllable {

  /**
   * Has a model been opened in this workspace?
   */
  var modelOpened = false

  /**
   * If true, don't send anything to standard output.
   */
  var silent = false

  /**
   * Internal use only.
   */
  var compilerTestingMode = false


  /**
   * Internal use only.
   */
  def initForTesting(worldSize: Int) {
    initForTesting(worldSize, "")
  }

  /**
   * Internal use only.
   */
  def initForTesting(minPxcor: Int, maxPxcor: Int, minPycor: Int, maxPycor: Int, source: String) {
    initForTesting(new WorldDimensions(minPxcor, maxPxcor, minPycor, maxPycor), source)
  }

  /**
   * Internal use only.
   */
  def initForTesting(worldSize: Int, modelString: String) {
    if (Version.is3D)
      initForTesting(new WorldDimensions3D(
        -worldSize, worldSize, -worldSize, worldSize, -worldSize, worldSize),
      modelString)
    else
      initForTesting(-worldSize, worldSize, -worldSize, worldSize, modelString)
  }

  def initForTesting(d: WorldDimensions, source: String): Unit

  private var _frameRate = 0.0
  override def frameRate = _frameRate
  override def frameRate(frameRate: Double) { _frameRate = frameRate }

  private var _showTickCounter = true
  override def showTickCounter = _showTickCounter
  override def showTickCounter(showTickCounter: Boolean) { _showTickCounter = showTickCounter }

  private var _tickCounterLabel = "ticks"
  override def tickCounterLabel = _tickCounterLabel
  override def tickCounterLabel(s: String) { _tickCounterLabel = tickCounterLabel }

  override def getMinimumWidth = 0
  override def insetWidth = 0
  override def computePatchSize(width: Int, numPatches: Int): Double =
    width / numPatches
  override def calculateHeight(worldHeight: Int, patchSize: Double) =
    (worldHeight * patchSize).toInt
  def calculateWidth(worldWidth: Int, patchSize: Double): Int =
    (worldWidth * patchSize).toInt

  override def patchSize = world.patchSize
  override def patchSize(patchSize: Double) {
    world.patchSize(patchSize)
    renderer.resetCache(patchSize)
    renderer.trailDrawer.rescaleDrawing()
  }

  override def resizeView() { }
  override def setSize(x: Int, y: Int) { }

  override def clearTurtles() {
    if (!compilerTestingMode)
      world.clearTurtles()
  }
}
