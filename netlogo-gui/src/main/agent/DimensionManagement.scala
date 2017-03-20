// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.{ AgentException, WorldDimensionException }
import org.nlogo.core.{ Program, WorldDimensions }
import java.util.{ HashMap => JHashMap, Map => JMap }

import java.lang.{ Double => JDouble, Integer => JInteger }

import World.NegativeOneInt

// TODO: There's a lot of repetition in this trait, it should be considered a candidate for code
// generation
trait DimensionManagement {
  private[agent] def topology: Topology

  private[agent] def getTopology: Topology = topology

  protected val dimensionVariableNames: Seq[String]

  private[agent] var rootsTable: RootsTable = _

  /// world geometry
  var _worldWidth: Int = _
  var _worldHeight: Int = _
  var _minPxcor: Int = _
  var _minPycor: Int = _
  var _maxPycor: Int = _
  var _maxPxcor: Int = _
  protected var _patchSize: Double = 12.0

  def worldWidth: Int = _worldWidth
  def worldHeight: Int = _worldHeight
  def minPxcor: Int = _minPxcor
  def minPycor: Int = _minPycor
  def maxPxcor: Int = _maxPxcor
  def maxPycor: Int = _maxPycor

  // boxed versions of geometry/size methods, for efficiency
  var _worldWidthBoxed: JDouble = JDouble.valueOf(_worldWidth)
  var _worldHeightBoxed: JDouble = JDouble.valueOf(_worldHeight)
  var _minPxcorBoxed: JDouble = JDouble.valueOf(_minPxcor)
  var _minPycorBoxed: JDouble = JDouble.valueOf(_minPycor)
  var _maxPxcorBoxed: JDouble = JDouble.valueOf(_maxPxcor)
  var _maxPycorBoxed: JDouble = JDouble.valueOf(_maxPycor)

  def patchSize: Double = _patchSize

  def patchSize(patchSize: Double): Boolean =
    if (_patchSize != patchSize) {
      _patchSize = patchSize
      true
    } else {
      false
    }

  def worldWidthBoxed = _worldWidthBoxed
  def worldHeightBoxed = _worldHeightBoxed
  def minPxcorBoxed = _minPxcorBoxed
  def minPycorBoxed = _minPycorBoxed
  def maxPxcorBoxed = _maxPxcorBoxed
  def maxPycorBoxed = _maxPycorBoxed

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
    if (variableName.equalsIgnoreCase("MIN-PXCOR")) {
      new WorldDimensions(value,      d.maxPxcor, d.minPycor, d.maxPycor, patchSize, wrappingAllowedInX, wrappingAllowedInY);
    } else if (variableName.equalsIgnoreCase("MAX-PXCOR")) {
      new WorldDimensions(d.minPxcor, value,      d.minPycor, d.maxPycor, patchSize, wrappingAllowedInX, wrappingAllowedInY);
    } else if (variableName.equalsIgnoreCase("MIN-PYCOR")) {
      new WorldDimensions(d.minPxcor, d.maxPxcor, value,      d.maxPycor, patchSize, wrappingAllowedInX, wrappingAllowedInY);
    } else if (variableName.equalsIgnoreCase("MAX-PYCOR")) {
      new WorldDimensions(d.minPxcor, d.maxPxcor, d.minPycor, value,     patchSize, wrappingAllowedInX, wrappingAllowedInY);
    } else if (variableName.equalsIgnoreCase("WORLD-WIDTH")) {
      val minPxcor = growMin(d.minPxcor, d.maxPxcor, value, d.minPxcor)
      val maxPxcor = growMax(d.minPxcor, d.maxPxcor, value, d.maxPxcor)
      new WorldDimensions(minPxcor, maxPxcor, d.minPycor, d.maxPycor, patchSize, wrappingAllowedInX, wrappingAllowedInY);
    } else if (variableName.equalsIgnoreCase("WORLD-HEIGHT")) {
      val minPycor = growMin(d.minPycor, d.maxPycor, value, d.minPycor)
      val maxPycor = growMax(d.minPycor, d.maxPycor, value, d.maxPycor)
      new WorldDimensions(d.minPxcor, d.maxPxcor, minPycor, maxPycor, patchSize, wrappingAllowedInX, wrappingAllowedInY);
    } else {
      d
    }
  }

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
    } else if (max == 0)
    -(value - 1)
  else
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
    } else if (min == 0)
    (value - 1)
  else
    d
  }

  @throws(classOf[AgentException])
  def roundX(x: Double): Int = {
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
  def roundY(y: Double): Int = {
    // floor() is slow so we don't use it
    val wrappedY =
      try {
        topology.wrapY(y);
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
    other._worldWidth = _worldWidth
    other._worldHeight = _worldHeight
    other._minPxcor = _minPxcor
    other._minPycor = _minPycor
    other._maxPycor = _maxPycor
    other._maxPxcor = _maxPxcor
    other._patchSize = _patchSize
    other._worldWidthBoxed = _worldWidthBoxed
    other._worldHeightBoxed = _worldHeightBoxed
    other._minPxcorBoxed = _minPxcorBoxed
    other._minPycorBoxed = _minPycorBoxed
    other._maxPxcorBoxed = _maxPxcorBoxed
    other._maxPycorBoxed = _maxPycorBoxed
  }
}
