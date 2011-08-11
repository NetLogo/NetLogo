package org.nlogo.prim.threed

import org.nlogo.agent.Turtle3D
import org.nlogo.api.LogoException
import org.nlogo.api.Constants.Infinitesimal
import org.nlogo.nvm.{ Context, Reporter, Syntax }

class _dz extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TYPE_NUMBER, "-T--")
  override def report(context: Context) = {
    val turtle = context.agent.asInstanceOf[Turtle3D]
    val value = turtle.dz
    validDouble(value)
    java.lang.Double.valueOf(
      if (StrictMath.abs(value) < 3.2e-15)
        0
      else
        value)
  }
}
