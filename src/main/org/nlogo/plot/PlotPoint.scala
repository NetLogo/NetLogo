package org.nlogo.plot

@serializable
@SerialVersionUID(0)
case class PlotPoint(x: Double, y: Double, isDown: Boolean, color: Int) // color is ARGB
