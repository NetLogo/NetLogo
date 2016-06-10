// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, Dimension, Font, Point, Rectangle }
import javax.swing.{ JPopupMenu, BorderFactory, JLabel }

import org.nlogo.api.{ Approximate, Dump, Version }
import org.nlogo.awt.{ Fonts => NlogoFonts }
import org.nlogo.core.{ View => CoreView }
import org.nlogo.window.Events.ResizeViewEvent
import org.nlogo.window.MouseMode._


object ViewWidget {
  private val InsideBorderHeight = 1

  // The 245 here was determined empirically by measuring the width
  // on Mac OS X and then adding some slop.  Yes, this an incredible
  // kludge, but I figure it's not worth it to try to do it the right
  // way, since all of this view widget sizing code is targeted to be
  // thrown out and redone anyway - ST 1/20/11
  private val MaxViewWidthFudgeValue = 245
}

class ViewWidget(workspace: GUIWorkspace)
    extends Widget
    with ViewWidgetInterface {

  import ViewWidget._

  type WidgetModel = CoreView

  val view = new View(workspace)
  val tickCounter = new TickCounterLabel(workspace.world)
  val displaySwitch = new DisplaySwitch(workspace)

  NlogoFonts.adjustDefaultFont(tickCounter)

  setBackground(InterfaceColors.GRAPHICS_BACKGROUND)
  setBorder(BorderFactory.createCompoundBorder(
        widgetBorder,
        BorderFactory.createMatteBorder(1, 1, 2, 2, InterfaceColors.GRAPHICS_BACKGROUND)))
  setLayout(null)
  add(view)
  val settings: WorldViewSettings =
    if (Version.is3D)
      new WorldViewSettings3D(workspace, this, tickCounter)
    else
      new WorldViewSettings2D(workspace, this, tickCounter);

  override def classDisplayName: String = "World & View"

  final def getExtraHeight: Int =
    getInsets.top + getInsets.bottom + InsideBorderHeight

  def getAdditionalHeight: Int =
    getExtraHeight

  override def doLayout(): Unit = {
    val availableWidth = getWidth - getInsets.left - getInsets.right
    val patchSize = computePatchSize(availableWidth, workspace.world.worldWidth)
    val graphicsHeight =
      StrictMath.round(patchSize * workspace.world.worldHeight).toInt

    // Note that we set the patch size first and then set the bounds of the view.
    // view.setBounds will force the Renderer to a particular size, overriding the
    // calculation the Render makes internally if need be -- CLB
    view.visualPatchSize(patchSize)
    view.setBounds(getInsets.left,
      getInsets.top + InsideBorderHeight,
      availableWidth, graphicsHeight)
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
    new Dimension(gSize.getWidth.toInt, getExtraHeight + gSize.height)
  }

  def insetWidth: Int =
    getInsets.left + getInsets.right;

  def calculateWidth(worldWidth: Int, patchSize: Double): Int = {
    ((worldWidth * patchSize) + insetWidth).toInt
  }

  def calculateHeight(worldHeight: Int, patchSize: Double): Int = {
    getExtraHeight + (patchSize * worldHeight).toInt
  }

  def resetSize(): Unit = {
    import workspace.world.{ worldWidth, worldHeight, patchSize => worldPatchSize }

    view.setSize(worldWidth, worldHeight, worldPatchSize)
    val dim = view.getPreferredSize
    setSize(dim.width + insetWidth,
        dim.height + getExtraHeight)
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
    val patchSizeBasedOnNewWidth =
      computePatchSize(newBounds.width - getInsets.left + getInsets.right, worldWidth)
    val patchSizeBasedOnNewHeight =
        computePatchSize(newBounds.height - getExtraHeight, worldHeight);
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
    val newHeight = ((newPatchSize * worldHeight) + getExtraHeight).toInt
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

  private[window] def applyNewFontSize(newFontSize: Int): Unit = {
    val font = view.getFont
    val zoomDiff = font.getSize - view.fontSize
    view.applyNewFontSize(newFontSize, zoomDiff)
  }

  /// tell the zooming code it's OK to grab our subcomponents and zoom them too

  override def zoomSubcomponents: Boolean = true

  /// ViewWidgetInterface

  override def asWidget: Widget = this

  /// events

  override def hasContextMenu: Boolean =
    true;

  override def populateContextMenu(menu: JPopupMenu, p: Point, source: Component): Point =
    view.populateContextMenu(menu, p, source)

  /// display switch

  private[window] def displaySwitchOn(on: Boolean): Unit = {
    displaySwitch.actionPerformed(null)
  }


  /// load & save

  override def model: WidgetModel =
    settings.model

  override def load(view: WidgetModel): AnyRef =
    settings.load(view)

  override def copyable: Boolean = false
}
