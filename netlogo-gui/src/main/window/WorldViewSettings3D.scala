// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.World3D
import org.nlogo.awt.Hierarchy
import org.nlogo.core.{ I18N, View => CoreView, Widget => CoreWidget, WorldDimensions, WorldDimensions3D }
import org.nlogo.swing.ModalProgressTask
import org.nlogo.window.Events.RemoveAllJobsEvent

class WorldViewSettings3D(workspace: GUIWorkspace, gw: ViewWidget, tickCounter: TickCounterLabel)
  extends WorldViewSettings(workspace, gw, tickCounter) {

  protected val world: World3D = workspace.world.asInstanceOf[World3D]

  protected var newMinZ: Int = _
  protected var newMaxZ: Int = _

  protected var newWrapZ: Boolean = _

  override def editPanel: EditPanel = new WorldEditPanel3D(this)

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

  override def cornerConfigs: Seq[OriginConfiguration] = {
    Seq(
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.bottomSouthwest"), 0, 0, 0),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.bottomNorthwest"), 0, 1, 0),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.bottomNortheast"), 1, 1, 0),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.bottomSoutheast"), 1, 0, 0),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.topSouthwest"), 0, 0, 1),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.topNorthwest"), 0, 1, 1),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.topNortheast"), 1, 1, 1),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.corner.topSoutheast"), 1, 0, 1)
    )
  }

  override def edgeConfigs: Seq[OriginConfiguration] = {
    Seq(
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.south"), 0.5, 0, 0.5),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.north"), 0.5, 1, 0.5),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.east"), 1, 0.5, 0.5),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.west"), 0, 0.5, 0.5),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.bottom"), 0.5, 0.5, 0),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.3D.origin.location.edge.top"), 0.5, 0.5, 1)
    )
  }

  override def getNorms: (Float, Float, Float) = {
    ((minPxcor.toFloat / (maxPxcor - minPxcor)).abs,
     (minPycor.toFloat / (maxPycor - minPycor)).abs,
     (minPzcor.toFloat / (maxPzcor - minPzcor)).abs)
  }

  override def configureEditors(editors: Seq[WorldIntegerEditor]): Unit = {
    editors(0).setEnabled(originType == OriginType.Custom || originConfig.map(_.x != 0).getOrElse(false))
    editors(1).setEnabled(originConfig.map(_.x != 1).getOrElse(true))
    editors(2).setEnabled(originType == OriginType.Custom || originConfig.map(_.y != 0).getOrElse(false))
    editors(3).setEnabled(originConfig.map(_.y != 1).getOrElse(true))
    editors(4).setEnabled(originType == OriginType.Custom || originConfig.map(_.z != 0).getOrElse(false))
    editors(5).setEnabled(originConfig.map(_.z != 1).getOrElse(true))

    if (originType != OriginType.Custom) {
      val width = (maxPxcor - minPxcor).toDouble
      val height = (maxPycor - minPycor).toDouble
      val depth = (maxPzcor - minPzcor).toDouble

      val x = originConfig.map(_.x).getOrElse(0.5)
      val y = originConfig.map(_.y).getOrElse(0.5)
      val z = originConfig.map(_.z).getOrElse(0.5)

      editors(0).set((-width * x).toInt)
      editors(1).set((width * (1 - x)).toInt)
      editors(2).set((-height * y).toInt)
      editors(3).set((height * (1 - y)).toInt)
      editors(4).set((-depth).toInt)
      editors(5).set((depth * (1 - z)).toInt)
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

  override def model: CoreWidget = {
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
