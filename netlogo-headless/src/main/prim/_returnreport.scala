// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.LocaleContext
import org.nlogo.nvm.{ Command, Context}
import org.nlogo.nvm.RuntimePrimitiveException

class _returnreport extends Command {
  override def displayName = "END"  // for use in error messages
  override def perform(context: Context): Unit = {
    perform_1(context)
  }
  def perform_1(context: Context): Unit = {
    throw new RuntimePrimitiveException(context, this,
      loc"Reached end of reporter procedure without REPORT being called.")
  }
}
