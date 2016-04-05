// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ Dump, Editable }
import org.nlogo.agent.ConstantSliderConstraint
import org.nlogo.core.{ Horizontal, I18N, Slider => CoreSlider, Vertical }

// This widget works iff the slider has a ConstantSliderConstraint
// object.  Since this is only being used to construct HubNet client
// interface, that is an acceptable limitation.  HubNet client sliders
// must have constant values for their constraints. -- CLB

class DummySliderWidget extends AbstractSliderWidget with Editable {
  type WidgetModel = CoreSlider

  setBorder( widgetBorder )

  override def classDisplayName =  I18N.gui.get("tabs.run.widgets.slider")

  def propertySet = Properties.dummySlider

  // this sets the value in the current constraint and then ensures
  // cached values are updated -- CLB
  def min( d: Double ){ if(setSliderConstraint(con.copy(minimum=d))) repaint() }
  def min = minimum.doubleValue

  def max( d: Double ){ if(setSliderConstraint(con.copy(maximum=d))) repaint() }
  def max = maximum.doubleValue

  def inc( d: Double ){ if(setSliderConstraint(con.copy(increment=d))) repaint() }
  def inc = increment.doubleValue

  private def con = constraint.asInstanceOf[ConstantSliderConstraint]

  override def load(model: WidgetModel, helper: Widget.LoadHelper): Object = {
    val min = model.min.toDouble
    val max = model.max.toDouble
    val value = model.default
    val inc = model.step.toDouble
    units = model.units.getOrElse("")
    vertical = model.direction == Vertical
    name = model.display.getOrElse("")
    val con = ConstantSliderConstraint(min, max, inc)
    con.defaultValue = value
    setSliderConstraint(con)  // ensure cached values are updated
    super.value = value
    setSize(model.right - model.left, model.bottom - model.top)
    this
  }

  override def model: WidgetModel = {
    val b = getBoundsTuple
    val savedName = if (name != null && name.trim != "") Some(name) else None
    CoreSlider(
      display = savedName,
      left = b._1, top = b._2, right = b._3, bottom = b._4,
      variable = savedName,
      min = constraint.minimum.toString,
      max = constraint.maximum.toString,
      default = constraint.defaultValue,
      step = constraint.increment.toString,
      direction = if (vertical) Vertical else Horizontal)
  }
}
