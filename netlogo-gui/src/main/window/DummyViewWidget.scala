// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Dimension, Rectangle }
import java.util.{ List => JList }
import javax.swing.border.LineBorder

import org.nlogo.agent.World
import org.nlogo.api.{ Editable, Property }
import org.nlogo.core.{ I18N, View => CoreView }
import org.nlogo.theme.InterfaceColors

class DummyViewWidget(val world: World)
    extends SingleErrorWidget
    with Editable {

  type WidgetModel = CoreView

  setBackgroundColor(Color.BLACK)

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


  override def constrainDrag(newBounds: Rectangle,
    originalBounds: Rectangle,
    mouseMode: MouseMode): Rectangle = {
    newWidth = newBounds.width
    newHeight = newBounds.height
    newBounds
  }

  override def hasContextMenu: Boolean = false

  override def syncTheme(): Unit = {
    setBorder(new LineBorder(InterfaceColors.viewBorder, 2))
  }

  /// load & save
  override def model: WidgetModel = {
    val b = getUnzoomedBounds
    CoreView(
      x = b.x, y = b.y, width = b.width, height = b.height,
      dimensions = world.getDimensions)
  }

  override def load(view: WidgetModel): AnyRef = {
    setBounds(view.x, view.y, view.width, view.height)

    newWidth = getWidth
    newHeight = getHeight

    return this
  }
}
