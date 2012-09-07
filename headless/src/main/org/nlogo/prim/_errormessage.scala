// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, Let, LogoException }
import org.nlogo.nvm.{ Reporter, Context }

/**
 * Gets the error message from the LetMap.
 * Used in conjunction with <code>carefully</code>.
 *
 * @see _carefully
 */
class _errormessage extends Reporter {
  // MethodRipper won't let us call a public method from perform_1() - ST 7/20/12
  private[this] var _let: Let = null
  def let = _let
  def let_=(let: Let) { _let = let }  // compiler will call this
  override def syntax =
    Syntax.reporterSyntax(Syntax.StringType)
  override def report(context: Context) =
    report_1(context)
  def report_1(context: Context) =
    context.getLet(_let).asInstanceOf[LogoException].getMessage
}
