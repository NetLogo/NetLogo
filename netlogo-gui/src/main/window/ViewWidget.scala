// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Point, Rectangle }
import javax.swing.border.LineBorder

import org.nlogo.api.{ Approximate, Version }
import org.nlogo.core.{Widget=>CoreWidget}
import org.nlogo.swing.PopupMenu
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.MouseMode._

class ViewWidget(workspace: GUIWorkspace) extends Widget with ViewWidgetInterface {
  val view = new View(workspace)
  val tickCounter = new TickCounterLabel(workspace.world)
  val displaySwitch = new DisplaySwitch(workspace)

  setLayout(null)
  add(view)

  val settings: WorldViewSettings = {
    if (Version.is3D) {
      new WorldViewSettings3D(workspace, this, tickCounter)
    } else {
      new WorldViewSettings2D(workspace, this, tickCounter)
    }
  }

  override def classDisplayName: String = "World & View"

  final def getExtraHeight: Int =
    getInsets.top + getInsets.bottom

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
    view.setBounds(getInsets.left, getInsets.top, availableWidth, graphicsHeight)
  }

  override def getEditable: Option[Editable] =
    Option(settings)

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
  override def getPreferredSize: Dimension =
    new Dimension(0, 0)

  override def getMinimumSize: Dimension = {
    val gSize = view.getMinimumSize
    new Dimension(gSize.getWidth.toInt, getExtraHeight + gSize.height)
  }

  def insetWidth: Int =
    getInsets.left + getInsets.right;

  def calculateWidth(worldWidth: Int, patchSize: Double): Int = {
    ((worldWidth * patchSize)).toInt
  }

  def calculateHeight(worldHeight: Int, patchSize: Double): Int = {
    (patchSize * worldHeight).toInt
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

  // the border size was changed for 7.0, so there can be weird sizing issues with older views. this method is used
  // during widget size conversion to determine whether the size of an old view needs to be corrected. it checks
  // whether the assigned view size from the model file matches the size computed from the world dimensions and the
  // patch size, returning true if they do not match. (Isaac B 8/7/25)
  def shouldAdjustSize: Boolean = {
    view.getWidth > (workspace.world.patchSize * workspace.world.worldWidth) ||
    view.getHeight > (workspace.world.patchSize * workspace.world.worldHeight)
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

  override def constrainDrag(newBounds: Rectangle, originalBounds: Rectangle, mouseMode: MouseMode): Rectangle = {
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

    view.renderer.trailDrawer.rescaleDrawing()

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

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.viewBackground())
    setBorder(new LineBorder(InterfaceColors.viewBorder(), 2))
    tickCounter.syncTheme()
  }

  /// font handling for turtle and patch labels

  private[window] def applyNewFontSize(newFontSize: Int): Unit = {
    val font = view.getFont
    val zoomDiff = font.getSize - view.fontSize
    view.applyNewFontSize(newFontSize, zoomDiff)
  }

  /// events

  override def hasContextMenu: Boolean =
    true;

  override def populateContextMenu(menu: PopupMenu, p: Point): Unit = {
    view.populateContextMenu(menu, p)
  }

  /// display switch

  private[window] def displaySwitchOn(on: Boolean): Unit = {
    displaySwitch.actionPerformed(null)
  }


  /// load & save

  override def model: CoreWidget =
    settings.model

  override def load(view: CoreWidget): Unit =
    settings.load(view)

  override def copyable: Boolean = false
}
