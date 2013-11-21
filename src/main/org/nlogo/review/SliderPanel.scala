// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.window.AbstractSliderWidget
import org.nlogo.mirror.ModelRun
import org.nlogo.window.Widget.LoadHelper
import java.awt.event.MouseWheelListener
import org.nlogo.mirror.WidgetKinds.Slider
import org.nlogo.mirror.WidgetKinds.Slider.Variables._
import java.awt.Graphics

class SliderPanel(
  panelBounds: java.awt.Rectangle,
  val run: ModelRun,
  val index: Int)
  extends AbstractSliderWidget
  with MirroredWidget {

  override val kind = Slider

  setBounds(panelBounds)

  override def value = mirroredVar[Double](SliderValue.id).getOrElse(0.0)
  override def minimum = mirroredVar[Double](Minimum.id).getOrElse(0.0)
  override def maximum = mirroredVar[Double](Maximum.id).getOrElse(0.0)
  override def increment = mirroredVar[Double](Increment.id).getOrElse(0.0)

  override def paintComponent(g: Graphics) {
    doLayout()
    super.paintComponent(g)
  }

  def load(strings: Seq[String], helper: LoadHelper): Object = ???
  def save: String = ???

  def removeAllListeners() {
    // this needs to be called after `vertical` has been set
    getMouseListeners.foreach(removeMouseListener)
    getMouseWheelListeners.foreach(removeMouseWheelListener)
    getComponents.foreach { c =>
      c.getMouseListeners.foreach(c.removeMouseListener)
      c.getMouseWheelListeners.foreach(c.removeMouseWheelListener)
      c.getMouseMotionListeners.foreach(c.removeMouseMotionListener)
    }
  }
}