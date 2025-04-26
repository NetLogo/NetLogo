// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.awt.Hierarchy
import org.nlogo.core.{ I18N, View => CoreView, Widget => CoreWidget, WorldDimensions }
import org.nlogo.swing.{ ModalProgressTask, OptionPane }

object WorldViewSettings2D {
  private val HubNetKick   = 0
  private val HubNetIgnore = 1
}

class WorldViewSettings2D(workspace: GUIWorkspace, gw: ViewWidget, tickCounter: TickCounterLabel)
    extends WorldViewSettings(workspace, gw, tickCounter) {

  import WorldViewSettings2D._

  protected val world = workspace.world

  override def editPanel: EditPanel = new WorldEditPanel2D(this)

  override def cornerConfigs: Seq[OriginConfiguration] = {
    Seq(
      OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner.bottomLeft"), 0, 0),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner.topLeft"), 0, 1),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner.topRight"), 1, 1),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner.bottomRight"), 1, 0)
    )
  }

  override def edgeConfigs: Seq[OriginConfiguration] = {
    Seq(
      OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge.bottom"), 0.5, 0),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge.top"), 0.5, 1),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge.right"), 1, 0.5),
      OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge.left"), 0, 0.5)
    )
  }

  override def getNorms: (Float, Float, Float) = {
    ((minPxcor.toFloat / (maxPxcor - minPxcor)).abs,
     (minPycor.toFloat / (maxPycor - minPycor)).abs,
     0.5f)
  }

  override def configureEditors(editors: Seq[WorldIntegerEditor]): Unit = {
    editors(0).setEnabled(originType == OriginType.Custom || originConfig.map(_.x != 0).getOrElse(false))
    editors(1).setEnabled(originConfig.map(_.x != 1).getOrElse(true))
    editors(2).setEnabled(originType == OriginType.Custom || originConfig.map(_.y != 0).getOrElse(false))
    editors(3).setEnabled(originConfig.map(_.y != 1).getOrElse(true))

    if (originType != OriginType.Custom) {
      val width = (maxPxcor - minPxcor).toDouble
      val height = (maxPycor - minPycor).toDouble

      val x = originConfig.map(_.x).getOrElse(0.5)
      val y = originConfig.map(_.y).getOrElse(0.5)

      editors(0).set((-width * x).toInt)
      editors(1).set((width * (1 - x)).toInt)
      editors(2).set((-height * y).toInt)
      editors(3).set((height * (1 - y)).toInt)
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

  override def model: CoreWidget = {
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
