// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.{ AgentException, WorldDimensionException }
import org.nlogo.core.WorldDimensions

import java.lang.{ Double => JDouble }

trait DimensionManagement { this: WorldJ =>
  def topology: Topology
  def getDimensions: WorldDimensions

  private[agent] def getTopology: Topology = topology

  protected val dimensionVariableNames: Seq[String]

  private[agent] var rootsTable: RootsTable = _

  // world geometry. The _...Boxed versions are for efficiency
  private def worldWidth(i: Int) = { _worldWidth = i }
  def worldWidth: Int = _worldWidth
  var _worldWidthBoxed: JDouble = JDouble.valueOf(_worldWidth)
  def worldWidthBoxed = _worldWidthBoxed

  private def worldHeight(i: Int) = { _worldHeight = i }
  def worldHeight: Int = _worldHeight
  var _worldHeightBoxed: JDouble = JDouble.valueOf(_worldHeight)
  def worldHeightBoxed = _worldHeightBoxed

  private def minPxcor(i: Int) = { _minPxcor = i }
  def minPxcor: Int = _minPxcor
  var _minPxcorBoxed: JDouble = JDouble.valueOf(_minPxcor)
  def minPxcorBoxed = _minPxcorBoxed

  private def maxPxcor(i: Int) = { _maxPxcor = i }
  def maxPxcor: Int = _maxPxcor
  var _maxPxcorBoxed: JDouble = JDouble.valueOf(_maxPxcor)
  def maxPxcorBoxed = _maxPxcorBoxed

  private def minPycor(i: Int) = { _minPycor = i }
  def minPycor: Int = _minPycor
  var _minPycorBoxed: JDouble = JDouble.valueOf(_minPycor)
  def minPycorBoxed = _minPycorBoxed

  private def maxPycor(i: Int) = { _maxPycor = i }
  def maxPycor: Int = _maxPycor
  var _maxPycorBoxed: JDouble = JDouble.valueOf(_maxPycor)
  def maxPycorBoxed = _maxPycorBoxed

  def patchSize: Double = _patchSize
  def patchSize(patchSize: Double): Boolean =
    if (_patchSize != patchSize) {
      _patchSize = patchSize
      true
    } else {
      false
    }

  // These are just being used for setting the checkboxes in the ViewWidget config dialog
  //   with default values. These should no be used within World to control any behavior.
  //   All wrapping related behavior specific to a topology is/should-be hardcoded in the methods
  //   for each specific topological implementation.
  def wrappingAllowedInX: Boolean =
    topology.isInstanceOf[Torus] || topology.isInstanceOf[VertCylinder]

  def wrappingAllowedInY: Boolean =
    topology.isInstanceOf[Torus] || topology.isInstanceOf[HorizCylinder]

  def isDimensionVariable(variableName: String): Boolean =
    dimensionVariableNames.contains(variableName.toUpperCase)

  @throws(classOf[WorldDimensionException])
  def setDimensionVariable(variableName: String, value: Int, d: WorldDimensions): WorldDimensions = {
    variableName.toUpperCase match {
      case "MIN-PXCOR" => d.copy(minPxcor = value)
      case "MAX-PXCOR" => d.copy(maxPxcor = value)
      case "MIN-PYCOR" => d.copy(minPycor = value)
      case "MAX-PYCOR" => d.copy(maxPycor = value)
      case "WORLD-WIDTH" =>
        val newMin = growMin(d.minPxcor, d.maxPxcor, value, d.minPxcor)
        val newMax = growMax(d.minPxcor, d.maxPxcor, value, d.maxPxcor)
        d.copy(minPxcor = newMin, maxPxcor = newMax)
      case "WORLD-HEIGHT" =>
        val newMin = growMin(d.minPycor, d.maxPycor, value, d.minPycor)
        val newMax = growMax(d.minPycor, d.maxPycor, value, d.maxPycor)
        d.copy(minPycor = newMin, maxPycor = newMax)
      case _ => d
    }
  }

  def dimensionsAdjustedForPatchSize(patchSize: Double): WorldDimensions =
    getDimensions.copy(patchSize = patchSize)

  def equalDimensions(d: WorldDimensions): Boolean =
    d.minPxcor == minPxcor &&
      d.maxPxcor == maxPxcor &&
      d.minPycor == minPycor &&
      d.maxPycor == maxPycor

  def validPatchCoordinates(xc: Int, yc: Int): Boolean =
    xc >= _minPxcor && xc <= _maxPxcor && yc >= _minPycor && yc <= _maxPycor

  @throws(classOf[WorldDimensionException])
  def growMin(min: Int, max: Int, value: Int, d: Int): Int = {
    if (value < 1) {
      throw new WorldDimensionException()
    }

    if (max == -min) {
      if (value % 2 != 1) {
        throw new WorldDimensionException()
      }
      -(value - 1) / 2
    } else if (max == 0) {
      -(value - 1)
    } else
      return d
  }

  @throws(classOf[WorldDimensionException])
  def growMax(min: Int, max: Int, value: Int, d: Int): Int = {
    if (value < 1) {
      throw new WorldDimensionException()
    }

    if (max == -min) {
      if (value % 2 != 1) {
        throw new WorldDimensionException()
      }
      (value - 1) / 2
    } else if (min == 0) {
      (value - 1)
    } else
      d
  }

  @throws(classOf[AgentException])
  def wrapAndRoundX(x: Double): Int = {
    // floor() is slow so we don't use it
    val wrappedX =
      try {
        topology.wrapX(x)
      } catch {
        case ex: AgentException => throw new AgentException("Cannot access patches beyond the limits of current world.")
      }
    if (wrappedX > 0) {
      (wrappedX + 0.5).toInt
    } else {
      val intPart = wrappedX.toInt
      val fractPart = intPart - wrappedX
      if (fractPart > 0.5) intPart - 1 else intPart
    }
  }

  @throws(classOf[AgentException])
  def wrapAndRoundY(y: Double): Int = {
    // floor() is slow so we don't use it
    val wrappedY =
      try {
        topology.wrapY(y)
      } catch {
        case ex: AgentException => throw new AgentException("Cannot access patches beyond the limits of current world.")
      }
    if (wrappedY > 0) {
      (wrappedY + 0.5).toInt
    } else {
      val intPart = wrappedY.toInt
      val fractPart = intPart - wrappedY
      if (fractPart > 0.5) intPart - 1 else intPart
    }
  }

  @throws(classOf[AgentException])
  def wrapX(x: Double): Double = topology.wrapX(x)
  @throws(classOf[AgentException])
  def wrapY(y: Double): Double = topology.wrapY(y)

  def wrap(pos: Double, min: Double, max: Double): Double =
    Topology.wrap(pos, min, max)

  def copyDimensions(other: DimensionManagement): Unit = {
    other.rootsTable = rootsTable
    other.worldWidth(_worldWidth)
    other.worldHeight(_worldHeight)
    other.minPxcor(_minPxcor)
    other.minPycor(_minPycor)
    other.maxPycor(_maxPycor)
    other.maxPxcor(_maxPxcor)
    other.patchSize(_patchSize)
    other._worldWidthBoxed = _worldWidthBoxed
    other._worldHeightBoxed = _worldHeightBoxed
    other._minPxcorBoxed = _minPxcorBoxed
    other._minPycorBoxed = _minPycorBoxed
    other._maxPxcorBoxed = _maxPxcorBoxed
    other._maxPycorBoxed = _maxPycorBoxed
  }
}
