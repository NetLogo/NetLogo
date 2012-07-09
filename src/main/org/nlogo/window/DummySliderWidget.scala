// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.ConstantSliderConstraint
import org.nlogo.api.Dump
import org.nlogo.api.Editable
import org.nlogo.api.I18N

// This widget works iff the slider has a ConstantSliderConstraint
// object.  Since this is only being used to construct HubNet client
// interface, that is an acceptable limitation.  HubNet client sliders
// must have constant values for their constraints. -- CLB

class DummySliderWidget extends AbstractSliderWidget with Editable {
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

  override def load(strings: Array[String], helper: Widget.LoadHelper) = {
    val min = strings(7).toDouble
    val max = strings(8).toDouble
    val value = strings(9).toDouble
    val inc = strings(10).toDouble
    if( strings.length > 12 ) units = if( strings(12) == "NIL" ) "" else strings(12)
    if( strings.length > 13 && strings(13).equals( "VERTICAL" ) ) vertical = true
    name =  org.nlogo.api.ModelReader.restoreLines( strings(6) )
    val con = ConstantSliderConstraint(min, max, inc)
    con.defaultValue = value
    setSliderConstraint( con )  // ensure cached values are updated
    super.value = value
    val Array(x1,y1,x2,y2) = strings.drop(1).take(4).map(_.toInt)
    setSize(x2 - x1, y2 - y1)
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
