// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

// if the size of the border surrounding the View changes, be sure
// to change the associated constants in ModelLoader

import java.awt.{ Component, Dimension, Font, Point, Rectangle }
import javax.swing.{ BorderFactory, JLabel, JPopupMenu }
import org.nlogo.api.{ Approximate, Dump, I18N, Version }
import org.nlogo.awt.Fonts

object ViewWidget {
  private val INSIDE_BORDER_HEIGHT = 1
}

class ViewWidget(workspace: GUIWorkspace) extends Widget with ViewWidgetInterface
    with Events.PeriodicUpdateEventHandler
    with Events.LoadBeginEventHandler with Events.LoadEndEventHandler {

  val tickCounter = new TickCounterLabel
  val displaySwitch = new DisplaySwitch(workspace)
  Fonts.adjustDefaultFont(tickCounter)
  val view = new View(workspace)

  val controlStrip = new ViewControlStrip(workspace, this)
  setBackground(InterfaceColors.GRAPHICS_BACKGROUND)
  setBorder(BorderFactory.createCompoundBorder(widgetBorder,
    BorderFactory.createMatteBorder(1, 3, 4, 2, InterfaceColors.GRAPHICS_BACKGROUND)))
  setLayout(null)
  add(view)
  add(controlStrip)
  val settings =
    if(Version.is3D)
      new WorldViewSettings3D(workspace, this)
    else
      new WorldViewSettings2D(workspace, this)

  override def classDisplayName = "World & View"

  def getInsideBorderHeight = ViewWidget.INSIDE_BORDER_HEIGHT
  def getExtraHeight = getInsets.top + getInsets.bottom + ViewWidget.INSIDE_BORDER_HEIGHT
  def getAdditionalHeight = getExtraHeight + controlStrip.getHeight

  override def doLayout() = {
    val availableWidth = getWidth - getInsets.left - getInsets.right
    val patchSize = computePatchSize(availableWidth, workspace.world.worldWidth)
    val graphicsHeight = StrictMath.round(patchSize * workspace.world.worldHeight).toInt
    val stripHeight = getHeight - graphicsHeight - getInsets.top - getInsets.bottom
    // Note that we set the patch size first and then set the bounds of the view.
    // view.setBounds will force the Renderer to a particular size, overriding the
    // calculation the Render makes internally if need be -- CLB
    view.visualPatchSize_=(patchSize)
    view.setBounds(ViewBoundsCalculator.calculateViewBounds(
      this, ViewWidget.INSIDE_BORDER_HEIGHT, patchSize, workspace.world.worldHeight))
    controlStrip.setBounds(getInsets.left, getInsets.top, availableWidth, stripHeight)
  }

  override def getEditable = settings

  def computePatchSize(width: Int, numPatches: Int): Double = {
    // This is sneaky.  We'd rather not have numbers with a zillion decimal places
    // show up in "Patch Size" when you edit the graphics window.
    // So instead of setting the patch to the exact quotient of
    // the size in pixels divided by the number of patches, we set
    // it to the number with the least junk after the decimal
    // point that still rounds to the correct # of pixels - ST 4/6/03
    val exactPatchSize = width.toDouble / numPatches.toDouble
    for(precision <- 0 to 14) {
      val roundedPatchSize = Approximate.approximate(exactPatchSize, precision)
      if((numPatches * roundedPatchSize).toInt == width)
        return roundedPatchSize
    }
    return exactPatchSize
  }

  /// sizing

  // just returning zeros prevents the "smart" preferred-size
  // code in EditView from getting confused - ST 6/6/02
  override def getPreferredSize(font: Font) = new Dimension(0, 0)
  override def needsPreferredWidthFudgeFactor = false
  override def getMinimumSize = {
    val gSize = view.getMinimumSize
    val stripSize = controlStrip.getMinimumSize
    if(gSize.width > stripSize.width) {
      new Dimension(gSize.width + getInsets.left + getInsets.right,
        gSize.height + getExtraHeight + stripSize.height)
    } else {
      // this gets tricky because if it's the control strip that's
      // determining the minimum width, then we need to calculate
      // what the graphics window's height will be at that width
      val ssx = workspace.world.worldWidth
      val ssy = workspace.world.worldHeight
      val minPatchSize = computePatchSize(stripSize.width, ssx)
      new Dimension(stripSize.width + getInsets.left + getInsets.right,
        stripSize.height + getExtraHeight + (minPatchSize * ssy).toInt)
    }
  }

  def insetWidth = getInsets.left + getInsets.right
  def getMinimumWidth = controlStrip.getMinimumSize.width + insetWidth

  def calculateWidth(worldWidth: Int, patchSize: Double) =
    (worldWidth * patchSize).toInt + getInsets.right + getInsets.left
  def calculateHeight(worldHeight: Int, patchSize: Double) =
    controlStrip.getMinimumSize.height + getExtraHeight + (patchSize * worldHeight).toInt

  def resetSize() = {
    view.setSize(workspace.world.worldWidth, workspace.world.worldHeight, workspace.world.patchSize)
    val dim = view.getPreferredSize
    setSize(dim.width + getInsets.left + getInsets.right,
      dim.height + getExtraHeight + controlStrip.getPreferredSize.height)
    doLayout()
    resetZoomInfo()
  }

  override def setSize(width: Int, height: Int) = {
    super.setSize(width, height)
    new Events.ResizeViewEvent(workspace.world.worldWidth, workspace.world.worldHeight).raise(this)
  }

  override def setBounds(bounds: Rectangle) = setBounds(bounds.x, bounds.y, bounds.width, bounds.height)
  override def setBounds(x: Int, y: Int, width: Int, height: Int) = {
    val bounds = getBounds()
    // only set the bounds if they've changed
    if(width != bounds.width || height != bounds.height || x != bounds.x || y != bounds.y) {
      super.setBounds(x, y, width, height)
      resetSizeInfo()
    }
  }

  override def getBoundsString = {
    // Oh man, this is hairy.  The purpose of this method is to
    // determine what bounds are written out when the model is
    // saved.  In the case of ViewWidget, the position information
    // is used at load time, but the sizing information is
    // ignored; instead, we let the size be determined by the
    // patch size.  However, there is one place where it matters
    // what size we write, and that's for the applets on the
    // NetLogo website.  We have Perl code on the site that looks
    // at all of the widgets in a model and computes their overall
    // bounding rectangle, so it knows what overall size to make
    // the applet for that model.  That sizing code runs into
    // trouble in the case of Algae, or any model where the world
    // is very tall and skinny, because in the applet,
    // ViewWidget's minimum width is larger than in the app,
    // because in the app the view control strip doesn't contain
    // the speed slider, but in the applet, it does.  So we need
    // to override here in order to write out a size that's right
    // for the applet.  The overriding is slightly tricky because
    // we need to make sure that we're working with an unzoomed size,
    // not a zoomed size. - ST 1/20/11
    val r = if(findWidgetContainer == null) getBounds() else findWidgetContainer.getUnzoomedBounds(this)
    // The 245 here was determined empirically by measuring the width
    // on Mac OS X and then adding some slop.  Yes, this an incredible
    // kludge, but I figure it's not worth it to try to do it the right
    // way, since all of this view widget sizing code is targeted to be
    // thrown out and redone anyway - ST 1/20/11
    val width = StrictMath.max(245, r.width)
    // and, ugh, this is copy-and-pasted from Widget.java. more
    // kludginess - ST 1/20/11
    s"${r.x}\n${r.y}\n${r.x + width}\n${r.y + r.height}\n"
  }

  override def constrainDrag(newBounds: Rectangle, originalBounds: Rectangle, mouseMode: MouseMode) = {
    import MouseMode._
    val stripHeight = controlStrip.getMinimumSize.height
    val patchSizeBasedOnNewWidth =
      computePatchSize(newBounds.width - getInsets.left + getInsets.right, workspace.world.worldWidth)
    val patchSizeBasedOnNewHeight =
      computePatchSize(newBounds.height - stripHeight - getExtraHeight, workspace.world.worldHeight)
    val newPatchSize =
      // case 1: only width changed; adjust height to match
      if (newBounds.height == originalBounds.height)
        patchSizeBasedOnNewWidth
      // case 2: only height changed; adjust width to match
      else if (newBounds.width == originalBounds.width)
        patchSizeBasedOnNewHeight
      // case 3: they both changed, use whichever results in the larger patch length
      else
        StrictMath.max(patchSizeBasedOnNewWidth, patchSizeBasedOnNewHeight)

    // since the new patch size is based on the new width make sure
    // to take into account the change in width due to zooming
    // newPatchSize -= view.renderer.zoom
    workspace.world.patchSize(newPatchSize)
    view.setSize(workspace.world.worldWidth, workspace.world.worldHeight, newPatchSize)

    view.renderer.trailDrawer.rescaleDrawing()

    val newWidth = (newPatchSize * workspace.world.worldWidth).toInt +
      getInsets.left + getInsets.right
    val newHeight = (newPatchSize * workspace.world.worldHeight).toInt +
      getExtraHeight + stripHeight
    val widthAdjust = newBounds.width - newWidth
    val heightAdjust = newBounds.height - newHeight
    var newX = newBounds.x
    var newY = newBounds.y
    mouseMode match {
      case NE => newY += heightAdjust
      case NW => newX += widthAdjust; newY += heightAdjust
      case SW => newX += widthAdjust
      case W => newX += widthAdjust
      case N => newY += heightAdjust
      case SE | S | E =>
      case _ => throw new IllegalStateException
    }
    mouseMode match {
      case N | S =>
        val midpointX = originalBounds.x + originalBounds.width / 2
        newX = midpointX - newWidth / 2
      case E | W =>
        val midpointY = originalBounds.y + originalBounds.height / 2
        newY = midpointY - newHeight / 2
      case _ => // do nothing
    }
    new Rectangle(newX, newY, newWidth, newHeight)
  }

  /// font handling for turtle and patch labels

  def applyNewFontSize(newFontSize: Int) = {
    val font = view.getFont
    val zoomDiff = font.getSize - view.fontSize
    view.applyNewFontSize(newFontSize, zoomDiff)
  }

  /// tell the zooming code it's OK to grab our subcomponents and zoom them too

  override def zoomSubcomponents = true
  
  /// ViewWidgetInterface

  def asWidget = this

  /// events

  private var _tickCounterLabel = ""
  def tickCounterLabel = _tickCounterLabel
  def tickCounterLabel_=(label: String) = {
    _tickCounterLabel = label
    handle(null: Events.PeriodicUpdateEvent)
  }

  def handle(e: Events.LoadBeginEvent) = {
    tickCounter.setText("")
    _tickCounterLabel = "ticks"
    tickCounter.setVisible(true)
  }

  def handle(e: Events.LoadEndEvent) = controlStrip.reset()

  def handle(e: Events.PeriodicUpdateEvent) = {
    val ticks = workspace.world.tickCounter.ticks
    val tickText = if(ticks == -1) "" else Dump.number(StrictMath.floor(ticks))
    tickCounter.setText("     " + _tickCounterLabel + ": " + tickText)
  }

  /// tick counter

  def showTickCounter = tickCounter.isVisible
  def showTickCounter_=(visible: Boolean) = tickCounter.setVisible(visible)


  class TickCounterLabel extends JLabel {
    override def getPreferredSize = getMinimumSize
    override def getMinimumSize = {
      val d = super.getMinimumSize
      val fontMetrics = getFontMetrics(getFont)
      d.width = StrictMath.max(d.width, fontMetrics.stringWidth(s"$tickCounterLabel: 00000000"))
      d
    }
  }

  override def hasContextMenu = true
  override def populateContextMenu(menu: JPopupMenu, p: Point, source: Component) =
    view.populateContextMenu(menu, p, source)

  /// display switch

  def displaySwitchOn(on: Boolean) = displaySwitch.actionPerformed(null)

  /// load & save

  override def save = settings.save
  override def load(strings: Seq[String], helper: Widget.LoadHelper) = settings.load(strings, helper.version)

  override def copyable = false
}
