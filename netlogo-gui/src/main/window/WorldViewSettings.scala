// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Dimension
import java.util.{ ArrayList => ArrayJList, List => JList }
import java.beans.{ PropertyChangeListener, PropertyChangeSupport }

import org.nlogo.core.{ CompilerException, I18N, UpdateMode, View => CoreView, WorldDimensions }
import org.nlogo.api.{ Editable, Property, RichWorldDimensions, WorldPropertiesInterface, WorldResizer },
  RichWorldDimensions._
import org.nlogo.workspace.WorldLoaderInterface
import org.nlogo.swing.ModalProgressTask

object WorldViewSettings {
  val TickCounterLabelProperty = "tickCounterLabel"
  val TickCounterVisibilityProperty = "showTickCounter"
  val ViewFontSizeProperty = "fontSize"
  val ViewPropertiesBeingEdited = "isBeingEdited"
  val ViewSize = "viewSize"
  val WorldDimensionsProperty = "worldDimensions"
}

import WorldViewSettings._

abstract class WorldViewSettings(protected val workspace: GUIWorkspaceScala)
    extends Editable
    with WorldLoaderInterface
    with WorldPropertiesInterface {

  type DimensionsType <: WorldDimensions

  val dimensionProperties: JList[Property] = new ArrayJList[Property]()
  val wrappingProperties: JList[Property] = new ArrayJList[Property]()
  val viewProperties: JList[Property] = new ArrayJList[Property]()
  val modelProperties: JList[Property] = new ArrayJList[Property]()
  val cornerChoices: JList[OriginConfiguration] = new ArrayJList[OriginConfiguration]()
  val edgeChoices: JList[OriginConfiguration] = new ArrayJList[OriginConfiguration]()
  val originConfigurations: JList[OriginConfiguration] = new ArrayJList[OriginConfiguration]()

  protected val propertyChangeSupport = new PropertyChangeSupport(this)

  protected var _currentDimensions: DimensionsType
  protected var _pendingDimensions: DimensionsType

  protected var _propertySet: JList[Property] = null

  private var _isBeingEdited: Boolean = false
  private var _showTickCounter = true
  private var _tickCounterLabel = "ticks"
  private var _viewFontSize: Int = 13
  private var _viewSize: Dimension = new Dimension(0, 0)

  protected def wrappingChanged: Boolean =
    _pendingDimensions.wrappingAllowedInX != _currentDimensions.wrappingAllowedInX ||
    _pendingDimensions.wrappingAllowedInY != _currentDimensions.wrappingAllowedInY
  protected def patchSizeChanged: Boolean =
    _pendingDimensions.patchSize != _currentDimensions.patchSize
  protected def edgesChanged: Boolean =
    _pendingDimensions.minPxcor != _currentDimensions.minPxcor ||
    _pendingDimensions.maxPxcor != _currentDimensions.maxPxcor ||
    _pendingDimensions.minPycor != _currentDimensions.minPycor ||
    _pendingDimensions.maxPycor != _currentDimensions.maxPycor

  protected var _error: Option[CompilerException] = None

  addProperties()

  def addDimensionProperties(): Unit
  def addWrappingProperties(): Unit
  def model: CoreView
  def toDimensionType(d: WorldDimensions): DimensionsType

  protected def createPatches(): Unit

  def classDisplayName: String = "Model Settings"

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
  ): DimensionsType

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
      _pendingDimensions = copyDimensions(minPxcor = minPxcor)
    }
  }

  def minPxcor: Int = _pendingDimensions.minPxcor

  def maxPxcor(maxPxcor: Int): Unit = {
    if (maxPxcor >= 0) {
      _pendingDimensions = copyDimensions(maxPxcor = maxPxcor)
    }
  }

  def maxPxcor: Int = _pendingDimensions.maxPxcor

  def minPycor(minPycor: Int): Unit = {
    if (minPycor <= 0) {
      _pendingDimensions = copyDimensions(minPycor = minPycor)
    }
  }

  def minPycor: Int = _pendingDimensions.minPycor

  def maxPycor(maxPycor: Int): Unit = {
    if (maxPycor >= 0) {
      _pendingDimensions = copyDimensions(maxPycor = maxPycor)
    }
  }

  def maxPycor: Int = _pendingDimensions.maxPycor

  def patchSize(size: Double): Unit = {
    _pendingDimensions = copyDimensions(patchSize = size)
  }

  def patchSize: Double = _pendingDimensions.patchSize

  def updateMode: UpdateMode = workspace.updateMode

  def updateMode(updateMode: UpdateMode): Unit = {
    workspace.updateMode(updateMode)
  }

  def wrappingX: Boolean = _pendingDimensions.wrappingAllowedInX

  def wrappingX(value: Boolean): Unit = {
    _pendingDimensions = copyDimensions(wrappingAllowedInX = value)
  }

  def wrappingY: Boolean = _pendingDimensions.wrappingAllowedInY

  def wrappingY(value: Boolean): Unit = {
    _pendingDimensions = copyDimensions(wrappingAllowedInY = value)
  }

  def fontSize: Int = _viewFontSize

  def fontSize(newSize: Int): Unit = {
    if (_viewFontSize != newSize) {
      val oldFontSize = _viewFontSize
      _viewFontSize = newSize
      propertyChangeSupport.firePropertyChange(ViewFontSizeProperty, oldFontSize, _viewFontSize)
    }
  }

  def frameRate: Double = workspace.frameRate

  def frameRate(frameRate: Double): Unit = {
    workspace.frameRate(frameRate)
  }

  def showTickCounter(visible: Boolean): Unit = {
    val oldVisibility = _showTickCounter
    _showTickCounter = visible
    propertyChangeSupport.firePropertyChange(TickCounterVisibilityProperty, oldVisibility, _showTickCounter)
  }

  def showTickCounter: Boolean = _showTickCounter

  def tickCounterLabel(label: String): Unit = {
    val oldLabel = _tickCounterLabel
    _tickCounterLabel = label
    propertyChangeSupport.firePropertyChange(TickCounterLabelProperty, oldLabel, _tickCounterLabel)
  }

  def tickCounterLabel: String = _tickCounterLabel

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
    if (_viewSize.getWidth != x || _viewSize.getHeight != y) {
      val oldViewSize = _viewSize
      _viewSize = new Dimension(x, y)
      propertyChangeSupport.firePropertyChange(ViewSize, oldViewSize, _viewSize)
    }
  }

  protected def changeActiveDimensions(d: DimensionsType): Unit = {
    if (_currentDimensions != d) {
      val oldDimensions = _currentDimensions
      _currentDimensions = d
      propertyChangeSupport.firePropertyChange(WorldDimensionsProperty, oldDimensions, _currentDimensions)
    }
  }

  case class SetDimensionRunnable(
    beforeActions: Seq[() => Unit],
    synchronizedActions: Seq[() => Unit],
    afterActions: Seq[() => Unit]
  ) extends Runnable() {
    override def run(): Unit = {
      beforeActions.foreach(_())
      workspace.world.synchronized {
        synchronizedActions.foreach(_())
      }
      afterActions.foreach(_())
    }
    def runActionBefore(f: () => Unit): SetDimensionRunnable =
      copy(beforeActions = beforeActions :+ f)

    def runActionSynchronized(f: () => Unit): SetDimensionRunnable =
      copy(synchronizedActions = synchronizedActions :+ f)

    def runActionAfter(f: () => Unit): SetDimensionRunnable =
      copy(afterActions = afterActions :+ f)
  }

  protected def aboutToChangePatches(stop: WorldResizer.JobStop): Unit = {
    stop match {
      case WorldResizer.StopNonObserverJobs => workspace.jobManager.haltNonObserverJobs()
      case WorldResizer.StopEverything =>
        workspace.jobManager.haltSecondary()
        workspace.jobManager.haltPrimary()
      case WorldResizer.StopNothing =>
    }
  }

  protected def didChangePatches(): Unit = {
    workspace.clearDrawing()
  }

  protected def updateTopology(): Unit = {
    workspace.changeTopology(_pendingDimensions.wrappingAllowedInX, _pendingDimensions.wrappingAllowedInY)
  }

  def setDimensions(d: WorldDimensions, showProgress: Boolean, jobStop: WorldResizer.JobStop): Unit = {
    _pendingDimensions = toDimensionType(d)

    val oldStatus =
      workspace.displayStatusRef.getAndUpdate(s => s.codeSet(false))

    var runnable = new SetDimensionRunnable(Seq(), Seq(), Seq())
      .runActionAfter { () =>
        workspace.displayStatusRef.set(oldStatus)
        workspace.enableDisplayUpdates()
        changeActiveDimensions(_pendingDimensions)
      }

    if (wrappingChanged) {
      runnable = runnable.runActionSynchronized { () => updateTopology() }
    }
    if (patchSizeChanged) {
      runnable = runnable.runActionSynchronized { () =>
        workspace.world.patchSize(_pendingDimensions.patchSize)
      }
    }
    if (edgesChanged) {
      runnable = runnable
        .runActionBefore { () => aboutToChangePatches(jobStop) }
        .runActionSynchronized { () =>
          workspace.world.clearTurtles()
          workspace.world.clearLinks()
          createPatches()
          workspace.patchesCreatedNotify()
        }
        .runActionAfter { () => didChangePatches() }
    }

    if (showProgress && (edgesChanged || patchSizeChanged))
      ModalProgressTask.onUIThread(workspace.getFrame, I18N.gui.get("view.resize.progress"), runnable)
    else
      runnable.run()
  }

  def sourceOffset: Int =
    // we should never be dealing with errors
    // related to the view
    throw new IllegalStateException();

  override def editStarted(): Unit = {
    if (! _isBeingEdited) {
      _pendingDimensions = _currentDimensions
      _isBeingEdited = true
      propertyChangeSupport.firePropertyChange(ViewPropertiesBeingEdited, false, _isBeingEdited)
    }
  }

  def notifyEditFinished(): Unit = {
    if (_isBeingEdited) {
      _isBeingEdited = false
      propertyChangeSupport.firePropertyChange(ViewPropertiesBeingEdited, true, _isBeingEdited)
    }
  }

  def addPropertyChangeListener(listener: PropertyChangeListener): Unit = {
    propertyChangeSupport.addPropertyChangeListener(listener)
  }

  private[window] def currentListeners: Seq[PropertyChangeListener] =
    propertyChangeSupport.getPropertyChangeListeners.toSeq

  def removePropertyChangeListener(listener: PropertyChangeListener): Unit = {
    propertyChangeSupport.removePropertyChangeListener(listener)
  }
}
