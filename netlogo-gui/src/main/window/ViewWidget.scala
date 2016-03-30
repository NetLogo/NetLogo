// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

// if the size of the border surrounding the View changes, be sure
// to change the associated constants in ModelLoader

import org.nlogo.api.Dump
import org.nlogo.api.Approximate
import org.nlogo.api.Version
import org.nlogo.awt.{ Fonts => NlogoFonts }
import org.nlogo.core.{ I18N, View => CoreView }
import org.nlogo.window.Events.{ PeriodicUpdateEvent, LoadBeginEvent, LoadEndEvent }
import org.nlogo.window.Events.ResizeViewEvent
import org.nlogo.window.MouseMode._

import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.Point
import java.awt.Rectangle
import javax.swing.JPopupMenu
import javax.swing.BorderFactory
import javax.swing.JLabel


object ViewWidget {
  private val InsideBorderHeight = 1
  private val TickCounterLabelDefault = "ticks"

  // The 245 here was determined empirically by measuring the width
  // on Mac OS X and then adding some slop.  Yes, this an incredible
  // kludge, but I figure it's not worth it to try to do it the right
  // way, since all of this view widget sizing code is targeted to be
  // thrown out and redone anyway - ST 1/20/11
  private val MaxViewWidthFudgeValue = 245
}

class ViewWidget(workspace: GUIWorkspace)
    extends Widget
    with ViewWidgetInterface
    with PeriodicUpdateEvent.Handler
    with LoadBeginEvent.Handler
    with LoadEndEvent.Handler {

  import ViewWidget._

  type WidgetModel = CoreView

  private var _tickCounterLabel: String = TickCounterLabelDefault
  val view = new View(workspace)
  val tickCounter: JLabel = new TickCounterLabel()
  val displaySwitch = new DisplaySwitch(workspace)
  val controlStrip = new ViewControlStrip(workspace, this)

  NlogoFonts.adjustDefaultFont(tickCounter)

  setBackground(InterfaceColors.GRAPHICS_BACKGROUND)
  setBorder(BorderFactory.createCompoundBorder(
        widgetBorder,
        BorderFactory.createMatteBorder(1, 3, 4, 2, InterfaceColors.GRAPHICS_BACKGROUND)))
  setLayout(null)
  add(view)
  add(controlStrip)
  val settings: WorldViewSettings =
    if (Version.is3D)
      new WorldViewSettings3D(workspace, this)
    else
      new WorldViewSettings2D(workspace, this);

  override def classDisplayName: String = "World & View"

  final def getExtraHeight: Int =
    getInsets.top + getInsets.bottom + InsideBorderHeight

  def getAdditionalHeight: Int =
    getExtraHeight + controlStrip.getHeight

  override def doLayout(): Unit = {
    val availableWidth = getWidth - getInsets.left - getInsets.right
    val patchSize = computePatchSize(availableWidth, workspace.world.worldWidth)
    val graphicsHeight =
      StrictMath.round(patchSize * workspace.world.worldHeight).toInt
    val stripHeight = getHeight - graphicsHeight - getInsets.top - getInsets.bottom

    // Note that we set the patch size first and then set the bounds of the view.
    // view.setBounds will force the Renderer to a particular size, overriding the
    // calculation the Render makes internally if need be -- CLB
    view.visualPatchSize(patchSize)
    view.setBounds(getInsets.left,
      getInsets.top + InsideBorderHeight + stripHeight,
      availableWidth, graphicsHeight)
    controlStrip.setBounds(getInsets.left, getInsets.top,
      availableWidth, stripHeight)
  }

  override def getEditable: AnyRef =
    settings

  def computePatchSize(width: Int, numPatches: Int): Double = {
    // This is sneaky.  We'd rather not have numbers with a zillion decimal places
    // show up in "Patch Size" when you edit the graphics window.
    // So instead of setting the patch to the exact quotient of
    // the size in pixels divided by the number of patches, we set
    // it to the number with the least junk after the decimal
    // point that still rounds to the correct # of pixels - ST 4/6/03
    val exactPatchSize = width.toDouble / numPatches.toDouble
    val roundedPatchSize = (0 to 15).map(precision => Approximate.approximate(exactPatchSize, precision))
      .find(roundedPatchSize => (numPatches * roundedPatchSize).toInt == width)
    roundedPatchSize.getOrElse(exactPatchSize)
  }

  /// sizing

  // just returning zeros prevents the "smart" preferred-size
  // code in EditView from getting confused - ST 6/6/02
  override def getPreferredSize(font: Font): Dimension =
    new Dimension(0, 0)

  override def needsPreferredWidthFudgeFactor: Boolean = false

  override def getMinimumSize: Dimension = {
    val gSize = view.getMinimumSize
    val stripSize = controlStrip.getMinimumSize
    val baseHeight = stripSize.height + getExtraHeight
    if (gSize.width > stripSize.width) {
      new Dimension(getMinimumWidth, baseHeight + gSize.height)
    } else {
      // this gets tricky because if it's the control strip that's
      // determining the minimum width, then we need to calculate
      // what the graphics window's height will be at that width
      val ssx = workspace.world.worldWidth
      val ssy = workspace.world.worldHeight
      val minPatchSize = computePatchSize(stripSize.width, ssx)
      new Dimension(getMinimumWidth, baseHeight + (minPatchSize * ssy).toInt)
    }
  }

  def insetWidth: Int =
    getInsets.left + getInsets.right;

  def calculateWidth(worldWidth: Int, patchSize: Double): Int = {
    ((worldWidth * patchSize) + insetWidth).toInt
  }

  def calculateHeight(worldHeight: Int, patchSize: Double): Int = {
    val stripSize = controlStrip.getMinimumSize
    stripSize.height + getExtraHeight + (patchSize * worldHeight).toInt
  }

  def getMinimumWidth: Int =
    controlStrip.getMinimumSize.width + insetWidth

  def resetSize(): Unit = {
    import workspace.world.{ worldWidth, worldHeight, patchSize => worldPatchSize }

    view.setSize(worldWidth, worldHeight, worldPatchSize)
    val dim = view.getPreferredSize
    setSize(dim.width + insetWidth,
        dim.height + getExtraHeight + controlStrip.getPreferredSize.height)
    doLayout()
    resetZoomInfo()
  }

  override def setSize(width: Int, height: Int): Unit = {
    super.setSize(width, height)
    new ResizeViewEvent(workspace.world.worldWidth, workspace.world.worldHeight)
        .raise(this);
  }

  override def setBounds(x: Int, y: Int, width: Int, height: Int): Unit = {
    val bounds = getBounds()
    // only set the bounds if they've changed
    if (width != bounds.width || height != bounds.height || x != bounds.x || y != bounds.y) {
      super.setBounds(x, y, width, height)
      resetSizeInfo()
    }
  }

  override def setBounds(bounds: Rectangle): Unit = {
    setBounds(bounds.x, bounds.y, bounds.width, bounds.height)
  }

  override def getBoundsString: String = {
    // If we're in a zoomed container, save the unzoomed size
    val r =
      if (findWidgetContainer == null)
        getBounds()
      else
        findWidgetContainer.getUnzoomedBounds(this)

    val width = StrictMath.max(MaxViewWidthFudgeValue, r.width)
    // and, ugh, this is copy-and-pasted from Widget.java. more
    // kludginess - ST 1/20/11
    Seq(r.x, r.y, r.x + width, r.y + r.height).mkString("\n")
  }

  override def constrainDrag(newBounds: Rectangle,
    originalBounds: Rectangle,
    mouseMode: MouseMode): Rectangle = {
      import workspace.world.{ worldWidth, worldHeight }
    val stripHeight = controlStrip.getMinimumSize.height
    val patchSizeBasedOnNewWidth =
      computePatchSize(newBounds.width - getInsets.left + getInsets.right, worldWidth)
    val patchSizeBasedOnNewHeight =
        computePatchSize(newBounds.height - stripHeight - getExtraHeight, worldHeight);
    val newPatchSize =
      if (newBounds.height == originalBounds.height)    // case 1: only width changed; adjust height to match
        patchSizeBasedOnNewWidth
      else if (newBounds.width == originalBounds.width) // case 2: only height changed; adjust width to match
        patchSizeBasedOnNewHeight
      else                                              // case 3: they both changed, use whichever results in the larger patch length
        StrictMath.max(patchSizeBasedOnNewWidth, patchSizeBasedOnNewHeight);

    // since the new patch size is based on the new width make sure
    // to take into account the change in width due to zooming
    workspace.world.patchSize(newPatchSize)
    view.setSize(worldWidth, worldHeight, newPatchSize)

    view.renderer.trailDrawer.rescaleDrawing

    val newWidth = ((newPatchSize * worldWidth) + insetWidth).toInt
    val newHeight = ((newPatchSize * worldHeight) + getExtraHeight + stripHeight).toInt
    val widthAdjust = newBounds.width - newWidth
    val heightAdjust = newBounds.height - newHeight
    var newX = newBounds.x
    var newY = newBounds.y
    mouseMode match {
      case NE =>
        newY += heightAdjust
      case NW =>
        newX += widthAdjust
        newY += heightAdjust
      case SW =>
        newX += widthAdjust
      case W =>
        newX += widthAdjust
      case N =>
        newY += heightAdjust;
      case _ =>
    }
    mouseMode match {
      case N | S =>
        val midpointX = originalBounds.x + originalBounds.width / 2
        newX = midpointX - newWidth / 2
      case E | W =>
        val midpointY = originalBounds.y + originalBounds.height / 2
        newY = midpointY - newHeight / 2
      case _ =>
    }
    new Rectangle(newX, newY, newWidth, newHeight)
  }

  /// font handling for turtle and patch labels

  protected def applyNewFontSize(newFontSize: Int): Unit = {
    val font = view.getFont
    val zoomDiff = font.getSize - view.fontSize
    view.applyNewFontSize(newFontSize, zoomDiff)
  }

  /// tell the zooming code it's OK to grab our subcomponents and zoom them too

  override def zoomSubcomponents: Boolean = true

  /// ViewWidgetInterface

  override def asWidget: Widget = this

  /// events

  def handle(e: LoadBeginEvent): Unit = {
    tickCounter.setText("")
    _tickCounterLabel = "ticks"
    tickCounter.setVisible(true)
  }

  def handle(e: LoadEndEvent): Unit = {
    controlStrip.reset()
  }

  def handle(e: PeriodicUpdateEvent): Unit = {
    redrawTickCounter()
  }

  protected def redrawTickCounter(): Unit = {
    val ticks = workspace.world.tickCounter.ticks
    val tickText =
        if (ticks == -1) "" else Dump.number(StrictMath.floor(ticks))
    tickCounter.setText("     " + tickCounterLabel + ": " + tickText)
  }

  /// tick counter

  def showTickCounter(visible: Boolean): Unit =
    tickCounter.setVisible(visible)

  def showTickCounter: Boolean =
    tickCounter.isVisible

  def tickCounterLabel(label: String): Unit = {
    _tickCounterLabel = label
    redrawTickCounter()
  }

  def tickCounterLabel: String = _tickCounterLabel

  private class TickCounterLabel extends javax.swing.JLabel {
    override def getPreferredSize: Dimension = getMinimumSize

    override def getMinimumSize: Dimension = {
      val d = super.getMinimumSize
      val fontMetrics = getFontMetrics(getFont)
      d.width = StrictMath.max(d.width, fontMetrics.stringWidth(tickCounterLabel + ": 00000000"))
      d
    }
  }

  override def hasContextMenu: Boolean =
    true;

  override def populateContextMenu(menu: JPopupMenu, p: Point, source: Component): Point =
    view.populateContextMenu(menu, p, source)

  /// display switch

  protected def displaySwitchOn(on: Boolean): Unit = {
    displaySwitch.actionPerformed(null)
  }


  /// load & save

  override def save: String = settings.save

  override def load(view: WidgetModel, helper: Widget.LoadHelper): AnyRef =
    settings.load(view, helper.version)

  override def copyable: Boolean = false
}
