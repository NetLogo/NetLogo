// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ Property, WorldPropertiesInterface }
import org.nlogo.core.{ CompilerException, UpdateMode, View => CoreView, WorldDimensions }
import org.nlogo.workspace.WorldLoaderInterface

trait WorldIntegerEditor {
  def setEnabled(enabled: Boolean): Unit
  def set(value: Int): Unit
}

abstract class WorldViewSettings(protected val workspace: GUIWorkspace, protected val gWidget: ViewWidget, tickCounter: TickCounterLabel)
  extends Editable
  with WorldLoaderInterface
  with WorldPropertiesInterface {

  protected var originType: OriginType = OriginType.Center
  protected var originConfig: Option[OriginConfiguration] = None

  protected var originalType: OriginType = originType
  protected var originalConfig: Option[OriginConfiguration] = originConfig

  protected var wrappingChanged: Boolean = false
  protected var edgesChanged: Boolean = false
  protected var patchSizeChanged: Boolean = false
  protected var fontSizeChanged: Boolean = false

  protected var newPatchSize: Double = _

  protected var newMinX: Int = _
  protected var newMaxX: Int = _
  protected var newMinY: Int = _
  protected var newMaxY: Int = _

  protected var newWrapX: Boolean = _
  protected var newWrapY: Boolean = _

  protected var newFontSize: Int = _

  protected var _error: Option[CompilerException] = None

  def classDisplayName: String = "Model Settings"

  def resizeWithProgress(showProgress: Boolean): Unit

  def model: CoreView

  def dimensionProperties: Seq[Property]
  def wrappingProperties: Seq[Property]
  def viewProperties: Seq[Property]
  def modelProperties: Seq[Property] = Properties.model

  val originTypes: Seq[OriginType] = Seq(OriginType.Center, OriginType.Corner, OriginType.Edge, OriginType.Custom)

  def cornerConfigs: Seq[OriginConfiguration]
  def edgeConfigs: Seq[OriginConfiguration]

  // the properties are manually added in WorldEditPanel (Isaac B 2/14/25)
  def propertySet = Seq[Property]()

  def getSelectedType: OriginType =
    originType

  def getSelectedConfig: Option[OriginConfiguration] =
    originConfig

  def setOriginType(originType: OriginType): Unit = {
    this.originType = originType

    originConfig = None
  }

  def setOriginConfig(originConfig: Option[OriginConfiguration]): Unit = {
    this.originConfig = originConfig
  }

  // helper for determining origin info from loaded patch coordinates (Isaac B 3/12/25)
  def setTypeAndConfig(): Unit = {
    val (x, y, z) = getNorms

    if (x == 0.5 && y == 0.5 && z == 0.5) {
      originType = OriginType.Center
      originConfig = None
    } else {
      val corner = cornerConfigs.find(config => config.x == x && config.y == y && config.z == z)

      if (corner.isDefined) {
        originType = OriginType.Corner
        originConfig = corner
      } else {
        val edge = edgeConfigs.find(config => config.x == x && config.y == y && config.z == z)

        if (edge.isDefined) {
          originType = OriginType.Edge
          originConfig = edge
        } else {
          originType = OriginType.Custom
          originConfig = None
        }
      }
    }

    originalType = originType
    originalConfig = originConfig
  }

  // calculate normalized origin coordinates, for use in setTypeAndConfig (Isaac B 3/12/25)
  protected def getNorms: (Float, Float, Float)

  def configureEditors(editors: Seq[WorldIntegerEditor]): Unit

  def apply(): Unit = {
    originalType = originType
    originalConfig = originConfig
  }

  def revert(): Unit = {
    originType = originalType
    originConfig = originalConfig
  }

  def load(view: CoreView): AnyRef = {
    workspace.world.displayOn(false)
    workspace.loadWorld(view, this)
    // we can't clearAll here because the globals may not
    // be allocated yet ev 7/12/06
    // note that we clear turtles inside the load method so
    // it can happen before we set the topology ev 7/19/06
    workspace.world.tickCounter.clear
    workspace.world.clearPatches
    workspace.world.displayOn(true)
    this
  }

  def smooth: Boolean =
    workspace.glView.antiAliasingOn

  def smooth(smooth: Boolean): Unit = {
    if (workspace.glView.antiAliasingOn != smooth)
      workspace.glView.antiAliasingOn(smooth)
  }

  def wireframe: Boolean = workspace.glView.wireframeOn

  def wireframe(on: Boolean): Unit = {
    if (on != wireframe) {
      workspace.glView.wireframeOn_=(on)
      workspace.glView.repaint()
    }
  }

  def dualView: Boolean = workspace.dualView

  def dualView(on: Boolean): Unit = {
    workspace.dualView(on)
  }

  def helpLink = Option.empty[String]

  override def liveUpdate: Boolean = false

  def minPxcor(minPxcor: Int): Unit = {
    if (minPxcor <= 0) {
      newMinX = minPxcor
      edgesChanged ||= newMinX != workspace.world.minPxcor
    }
  }

  def minPxcor: Int = newMinX

  def maxPxcor(maxPxcor: Int): Unit = {
    if (maxPxcor >= 0) {
      newMaxX = maxPxcor
      edgesChanged ||= newMaxX != workspace.world.maxPxcor
    }
  }

  def maxPxcor: Int = newMaxX

  def minPycor(minPycor: Int): Unit = {
    if (minPycor <= 0) {
      newMinY = minPycor
      edgesChanged ||= newMinY != workspace.world.minPycor
    }
  }

  def minPycor: Int = newMinY

  def maxPycor(maxPycor: Int): Unit = {
    if (maxPycor >= 0) {
      newMaxY = maxPycor
      edgesChanged ||= newMaxY != workspace.world.maxPycor
    }
  }

  def maxPycor: Int = newMaxY

  def patchSize(size: Double): Unit = {
    newPatchSize = size
    patchSizeChanged ||= size != patchSize
  }

  def patchSize: Double = workspace.world.patchSize

  def updateMode: UpdateMode = workspace.updateMode

  def updateMode(updateMode: UpdateMode): Unit = {
    workspace.updateMode(updateMode)
  }

  def wrappingX: Boolean = {
    if (! wrappingChanged)
      newWrapX = workspace.world.wrappingAllowedInX

    newWrapX
  }

  def wrappingX(value: Boolean): Unit = {
    newWrapX = value
    wrappingChanged ||= newWrapX != workspace.world.wrappingAllowedInX
  }

  def wrappingY: Boolean = {
    if (!wrappingChanged)
      newWrapY = workspace.world.wrappingAllowedInY

    newWrapY
  }

  def wrappingY(value: Boolean): Unit = {
    newWrapY = value
    wrappingChanged ||= newWrapY != workspace.world.wrappingAllowedInY
  }

  def fontSize: Int = gWidget.view.fontSize

  // this must be public because it's listed in our property set - ST 1/20/04
  def fontSize(newSize: Int): Unit = {
    this.newFontSize = newSize
    if (newSize != fontSize)
      fontSizeChanged = true

    workspace.viewManager.applyNewFontSize(newSize)
  }

  def frameRate: Double = workspace.frameRate

  def frameRate(frameRate: Double): Unit = {
    workspace.frameRate(frameRate)
  }

  def showTickCounter(visible: Boolean): Unit = {
    tickCounter.visibility = visible
  }

  def showTickCounter: Boolean =
    tickCounter.visibility

  def tickCounterLabel(label: String): Unit = {
    tickCounter.label = label
  }

  def tickCounterLabel: String =
    tickCounter.label

  def changeTopology(wrapX: Boolean, wrapY: Boolean): Unit = {
    workspace.changeTopology(wrapX, wrapY)
  }

  def clearTurtles(): Unit = {
    workspace.world.clearTurtles()
  }

  def anyErrors: Boolean = _error.isDefined

  def error(e: Exception): Unit = {
    e match {
      case e: CompilerException => _error = Some(e)
      case _ => throw new IllegalStateException(e)
    }
  }

  def error(o: AnyRef, e: Exception): Unit = {
    error(e)
  }

  def error: Exception = _error.orNull

  def error(a: AnyRef): Exception = _error.orNull

  def setSize(x: Int, y: Int): Unit = {
    gWidget.setSize(x, y)
  }

  def getMinimumWidth: Int = gWidget.getMinimumSize.width

  def insetWidth: Int = gWidget.insetWidth

  def computePatchSize(width: Int, numPatches: Int): Double =
    gWidget.computePatchSize(width, numPatches)

  def calculateHeight(worldHeight: Int, patchSize: Double): Int =
    gWidget.calculateHeight(worldHeight, patchSize)

  def calculateWidth(worldWidth: Int, patchSize: Double): Int =
    gWidget.calculateWidth(worldWidth, patchSize)

  def setDimensions(d: WorldDimensions, newPatchSize: Double): Unit = {
    workspace.world.patchSize(newPatchSize)
    setDimensions(d)
    patchSize(newPatchSize)
    gWidget.resetSize()
  }

  def setDimensions(d: WorldDimensions): Unit = {
    setDimensions(d.minPxcor, d.maxPxcor, d.minPycor, d.maxPycor)
  }

  def setDimensions(minPxcor: Int, maxPxcor: Int,
                    minPycor: Int, maxPycor: Int): Unit = {
    newMinX = minPxcor
    newMaxX = maxPxcor
    newMinY = minPycor
    newMaxY = maxPycor
    if (minPxcor != workspace.world.minPxcor ||
        maxPxcor != workspace.world.maxPxcor ||
        minPycor != workspace.world.minPycor ||
        maxPycor != workspace.world.maxPycor) {
      prepareForWorldResize()
      workspace.world
        .createPatches(minPxcor, maxPxcor, minPycor, maxPycor)
      finishWorldResize()
    }
  }

  private[window] def prepareForWorldResize(): Unit = {
    workspace.jobManager.haltNonObserverJobs()
    workspace.world.clearTurtles()
    workspace.world.clearLinks()
  }

  private[window] def finishWorldResize(): Unit = {
    workspace.patchesCreatedNotify()
    gWidget.resetSize()
    workspace.clearDrawing()
  }

  def sourceOffset: Int =
    // we should never be dealing with errors
    // related to the view
    throw new IllegalStateException();
}
