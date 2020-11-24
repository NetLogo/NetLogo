// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Nobody
import org.nlogo.nvm.{ Context, Reporter, RuntimePrimitiveException }

class _breedsingular(breedName: String) extends Reporter {


  override def toString: String = s"${super.toString}:$breedName"

  override def report(context: Context) = report_1(context, argEvalDoubleValue(context, 0))

  def report_1(context: Context, idDouble: Double): AnyRef = {
    val id = validLong(idDouble, context)
    if (id != idDouble)
      throw new RuntimePrimitiveException(context, this, s"$idDouble is not an integer")
    val turtle = world.getTurtle(id)
    if (turtle == null) return Nobody
    val breed = world.getBreed(breedName)
    if (turtle.getBreed != breed)
      throw new RuntimePrimitiveException(
        context, this, s"$turtle is not a ${world.getBreedSingular(breed)}")
    turtle
  }
}
