// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.World3D
import org.nlogo.api.{ I18N, WorldDimensions }
import org.nlogo.awt.Hierarchy
import org.nlogo.swing.ModalProgressTask
import scala.collection.JavaConverters._

class WorldViewSettings3D(workspace: GUIWorkspace, gWidget: ViewWidget) extends WorldViewSettings(workspace, gWidget) {
  protected val world = workspace.world.asInstanceOf[World3D]

  protected var newMinZ: Int = 0
  def minPzcor = newMinZ
  def minPzcor_=(minPzcor: Int) = if(minPzcor <= 0) {
      newMinZ = minPzcor
      edgesChanged = edgesChanged || (newMinZ != world.minPzcor)
    }

  protected var newMaxZ: Int = 0
  def maxPzcor = newMaxZ
  def maxPzcor_=(maxPzcor: Int) = if(maxPzcor >= 0) {
      newMaxZ = maxPzcor
      edgesChanged = edgesChanged || (newMaxZ != world.maxPzcor)
    }

  protected var newWrapZ = false
  def wrappingZ = {
    if(!wrappingChanged)
      newWrapZ = world.wrappingAllowedInZ
    newWrapZ
  }
  def wrappingZ_=(value: Boolean) = {
    newWrapZ = value
    wrappingChanged = wrappingChanged || (newWrapZ != world.wrappingAllowedInZ)
  }

  override def addDimensionProperties() = dimensionProperties ++= Properties.dims3D.asScala
  override def addWrappingProperties() = wrappingProperties ++= Properties.wrap3D.asScala

  override def addCornerChoices() = {
    implicit val i18nName = I18N.Prefix("edit.viewSettings.3D.origin.location.corner")
    cornerChoices += new OriginConfiguration(I18N.gui("bottomSouthwest"),
      Array(false, true, false, true, false, true), Array(true, false, true, false, true, false))
    cornerChoices += new OriginConfiguration(I18N.gui("bottomNorthwest"),
      Array(false, true, true, false, false, true), Array(true, false, false, true, true, false))
    cornerChoices += new OriginConfiguration(I18N.gui("bottomNortheast"),
      Array(true, false, true, false, false, true), Array(false, true, false, true, true, false))
    cornerChoices += new OriginConfiguration(I18N.gui("bottomSoutheast"),
      Array(true, false, false, true, false, true), Array(false, true, true, false, true, false))
    cornerChoices += new OriginConfiguration(I18N.gui("topSouthwest"),
      Array(false, true, false, true, true, false), Array(true, false, true, false, false, true))
    cornerChoices += new OriginConfiguration(I18N.gui("topNorthwest"),
      Array(false, true, true, false, true, false), Array(true, false, false, true, false, true))
    cornerChoices += new OriginConfiguration(I18N.gui("topNortheast"),
      Array(true, false, true, false, true, false), Array(false, true, false, true, false, true))
    cornerChoices += new OriginConfiguration(I18N.gui("topSoutheast"),
      Array(true, false, false, true, true, false), Array(false, true, true, false, false, true))
  }

  override def addEdgeChoices() = {
    implicit val i18nName = I18N.Prefix("edit.viewSettings.3D.origin.location.edge")
    edgeChoices += new OriginConfiguration(I18N.gui("south"),
      Array(true, true, false, true, true, true), Array(false, false, true, false, false, false))
    edgeChoices += new OriginConfiguration(I18N.gui("north"),
      Array(true, true, true, false, true, true), Array(false, false, false, true, false, false))
    edgeChoices += new OriginConfiguration(I18N.gui("east"),
      Array(true, false, true, true, true, true), Array(false, true, false, false, false, false))
    edgeChoices += new OriginConfiguration(I18N.gui("west"),
      Array(false, true, true, true, true, true), Array(true, false, false, false, false, false))
    edgeChoices += new OriginConfiguration(I18N.gui("bottom"),
      Array(true, true, true, true, false, true), Array(false, false, false, false, true, false))
    edgeChoices += new OriginConfiguration(I18N.gui("top"),
      Array(true, true, true, true, true, false), Array(false, false, false, false, false, true))
  }

  override def addOriginConfigurations() = {
    implicit val i18nName = I18N.Prefix("edit.viewSettings.origin.location")
    originConfigurations += new OriginConfiguration(I18N.gui("center"),
      Array(false, true, false, true, false, true), Array(false, false, false, false, false, false))
    originConfigurations += new OriginConfiguration(I18N.gui("corner"),
      Array(true, true, true, true, true, true), Array(false, false, false, false, false, false))
    originConfigurations += new OriginConfiguration(I18N.gui("edge"),
      Array(true, true, true, true, true, true), Array(false, false, false, false, false, false))
    originConfigurations += new OriginConfiguration(I18N.gui("custom"),
      Array(true, true, true, true, true, true), Array(false, false, false, false, false, false))
  }

  override def firstEditor = 0
  override def lastEditor = 5

  override def getSelectedLocation = {
    val minx = minPxcor
    val maxx = maxPxcor
    val miny = minPycor
    val maxy = maxPycor
    val maxz = maxPzcor
    val minz = minPzcor

    if(minx == (-maxx) && miny == (-maxy) && minz == (-maxz))
      0
    else if((minx == 0 || maxx == 0) && (miny == 0 || maxy == 0) && (minz == 0 || maxz == 0))
      1
    else if (minx == 0 || maxx == 0 || miny == 0 || maxy == 0 || minz == 0 || maxz == 0)
      2
    else
      3
  }

  override def getSelectedConfiguration = {
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
    if(oldGraphicsOn)
      world.displayOn(false)

    val runnable = new Runnable {
        def run() = {
          if(edgesChanged) {
            new Events.RemoveAllJobsEvent().raise(gWidget)
            world.clearTurtles()
            world.clearLinks()
            world.createPatches(newMinX, newMaxX,
                newMinY, newMaxY,
                newMinZ, newMaxZ)
            workspace.patchesCreatedNotify()
            gWidget.resetSize()
          }
          if(patchSizeChanged) {
            world.patchSize(newPatchSize)
            gWidget.resetSize()
          }

          if(edgesChanged)
            workspace.clearDrawing()
          else
            gWidget.view.renderer.trailDrawer.rescaleDrawing()
        }
      }
    if(showProgress)
      ModalProgressTask(Hierarchy.getFrame(gWidget), "Resizing...", runnable, true)
    else
      runnable.run()
    gWidget.displaySwitchOn(true)
    if(oldGraphicsOn) {
      world.displayOn(true)
      gWidget.view.dirty()
      gWidget.view.repaint()
    }
  }

  override def setDimensions(d: WorldDimensions) = d match {
      case dd: org.nlogo.api.WorldDimensions3D =>
        setDimensions(dd.minPxcor, dd.maxPxcor, dd.minPycor, dd.maxPycor, dd.minPzcor, dd.maxPzcor)
      case _ =>
        setDimensions(d.minPxcor, d.maxPxcor, d.minPycor, d.maxPycor, 0, 0)
    }

  def setDimensions(minPxcor: Int, maxPxcor: Int,
      minPycor: Int, maxPycor: Int,
      minPzcor: Int, maxPzcor: Int) = {
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

  override def save = "GRAPHICS-WINDOW\n" +
    gWidget.getBoundsString +
    s"${if(-world.minPxcor == world.maxPxcor) world.maxPxcor else -1}\n" +
    s"${if(-world.minPycor == world.maxPycor) world.maxPycor else -1}\n" +
    s"${world.patchSize}\n" + //7
    "1\n" + //8 shapesOn
    s"${gWidget.view.fontSize}\n" + //9
    // old exactDraw & hex settings, no longer used - ST 8/13/03, 1/4/07
    "1\n1\n1\n0\n" +  // 10 11 12 13
    s"${if(world.wrappingAllowedInX) 1 else 0}\n" + // 14
    s"${if(world.wrappingAllowedInY) 1 else 0}\n" + // 15
    "1\n" + // thin turtle pens are always on 16
    s"${world.minPxcor}\n" + // 17
    s"${world.maxPxcor}\n" + // 18
    s"${world.minPycor}\n" + // 19
    s"${world.maxPycor}\n" + // 20
    s"${world.minPzcor}\n" + // 21
    s"${world.maxPzcor}\n" + // 22
    s"${if(world.wrappingAllowedInZ) 1 else 0}\n" + // 23
    s"${workspace.updateMode().save}\n" + // 24
    s"${if(showTickCounter) 1 else 0}\n" + // 25
    s"${if(tickCounterLabel.trim == "") "NIL" else tickCounterLabel}\n" + // 26
    s"$frameRate\n" // 27
}
