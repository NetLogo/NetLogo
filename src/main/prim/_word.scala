// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Dump
import org.nlogo.nvm.{ Context, Reporter, Pure, CustomGenerated }

class _word extends Reporter with Pure with CustomGenerated {
  override def report(context: Context): String =
    args.map(arg => Dump.logoObject(arg.report(context)))
      .mkString
}
