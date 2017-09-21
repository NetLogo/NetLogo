// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Dimension
import org.nlogo.core.{ I18N, View => CoreView, WorldDimensions }
import
  org.nlogo.api.{ RichWorldDimensions, WorldResizer },
    RichWorldDimensions._
import org.nlogo.swing.OptionDialog
import org.nlogo.workspace.DefaultWorldLoader

object WorldViewSettings2D {
  private val HubNetKick   = 0
  private val HubNetIgnore = 1
}

class WorldViewSettings2D(workspace: GUIWorkspaceScala, padding: Dimension)
  extends WorldViewSettings(workspace)
  with DefaultWorldLoader {

  import WorldViewSettings2D._

  type DimensionsType = WorldDimensions

  protected var _pendingDimensions: DimensionsType = WorldDimensions(0, 1, 0, 1)
  protected var _currentDimensions: DimensionsType = WorldDimensions(0, 1, 0, 1)

  protected val world = workspace.world

  override def addDimensionProperties(): Unit = {
    dimensionProperties.addAll(Properties.dims2D)
  }

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
  ): WorldDimensions = {
    WorldDimensions(minPxcor, maxPxcor, minPycor, maxPycor, patchSize, wrappingAllowedInX, wrappingAllowedInY)
  }

  def toDimensionType(d: WorldDimensions): DimensionsType = d.to2D

  override def addWrappingProperties(): Unit = {
    wrappingProperties.addAll(Properties.wrap2D)
  }

  def editFinished(): Boolean = {
    notifyEditFinished()

    setDimensions(_pendingDimensions, true, WorldResizer.StopEverything)

    true
  }

  private def hubnetDecision(): Int = {
    if (workspace.hubNetRunning) {
      val message = I18N.gui.get("view.resize.hubnet.warning")
      val title = I18N.gui.get("view.resize.hubnet.prompt")
      val options = Array[Object](
        I18N.gui.get("view.resize.hubnet.kick"),
        I18N.gui.get("view.resize.hubnet.dontkick"))
      OptionDialog.showMessage(workspace.getFrame, title, message, options)
    } else
      HubNetIgnore
  }

  override protected def aboutToChangePatches(stop: WorldResizer.JobStop): Unit = {
    super.aboutToChangePatches(stop)
    if (hubnetDecision() == HubNetKick) {
      /* kick clients first, then resize world */
      workspace.hubNetManager.foreach(_.reset())
    }
  }

  protected def createPatches(): Unit = {
    world.createPatches(_pendingDimensions.minPxcor, _pendingDimensions.maxPxcor,
      _pendingDimensions.minPycor, _pendingDimensions.maxPycor)
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

  def calculateHeight(d: WorldDimensions): Int =
    padding.getHeight.toInt + (d.patchSize * d.height).toInt

  def calculateWidth(d: WorldDimensions): Int =
    padding.getWidth.toInt + (d.patchSize * d.width).toInt

  def computePatchSize(width: Int, numPatches: Int): Double =
    ViewWidget.computePatchSize(width, numPatches)

  def insetWidth: Int =
    padding.getWidth.toInt

  def getMinimumWidth: Int =
    workspace.world.worldWidth.toInt + padding.getWidth.toInt

}
