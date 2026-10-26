// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.{ ChooserConstraint, InputBoxConstraint }

trait ConstraintManager {
  def addBooleanConstraint(varname: String, defaultValue: Boolean): Unit
  def addChooserConstraint(varname: String, constraint: ChooserConstraint): Unit
  def addInputBoxConstraint(varname: String, constraint: InputBoxConstraint): Unit
  def addSliderConstraint(slider: SliderWidget, varname: String, minSpec: String, maxSpec: String, incSpec: String,
                          value: Double): Unit
}

class DummyConstraintManager extends ConstraintManager {
  override def addBooleanConstraint(varname: String, defaultValue: Boolean): Unit = {}
  override def addChooserConstraint(varname: String, constraint: ChooserConstraint): Unit = {}
  override def addInputBoxConstraint(varname: String, constraint: InputBoxConstraint): Unit = {}
  override def addSliderConstraint(slider: SliderWidget, varname: String, minSpec: String, maxSpec: String,
                                   incSpec: String, value: Double): Unit = {}
}
