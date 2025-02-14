// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.Property
import org.nlogo.awt.Hierarchy
import org.nlogo.core.{ I18N, View => CoreView, WorldDimensions }
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

  override def cornerChoices: Seq[OriginConfiguration] = {
    Seq(
      new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner.bottomLeft"),
        Array(false, true, false, true),
        Array(true, false, true, false)),
      new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner.topLeft"),
        Array(false, true, true, false),
        Array(true, false, false, true)),
      new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner.topRight"),
        Array(true, false, true, false),
        Array(false, true, false, true)),
      new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner.bottomRight"),
        Array(true, false, false, true),
        Array(false, true, true, false))
    )
  }

  override def edgeChoices: Seq[OriginConfiguration] = {
    Seq(
      new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge.bottom"),
        Array(true, true, false, true),
        Array(false, false, true, false)),
      new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge.top"),
        Array(true, true, true, false),
        Array(false, false, false, true)),
      new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge.right"),
        Array(true, false, true, true),
        Array(false, true, false, false)),
      new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge.left"),
        Array(false, true, true, true),
        Array(true, false, false, false))
    )
  }

  override def originConfigurations: Seq[OriginConfiguration] = {
    Seq(
      new OriginConfiguration
        (I18N.gui.get("edit.viewSettings.origin.location.center"),
          Array(false, true, false, true),
          Array(false, false, false, false)),
      new OriginConfiguration
        (I18N.gui.get("edit.viewSettings.origin.location.corner"),
          Array(true, true, true, true),
          Array(false, false, false, false)),
      new OriginConfiguration
        (I18N.gui.get("edit.viewSettings.origin.location.edge"),
          Array(true, true, true, true),
          Array(false, false, false, false)),
      new OriginConfiguration
        (I18N.gui.get("edit.viewSettings.origin.location.custom"),
          Array(true, true, true, true),
          Array(false, false, false, false))
    )
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
                       List(I18N.gui.get("view.resize.hubnet.kick"), I18N.gui.get("view.resize.hubnet.dontkick")),
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
