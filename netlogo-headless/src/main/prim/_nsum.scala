// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Patch, Turtle }
import org.nlogo.api.{ Dump, TypeNames }
import org.nlogo.core.I18N
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _nsum(vn: Int) extends Reporter {
  override def toString =
    super.toString + ":" +
      Option(world).map(_.patchesOwnNameAt(vn)).getOrElse(vn.toString)

  override def report(context: Context): java.lang.Double =
    Double.box(report_1(context))

  def report_1(context: Context): Double = {
    val patch = context.agent match {
      case t: Turtle => t.getPatchHere
      case p: Patch => p
      case a => throw new Exception(s"Unexpected agent: $a")
    }
    var sum = 0d
    val it = patch.getNeighbors.iterator
    while(it.hasNext)
      it.next().asInstanceOf[Patch].getPatchVariable(vn) match {
        case d: java.lang.Double =>
          sum += d.doubleValue
        case x =>
          throw new RuntimePrimitiveException(
            context, this, I18N.errors.getN(
              "org.nlogo.prim.$common.noSumOfListWithNonNumbers",
              Dump.logoObject(x).toString, TypeNames.name(x)))
      }
    validDouble(sum, context)
  }

}
