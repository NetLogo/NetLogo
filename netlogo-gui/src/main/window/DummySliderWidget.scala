// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.ConstantSliderConstraint
import org.nlogo.core.{ Horizontal, I18N, Slider => CoreSlider, Vertical }

// This widget works iff the slider has a ConstantSliderConstraint
// object.  Since this is only being used to construct HubNet client
// interface, that is an acceptable limitation.  HubNet client sliders
// must have constant values for their constraints. -- CLB

class DummySliderWidget extends AbstractSliderWidget with Editable {
  type WidgetModel = CoreSlider

  override def classDisplayName = I18N.gui.get("tabs.run.widgets.slider")

  override def editPanel: EditPanel =
    null

  // this sets the value in the current constraint and then ensures
  // cached values are updated -- CLB
  def min( d: Double ){ if(setSliderConstraint(con.copy(min=d))) repaint() }
  def min = minimum.doubleValue

  def max( d: Double ){ if(setSliderConstraint(con.copy(max=d))) repaint() }
  def max = maximum.doubleValue

  def inc( d: Double ){ if(setSliderConstraint(con.copy(inc=d))) repaint() }
  def inc = increment.doubleValue

  private def con = constraint.asInstanceOf[ConstantSliderConstraint]

  override def load(model: WidgetModel): AnyRef = {
    val min = model.min.toDouble
    val max = model.max.toDouble
    val value = model.default
    val inc = model.step.toDouble
    setUnits(model.units.optionToPotentiallyEmptyString)
    setVertical(model.direction == Vertical)
    setName2(model.display.optionToPotentiallyEmptyString)
    val con = ConstantSliderConstraint(min, max, inc)
    con.defaultValue = value
    setSliderConstraint(con)  // ensure cached values are updated
    setValue(value)
    oldSize(model.oldSize)
    setSize(model.width, model.height)
    this
  }

  override def model: WidgetModel = {
    val b = getUnzoomedBounds
    val savedName = name.potentiallyEmptyStringToOption
    CoreSlider(
      display = savedName,
      x = b.x, y = b.y, width = b.width, height = b.height,
      oldSize = _oldSize,
      variable = savedName,
      min = min.toString,
      max = max.toString,
      default = constraint.defaultValue,
      step = inc.toString,
      units = units.potentiallyEmptyStringToOption,
      direction = if (vertical) Vertical else Horizontal)
  }
}
