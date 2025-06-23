// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Dimension, Rectangle }
import javax.swing.border.LineBorder

import org.nlogo.agent.World
import org.nlogo.core.{ I18N, View => CoreView, Widget => CoreWidget }
import org.nlogo.theme.InterfaceColors

class DummyViewWidget(val world: World) extends SingleErrorWidget with Editable {
  setBackgroundColor(Color.BLACK)

  private var newWidth = StrictMath.round(world.worldWidth * world.patchSize).toInt
  private var newHeight = StrictMath.round(world.worldHeight * world.patchSize).toInt
  setSize(newWidth, newHeight)

  override def classDisplayName: String =
    I18N.gui.get("tabs.run.widgets.view")

  override def editPanel: EditPanel = new DummyViewEditPanel(this)

  override def getEditable: Option[Editable] = Some(this)

  // nullary method to prevent conflict with Component.width
  def width(): Int = newWidth

  def setWidth(width: Int): Unit = {
    newWidth = width
  }

  // nullary method prevents conflict with Component.height
  def height(): Int = newHeight

  def setHeight(height: Int): Unit = {
    newHeight = height
  }

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
    setBorder(new LineBorder(InterfaceColors.viewBorder(), 2))
  }

  /// load & save
  override def model: CoreWidget = {
    val b = getUnzoomedBounds
    CoreView(
      x = b.x, y = b.y, width = b.width, height = b.height,
      dimensions = world.getDimensions)
  }

  override def load(view: CoreWidget): Unit = {
    setBounds(view.x, view.y, view.width, view.height)

    newWidth = getWidth
    newHeight = getHeight
  }
}
