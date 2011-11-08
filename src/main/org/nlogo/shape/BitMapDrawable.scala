package org.nlogo.shape

import org.nlogo.render.Drawable
//import org.nlogo.agent.Turtle
import org.nlogo.api.Turtle
import java.awt.Toolkit
import org.nlogo.api.{GraphicsInterface}

class BitMapDrawable(turtle:Turtle, psize:Double) extends Drawable {
  val shape = turtle.shape;
  def draw(g: GraphicsInterface, patchsize:Double){

    val img = Toolkit.getDefaultToolkit.getImage(shape.substring(1))

    //draw the turtle as (size of the patch * turtle size)
    val tsize = (math.ceil(psize * turtle.size)).toInt;
    g.drawImage(img, 0, 0, tsize, tsize)
    g.rotate((turtle.heading * 6.283 / 360), tsize/2, tsize/2)
  }

  def adjustSize(objSize: Double, patchSize: Double): Double = 1
}