// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ Dump, Editable }
import org.nlogo.agent.ConstantSliderConstraint
import org.nlogo.core.{ I18N, Slider => CoreSlider, Vertical }

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

  override def save = {
    val s = new StringBuilder()
    s.append( "SLIDER\n" )
    s.append( getBoundsString )
    // the file format has separate entries for name and display name,
    // but at least at present, they are always equal, so we just
    // write out the name twice - ST 6/3/02
    if( null != name && name.trim != ""  ){
      s.append( name + "\n" )
      s.append( name + "\n" )
    }
    else{
      s.append("NIL\n")
      s.append("NIL\n")
    }
    s.append( Dump.number( minimum ) + "\n" )
    s.append( Dump.number( maximum ) + "\n" )
    s.append( Dump.number( value ) + "\n" )
    s.append( Dump.number( increment ) + "\n" )
    s.append( "1\n" )   // for compatibility
    if( ( null != units ) && ( units.trim!= "" )  ) s.append( units + "\n" )
    else s.append("NIL\n")

    if ( vertical ) s.append( "VERTICAL\n" )
    else s.append( "HORIZONTAL\n" )

    s.toString
  }
}
