// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Dimension, Rectangle }
import javax.swing.BorderFactory
import org.nlogo.agent.World
import org.nlogo.api.{ Editable, I18N }

class DummyViewWidget(world: World) extends SingleErrorWidget with Editable {
  setBackground(Color.black)
  setBorder(BorderFactory.createCompoundBorder(
    widgetBorder,BorderFactory.createMatteBorder(1, 3, 4, 2, Color.black)))
  var _width: Int = StrictMath.round(world.worldWidth * world.patchSize).toInt
  var _height: Int = StrictMath.round(world.worldHeight * world.patchSize).toInt
  setSize(_width, _height)

  override def classDisplayName = I18N.gui.get("tabs.run.widgets.view")

  def propertySet = Properties.dummyView

  override def editFinished() = {
    if(_width != getWidth || _height != getHeight) {
      setSize(new Dimension(_width, _height))
      resetSizeInfo()
    }
    true
  }

  override def getMinimumSize = new Dimension(world.worldWidth, world.worldHeight)
  
  override def needsPreferredWidthFudgeFactor = false

  override def constrainDrag(newBounds: Rectangle, originalBounds: Rectangle, mouseMode: MouseMode) = {
    _width = newBounds.width
    _height = newBounds.height
    newBounds
  }

  override def hasContextMenu = false

  /// load & save

  override def save = "VIEW\n" +
    getBoundsString +
    "0\n0\n" + // screen-edge-x/y
    "0\n" + //7
    "1\n1\n" + // 8 9
    // old exactDraw settings, no longer used - ST 8/13/03
    "1\n1\n1\n" +  // 10 11 12
    // hooray for hex! - ST 6/16/04
    "0\n" + // 13
    "1\n1\n" + // 14 15
    "1\n" + // thin turtle pens are always on 16
    world.minPxcor + "\n" + // 17
    world.maxPxcor + "\n" + // 18
    world.minPycor + "\n" + // 19
    world.maxPycor + "\n" // 20

  override def load(strings: scala.collection.Seq[String], helper: Widget.LoadHelper) = {
    val x1 = strings(1).toInt
    val y1 = strings(2).toInt
    val x2 = strings(3).toInt
    val y2 = strings(4).toInt

    val patchSize = strings(7).toDouble
    // in older models don't trust the saved width and height because
    // some are wrong but the patchSize should always be correct.
    // I don't know what the problem was or how long it was a problem
    // but it's irrelevant now that client views can have any dimensions ev 6/20/08
    if(patchSize > 0)
      setBounds(x1, y1, (patchSize * world.worldWidth).toInt, (patchSize * world.worldHeight).toInt)
    else
      setBounds(x1, y1, x2 - x1, y2 - y1)

    _width = getWidth
    _height = getHeight

    this
  }
}
