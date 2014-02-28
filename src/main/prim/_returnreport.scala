// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, I18N }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _returnreport extends Command {
  override def syntax =
    Syntax.commandSyntax
  override def displayName = "END"  // for use in error messages
  override def perform(context: Context) {
    perform_1(context)
  }
  def perform_1(context: Context) {
    throw new EngineException(
      context, this,
      I18N.errors.get(
        "org.nlogo.prim._returnreport.reportNotCalledInReportProcedure"))
  }
}
