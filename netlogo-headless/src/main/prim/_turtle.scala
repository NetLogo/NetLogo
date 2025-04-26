// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Nobody
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _turtle extends Reporter {

  override def report(context: Context): AnyRef =
    report_1(context, argEvalDoubleValue(context, 0))

  def report_1(context: Context, idDouble: Double): AnyRef = {
    val id = validLong(idDouble, context)
    if (id != idDouble)
      throw new RuntimePrimitiveException(
        context, this, s"$idDouble is not an integer")
    val turtle = world.getTurtle(id)
    if (turtle == null) Nobody
    else turtle
  }

}
