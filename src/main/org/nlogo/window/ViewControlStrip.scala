// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BorderLayout, Component, Dimension, Font, Point }
import java.awt.event.{ ActionEvent, ActionListener, MouseAdapter, MouseEvent }
import javax.swing.{ BorderFactory, ImageIcon, JButton, JPanel }
import javax.swing.plaf.basic
import org.nlogo.api.I18N
import org.nlogo.awt.{ Fonts, RowLayout }
import org.nlogo.swing.OptionDialog
import org.nlogo.swing.Utils._

object ViewControlStrip {
  private val MIN_HEIGHT = 20
  // this is needed because we don't want the tooltips to overlap
  // the graphics window itself, because that results in expensive
  // repaints - ST 10/27/03
  private val TOOL_TIP_OFFSET = new Point(0, -18)
}

class ViewControlStrip(workspace: GUIWorkspace, viewWidget: ViewWidget) extends JPanel {
  /// setup and layout

  setBackground(InterfaceColors.GRAPHICS_BACKGROUND)
  val _layout = new BorderLayout
  _layout.setVgap(0)
  setLayout(_layout)
  private var sizeControlXY, sizeControlX, sizeControlY: SizeControl = null
  if(workspace.kioskLevel == GUIWorkspaceJ.KioskLevel.NONE) {
    val sizeControlPanel = new JPanel
    sizeControlPanel.setLayout(new RowLayout(1, Component.RIGHT_ALIGNMENT, Component.CENTER_ALIGNMENT))

    sizeControlXY = new SizeControl("/images/arrowsdiag.gif", 1, 1)
    sizeControlXY.setToolTipText("Change width and height of world")
    sizeControlPanel.add(sizeControlXY)

    sizeControlX = new SizeControl("/images/arrowsx.gif", 1, 0)
    sizeControlX.setToolTipText("Change width of world")
    sizeControlPanel.add(sizeControlX)

    sizeControlY = new SizeControl("/images/arrowsy.gif", 0, 1)
    sizeControlY.setToolTipText("Change height of world")
    sizeControlPanel.add(sizeControlY)

    add(sizeControlPanel, BorderLayout.WEST)
    sizeControlPanel.setOpaque(false)
    add(viewWidget.tickCounter, BorderLayout.CENTER)
  } else {
    add(viewWidget.tickCounter, BorderLayout.WEST)
    val speedSlider = new SpeedSliderPanel(workspace, false)
    speedSlider.setOpaque(false)
    add(speedSlider, BorderLayout.CENTER)
  }
  if(workspace.kioskLevel == GUIWorkspaceJ.KioskLevel.NONE) {
    val threedButton = new ThreedButton
    add(threedButton, BorderLayout.EAST)
  }

  override def getMinimumSize = new Dimension(super.getMinimumSize.width, ViewControlStrip.MIN_HEIGHT)

  // special case: on every platform, we insist that the preferred size
  // be exactly the same, so the pixel-exact same size gets saved in the
  // model on every platform - ST 9/21/03
  override def getPreferredSize =
    if(viewWidget.isZoomed)
      super.getPreferredSize
    else
      new Dimension(super.getPreferredSize.width, ViewControlStrip.MIN_HEIGHT)

  override def doLayout() = {
    if(workspace.kioskLevel == GUIWorkspaceJ.KioskLevel.NONE) {
      sizeControlXY.setVisible(true)
      sizeControlX.setVisible(true)
      sizeControlY.setVisible(true)
    }
    super.doLayout()
  }

  /// misc

  def reset() = {
    enableSizeControls((workspace.world.maxPxcor == -workspace.world.minPxcor
        || workspace.world.minPxcor == 0 || workspace.world.maxPxcor == 0),
      (workspace.world.maxPycor == -workspace.world.minPycor
        || workspace.world.minPycor == 0 || workspace.world.maxPycor == 0))
    // this next line shouldn't be necessary, but in Java 1.4.2U1DP3
    // on OS X it became necessary in the applet, which is
    // probably a VM bug, but it's OK, I think it's harmless
    // - ST 7/13/04
    doLayout()
  }

  def enableSizeControls(x: Boolean, y: Boolean) =
    if(workspace.kioskLevel == GUIWorkspaceJ.KioskLevel.NONE) {
      sizeControlX.setEnabled(x)
      sizeControlY.setEnabled(y)
      sizeControlXY.setEnabled(x && y)
    }

  /// subparts

  private class ThreedButton extends JButton(" 3D ") { // spaces so it isn't so tiny
    setFont(new Font(Fonts.platformFont, Font.PLAIN, 10))
    setBackground(InterfaceColors.GRAPHICS_BACKGROUND)
    setBorder(createWidgetBorder)
    setFocusable(false)
    setToolTipText("Switch to 3D view")
    addActionListener(workspace.switchTo3DViewAction)

    override def getToolTipLocation(e: MouseEvent) = ViewControlStrip.TOOL_TIP_OFFSET

    // without this it looks funny on Windows - ST 9/18/03
    override def updateUI() = setUI(new basic.BasicButtonUI)
  }

  // sexChange - how much this instance alters screen-edge-x
  // seyChange -  "   "    "      "       "    screen-egde-y
  private class SizeControl(imagePath: String, sexChange: Int, seyChange: Int)
      extends JButton with ActionListener {
    private var mousePressLoc: Point = null

    setOpaque(false)
    setFocusable(false)

    setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2))
    setIcon(new ImageIcon(classOf[SizeControl].getResource(imagePath)))
    addMouseListener(new MouseAdapter {
        override def mousePressed(e: MouseEvent) = mousePressLoc = e.getPoint
      })
    addActionListener(this)

    override def getToolTipLocation(e: MouseEvent) = ViewControlStrip.TOOL_TIP_OFFSET

    // without this it looks funny on Windows - ST 9/18/03
    override def updateUI() = setUI(new basic.BasicButtonUI)

    private def increase =
      if(sexChange == 1 && seyChange == 0)
        mousePressLoc.x >= getWidth / 2
      else if(sexChange == 0 && seyChange == 1)
        mousePressLoc.y >= getHeight / 2
      else
        mousePressLoc.x + mousePressLoc.y >= (getHeight + getWidth) / 2

    def actionPerformed(e: ActionEvent): Unit = {
      if(!checkWithUser()) return

      val maxPxcor = workspace.world.maxPxcor
      val minPxcor = workspace.world.minPxcor
      val maxPycor = workspace.world.maxPycor
      val minPycor = workspace.world.minPycor

      val deltax = sexChange * (if(increase) 1 else -1)
      val deltay = seyChange * (if(increase) 1 else -1)

      // note that if none of the following conditions are true
      // we don't want to change  the size of the world at all
      // of course the controls should be disabled but just in case...
      var minx = minPxcor
      var maxx = maxPxcor
      var miny = minPycor
      var maxy = maxPycor

      if(maxPxcor == -minPxcor) {
        minx = minPxcor - deltax
        maxx = maxPxcor + deltax
      } else if(maxPxcor == 0) {
        minx = minPxcor - deltax
      } else if(minPxcor == 0) {
        maxx = maxPxcor + deltax
      }

      if(maxPycor == -minPycor) {
        miny = minPycor - deltay
        maxy = maxPycor + deltay
      } else if(maxPycor == 0) {
        miny = minPycor - deltay
      } else if(minPycor == 0) {
        maxy = maxPycor + deltay
      }

      if(newSizeOK(maxx - minx, maxy - miny) &&
          (minx != minPxcor || maxx != maxPxcor ||
           miny != minPycor || maxy != maxPycor)) {
        val viewWidget = workspace.view.getParent.asInstanceOf[ViewWidget]
        viewWidget.settings.setDimensions(minx, maxx, miny, maxy)
        viewWidget.settings.resizeWithProgress(false) // false = no progress dialog
      }
    }
  }

  private def newSizeOK(sizeX: Int, sizeY: Int) =
    sizeX >= 1 && sizeY >= 1 && workspace.world.patchSize * sizeX >= getMinimumSize.width

  private def checkWithUser() =
    !workspace.jobManager.anyPrimaryJobs ||
      0 == OptionDialog.show(this, I18N.gui.get("common.messages.warning"),
        "Changing the size will halt and clear the world.",
        Array("Change Size", I18N.gui.get("common.buttons.cancel")))

}
