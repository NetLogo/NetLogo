// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.LogoException
import org.nlogo.core.Let
import org.nlogo.nvm.{ Context, Reporter }

/**
 * Gets the error message from the LetMap.
 * Used in conjunction with <code>carefully</code>.
 *
 * @see _carefully
 */
class _errormessage(let: Let) extends Reporter {
  override def report(context: Context): String =
    report_1(context)
  def report_1(context: Context): String =
    context.getLet(let).asInstanceOf[LogoException].getMessage
}
