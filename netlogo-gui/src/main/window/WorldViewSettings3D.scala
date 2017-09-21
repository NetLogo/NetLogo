// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.{ I18N, View => CoreView, WorldDimensions }
import org.nlogo.api.{ RichWorldDimensions, WorldDimensions3D, WorldResizer },
  RichWorldDimensions._
import org.nlogo.agent.World3D

class WorldViewSettings3D(workspace: GUIWorkspaceScala)
  extends WorldViewSettings(workspace) {

  type DimensionsType = WorldDimensions3D

  protected val world: World3D = workspace.world.asInstanceOf[World3D]
  protected var _pendingDimensions: DimensionsType = WorldDimensions3D(0, 1, 0, 1, 0, 1, 12.0, true, true, true)
  protected var _currentDimensions: DimensionsType = WorldDimensions3D(0, 1, 0, 1, 0, 1, 12.0, true, true, true)

  override protected def wrappingChanged: Boolean =
    super.wrappingChanged ||
      _pendingDimensions.wrappingAllowedInZ != _currentDimensions.wrappingAllowedInZ

  override protected def edgesChanged: Boolean =
    super.edgesChanged ||
      _pendingDimensions.minPzcor != _currentDimensions.minPzcor ||
      _pendingDimensions.maxPzcor != _currentDimensions.maxPzcor

  def minPzcor(minPzcor: Int): Unit = {
    if (minPzcor <= 0) {
      _pendingDimensions = copyDimensions(minPzcor = minPzcor)
    }
  }

  def minPzcor: Int = _pendingDimensions.minPzcor

  def maxPzcor(maxPzcor: Int): Unit = {
    if (maxPzcor >= 0) {
      _pendingDimensions = copyDimensions(maxPzcor = maxPzcor)
    }
  }

  def maxPzcor: Int = _pendingDimensions.maxPzcor

  def wrappingZ: Boolean = _pendingDimensions.wrappingAllowedInZ

  def wrappingZ(value: Boolean): Unit = {
    _pendingDimensions = copyDimensions(wrappingAllowedInZ = value)
  }

  override def addDimensionProperties(): Unit = {
    dimensionProperties.addAll(Properties.dims3D)
  }

  override def addWrappingProperties(): Unit = {
    wrappingProperties.addAll(Properties.wrap3D)
  }

  override def addCornerChoices(): Unit = {
    cornerChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.bottomSouthwest"),
        Array(false, true, false, true, false, true),
        Array(true, false, true, false, true, false)))
    cornerChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.bottomNorthwest"),
        Array(false, true, true, false, false, true),
        Array(true, false, false, true, true, false)))
    cornerChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.bottomNortheast"),
        Array(true, false, true, false, false, true),
        Array(false, true, false, true, true, false)))
    cornerChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.bottomSoutheast"),
        Array(true, false, false, true, false, true),
        Array(false, true, true, false, true, false)))
    cornerChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.topSouthwest"),
        Array(false, true, false, true, true, false),
        Array(true, false, true, false, false, true)))
    cornerChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.topNorthwest"),
        Array(false, true, true, false, true, false),
        Array(true, false, false, true, false, true)))
    cornerChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.topNortheast"),
        Array(true, false, true, false, true, false),
        Array(false, true, false, true, false, true)))
    cornerChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.topSoutheast"),
        Array(true, false, false, true, true, false),
        Array(false, true, true, false, false, true)))
  }

  override def addEdgeChoices(): Unit = {
    edgeChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.south"),
        Array(true, true, false, true, true, true),
        Array(false, false, true, false, false, false)))
    edgeChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.north"),
        Array(true, true, true, false, true, true),
        Array(false, false, false, true, false, false)))
    edgeChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.east"),
        Array(true, false, true, true, true, true),
        Array(false, true, false, false, false, false)))
    edgeChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.west"),
        Array(false, true, true, true, true, true),
        Array(true, false, false, false, false, false)))
    edgeChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.bottom"),
        Array(true, true, true, true, false, true),
        Array(false, false, false, false, true, false)))
    edgeChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.top"),
        Array(true, true, true, true, true, false),
        Array(false, false, false, false, false, true)))
  }

  override def addOriginConfigurations(): Unit = {
    originConfigurations.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.center"),
        Array(false, true, false, true, false, true),
        Array(false, false, false, false, false, false)))
    originConfigurations.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner"),
        Array(true, true, true, true, true, true),
        Array(false, false, false, false, false, false)))
    originConfigurations.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge"),
        Array(true, true, true, true, true, true),
        Array(false, false, false, false, false, false)))
    originConfigurations.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.custom"),
        Array(true, true, true, true, true, true),
        Array(false, false, false, false, false, false)))
  }

  override def adjustDimensions(d: WorldDimensions): WorldDimensions = d
  override def calculateViewSize(d: WorldDimensions, v: CoreView) =
    (v.right - v.left, v.bottom - v.top)

  def copyDimensions(
    minPxcor: Int = _pendingDimensions.minPxcor,
    maxPxcor: Int = _pendingDimensions.maxPxcor,
    minPycor: Int = _pendingDimensions.minPycor,
    maxPycor: Int = _pendingDimensions.maxPycor,
    minPzcor: Int = _pendingDimensions.defaultMinPzcor,
    maxPzcor: Int = _pendingDimensions.defaultMaxPzcor,
    patchSize: Double = _pendingDimensions.patchSize,
    wrappingAllowedInX: Boolean = _pendingDimensions.wrappingAllowedInX,
    wrappingAllowedInY: Boolean = _pendingDimensions.wrappingAllowedInY,
    wrappingAllowedInZ: Boolean = _pendingDimensions.defaultWrappingInZ
  ): DimensionsType = {
    WorldDimensions3D(minPxcor, maxPxcor, minPycor, maxPycor, minPzcor, maxPzcor, patchSize,
      wrappingAllowedInX, wrappingAllowedInY, wrappingAllowedInZ)
  }

  def toDimensionType(d: WorldDimensions): DimensionsType = d.to3D

  override def firstEditor: Int = 0

  override def lastEditor: Int = 5

  override def getSelectedLocation: Int = {
    val minx = minPxcor
    val maxx = maxPxcor
    val miny = minPycor
    val maxy = maxPycor
    val maxz = maxPzcor
    val minz = minPzcor

    if (minx == (-maxx) && miny == (-maxy) && minz == (-maxz))
      0
    else if ((minx == 0 || maxx == 0)
        && (miny == 0 || maxy == 0)
        && (minz == 0 || maxz == 0))
      1
    else if (minx == 0 || maxx == 0 ||
        miny == 0 || maxy == 0 ||
        minz == 0 || maxz == 0)
      2
    else
      3
  }

  override def getSelectedConfiguration: Int = {
    val minx = minPxcor
    val maxx = maxPxcor
    val miny = minPycor
    val maxy = maxPycor
    val minz = minPzcor
    val maxz = maxPzcor

    if (minx == 0 && miny == 0 && minz == 0)
      0
    else if (minx == 0 && maxy == 0 && minz == 0)
      1
    else if (maxx == 0 && maxy == 0 && minz == 0)
      2
    else if (maxx == 0 && miny == 0 && minz == 0)
      3
    else if (minx == 0 && miny == 0 && maxz == 0)
      4
    else if (minx == 0 && maxy == 0 && maxz == 0)
      5
    else if (maxx == 0 && maxy == 0 && maxz == 0)
      6
    else if (maxx == 0 && miny == 0 && maxz == 0)
      7
    else if (minx == 0)
      3
    else if (maxx == 0)
      2
    else if (miny == 0)
      0
    else if (maxy == 0)
      1
    else if (minz == 0)
      4
    else if (maxz == 0)
      5
    else
      0
  }

  def editFinished(): Boolean = {
    notifyEditFinished()

    setDimensions(_pendingDimensions, true, WorldResizer.StopEverything)

    true
  }

  protected def createPatches(): Unit = {
    world.createPatches(_pendingDimensions.minPxcor, _pendingDimensions.maxPxcor,
      _pendingDimensions.minPycor, _pendingDimensions.maxPycor,
      _pendingDimensions.minPzcor, _pendingDimensions.maxPzcor)
  }

  override def model: CoreView = {
    val label = if (tickCounterLabel == null || tickCounterLabel.trim == "") None else Some(tickCounterLabel)
    CoreView(
      left = 0, top = 0, right = 0, bottom = 0,
      dimensions = _currentDimensions,
      fontSize = fontSize,
      updateMode = workspace.updateMode(),
      showTickCounter = showTickCounter,
      tickCounterLabel = label,
      frameRate = frameRate)
  }
}
