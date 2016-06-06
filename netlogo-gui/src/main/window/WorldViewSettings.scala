// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.util.{ ArrayList => ArrayJList, List => JList }

import org.nlogo.core.{ CompilerException, I18N, UpdateMode, View => CoreView, WorldDimensions }
import org.nlogo.api.{ Editable, Property, WorldPropertiesInterface }
import org.nlogo.workspace.WorldLoaderInterface

abstract class WorldViewSettings(protected val workspace: GUIWorkspace, protected val gWidget: ViewWidget, tickCounter: TickCounterLabel)
    extends Editable
    with WorldLoaderInterface
    with WorldPropertiesInterface {

  val dimensionProperties: JList[Property] = new ArrayJList[Property]()
  val wrappingProperties: JList[Property] = new ArrayJList[Property]()
  val viewProperties: JList[Property] = new ArrayJList[Property]()
  val modelProperties: JList[Property] = new ArrayJList[Property]()
  val cornerChoices: JList[OriginConfiguration] = new ArrayJList[OriginConfiguration]()
  val edgeChoices: JList[OriginConfiguration] = new ArrayJList[OriginConfiguration]()
  val originConfigurations: JList[OriginConfiguration] = new ArrayJList[OriginConfiguration]()

  protected var _propertySet: JList[Property] = null

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

  addProperties()

  def classDisplayName: String = "Model Settings"

  def resizeWithProgress(showProgress: Boolean): Unit

  def model: CoreView

  def addWrappingProperties(): Unit
  def addDimensionProperties(): Unit

  protected def addProperties(): Unit = {
    propertySet(new ArrayJList[Property]())
    addDimensionProperties()
    addWrappingProperties()
    addViewProperties()
    addModelProperties()
    addCornerChoices()
    addEdgeChoices()
    addOriginConfigurations()
  }

  def addViewProperties(): Unit = {
    viewProperties.addAll(Properties.view2D)
  }

  def addModelProperties(): Unit = {
    modelProperties.addAll(Properties.model)
  }

  def refreshViewProperties(threedView: Boolean): Unit = {
    viewProperties.clear()
    addViewProperties()
    if (threedView)
      viewProperties.addAll(Properties.view3D)
  }

  def addCornerChoices(): Unit = {
    cornerChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner.bottomLeft"),
        Array(false, true, false, true),
        Array(true, false, true, false)))
    cornerChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner.topLeft"),
        Array(false, true, true, false),
        Array(true, false, false, true)))
    cornerChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner.topRight"),
        Array(true, false, true, false),
        Array(false, true, false, true)))
    cornerChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.corner.bottomRight"),
        Array(true, false, false, true),
        Array(false, true, true, false)))
  }

  def addEdgeChoices(): Unit = {
    edgeChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge.bottom"),
        Array(true, true, false, true),
        Array(false, false, true, false)))
    edgeChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge.top"),
        Array(true, true, true, false),
        Array(false, false, false, true)))
    edgeChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge.right"),
        Array(true, false, true, true),
        Array(false, true, false, false)))
    edgeChoices.add(new OriginConfiguration(I18N.gui.get("edit.viewSettings.origin.location.edge.left"),
        Array(false, true, true, true),
        Array(true, false, false, false)))
  }

  def addOriginConfigurations(): Unit = {
    originConfigurations.add(new OriginConfiguration
        (I18N.gui.get("edit.viewSettings.origin.location.center"),
            Array(false, true, false, true),
            Array(false, false, false, false)))
    originConfigurations.add(new OriginConfiguration
        (I18N.gui.get("edit.viewSettings.origin.location.corner"),
            Array(true, true, true, true),
            Array(false, false, false, false)))
    originConfigurations.add(new OriginConfiguration
        (I18N.gui.get("edit.viewSettings.origin.location.edge"),
            Array(true, true, true, true),
            Array(false, false, false, false)))
    originConfigurations.add(new OriginConfiguration
        (I18N.gui.get("edit.viewSettings.origin.location.custom"),
            Array(true, true, true, true),
            Array(false, false, false, false)))
  }

  def firstEditor: Int = 0

  def lastEditor: Int = 3

  def getSelectedLocation: Int = {
    val minx = minPxcor
    val maxx = maxPxcor
    val miny = minPycor
    val maxy = maxPycor

    if (minx == (-maxx) && miny == (-maxy))
      0
    else if ((minx == 0 || maxx == 0) && (miny == 0 || maxy == 0))
      1
    else if (minx == 0 || maxx == 0 || miny == 0 || maxy == 0)
      2
    else
      3
  }

  def getSelectedConfiguration: Int = {
    val minx = minPxcor
    val maxx = maxPxcor
    val miny = minPycor
    val maxy = maxPycor

    if (minx == 0 && miny == 0)
      0
    else if (minx == 0 && maxy == 0)
      1
    else if (maxx == 0 && maxy == 0)
      2
    else if (maxx == 0 && miny == 0)
      3
    else if (minx == 0)
      3
    else if (maxx == 0)
      2
    else if (miny == 0)
      0
    else if (maxy == 0)
      1
    else
      0
  }

  def load(view: CoreView): AnyRef = {
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

  def propertySet: JList[Property] = _propertySet

  def propertySet(propertySet: JList[Property]): Unit = {
    _propertySet = propertySet
  }

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
