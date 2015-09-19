// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.World
import org.nlogo.awt.Hierarchy
import org.nlogo.swing.{ ModalProgressTask, OptionDialog }
import scala.collection.JavaConverters._

class WorldViewSettings2D(workspace: GUIWorkspace, gWidget: ViewWidget)
    extends WorldViewSettings(workspace, gWidget) {
  protected val world = workspace.world

  override def addDimensionProperties() = dimensionProperties ++= Properties.dims2D.asScala
  override def addWrappingProperties() = wrappingProperties ++= Properties.wrap2D.asScala

  def editFinished() = {
    gWidget.editFinished()

    if(wrappingChanged) {
      workspace.changeTopology(newWrapX, newWrapY)
      wrappingChanged = false
    }
    if(edgesChanged || patchSizeChanged) {
      resizeWithProgress(true)
      edgesChanged = false
      patchSizeChanged = false
    }
    if(fontSizeChanged) {
      gWidget.applyNewFontSize(newFontSize)
      fontSizeChanged = false
    }
    gWidget.view.dirty()
    gWidget.view.repaint()
    workspace.glView.editFinished()
    true
  }

  override def resizeWithProgress(showProgress: Boolean) = {
    val oldGraphicsOn = world.displayOn
    if(oldGraphicsOn) world.displayOn(false)

    val runnable = new Runnable {
      val KICK = 0
      val IGNORE = 1

      def run() = {
        if(edgesChanged) {
          /**
           * All turtles die when the world changes sizes.
           * If hubnet is running, we need the user a choice
           * of kicking out all the clients first, not kicking anyone,
           * or cancelling altogether.
           * This is because most hubnet clients will exhibit undefined
           * behavior because their turtle has died.
           */
          new Events.RemoveAllJobsEvent().raise(gWidget)
          if(hubnetDecision() == KICK /* kick clients first, then resize world */)
            workspace.hubNetManager.reset()

          world.clearTurtles()
          world.clearLinks()
          world.createPatches(newMinX, newMaxX, newMinY, newMaxY)
          workspace.patchesCreatedNotify()
          gWidget.resetSize()
        }
        if(patchSizeChanged) {
          world.patchSize(newPatchSize)
          gWidget.resetSize()
        }
        if(edgesChanged)
          gWidget.view.renderer.trailDrawer.clearDrawing()
        else
          gWidget.view.renderer.trailDrawer.rescaleDrawing()
      }

      private def hubnetDecision() =
        if(workspace.hubNetRunning) {
          val message = "Resizing the world kills all turtles. " +
            "This may cause HubNet clients to be unresponsive. " +
            "Consider kicking out all clients before proceeding."
          OptionDialog.show(workspace.getFrame, "Kick clients?", message,
            /* these things show up in reverse order on the popup, not sure why */
            Array("Kick clients", "Don't kick"))
        } else IGNORE
    }
    if(showProgress)
      ModalProgressTask(Hierarchy.getFrame(gWidget), "Resizing...", runnable, false)
    else
      runnable.run()
    gWidget.displaySwitchOn(true)
    if(oldGraphicsOn) {
      world.displayOn(true)
      gWidget.view.dirty()
      gWidget.view.repaint()
    }
  }

  override def save = "GRAPHICS-WINDOW\n" +
    gWidget.getBoundsString +
    (if(-world.minPxcor==world.maxPxcor) world.maxPxcor else -1) + "\n" +
    (if(-world.minPycor==world.maxPycor) world.maxPycor else -1) + "\n" +
    world.patchSize + "\n" + //7
    "1\n" + //8 bye bye shapesOn
    gWidget.view.fontSize + "\n" + //9
    // old exactDraw & hex settings, no longer used - ST 8/13/03
    "1\n1\n1\n0\n" +  // 10 11 12 13
    (if(world.wrappingAllowedInX) 1 else 0) + "\n" + // 14
    (if(world.wrappingAllowedInY) 1 else 0) + "\n" + //15
    "1\n" + // thin turtle pens are always on
    world.minPxcor + "\n" + // 17
    world.maxPxcor + "\n" + // 18
    world.minPycor + "\n" + // 19
    world.maxPycor + "\n" + // 20
    // saved twice for historical reasons
    (workspace.updateMode().save+"\n")*2 + // 21 22
    (if(showTickCounter) 1 else 0) + "\n" + // 23
    (if(tickCounterLabel.trim=="") "NIL" else tickCounterLabel) + "\n" + // 24
    frameRate + "\n" // 25

}
