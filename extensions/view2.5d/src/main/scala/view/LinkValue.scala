package view25d.view

import java.awt.Color

// The link value class contains the data needed to specify a 3D link for
// view25d.

// Note that when turtles and links are displayed, their z coordinates are multiplied by the zScale associated with the view

case class LinkValue(
    shape: String,     // the link shape is currently always a line
    color: Color,      // link color
    thickness: Double, // link thickness
    xcor1: Double,     // x coordinate of end1 turtle
    ycor1: Double,     // y coordinate of end1 turtle
    zcor1: Double,     // z coordinate of end1 turtle
    xcor2: Double,     // x coordinate of end2 turtle
    ycor2: Double,     // y coordinate of end2 turtle
    zcor2: Double,     // z coordinate of end2 turtle
) {
  override def toString: String =
    s"End1: [$xcor1 $ycor1 $zcor1], End2: [$xcor2 $ycor2 $zcor2], Thickness: $thickness, Shape: $shape, Color: $color"
}
