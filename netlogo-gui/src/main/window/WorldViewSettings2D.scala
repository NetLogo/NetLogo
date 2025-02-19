// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.Property
import org.nlogo.awt.Hierarchy
import org.nlogo.core.{ I18N, View => CoreView, WorldDimensions }
import org.nlogo.properties.IntegerEditor
import org.nlogo.swing.{ ModalProgressTask, OptionPane }

object WorldViewSettings2D {
  private val HubNetKick   = 0
  private val HubNetIgnore = 1
}

class WorldViewSettings2D(workspace: GUIWorkspace, gw: ViewWidget, tickCounter: TickCounterLabel)
    extends WorldViewSettings(workspace, gw, tickCounter) {

  import WorldViewSettings2D._

  protected val world = workspace.world

  override def dimensionProperties: Seq[Property] =
    Properties.dims2D

  override def wrappingProperties: Seq[Property] =
    Properties.wrap2D

  override def viewProperties: Seq[Property] =
    Properties.view2D

  override def cornerConfigs: Seq[OriginConfiguration] = {
    Seq(
      OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner.bottomLeft"), Extent.Min, Extent.Min),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner.topLeft"), Extent.Min, Extent.Max),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner.topRight"), Extent.Max, Extent.Max),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner.bottomRight"), Extent.Max, Extent.Min)
    )
  }

  override def edgeConfigs: Seq[OriginConfiguration] = {
    Seq(
      OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge.bottom"), Extent.Center, Extent.Min),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge.top"), Extent.Center, Extent.Max),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge.right"), Extent.Max, Extent.Center),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge.left"), Extent.Min, Extent.Center)
    )
  }

  override def configureEditors(editors: Seq[IntegerEditor]): Unit = {
    editors(0).setEnabled(originType == OriginType.Custom || originConfig.map(_.x != Extent.Min).getOrElse(false))
    editors(1).setEnabled(originConfig.map(_.x != Extent.Max).getOrElse(true))
    editors(2).setEnabled(originType == OriginType.Custom || originConfig.map(_.y != Extent.Min).getOrElse(false))
    editors(3).setEnabled(originConfig.map(_.y != Extent.Max).getOrElse(true))

    if (originType != OriginType.Custom) {
      val width = maxPxcor - minPxcor
      val height = maxPycor - minPycor

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
    workspace.glView.editFinished

    true
  }

  class RunResize extends Runnable {
    /**
     * All turtles die when the world changes sizes.
     * If hubnet is running, we need the user a choice
     * of kicking out all the clients first, not kicking anyone,
     * or cancelling altogether.
     * This is because most hubnet clients will exhibit undefined
     * behavior because their turtle has died.
     */
    override def run(): Unit = {
      if (edgesChanged) {
        new org.nlogo.window.Events.RemoveAllJobsEvent().raise(gWidget)
        if (hubnetDecision() == HubNetKick) {
          /* kick clients first, then resize world */
         workspace.hubNetManager.foreach(_.reset())
        }

        world.clearTurtles()
        world.clearLinks()
        world.createPatches(newMinX, newMaxX, newMinY, newMaxY)
        workspace.patchesCreatedNotify()
        gWidget.resetSize()
      }
      if (patchSizeChanged) {
        world.patchSize(newPatchSize)
        gWidget.resetSize()
      }

      if (edgesChanged)
        gWidget.view.renderer.trailDrawer.clearDrawing();
      else
        gWidget.view.renderer.trailDrawer.rescaleDrawing();
    }

    private def hubnetDecision(): Int = {
      if (workspace.hubNetRunning) {
        new OptionPane(workspace.getFrame, I18N.gui.get("view.resize.hubnet.prompt"),
                       I18N.gui.get("view.resize.hubnet.warning"),
                       Seq(I18N.gui.get("view.resize.hubnet.kick"), I18N.gui.get("view.resize.hubnet.dontkick")),
                       OptionPane.Icons.Question).getSelectedIndex
      } else
        HubNetIgnore
    }
  }

  override def resizeWithProgress(showProgress: Boolean): Unit = {
    val oldGraphicsOn = world.displayOn
    if (oldGraphicsOn)
      world.displayOn(false)

    val runnable = new RunResize()
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

  override def model: CoreView = {
    val b = gWidget.getUnzoomedBounds
    val dimensions = WorldDimensions(
      world.minPxcor, world.maxPxcor,
      world.minPycor, world.maxPycor,
      world.patchSize, world.wrappingAllowedInX, world.wrappingAllowedInY)
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
}
