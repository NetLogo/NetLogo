// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, Equality, Nobody }
import org.nlogo.nvm.{ Reporter, Pure, Context }
import org.nlogo.agent.{ Agent, Turtle, Patch, Link }

class _equal extends Reporter with Pure {

  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.WildcardType,
      right = Array(Syntax.WildcardType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 5)

  override def report(context: Context) =
    Boolean.box(
      report_1(context,
               args(0).report(context),
               args(1).report(context)))

  def report_1(context: Context, arg0: AnyRef, arg1: AnyRef) =
    Equality.equals(arg0, arg1)

  def report_2(context: Context, d0: Double, d1: Double) =
    d0 == d1

  def report_3(context: Context, d0: Double, arg1: AnyRef) =
    arg1 match {
      case d: java.lang.Double =>
        d0 == d.doubleValue
      case _ =>
        false
    }

  def report_4(context: Context, arg0: AnyRef, d1: Double) =
    arg0 match {
      case d: java.lang.Double =>
        d1 == d.doubleValue
      case _ =>
        false
    }

  def report_5(context: Context, arg0: Boolean, arg1: Boolean) =
    arg0 == arg1

  def report_6(context: Context, arg0: Boolean, arg1: AnyRef) =
    arg1 match {
      case b: java.lang.Boolean =>
        arg0 == b.booleanValue
      case _ =>
        false
    }

  def report_7(context: Context, arg0: AnyRef, arg1: Boolean) =
    arg0 match {
      case b: java.lang.Boolean =>
        arg1 == b.booleanValue
      case _ =>
        false
    }

  def report_8(context: Context, arg0: String, arg1: String) =
    arg0 == arg1

  def report_9(context: Context, arg0: AnyRef, arg1: String) =
    arg1 == arg0

  def report_10(context: Context, arg0: String, arg1: AnyRef) =
    arg0 == arg1

  def report_11(context: Context, arg0: Turtle, arg1: Turtle) =
    arg0.id == arg1.id

  def report_12(context: Context, arg0: Patch, arg1: Patch) =
    arg0.id == arg1.id

  def report_13(context: Context, arg0: Link, arg1: Link) =
    arg0.id == arg1.id

}
