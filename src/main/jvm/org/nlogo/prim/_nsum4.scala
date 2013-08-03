// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Patch, Turtle }
import org.nlogo.api.{ Dump, I18N, Syntax, TypeNames }
import org.nlogo.nvm.{ Context, EngineException, Reporter }

class _nsum4(vn: Int) extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.ReferenceType),
      Syntax.NumberType, "-TP-")

  override def toString =
    super.toString + ":" +
      Option(world).map(_.patchesOwnNameAt(vn)).getOrElse(vn.toString)

  override def report(context: Context) =
    Double.box(report_1(context))

  def report_1(context: Context): Double = {
    val patch = context.agent match {
      case t: Turtle => t.getPatchHere
      case p: Patch => p
    }
    var sum = 0d
    val it = patch.getNeighbors4.iterator
    while(it.hasNext)
      it.next().asInstanceOf[Patch].getPatchVariable(vn) match {
        case d: java.lang.Double =>
          sum += d.doubleValue
        case x =>
          throw new EngineException(
            context, this, I18N.errors.getN(
              "org.nlogo.prim.$common.noSumOfListWithNonNumbers",
              Dump.logoObject(x).toString, TypeNames.name(x)))
      }
    validDouble(sum)
  }

}
