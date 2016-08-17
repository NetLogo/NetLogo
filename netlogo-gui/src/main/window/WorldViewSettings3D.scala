// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.core.{ I18N, View => CoreView, WorldDimensions }
import org.nlogo.api.WorldDimensions3D
import org.nlogo.agent.World3D
import org.nlogo.window.Events.RemoveAllJobsEvent
import org.nlogo.swing.ModalProgressTask
import org.nlogo.awt.Hierarchy

class WorldViewSettings3D(workspace: GUIWorkspace, gw: ViewWidget, tickCounter: TickCounterLabel)
  extends WorldViewSettings(workspace, gw, tickCounter) {

  protected val world: World3D = workspace.world.asInstanceOf[World3D]

  protected var newMinZ: Int = _
  protected var newMaxZ: Int = _

  protected var newWrapZ: Boolean = _

  def minPzcor(minPzcor: Int): Unit = {
    if (minPzcor <= 0) {
      newMinZ = minPzcor
      edgesChanged ||= newMinZ != world.minPzcor
    }
  }

  def minPzcor: Int = newMinZ

  def maxPzcor(maxPzcor: Int): Unit = {
    if (maxPzcor >= 0) {
      newMaxZ = maxPzcor
      edgesChanged ||= newMaxZ != world.maxPzcor
    }
  }

  def maxPzcor: Int = newMaxZ

  def wrappingZ: Boolean = {
    if (!wrappingChanged)
      newWrapZ = world.wrappingAllowedInZ

    newWrapZ
  }

  def wrappingZ(value: Boolean): Unit = {
    newWrapZ = value
    wrappingChanged ||= newWrapZ != world.wrappingAllowedInZ
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
    gWidget.editFinished()

    if (wrappingChanged) {
      workspace.changeTopology(newWrapX, newWrapY)
      wrappingChanged = false
    }
    if (edgesChanged || patchSizeChanged) {
      resizeWithProgress(true)
      edgesChanged = false
      patchSizeChanged = false
    }
    if (fontSizeChanged) {
      gWidget.applyNewFontSize(newFontSize)
      fontSizeChanged = false
    }
    gWidget.view.dirty()
    gWidget.view.repaint()
    workspace.glView.editFinished()
    true
  }

  class ResizeRunner extends Runnable {
    def run(): Unit = {
      try {
        if (edgesChanged) {
          new RemoveAllJobsEvent().raise(gWidget)
          world.clearTurtles()
          world.clearLinks()
          world.createPatches(newMinX, newMaxX,
            newMinY, newMaxY, newMinZ, newMaxZ)
          workspace.patchesCreatedNotify()
          gWidget.resetSize()
        }
        if (patchSizeChanged) {
          world.patchSize(newPatchSize)
          gWidget.resetSize()
        }

        if (edgesChanged)
          workspace.clearDrawing()
        else
          gWidget.view.renderer.trailDrawer.rescaleDrawing()
        } catch {
          case e: Exception =>
            println("Exception in resizing thread: " + e + " " + e.getMessage)
            throw e
        }
    }
  }

  override def resizeWithProgress(showProgress: Boolean): Unit = {
    val oldGraphicsOn = world.displayOn
    if (oldGraphicsOn)
      world.displayOn(false)

    val runnable = new ResizeRunner()
    if (showProgress)
      ModalProgressTask.apply(Hierarchy.getFrame(gWidget),
        I18N.gui.get("view.resize.progress"), runnable)
    else
      runnable.run()

    gWidget.displaySwitchOn(true)
    if (oldGraphicsOn) {
      world.displayOn(true)
      gWidget.view.dirty()
      gWidget.view.repaint()
    }
  }

  override def setDimensions(d: WorldDimensions): Unit = {
    d match {
      case dd: WorldDimensions3D =>
        setDimensions(dd.minPxcor, dd.maxPxcor, dd.minPycor, dd.maxPycor, dd.minPzcor, dd.maxPzcor)
      case d =>
        setDimensions(d.minPxcor, d.maxPxcor, d.minPycor, d.maxPycor, 0, 0)
    }
  }

  def setDimensions(minPxcor: Int, maxPxcor: Int,
                    minPycor: Int, maxPycor: Int,
                    minPzcor: Int, maxPzcor: Int): Unit = {
    newMinX = minPxcor
    newMaxX = maxPxcor
    newMinY = minPycor
    newMaxY = maxPycor
    newMinZ = minPzcor
    newMaxZ = maxPzcor

    if (minPxcor != world.minPxcor ||
        maxPxcor != world.maxPxcor ||
        minPycor != world.minPycor ||
        maxPycor != world.maxPycor ||
        minPzcor != world.minPzcor ||
        maxPzcor != world.maxPzcor) {
      prepareForWorldResize()
      world.createPatches(minPxcor, maxPxcor, minPycor, maxPycor, minPzcor, maxPzcor)
      finishWorldResize()
    }
  }

  override def model: CoreView = {
    val dimensions = new WorldDimensions3D(
      world.minPxcor, world.maxPxcor,
      world.minPycor, world.maxPycor,
      world.minPzcor, world.maxPzcor,
      patchSize = world.patchSize,
      wrappingAllowedInX = world.wrappingAllowedInX,
      wrappingAllowedInY = world.wrappingAllowedInY,
      wrappingAllowedInZ = world.wrappingAllowedInZ)
    val b = gWidget.getBoundsTuple
    val label = if (tickCounterLabel == null || tickCounterLabel.trim == "") None else Some(tickCounterLabel)
    CoreView(
      left = b._1, top = b._2, right = b._3, bottom = b._4,
      dimensions = dimensions,
      fontSize = gWidget.view.fontSize,
      updateMode = workspace.updateMode(),
      showTickCounter = showTickCounter,
      tickCounterLabel = label,
      frameRate = frameRate)
  }
}
