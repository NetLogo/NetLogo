// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.ConstantSliderConstraint
import org.nlogo.core.{ Horizontal, I18N, Slider => CoreSlider, Vertical, Widget => CoreWidget }

// This widget works iff the slider has a ConstantSliderConstraint
// object.  Since this is only being used to construct HubNet client
// interface, that is an acceptable limitation.  HubNet client sliders
// must have constant values for their constraints. -- CLB

class DummySliderWidget extends AbstractSliderWidget with Editable {
  override def classDisplayName = I18N.gui.get("tabs.run.widgets.slider")

  override def editPanel: EditPanel = new DummySliderEditPanel(this)

  // this sets the value in the current constraint and then ensures
  // cached values are updated -- CLB
  def setMin( d: Double ): Unit = { if(setSliderConstraint(con.copy(min=d))) repaint() }
  def min = minimum.doubleValue

  def setMax( d: Double ): Unit = { if(setSliderConstraint(con.copy(max=d))) repaint() }
  def max = maximum.doubleValue

  def setInc( d: Double ): Unit = { if(setSliderConstraint(con.copy(inc=d))) repaint() }
  def inc = increment.doubleValue

  private def con = constraint.asInstanceOf[ConstantSliderConstraint]

  override def load(model: CoreWidget): Unit = {
    model match {
      case s: CoreSlider =>
        val min = s.min.toDouble
        val max = s.max.toDouble
        val value = s.default
        val inc = s.step.toDouble
        setUnits(s.units.optionToPotentiallyEmptyString)
        setVertical(s.direction == Vertical)
        setVarName(s.display.optionToPotentiallyEmptyString)
        val con = ConstantSliderConstraint(min, max, inc)
        con.defaultValue = value
        setSliderConstraint(con)  // ensure cached values are updated
        super.value = value
        oldSize(s.oldSize)
        setSize(s.width, s.height)

      case _ =>
    }
  }

  override def model: CoreWidget = {
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
