// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Context, Reporter }

class _homedirectory extends Reporter {

  override def report(context: Context) =
    report_1(context)
  def report_1(context: Context) =
    System.getProperty("user.home")
}
