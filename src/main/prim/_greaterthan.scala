// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, I18N, TypeNames }
import org.nlogo.nvm.{ Reporter, Pure, Context, EngineException }
import org.nlogo.agent.{ Agent, Turtle, Patch, Link }

class _greaterthan extends Reporter with Pure {

  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.NumberType | Syntax.StringType | Syntax.AgentType,
      right = Array(Syntax.NumberType | Syntax.StringType | Syntax.AgentType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 4)

  override def report(context: Context): java.lang.Boolean =
    Boolean.box(
      report_1(context,
               args(0).report(context),
               args(1).report(context)))

  def report_1(context: Context, o1: AnyRef, o2: AnyRef): Boolean = {
    if (o1.isInstanceOf[java.lang.Double] && o2.isInstanceOf[java.lang.Double])
      return o1.asInstanceOf[java.lang.Double].doubleValue >
        o2.asInstanceOf[java.lang.Double].doubleValue
    if (o1.isInstanceOf[String] && o2.isInstanceOf[String])
      return o1.asInstanceOf[String].compareTo(o2.asInstanceOf[String]) > 0
    if (o1.isInstanceOf[Agent] && o2.isInstanceOf[Agent]) {
      val a1 = o1.asInstanceOf[Agent]
      val a2 = o2.asInstanceOf[Agent]
      if (a1.agentBit == a2.agentBit)
        return a1.compareTo(a2) > 0
    }
    throw new EngineException(
      context, this, I18N.errors.getN(
        "org.nlogo.prim._greaterthan.cannotCompareParameters",
        TypeNames.aName(o1), TypeNames.aName(o2)))
  }

  def report_2(context: Context, arg0: String, arg1: String): Boolean =
    arg0.compareTo(arg1) > 0

  def report_3(context: Context, arg0: Double, arg1: Double): Boolean =
    arg0 > arg1

  def report_4(context: Context, arg0: Turtle, arg1: Turtle): Boolean =
    arg0.compareTo(arg1) > 0

  def report_5(context: Context, arg0: Patch, arg1: Patch): Boolean =
    arg0.compareTo(arg1) > 0

  def report_6(context: Context, arg0: Link, arg1: Link): Boolean =
    arg0.compareTo(arg1) > 0

  def report_7(context: Context, arg0: Double, arg1: AnyRef): Boolean =
    arg1 match {
      case d: java.lang.Double =>
        arg0 > d.doubleValue
      case _ =>
        throw new EngineException(
          context, this, I18N.errors.getN(
            "org.nlogo.prim._greaterthan.cannotCompareParameters",
            TypeNames.aName(Double.box(arg0)), TypeNames.aName(arg1)))
    }

  def report_8(context: Context, arg0: AnyRef, arg1: Double): Boolean =
    arg0 match {
      case d: java.lang.Double =>
        d.doubleValue > arg1
      case _ =>
        throw new EngineException(
          context, this, I18N.errors.getN(
            "org.nlogo.prim._greaterthan.cannotCompareParameters",
            TypeNames.aName(arg0), TypeNames.aName(Double.box(arg1))))
    }

}
