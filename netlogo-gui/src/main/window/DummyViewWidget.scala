// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.{ I18N, View => CoreView }
import org.nlogo.api.Editable
import org.nlogo.api.Property
import org.nlogo.agent.World

import java.awt.Color
import java.awt.Dimension
import java.awt.Rectangle
import java.util.{ List => JList }

import javax.swing.BorderFactory

class DummyViewWidget(val world: World)
    extends SingleErrorWidget
    with Editable {

  type WidgetModel = CoreView

  setBackground(Color.black)
  setBorder(
      BorderFactory.createCompoundBorder(widgetBorder,
        BorderFactory.createMatteBorder(1, 3, 4, 2, Color.black)))

  private var newWidth = StrictMath.round(world.worldWidth * world.patchSize).toInt
  private var newHeight = StrictMath.round(world.worldHeight * world.patchSize).toInt
  setSize(newWidth, newHeight)

  @Override
  override def classDisplayName: String =
    I18N.gui.get("tabs.run.widgets.view")

  // nullary method to prevent conflict with Component.width
  def width(): Int = newWidth

  def width(width: Int): Unit = {
    newWidth = width
  }

  // nullary method prevents conflict with Component.height
  def height(): Int = newHeight

  def height(height: Int): Unit = {
    newHeight = height
  }

  def propertySet: JList[Property] =
    Properties.dummyView

  override def editFinished(): Boolean = {
    if (newWidth != getWidth || newHeight != getHeight) {
      setSize(new Dimension(newWidth, newHeight))
      resetSizeInfo()
    }

    true
  }

  override def getMinimumSize: Dimension =
    new Dimension(world.worldWidth, world.worldHeight)

  override def needsPreferredWidthFudgeFactor: Boolean =
    false

  override def constrainDrag(newBounds: Rectangle,
    originalBounds: Rectangle,
    mouseMode: MouseMode): Rectangle = {
    newWidth = newBounds.width
    newHeight = newBounds.height
    newBounds
  }

  override def hasContextMenu: Boolean = false

  /// load & save

  override def save: String = {
    "VIEW\n" +
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
      world.maxPycor + "\n";// 20
  }

  override def load(view: WidgetModel, helper: Widget.LoadHelper): AnyRef = {
    setBounds(view.left, view.top, view.right - view.left, view.bottom - view.top)

    newWidth = getWidth
    newHeight = getHeight

    return this
  }
}
