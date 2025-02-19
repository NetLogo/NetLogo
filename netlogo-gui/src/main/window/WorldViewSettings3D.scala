// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.World3D
import org.nlogo.api.Property
import org.nlogo.awt.Hierarchy
import org.nlogo.core.{ I18N, View => CoreView, WorldDimensions, WorldDimensions3D }
import org.nlogo.properties.IntegerEditor
import org.nlogo.swing.ModalProgressTask
import org.nlogo.window.Events.RemoveAllJobsEvent

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

  override def dimensionProperties: Seq[Property] =
    Properties.dims3D

  override def wrappingProperties: Seq[Property] =
    Properties.wrap3D

  override def viewProperties: Seq[Property] =
    Properties.view2D ++ Properties.view3D

  override def cornerConfigs: Seq[OriginConfiguration] = {
    Seq(
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.bottomSouthwest"),
                          Extent.Min, Extent.Min, Extent.Min),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.bottomNorthwest"),
                          Extent.Min, Extent.Max, Extent.Min),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.bottomNortheast"),
                          Extent.Max, Extent.Max, Extent.Min),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.bottomSoutheast"),
                          Extent.Max, Extent.Min, Extent.Min),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.topSouthwest"),
                          Extent.Min, Extent.Min, Extent.Max),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.topNorthwest"),
                          Extent.Min, Extent.Max, Extent.Max),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.topNortheast"),
                          Extent.Max, Extent.Max, Extent.Max),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.topSoutheast"),
                          Extent.Max, Extent.Min, Extent.Max)
    )
  }

  override def edgeConfigs: Seq[OriginConfiguration] = {
    Seq(
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.south"),
                          Extent.Center, Extent.Min, Extent.Center),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.north"),
                          Extent.Center, Extent.Max, Extent.Center),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.east"),
                          Extent.Max, Extent.Center, Extent.Center),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.west"),
                          Extent.Min, Extent.Center, Extent.Center),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.bottom"),
                          Extent.Center, Extent.Center, Extent.Min),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.top"),
                          Extent.Center, Extent.Center, Extent.Max)
    )
  }

  override def configureEditors(editors: Seq[IntegerEditor]): Unit = {
    editors(0).setEnabled(originType == OriginType.Custom || originConfig.map(_.x != Extent.Min).getOrElse(false))
    editors(1).setEnabled(originConfig.map(_.x != Extent.Max).getOrElse(true))
    editors(2).setEnabled(originType == OriginType.Custom || originConfig.map(_.y != Extent.Min).getOrElse(false))
    editors(3).setEnabled(originConfig.map(_.y != Extent.Max).getOrElse(true))
    editors(4).setEnabled(originType == OriginType.Custom || originConfig.map(_.z != Extent.Min).getOrElse(false))
    editors(5).setEnabled(originConfig.map(_.z != Extent.Max).getOrElse(true))

    if (originType != OriginType.Custom) {
      val width = maxPxcor - minPxcor
      val height = maxPycor - minPycor
      val depth = maxPzcor - minPzcor

      originConfig.map(_.x) match {
        case Some(Extent.Min) =>
          editors(0).set(0)
          editors(1).set(width)

        case Some(Extent.Max) =>
          editors(0).set(-width)
          editors(1).set(0)

        case _ =>
          editors(0).set(-width / 2)
          editors(1).set(width / 2)
      }

      originConfig.map(_.y) match {
        case Some(Extent.Min) =>
          editors(2).set(0)
          editors(3).set(height)

        case Some(Extent.Max) =>
          editors(2).set(-height)
          editors(3).set(0)

        case _ =>
          editors(2).set(-height / 2)
          editors(3).set(height / 2)
      }

      originConfig.map(_.z) match {
        case Some(Extent.Min) =>
          editors(4).set(0)
          editors(5).set(depth)

        case Some(Extent.Max) =>
          editors(4).set(-depth)
          editors(5).set(0)

        case _ =>
          editors(4).set(-depth / 2)
          editors(5).set(depth / 2)
      }
    }
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
      ModalProgressTask.onUIThread(Hierarchy.getFrame(gWidget),
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
    val b = gWidget.getUnzoomedBounds
    val label = if (tickCounterLabel == null || tickCounterLabel.trim == "") None else Some(tickCounterLabel)
    CoreView(
      x = b.x, y = b.y, width = b.width, height = b.height,
      dimensions = dimensions,
      fontSize = gWidget.view.fontSize,
      updateMode = workspace.updateMode(),
      showTickCounter = showTickCounter,
      tickCounterLabel = label,
      frameRate = frameRate)
  }

  override def fontSize(newSize: Int): Unit = {
    // AAB 03/26/2020 - fix issue #1811
    // Font value not being set from .nlogo3d file
    gWidget.view.applyNewFontSize(newSize, 0)
    super.fontSize(newSize)
  }
}
