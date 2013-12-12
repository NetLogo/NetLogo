// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.Graphics

import org.nlogo.mirror.Kind
import org.nlogo.mirror.ModelRun
import org.nlogo.mirror.WidgetKinds.Slider
import org.nlogo.mirror.WidgetKinds.Slider.Variables._
import org.nlogo.window.AbstractSliderWidget
import org.nlogo.window.Widget.LoadHelper

class SliderPanel(
  panelBounds: java.awt.Rectangle,
  val run: ModelRun,
  val index: Int)
  extends {
    // early definition because indirectly needed 
    // by AbstractSliderWidget constructor:
    val kind: Kind = Slider
  } with AbstractSliderWidget
  with MirroredWidget {

  setBounds(panelBounds)

  override def value = mirroredVar[Double](SliderValue.id).getOrElse(0.0)
  override def minimum = mirroredVar[Double](Minimum.id).getOrElse(0.0)
  override def maximum = mirroredVar[Double](Maximum.id).getOrElse(0.0)
  override def increment = mirroredVar[Double](Increment.id).getOrElse(0.0)

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

  // override add/removeNotify so we don't get WidgetAdded/Removed
  // events making the model dirty
  override def removeNotify: Unit = {}
  override def addNotify: Unit = {}
}