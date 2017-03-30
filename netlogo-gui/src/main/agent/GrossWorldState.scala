// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.TrailDrawerInterface

// The vars and methods in this track the rendering state of the world.
// They should be considered transient and equality should not take them into account.
trait GrossWorldState extends WorldKernel { this: CoreWorld =>
  // possibly need another array for 3D colors
  // since it seems messy to collapse 3D array into 2D
  protected var _patchColors: Array[Int] = _
  def patchColors: Array[Int] = _patchColors

  // this is used by the OpenGL texture code to decide whether
  // it needs to make a new texture or not - ST 2/9/05
  protected var _patchColorsDirty: Boolean = true
  def patchColorsDirty: Boolean = _patchColorsDirty
  private[agent] def patchColorsDirty(dirty: Boolean): Unit = { _patchColorsDirty = dirty }
  def markPatchColorsDirty(): Unit = { _patchColorsDirty = true }
  def markPatchColorsClean(): Unit = { _patchColorsDirty = false }

  // performance optimization -- avoid drawing an all-black bitmap if we
  // could just paint one big black rectangle
  protected var _patchesAllBlack = true
  def patchesAllBlack: Boolean = _patchesAllBlack
  private[agent] def patchesAllBlack(areBlack: Boolean): Unit = { _patchesAllBlack = areBlack }

  // for efficiency in Renderer
  protected var _patchesWithLabels: Int = 0
  def patchesWithLabels: Int = _patchesWithLabels
  private[agent] def addPatchLabel(): Unit = { _patchesWithLabels += 1 }
  private[agent] def removePatchLabel(): Unit = { _patchesWithLabels -= 1 }

  /// patch scratch
  //  a scratch area that can be used by commands such as _diffuse
  protected var _patchScratch: Array[Array[Double]] = _
  def getPatchScratch: Array[Array[Double]] = {
    if (_patchScratch == null) {
      _patchScratch = Array.ofDim[Double](_worldWidth, _worldHeight)
    }
    _patchScratch
  }

  // performance optimization for 3D renderer -- avoid sorting by distance
  // from observer unless we need to.  once this flag becomes true, we don't
  // work as hard as we could to return it back to false, because doing so
  // would be expensive.  we just reset it at clear-all time.
  protected var _mayHavePartiallyTransparentObjects = false
  def mayHavePartiallyTransparentObjects: Boolean = _mayHavePartiallyTransparentObjects
  private[agent] def mayHavePartiallyTransparentObjects(have: Boolean): Unit = {
    _mayHavePartiallyTransparentObjects = have
  }

  abstract override def clearAll(): Unit = {
    super.clearAll()
    _patchesAllBlack = true
    _mayHavePartiallyTransparentObjects = false
  }

  // This is a flag that the engine checks in its tightest innermost loops
  // to see if maybe it should stop running NetLogo code for a moment
  // and do something like halt or update the display.  It doesn't
  // particularly make sense to keep it in World, but since the check
  // occurs in inner loops, we want to put in a place where the engine
  // can get to it very quickly.  And since every Instruction has a
  // World object in it, the engine can always get to World quickly.
  //  - ST 1/10/07
  @volatile
  var comeUpForAir: Boolean = false  // NOPMD pmd doesn't like 'volatile'

  private var _displayOn: Boolean = true
  def displayOn = _displayOn
  def displayOn(displayOn: Boolean): Unit = {
    _displayOn = displayOn
  }

  // the trail drawer isn't as gross as the rest of the state and might actually be
  // *necessary* in a way that the rest of this trait isn't
  def trailDrawer(trailDrawer: TrailDrawerInterface): Unit = {
    _trailDrawer = trailDrawer
  }

  def trailDrawer = _trailDrawer
  private var _trailDrawer: TrailDrawerInterface = _
  def markDrawingClean(): Unit = {
    _trailDrawer.sendPixels(false)
  }
  def getDrawing: AnyRef = _trailDrawer.getDrawing
  def sendPixels: Boolean = _trailDrawer.sendPixels

  def copyGrossState(other: GrossWorldState): Unit = {
    other._patchColors = _patchColors
    other._trailDrawer = _trailDrawer
    other._displayOn = _displayOn
    other._mayHavePartiallyTransparentObjects = _mayHavePartiallyTransparentObjects
    other._patchScratch = _patchScratch
    other._patchesWithLabels = _patchesWithLabels
    other._patchesAllBlack = _patchesAllBlack
    other._patchColorsDirty = _patchColorsDirty
  }
}
