// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.{ I18N, View => CoreView, WorldDimensions }
import org.nlogo.agent.World
import org.nlogo.awt.Hierarchy
import org.nlogo.swing.{ ModalProgressTask, OptionDialog }

object WorldViewSettings2D {
  private val HubNetKick   = 0
  private val HubNetIgnore = 1
}

class WorldViewSettings2D(workspace: GUIWorkspace, gw: ViewWidget, tickCounter: TickCounterLabel)
    extends WorldViewSettings(workspace, gw, tickCounter) {

  import WorldViewSettings2D._

  protected val world = workspace.world

  override def addDimensionProperties(): Unit = {
    dimensionProperties.addAll(Properties.dims2D)
  }

  override def addWrappingProperties(): Unit = {
    wrappingProperties.addAll(Properties.wrap2D)
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
        val message = I18N.gui.get("view.resize.hubnet.warning")
        val title = I18N.gui.get("view.resize.hubnet.prompt")
        val options = Array[Object](
          I18N.gui.get("view.resize.hubnet.kick"),
          I18N.gui.get("view.resize.hubnet.dontkick"))
        OptionDialog.show(workspace.getFrame, title, message, options)
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
      ModalProgressTask(Hierarchy.getFrame(gWidget),
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
    val b = gWidget.getBoundsTuple
    val dimensions = WorldDimensions(
      world.minPxcor, world.maxPxcor,
      world.minPycor, world.maxPycor,
      world.patchSize, world.wrappingAllowedInX, world.wrappingAllowedInY)
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
