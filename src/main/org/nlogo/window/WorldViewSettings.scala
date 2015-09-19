// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.util.List
import org.nlogo.api.{ CompilerException, Editable, I18N, Property, UpdateMode,
  WorldDimensions, WorldPropertiesInterface }
import org.nlogo.workspace.WorldLoaderInterface
import scala.collection.JavaConverters._
import scala.collection.mutable

abstract class WorldViewSettings(workspace: GUIWorkspace, gWidget: ViewWidget) extends Editable
    with WorldLoaderInterface with WorldPropertiesInterface {
  implicit val i18nName = I18N.Prefix("edit.viewSettings.origin.location")

  def classDisplayName = "Model Settings"

  def resizeWithProgress(showProgress: Boolean): Unit
  def save: String
  def addWrappingProperties(): Unit
  def addDimensionProperties(): Unit

  protected var dimensionProperties, wrappingProperties, viewProperties,
    modelProperties: mutable.ArrayBuffer[Property] = null
  protected var cornerChoices, edgeChoices, originConfigurations: mutable.ArrayBuffer[OriginConfiguration] = null

  addProperties()

  protected def addProperties() = {
    _propertySet = new mutable.ArrayBuffer[Property]
    dimensionProperties = new mutable.ArrayBuffer[Property]
    addDimensionProperties()
    wrappingProperties = new mutable.ArrayBuffer[Property]
    addWrappingProperties()
    viewProperties = new mutable.ArrayBuffer[Property]
    addViewProperties()
    modelProperties = new mutable.ArrayBuffer[Property]
    addModelProperties()
    cornerChoices = new mutable.ArrayBuffer[OriginConfiguration]
    addCornerChoices()
    edgeChoices = new mutable.ArrayBuffer[OriginConfiguration]
    addEdgeChoices()
    originConfigurations = new mutable.ArrayBuffer[OriginConfiguration]
    addOriginConfigurations()
  }

  def getDimensionProperties = dimensionProperties
  def getWrappingProperties = wrappingProperties
  def getViewProperties = viewProperties
  def getModelProperties = modelProperties
  def getCornerChoices = cornerChoices
  def getEdgeChoices = edgeChoices
  def getOriginConfigurations = originConfigurations

  def addViewProperties() = viewProperties ++= Properties.view2D.asScala
  def addModelProperties() = modelProperties ++= Properties.model.asScala

  def refreshViewProperties(threedView: Boolean) = {
    viewProperties.clear()
    addViewProperties()
    if(threedView)
      viewProperties ++= Properties.view3D.asScala
  }

  def addCornerChoices() = {
    cornerChoices += new OriginConfiguration(I18N.gui("corner.bottomLeft"),
      Array(false, true, false, true), Array(true, false, true, false))
    cornerChoices += new OriginConfiguration(I18N.gui("corner.topLeft"),
      Array(false, true, true, false), Array(true, false, false, true))
    cornerChoices += new OriginConfiguration(I18N.gui("corner.topRight"),
      Array(true, false, true, false), Array(false, true, false, true))
    cornerChoices += new OriginConfiguration(I18N.gui("corner.bottomRight"),
      Array(true, false, false, true), Array(false, true, true, false))
  }

  def addEdgeChoices() = {
    edgeChoices += new OriginConfiguration(I18N.gui("edge.bottom"),
      Array(true, true, false, true), Array(false, false, true, false))
    edgeChoices += new OriginConfiguration(I18N.gui("edge.top"),
      Array(true, true, true, false), Array(false, false, false, true))
    edgeChoices += new OriginConfiguration(I18N.gui("edge.right"),
      Array(true, false, true, true), Array(false, true, false, false))
    edgeChoices += new OriginConfiguration(I18N.gui("edge.left"),
      Array(false, true, true, true), Array(true, false, false, false))
  }

  def addOriginConfigurations() = {
    originConfigurations += new OriginConfiguration(I18N.gui("center"),
      Array(false, true, false, true), Array(false, false, false, false))
    originConfigurations += new OriginConfiguration(I18N.gui("corner"),
      Array(true, true, true, true), Array(false, false, false, false))
    originConfigurations += new OriginConfiguration(I18N.gui("edge"),
      Array(true, true, true, true), Array(false, false, false, false))
    originConfigurations += new OriginConfiguration(I18N.gui("custom"),
      Array(true, true, true, true), Array(false, false, false, false))
  }

  def firstEditor = 0
  def lastEditor  = 3

  def getSelectedLocation = {
    val minx = minPxcor
    val maxx = maxPxcor
    val miny = minPycor
    val maxy = maxPycor

    if(minx == (-maxx) && miny == (-maxy))
      0
    else if ((minx == 0 || maxx == 0) && (miny == 0 || maxy == 0))
      1
    else if (minx == 0 || maxx == 0 || miny == 0 || maxy == 0)
      2
    else
      3
  }

  def getSelectedConfiguration = {
    val minx = minPxcor
    val maxx = maxPxcor
    val miny = minPycor
    val maxy = maxPycor

    if(minx == 0 && miny == 0)
      0
    else if(minx == 0 && maxy == 0)
      1
    else if(maxx == 0 && maxy == 0)
      2
    else if(maxx == 0 && miny == 0)
      3
    else if(minx == 0)
      3
    else if(maxx == 0)
      2
    else if(miny == 0)
      0
    else if(maxy == 0)
      1
    else
      0
  }

  def load(strings: Seq[String], version: String) = {
    workspace.loadWorld(strings, version, this)
    // we can't clearAll here because the globals may not
    // be allocated yet ev 7/12/06
    // note that we clear turtles inside the load method so
    // it can happen before we set the topology ev 7/19/06
    workspace.world.tickCounter.clear()
    workspace.world.clearPatches()
    workspace.world.displayOn(true)
    this
  }

  def smooth = workspace.glView.antiAliasingOn
  def smooth_=(_smooth: Boolean) = if (workspace.glView.antiAliasingOn != _smooth)
      workspace.glView.antiAliasingOn = _smooth
  def wireframe =workspace.glView.wireframeOn
  def wireframe_=(on: Boolean) = if (on != wireframe) {
      workspace.glView.wireframeOn = on
      workspace.glView.repaint()
    }
  def dualView = workspace.dualView
  def dualView_=(on: Boolean) = workspace.dualView(on)

  def helpLink = Option(null)

  private var _propertySet: Seq[Property] = null
  def propertySet = _propertySet.asJava
  def propertySet_=(__propertySet: List[Property]) = _propertySet = __propertySet.asScala

  protected var wrappingChanged, edgesChanged, patchSizeChanged, fontSizeChanged = false
  protected var newPatchSize = 0: Double

  protected var newMinX, newMaxX, newMinY, newMaxY = 0
  protected var newWrapX, newWrapY = false

  def minPxcor = newMinX
  def minPxcor_=(_minPxcor: Int) = if(_minPxcor <= 0) {
      newMinX = _minPxcor
      edgesChanged = edgesChanged || (newMinX != workspace.world.minPxcor)
    }
  def maxPxcor = newMaxX
  def maxPxcor_=(_maxPxcor: Int) = if(_maxPxcor >= 0) {
      newMaxX = _maxPxcor
      edgesChanged = edgesChanged || (newMaxX != workspace.world.maxPxcor)
    }
  def minPycor = newMinY
  def minPycor_=(_minPycor: Int) = if(_minPycor <= 0) {
      newMinY = _minPycor
      edgesChanged = edgesChanged || (newMinY != workspace.world.minPycor)
    }
  def maxPycor = newMaxY
  def maxPycor_=(_maxPycor: Int) = if(_maxPycor >= 0) {
      newMaxY = _maxPycor
      edgesChanged = edgesChanged || (newMaxY != workspace.world.maxPycor)
    }

  def patchSize = workspace.world.patchSize
  def patchSize_=(size: Double) = {
    newPatchSize = size
    patchSizeChanged = patchSizeChanged || (size != patchSize)
  }

  def updateMode = workspace.updateMode()
  def updateMode_=(updateMode: UpdateMode) = workspace.updateMode(updateMode)

  def wrappingX = {
    if(!wrappingChanged)
      newWrapX = workspace.world.wrappingAllowedInX
    newWrapX
  }
  def wrappingX(value: Boolean) = {
    newWrapX = value
    wrappingChanged = wrappingChanged || (newWrapX != workspace.world.wrappingAllowedInX)
  }

  def wrappingY = {
    if(!wrappingChanged)
      newWrapY = workspace.world.wrappingAllowedInY
    newWrapY
  }
  def wrappingY(value: Boolean) = {
    newWrapY = value
    wrappingChanged = wrappingChanged || (newWrapY != workspace.world.wrappingAllowedInY)
  }

  protected var newFontSize = 0
  def fontSize = gWidget.view.fontSize
  // this must be public because it's listed in our property set - ST 1/20/04
  def fontSize_=(_fontSize: Int) = {
    newFontSize = _fontSize
    if (newFontSize != fontSize)
      fontSizeChanged = true
    workspace.viewManager.applyNewFontSize(newFontSize)
  }

  def frameRate = workspace.frameRate
  def frameRate_=(frameRate: Double) = workspace.frameRate(frameRate)

  def showTickCounter = workspace.viewWidget.showTickCounter
  def showTickCounter_=(visible: Boolean) = workspace.viewWidget.showTickCounter = visible

  def tickCounterLabel = workspace.viewWidget.tickCounterLabel
  def tickCounterLabel_=(label: String) = workspace.viewWidget.tickCounterLabel = label

  def changeTopology(wrapX: Boolean, wrapY: Boolean) = workspace.changeTopology(wrapX, wrapY)

  def clearTurtles() = workspace.world.clearTurtles()

  protected var _error: CompilerException = null

  def anyErrors = error != null
  def error = _error
  def error(o: AnyRef) = error
  def error(o: AnyRef, e: Exception) = error = e
  def error_=(e: Exception) = e match {
    case err: CompilerException => _error = err
    case _ => throw new IllegalStateException(e)
  }

  def setSize(x: Int, y: Int) = gWidget.setSize(x, y)

  def getMinimumWidth = gWidget.getMinimumWidth
  def insetWidth = gWidget.insetWidth

  def computePatchSize(width: Int, numPatches: Int) = gWidget.computePatchSize(width, numPatches)
  def calculateWidth(worldWidth: Int, patchSize: Double) = gWidget.calculateWidth(worldWidth, patchSize)
  def calculateHeight(worldHeight: Int, patchSize: Double) = gWidget.calculateHeight(worldHeight, patchSize)

  def setDimensions(d: WorldDimensions, _patchSize: Double) = {
    workspace.world.patchSize(_patchSize)
    setDimensions(d)
    patchSize = _patchSize
    gWidget.resetSize()
  }
  def setDimensions(d: WorldDimensions): Unit = setDimensions(d.minPxcor, d.maxPxcor, d.minPycor, d.maxPycor)
  def setDimensions(minPxcor: Int, maxPxcor: Int, minPycor: Int, maxPycor: Int) = {
    newMinX = minPxcor
    newMaxX = maxPxcor
    newMinY = minPycor
    newMaxY = maxPycor
    if(minPxcor != workspace.world.minPxcor || maxPxcor != workspace.world.maxPxcor ||
        minPycor != workspace.world.minPycor || maxPycor != workspace.world.maxPycor) {
      prepareForWorldResize()
      workspace.world.createPatches(minPxcor, maxPxcor, minPycor, maxPycor)
      finishWorldResize()
    }
  }

  def prepareForWorldResize() = {
    workspace.jobManager.haltNonObserverJobs()
    workspace.world.clearTurtles()
    workspace.world.clearLinks()
  }

  def finishWorldResize() = {
    workspace.patchesCreatedNotify()
    gWidget.resetSize()
    workspace.clearDrawing()
  }

  // we should never be dealing with errors
  // related to the view
  def sourceOffset = throw new IllegalStateException
}
