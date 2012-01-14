package org.nlogo.hubnet.mirroring

// Points may leave the X coordinate unspecified, in which case the
// next X coordinate in the client-side plot will be used.

case class PlotPoint(xcor: Option[Double], ycor: Double)
